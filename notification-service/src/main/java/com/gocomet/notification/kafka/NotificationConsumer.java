package com.gocomet.notification.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@Slf4j
public class NotificationConsumer {

    @KafkaListener(
        topics = {"shipment-created", "shipment-updated"},
        groupId = "notification-service-group",
        concurrency = "5"
    )
    public void handleShipmentEvent(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        try {
            String eventType = (String) event.get("eventType");
            String trackingNumber = (String) event.get("trackingNumber");
            String status = (String) event.get("status");
            String userId = (String) event.get("userId");

            // Route to appropriate notification channel
            switch (eventType) {
                case "SHIPMENT_CREATED" -> sendCreatedNotification(trackingNumber, userId, event);
                case "SHIPMENT_UPDATED" -> sendStatusUpdateNotification(trackingNumber, userId, status, event);
                default -> log.warn("Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Notification processing failed: {}", e.getMessage(), e);
        }
    }

    private void sendCreatedNotification(String trackingNumber, String userId, Map<String, Object> event) {
        String message = String.format(
            "[EMAIL] To: user-%s | Subject: Shipment %s Created | " +
            "Your shipment from %s to %s has been registered. Track it with: %s",
            userId, trackingNumber, event.get("origin"), event.get("destination"), trackingNumber
        );
        log.info("[NOTIFICATION] {}", message);
        simulateEmailSend(userId, "Shipment Created - " + trackingNumber, message);
    }

    private void sendStatusUpdateNotification(String trackingNumber, String userId,
                                               String status, Map<String, Object> event) {
        String prev = (String) event.getOrDefault("previousStatus", "UNKNOWN");
        String message = String.format(
            "[EMAIL] To: user-%s | Subject: Shipment %s Status Update | " +
            "Your shipment status changed: %s → %s",
            userId, trackingNumber, prev, status
        );
        log.info("[NOTIFICATION] {}", message);

        // Send SMS for critical status changes
        if ("DELIVERED".equals(status) || "FAILED".equals(status)) {
            sendSmsNotification(userId, trackingNumber, status);
        }

        simulateEmailSend(userId, "Shipment Update - " + trackingNumber, message);
    }

    private void simulateEmailSend(String userId, String subject, String body) {
        // In production: integrate with SendGrid, AWS SES, etc.
        log.info("[EMAIL SIM] timestamp={} userId={} subject={}", LocalDateTime.now(), userId, subject);
    }

    private void sendSmsNotification(String userId, String trackingNumber, String status) {
        // In production: integrate with Twilio, AWS SNS, etc.
        String sms = String.format("[SMS SIM] userId=%s | Shipment %s is now %s", userId, trackingNumber, status);
        log.info("[SMS] {}", sms);
    }
}
