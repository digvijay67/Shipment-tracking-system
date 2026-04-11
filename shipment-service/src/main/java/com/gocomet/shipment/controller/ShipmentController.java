package com.gocomet.shipment.controller;

import com.gocomet.shipment.dto.ShipmentDtos.*;
import com.gocomet.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<ShipmentResponse>> create(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateShipmentRequest req) {
        ShipmentResponse shipment = shipmentService.createShipment(UUID.fromString(userId), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Shipment created successfully", shipment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getShipment(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Shipment retrieved", shipmentService.getShipment(id)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ShipmentResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateStatusRequest req) {
        ShipmentResponse updated = shipmentService.updateStatus(id, req, UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("Status updated", updated));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ShipmentResponse>>> getUserShipments(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        Page<ShipmentResponse> shipments = shipmentService.getUserShipments(
                UUID.fromString(userId), page, size, status);
        return ResponseEntity.ok(ApiResponse.success("Shipments retrieved", shipments));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    }
}
