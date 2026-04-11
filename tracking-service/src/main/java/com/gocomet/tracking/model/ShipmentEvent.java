package com.gocomet.tracking.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shipment_events", indexes = {
    @Index(name = "idx_events_shipment_id", columnList = "shipmentId"),
    @Index(name = "idx_events_created_at", columnList = "createdAt")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ShipmentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID shipmentId;

    @Column(nullable = false, length = 50)
    private String trackingNumber;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(length = 50)
    private String previousStatus;

    @Column(length = 255)
    private String location;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 50)
    private String eventType;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
