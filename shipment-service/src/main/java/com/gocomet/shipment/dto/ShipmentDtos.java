package com.gocomet.shipment.dto;

import com.gocomet.shipment.model.Shipment.ShipmentStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class ShipmentDtos {

    @Data
    public static class CreateShipmentRequest {
        @NotBlank @Size(max = 255)
        private String origin;
        @NotBlank @Size(max = 255)
        private String destination;
        @NotBlank @Size(max = 255)
        private String senderName;
        @NotBlank @Size(max = 255)
        private String receiverName;
        private String receiverPhone;
        @NotNull @Positive
        private Double weightKg;
        private Double distanceKm;
        private String carrier;
        private LocalDateTime expectedDelivery;
        private String notes;
    }

    @Data
    public static class UpdateStatusRequest {
        @NotNull
        private ShipmentStatus status;
        private String location;
        private String notes;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ShipmentResponse {
        private UUID id;
        private String trackingNumber;
        private UUID userId;
        private String origin;
        private String destination;
        private String senderName;
        private String receiverName;
        private String receiverPhone;
        private Double weightKg;
        private Double distanceKm;
        private String carrier;
        private ShipmentStatus status;
        private LocalDateTime expectedDelivery;
        private LocalDateTime actualDelivery;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
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
