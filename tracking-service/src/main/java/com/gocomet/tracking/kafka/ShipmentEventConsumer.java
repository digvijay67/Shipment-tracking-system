package com.gocomet.tracking.kafka;

import com.gocomet.tracking.model.ShipmentEvent;
import com.gocomet.tracking.repository.ShipmentEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentEventConsumer {

    private final ShipmentEventRepository eventRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TRACKING_PREFIX = "tracking:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    @KafkaListener(
        topics = {"shipment-created", "shipment-updated"},
        groupId = "tracking-service-group",
        concurrency = "10"
    )
    public void consumeShipmentEvent(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            String eventType = (String) event.get("eventType");
            String shipmentIdStr = (String) event.get("shipmentId");
            String trackingNumber = (String) event.get("trackingNumber");
            String status = (String) event.get("status");

            log.debug("Consuming event {} for shipment {} from {}-{}@{}",
                    eventType, shipmentIdStr, topic, partition, offset);

            // Persist event to PostgreSQL audit trail
            ShipmentEvent shipmentEvent = ShipmentEvent.builder()
                    .shipmentId(UUID.fromString(shipmentIdStr))
                    .trackingNumber(trackingNumber)
                    .status(status)
                    .previousStatus((String) event.get("previousStatus"))
                    .eventType(eventType)
                    .description(buildDescription(eventType, status, event))
                    .build();

            eventRepository.save(shipmentEvent);

            // Update Redis with latest state for ultra-fast reads
            updateRedisState(shipmentIdStr, trackingNumber, event);

            log.info("Processed {} for tracking number {}", eventType, trackingNumber);

        } catch (Exception e) {
            log.error("Error processing shipment event: {}", e.getMessage(), e);
            // In production: send to Dead Letter Queue
        }
    }

    private void updateRedisState(String shipmentId, String trackingNumber, Map<String, Object> event) {
        try {
            // Cache by shipment ID
            String keyById = TRACKING_PREFIX + "id:" + shipmentId;
            redisTemplate.opsForValue().set(keyById, event, CACHE_TTL);

            // Cache by tracking number for public lookup
            String keyByTracking = TRACKING_PREFIX + "num:" + trackingNumber;
            redisTemplate.opsForValue().set(keyByTracking, event, CACHE_TTL);

        } catch (Exception e) {
            log.warn("Failed to update Redis tracking state: {}", e.getMessage());
        }
    }

    private String buildDescription(String eventType, String status, Map<String, Object> event) {
        return switch (eventType) {
            case "SHIPMENT_CREATED" -> "Shipment created and registered in system";
            case "SHIPMENT_UPDATED" -> String.format("Status changed from %s to %s",
                    event.get("previousStatus"), status);
            default -> "Shipment event: " + eventType;
        };
    }
}
