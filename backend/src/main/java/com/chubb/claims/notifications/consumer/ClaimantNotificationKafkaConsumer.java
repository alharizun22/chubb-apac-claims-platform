package com.chubb.claims.notifications.consumer;

import com.chubb.claims.common.event.*;
import com.chubb.claims.notifications.NotificationRecord;
import com.chubb.claims.notifications.NotificationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Asynchronously consumes claim events and simulates dispatching notifications
 * (SMS / Email / In-App alerts) to claimants and claims officers.
 */
@Component
public class ClaimantNotificationKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(ClaimantNotificationKafkaConsumer.class);

    private final NotificationStore notificationStore;

    public ClaimantNotificationKafkaConsumer(NotificationStore notificationStore) {
        this.notificationStore = notificationStore;
    }

    @KafkaListener(
            topics = "${chubb.kafka.topics.claim-events:chubb.claims.events}",
            groupId = "chubb-notifications-group",
            autoStartup = "${spring.kafka.listener.auto-startup:true}"
    )
    @EventListener
    public void onNotificationEvent(Object eventPayload) {
        if (eventPayload instanceof ClaimSubmittedEvent event) {
            handleClaimSubmitted(event);
        } else if (eventPayload instanceof ClaimAssignedEvent event) {
            handleClaimAssigned(event);
        } else if (eventPayload instanceof ClaimInfoRequestedEvent event) {
            handleClaimInfoRequested(event);
        } else if (eventPayload instanceof ClaimInfoProvidedEvent event) {
            handleClaimInfoProvided(event);
        } else if (eventPayload instanceof ClaimDecidedEvent event) {
            handleClaimDecided(event);
        } else if (eventPayload instanceof ClaimDomainEvent genericEvent) {
            log.debug("[Notification Simulator] Generic domain event received: {}", genericEvent.getEventType());
        }
    }

    private void handleClaimSubmitted(ClaimSubmittedEvent event) {
        String msg = String.format("Dear %s, your Chubb %s claim (%s) for %s %s has been received and logged.",
                event.getClaimantName(), event.getClaimType(), event.getReferenceNumber(),
                event.getMarket(), event.getEstimatedAmount());
        log.info("[NOTIFICATION DISPATCHED - SMS/EMAIL] Recipient='{}': {}", event.getClaimantId(), msg);

        record(event.getClaimId(), event.getReferenceNumber(), event.getClaimantId(),
                "Claim Submitted", msg, "EMAIL");
    }

    private void handleClaimAssigned(ClaimAssignedEvent event) {
        String msg = String.format("Your claim %s has been assigned to claims officer %s for review.",
                event.getReferenceNumber(), event.getOfficerId());
        log.info("[NOTIFICATION DISPATCHED - IN_APP] Claim={}: {}", event.getReferenceNumber(), msg);

        record(event.getClaimId(), event.getReferenceNumber(), "Claimant",
                "Officer Assigned", msg, "IN_APP");
    }

    private void handleClaimInfoRequested(ClaimInfoRequestedEvent event) {
        String msg = String.format("ACTION REQUIRED on claim %s: Claims Officer %s requested additional details: %s",
                event.getReferenceNumber(), event.getOfficerId(), event.getNote());
        log.warn("[NOTIFICATION DISPATCHED - URGENT SMS] Claim={}: {}", event.getReferenceNumber(), msg);

        record(event.getClaimId(), event.getReferenceNumber(), "Claimant",
                "Action Required: Information Requested", msg, "SMS");
    }

    private void handleClaimInfoProvided(ClaimInfoProvidedEvent event) {
        String msg = String.format("Supplementary details provided for claim %s by %s. Review resumed.",
                event.getReferenceNumber(), event.getClaimantId());
        log.info("[NOTIFICATION DISPATCHED - OFFICER INBOX] Claim={}: {}", event.getReferenceNumber(), msg);

        record(event.getClaimId(), event.getReferenceNumber(), "Officer",
                "Claimant Response Received", msg, "IN_APP");
    }

    private void handleClaimDecided(ClaimDecidedEvent event) {
        boolean settled = "SETTLED".equalsIgnoreCase(event.getDecision());
        String title = settled ? "Claim Approved & Settled" : "Notice of Claim Decision";
        String msg = settled
                ? String.format("Notice of Settlement: Claim %s has been approved. Payout of %s %s authorized.",
                event.getReferenceNumber(), event.getMarket(), event.getSettledAmount())
                : String.format("Notice of Rejection: Claim %s could not be approved. Reason: %s",
                event.getReferenceNumber(), event.getReason());

        log.info("[NOTIFICATION DISPATCHED - OFFICIAL NOTICE] Recipient=Claimant, Claim={}: {}",
                event.getReferenceNumber(), msg);

        record(event.getClaimId(), event.getReferenceNumber(), "Claimant",
                title, msg, "EMAIL");
    }

    private void record(UUID claimId, String ref, String recipient, String title, String msg, String channel) {
        notificationStore.add(NotificationRecord.builder()
                .id(UUID.randomUUID())
                .claimId(claimId)
                .referenceNumber(ref)
                .recipient(recipient)
                .title(title)
                .message(msg)
                .channel(channel)
                .sentAt(Instant.now())
                .build());
    }
}
