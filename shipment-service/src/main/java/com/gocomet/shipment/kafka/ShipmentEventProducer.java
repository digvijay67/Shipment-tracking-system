package com.gocomet.shipment.kafka;

import com.gocomet.shipment.model.Shipment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.shipment-created}")
    private String shipmentCreatedTopic;

    @Value("${kafka.topics.shipment-updated}")
    private String shipmentUpdatedTopic;

    public void publishShipmentCreated(Shipment shipment) {
        Map<String, Object> event = buildEvent("SHIPMENT_CREATED", shipment);
        sendEvent(shipmentCreatedTopic, shipment.getId().toString(), event);
    }

    public void publishShipmentUpdated(Shipment shipment, String previousStatus) {
        Map<String, Object> event = buildEvent("SHIPMENT_UPDATED", shipment);
        event.put("previousStatus", previousStatus);
        sendEvent(shipmentUpdatedTopic, shipment.getId().toString(), event);
    }

    private Map<String, Object> buildEvent(String eventType, Shipment shipment) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("shipmentId", shipment.getId().toString());
        event.put("trackingNumber", shipment.getTrackingNumber());
        event.put("userId", shipment.getUserId().toString());
        event.put("status", shipment.getStatus().name());
        event.put("origin", shipment.getOrigin());
        event.put("destination", shipment.getDestination());
        event.put("carrier", shipment.getCarrier());
        event.put("distanceKm", shipment.getDistanceKm());
        event.put("weightKg", shipment.getWeightKg());
        event.put("expectedDelivery", shipment.getExpectedDelivery() != null
                ? shipment.getExpectedDelivery().toString() : null);
        event.put("timestamp", LocalDateTime.now().toString());
        return event;
    }

    private void sendEvent(String topic, String key, Map<String, Object> event) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event to topic {}: {}", topic, ex.getMessage());
            } else {
                log.debug("Published event to {} partition={} offset={}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
