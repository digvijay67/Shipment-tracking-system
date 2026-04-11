package com.gocomet.shipment.repository;

import com.gocomet.shipment.model.Shipment;
import com.gocomet.shipment.model.Shipment.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    Page<Shipment> findByUserId(UUID userId, Pageable pageable);
    Page<Shipment> findByUserIdAndStatus(UUID userId, ShipmentStatus status, Pageable pageable);

    @Query("SELECT s FROM Shipment s WHERE s.expectedDelivery < :now AND s.status NOT IN :completedStatuses")
    List<Shipment> findOverdueShipments(LocalDateTime now, List<ShipmentStatus> completedStatuses);

    long countByStatus(ShipmentStatus status);
}
