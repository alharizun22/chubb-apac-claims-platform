package com.chubb.claims.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDecidedEvent implements ClaimDomainEvent {
    public static final String EVENT_TYPE = "claim.decided";

    @Builder.Default
    private String eventType = EVENT_TYPE;
    private UUID claimId;
    private String referenceNumber;
    private String market;
    private String decision; // SETTLED or REJECTED
    private BigDecimal settledAmount;
    private String reason;
    private String officerId;
    @Builder.Default
    private Instant occurredAt = Instant.now();
}
