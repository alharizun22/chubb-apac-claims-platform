package com.chubb.claims.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimInfoProvidedEvent implements ClaimDomainEvent {
    public static final String EVENT_TYPE = "claim.info_provided";

    @Builder.Default
    private String eventType = EVENT_TYPE;
    private UUID claimId;
    private String referenceNumber;
    private String market;
    private String claimantId;
    private String note;
    @Builder.Default
    private Instant occurredAt = Instant.now();
}
