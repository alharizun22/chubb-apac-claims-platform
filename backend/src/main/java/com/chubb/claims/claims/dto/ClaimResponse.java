package com.chubb.claims.claims.dto;

import com.chubb.claims.claims.model.Claim;
import com.chubb.claims.claims.model.ClaimStatus;
import com.chubb.claims.claims.model.ClaimType;
import com.chubb.claims.claims.model.Market;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {
    private UUID id;
    private String referenceNumber;
    private Market market;
    private String marketDisplayName;
    private ClaimType claimType;
    private String policyNumber;
    private String claimantName;
    private String claimantId;
    private LocalDate incidentDate;
    private String description;
    private BigDecimal estimatedAmount;
    private BigDecimal settledAmount;
    private ClaimStatus status;
    private String assignedOfficerId;
    private Instant createdAt;
    private Instant updatedAt;

    public static ClaimResponse fromEntity(Claim claim) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .referenceNumber(claim.getReferenceNumber())
                .market(claim.getMarket())
                .marketDisplayName(claim.getMarket().getDisplayName())
                .claimType(claim.getClaimType())
                .policyNumber(claim.getPolicyNumber())
                .claimantName(claim.getClaimantName())
                .claimantId(claim.getClaimantId())
                .incidentDate(claim.getIncidentDate())
                .description(claim.getDescription())
                .estimatedAmount(claim.getEstimatedAmount())
                .settledAmount(claim.getSettledAmount())
                .status(claim.getStatus())
                .assignedOfficerId(claim.getAssignedOfficerId())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }
}
