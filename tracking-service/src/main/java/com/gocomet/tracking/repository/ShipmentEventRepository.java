package com.gocomet.tracking.repository;

import com.gocomet.tracking.model.ShipmentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShipmentEventRepository extends JpaRepository<ShipmentEvent, UUID> {
    List<ShipmentEvent> findByShipmentIdOrderByCreatedAtAsc(UUID shipmentId);
    List<ShipmentEvent> findByTrackingNumberOrderByCreatedAtAsc(String trackingNumber);
    long countByShipmentId(UUID shipmentId);
}
