package com.gocomet.shipment.service;

import com.gocomet.shipment.dto.ShipmentDtos.*;
import com.gocomet.shipment.kafka.ShipmentEventProducer;
import com.gocomet.shipment.model.Shipment;
import com.gocomet.shipment.model.Shipment.ShipmentStatus;
import com.gocomet.shipment.repository.ShipmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentEventProducer eventProducer;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "shipment:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    @Transactional
    public ShipmentResponse createShipment(UUID userId, CreateShipmentRequest req) {
        String trackingNumber = generateTrackingNumber();

        Shipment shipment = Shipment.builder()
                .trackingNumber(trackingNumber)
                .userId(userId)
                .origin(req.getOrigin())
                .destination(req.getDestination())
                .senderName(req.getSenderName())
                .receiverName(req.getReceiverName())
                .receiverPhone(req.getReceiverPhone())
                .weightKg(req.getWeightKg())
                .distanceKm(req.getDistanceKm())
                .carrier(req.getCarrier() != null ? req.getCarrier() : "GoComet Logistics")
                .expectedDelivery(req.getExpectedDelivery())
                .notes(req.getNotes())
                .status(ShipmentStatus.PENDING)
                .build();

        shipment = shipmentRepository.save(shipment);

        // Cache the new shipment
        cacheShipment(shipment);

        // Publish to Kafka asynchronously
        eventProducer.publishShipmentCreated(shipment);

        log.info("Shipment created: {} for user: {}", trackingNumber, userId);
        return toResponse(shipment);
    }

    @Transactional
    public ShipmentResponse updateStatus(UUID shipmentId, UpdateStatusRequest req, UUID userId) {
        Shipment shipment = getShipmentEntity(shipmentId);

        // Validate user owns this shipment
        if (!shipment.getUserId().equals(userId)) {
            throw new SecurityException("Not authorized to update this shipment");
        }

        String previousStatus = shipment.getStatus().name();
        shipment.setStatus(req.getStatus());

        if (req.getStatus() == ShipmentStatus.DELIVERED) {
            shipment.setActualDelivery(java.time.LocalDateTime.now());
        }

        if (req.getNotes() != null) {
            shipment.setNotes(req.getNotes());
        }

        shipment = shipmentRepository.save(shipment);

        // Invalidate and re-cache
        invalidateCache(shipmentId);
        cacheShipment(shipment);

        // Publish update event
        eventProducer.publishShipmentUpdated(shipment, previousStatus);

        log.info("Shipment {} status updated: {} -> {}", shipmentId, previousStatus, req.getStatus());
        return toResponse(shipment);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipment(UUID shipmentId) {
        // Try cache first
        String cacheKey = CACHE_PREFIX + shipmentId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("Cache HIT for shipment {}", shipmentId);
                return objectMapper.convertValue(cached, ShipmentResponse.class);
            }
        } catch (Exception e) {
            log.warn("Redis cache read failed, falling back to DB: {}", e.getMessage());
        }

        log.debug("Cache MISS for shipment {}", shipmentId);
        Shipment shipment = getShipmentEntity(shipmentId);
        cacheShipment(shipment);
        return toResponse(shipment);
    }

    @Transactional(readOnly = true)
    public Page<ShipmentResponse> getUserShipments(UUID userId, int page, int size, String status) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Shipment> shipments = (status != null && !status.isBlank())
                ? shipmentRepository.findByUserIdAndStatus(userId, ShipmentStatus.valueOf(status), pageRequest)
                : shipmentRepository.findByUserId(userId, pageRequest);

        return shipments.map(this::toResponse);
    }

    private Shipment getShipmentEntity(UUID id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + id));
    }

    private void cacheShipment(Shipment shipment) {
        try {
            String key = CACHE_PREFIX + shipment.getId();
            redisTemplate.opsForValue().set(key, toResponse(shipment), CACHE_TTL);
        } catch (Exception e) {
            log.warn("Redis cache write failed: {}", e.getMessage());
        }
    }

    private void invalidateCache(UUID shipmentId) {
        try {
            redisTemplate.delete(CACHE_PREFIX + shipmentId);
        } catch (Exception e) {
            log.warn("Redis cache invalidation failed: {}", e.getMessage());
        }
    }

    private String generateTrackingNumber() {
        return "GC" + System.currentTimeMillis() +
               String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    public ShipmentResponse toResponse(Shipment s) {
        return ShipmentResponse.builder()
                .id(s.getId()).trackingNumber(s.getTrackingNumber())
                .userId(s.getUserId()).origin(s.getOrigin())
                .destination(s.getDestination()).senderName(s.getSenderName())
                .receiverName(s.getReceiverName()).receiverPhone(s.getReceiverPhone())
                .weightKg(s.getWeightKg()).distanceKm(s.getDistanceKm())
                .carrier(s.getCarrier()).status(s.getStatus())
                .expectedDelivery(s.getExpectedDelivery()).actualDelivery(s.getActualDelivery())
                .notes(s.getNotes()).createdAt(s.getCreatedAt()).updatedAt(s.getUpdatedAt())
                .build();
    }
}
