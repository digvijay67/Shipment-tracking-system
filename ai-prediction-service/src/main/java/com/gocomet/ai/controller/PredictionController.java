package com.gocomet.ai.controller;

import com.gocomet.ai.dto.PredictionDtos.*;
import com.gocomet.ai.service.PredictionEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/predictions")
@RequiredArgsConstructor
@Slf4j
public class PredictionController {

    private final PredictionEngine predictionEngine;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "prediction:";
    private static final Duration CACHE_TTL  = Duration.ofMinutes(10);

    /** Full ETA + risk prediction for a shipment */
    @PostMapping("/predict")
    public ResponseEntity<ApiResponse<PredictionResponse>> predict(
            @RequestBody PredictionRequest req) {

        String cacheKey = CACHE_PREFIX + req.getShipmentId();

        // Cache-aside: serve stale results fast
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                PredictionResponse cr = objectMapper.convertValue(cached, PredictionResponse.class);
                return ResponseEntity.ok(ApiResponse.success("Prediction (cached)", cr));
            }
        } catch (Exception ignored) {}

        PredictionResponse result = predictionEngine.predict(req);

        try {
            redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL);
        } catch (Exception ignored) {}

        return ResponseEntity.ok(ApiResponse.success("Prediction generated", result));
    }

    /** Quick ETA only — lightweight endpoint */
    @GetMapping("/{shipmentId}/eta")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEta(
            @PathVariable UUID shipmentId,
            @RequestParam(required = false) Double distanceKm,
            @RequestParam(required = false) Double weightKg,
            @RequestParam(required = false, defaultValue = "STANDARD") String carrier,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination) {

        PredictionRequest req = new PredictionRequest();
        req.setShipmentId(shipmentId);
        req.setDistanceKm(distanceKm);
        req.setWeightKg(weightKg != null ? weightKg : 10.0);
        req.setCarrier(carrier);
        req.setOrigin(origin != null ? origin : "Unknown");
        req.setDestination(destination != null ? destination : "Unknown");
        req.setShipmentDate(java.time.LocalDateTime.now());

        PredictionResponse result = predictionEngine.predict(req);

        return ResponseEntity.ok(ApiResponse.success("ETA calculated", Map.of(
            "shipmentId",       shipmentId.toString(),
            "etaHours",         result.getPredictedEtaHours(),
            "etaDate",          result.getPredictedEtaDate().toString(),
            "confidenceScore",  result.getConfidenceScore()
        )));
    }

    /** Risk assessment only */
    @GetMapping("/{shipmentId}/risk")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRisk(
            @PathVariable UUID shipmentId,
            @RequestParam(required = false) Double distanceKm,
            @RequestParam(required = false) Double weightKg,
            @RequestParam(required = false, defaultValue = "STANDARD") String carrier,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination) {

        PredictionRequest req = new PredictionRequest();
        req.setShipmentId(shipmentId);
        req.setDistanceKm(distanceKm);
        req.setWeightKg(weightKg != null ? weightKg : 10.0);
        req.setCarrier(carrier);
        req.setOrigin(origin != null ? origin : "Unknown");
        req.setDestination(destination != null ? destination : "Unknown");
        req.setShipmentDate(java.time.LocalDateTime.now());

        PredictionResponse result = predictionEngine.predict(req);

        return ResponseEntity.ok(ApiResponse.success("Risk assessed", Map.of(
            "shipmentId",  shipmentId.toString(),
            "riskLevel",   result.getRiskLevel(),
            "riskScore",   result.getRiskScore(),
            "riskFactors", result.getRiskFactors()
        )));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleError(Exception e) {
        log.error("Prediction error: {}", e.getMessage());
        return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
    }
}
