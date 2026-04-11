package com.gocomet.ai.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class PredictionDtos {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class PredictionRequest {
        private UUID shipmentId;
        private String origin;
        private String destination;
        private Double distanceKm;
        private Double weightKg;
        private String carrier;
        private String status;
        private LocalDateTime shipmentDate;
        private LocalDateTime expectedDelivery;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PredictionResponse {
        private UUID shipmentId;
        private double predictedEtaHours;
        private LocalDateTime predictedEtaDate;
        private String riskLevel;
        private double riskScore;
        private String riskFactors;
        private double confidenceScore;
        private PredictionBreakdown breakdown;
        private LocalDateTime generatedAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PredictionBreakdown {
        private double distanceKm;
        private double avgSpeedKmh;
        private double trafficFactor;
        private double weatherFactor;
        private double carrierFactor;
        private double weightPenaltyFactor;
        private double rawTransitHours;
        private double handlingHours;
    }

    @Data
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private LocalDateTime timestamp = LocalDateTime.now();

        public static <T> ApiResponse<T> success(String message, T data) {
            ApiResponse<T> r = new ApiResponse<>();
            r.success = true; r.message = message; r.data = data; return r;
        }
        public static <T> ApiResponse<T> error(String message) {
            ApiResponse<T> r = new ApiResponse<>();
            r.success = false; r.message = message; return r;
        }
    }
}
