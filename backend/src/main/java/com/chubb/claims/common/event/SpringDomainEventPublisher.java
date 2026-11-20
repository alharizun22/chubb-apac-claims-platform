package com.chubb.claims.common.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SpringDomainEventPublisher.class);
    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(ClaimDomainEvent event) {
        log.info("Publishing domain event [{}]: claimId={}, ref={}",
                event.getEventType(), event.getClaimId(), event.getReferenceNumber());
        applicationEventPublisher.publishEvent(event);
    }
}
