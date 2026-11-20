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
public class ClaimSubmittedEvent implements ClaimDomainEvent {
    public static final String EVENT_TYPE = "claim.submitted";

    @Builder.Default
    private String eventType = EVENT_TYPE;
    private UUID claimId;
    private String referenceNumber;
    private String market;
    private String claimType;
    private String claimantId;
    private String claimantName;
    private BigDecimal estimatedAmount;
    @Builder.Default
    private Instant occurredAt = Instant.now();
}
