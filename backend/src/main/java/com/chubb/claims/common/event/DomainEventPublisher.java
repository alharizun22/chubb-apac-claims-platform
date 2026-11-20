package com.chubb.claims.common.event;

public interface DomainEventPublisher {
    void publish(ClaimDomainEvent event);
}
