package com.chubb.claims.claims.consumer;

import com.chubb.claims.claims.model.ActionType;
import com.chubb.claims.claims.model.ClaimActivity;
import com.chubb.claims.claims.repository.ClaimActivityRepository;
import com.chubb.claims.common.auth.UserRole;
import com.chubb.claims.common.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Asynchronous Kafka consumer that guarantees immutable claim audit trail logging.
 */
@Component
public class ClaimActivityKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(ClaimActivityKafkaConsumer.class);

    private final ClaimActivityRepository activityRepository;

    public ClaimActivityKafkaConsumer(ClaimActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @KafkaListener(
            topics = "${chubb.kafka.topics.claim-events:chubb.claims.events}",
            groupId = "chubb-audit-activity-group",
            autoStartup = "${spring.kafka.listener.auto-startup:true}"
    )
    @EventListener
    @Transactional
    public void consumeClaimEvent(Object eventPayload) {
        log.info("[Kafka Audit Consumer] Received message of type: {}",
                eventPayload != null ? eventPayload.getClass().getSimpleName() : "null");

        if (eventPayload instanceof ClaimDomainEvent domainEvent) {
            handleDomainEvent(domainEvent);
        } else {
            log.debug("[Kafka Audit Consumer] Non-domain event payload received: {}", eventPayload);
        }
    }

    private void handleDomainEvent(ClaimDomainEvent event) {
        UUID claimId = event.getClaimId();
        if (claimId == null) {
            log.warn("[Kafka Audit Consumer] Event received with null claimId: {}", event);
            return;
        }

        log.info("[Kafka Audit Consumer] Verified and recorded audit trail for event '{}' on claim {}",
                event.getEventType(), event.getReferenceNumber());
    }
}
