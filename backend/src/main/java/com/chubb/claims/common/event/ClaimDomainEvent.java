package com.chubb.claims.common.event;

import java.time.Instant;
import java.util.UUID;

public interface ClaimDomainEvent {
    String getEventType();
    UUID getClaimId();
    String getReferenceNumber();
    String getMarket();
    Instant getOccurredAt();
}
