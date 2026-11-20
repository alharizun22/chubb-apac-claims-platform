package com.chubb.claims.claims.dto;

import com.chubb.claims.claims.model.ClaimType;
import com.chubb.claims.claims.model.Market;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitClaimRequest {

    @NotNull(message = "Market is required (MY, SG, TH, HK, AU, JP)")
    @Schema(description = "Chubb APAC market code", example = "MY")
    private Market market;

    @NotNull(message = "Claim type is required (MOTOR, PROPERTY)")
    @Schema(description = "Type of insurance claim", example = "MOTOR")
    private ClaimType claimType;

    @NotBlank(message = "Policy number is required")
    @Schema(description = "Policy number", example = "POL-MY-2024-8831")
    private String policyNumber;

    @NotBlank(message = "Claimant name is required")
    @Schema(description = "Full legal name of the claimant", example = "Ahmad Razak")
    private String claimantName;

    @NotBlank(message = "Claimant identifier is required")
    @Schema(description = "Unique claimant account or national ID", example = "claimant-001")
    private String claimantId;

    @NotNull(message = "Incident date is required")
    @PastOrPresent(message = "Incident date cannot be in the future")
    @Schema(description = "Date when the loss/incident occurred", example = "2026-11-15")
    private LocalDate incidentDate;

    @NotBlank(message = "Incident description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    @Schema(description = "Detailed description of the loss incident", example = "Vehicle collision at Jalan Ampang intersection during heavy rain.")
    private String description;

    @NotNull(message = "Estimated amount is required")
    @DecimalMin(value = "0.01", message = "Estimated claim amount must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Estimated amount must have at most 2 decimal places")
    @Schema(description = "Estimated claim loss amount in local currency", example = "4500.00")
    private BigDecimal estimatedAmount;
}
