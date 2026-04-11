package com.gocomet.shipment.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shipments", indexes = {
    @Index(name = "idx_shipments_user_id", columnList = "userId"),
    @Index(name = "idx_shipments_tracking_number", columnList = "trackingNumber", unique = true),
    @Index(name = "idx_shipments_status", columnList = "status"),
    @Index(name = "idx_shipments_created_at", columnList = "createdAt")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String trackingNumber;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String origin;

    @Column(nullable = false, length = 255)
    private String destination;

    @Column(nullable = false, length = 255)
    private String senderName;

    @Column(nullable = false, length = 255)
    private String receiverName;

    @Column(length = 255)
    private String receiverPhone;

    @Column(nullable = false)
    private Double weightKg;

    @Column
    private Double distanceKm;

    @Column(length = 50)
    private String carrier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ShipmentStatus status = ShipmentStatus.PENDING;

    @Column
    private LocalDateTime expectedDelivery;

    @Column
    private LocalDateTime actualDelivery;

    @Column(length = 1000)
    private String notes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum ShipmentStatus {
        PENDING, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED, RETURNED, CANCELLED
    }
}
