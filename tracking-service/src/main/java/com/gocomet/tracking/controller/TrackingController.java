package com.gocomet.tracking.controller;

import com.gocomet.tracking.model.ShipmentEvent;
import com.gocomet.tracking.repository.ShipmentEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/tracking")
@RequiredArgsConstructor
@Slf4j
public class TrackingController {

    private final ShipmentEventRepository eventRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /** Real-time status from Redis cache (ultra-fast path) */
    @GetMapping("/{shipmentId}/live")
    public ResponseEntity<Map<String, Object>> getLiveStatus(@PathVariable UUID shipmentId) {
        String key = "tracking:id:" + shipmentId;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
            "success", true,
            "source", "redis-cache",
            "data", cached,
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    /** Full audit trail from PostgreSQL */
    @GetMapping("/{shipmentId}/history")
    public ResponseEntity<Map<String, Object>> getHistory(@PathVariable UUID shipmentId) {
        List<ShipmentEvent> events = eventRepository.findByShipmentIdOrderByCreatedAtAsc(shipmentId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "shipmentId", shipmentId.toString(),
            "eventCount", events.size(),
            "events", events
        ));
    }

    /** Look up by tracking number (public-facing) */
    @GetMapping("/number/{trackingNumber}")
    public ResponseEntity<Map<String, Object>> getByTrackingNumber(@PathVariable String trackingNumber) {
        // Try Redis first
        String cacheKey = "tracking:num:" + trackingNumber;
        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "source", "redis-cache",
                "data", cached
            ));
        }

        // Fallback: PostgreSQL
        List<ShipmentEvent> events = eventRepository.findByTrackingNumberOrderByCreatedAtAsc(trackingNumber);
        if (events.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
            "success", true,
            "source", "database",
            "trackingNumber", trackingNumber,
            "latestStatus", events.get(events.size() - 1).getStatus(),
            "events", events
        ));
    }
}
