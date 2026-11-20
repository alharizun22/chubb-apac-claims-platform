package com.chubb.claims.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettleClaimRequest {

    @NotNull(message = "Settled payout amount is required")
    @DecimalMin(value = "0.01", message = "Settled amount must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Settled amount must have at most 2 decimal places")
    @Schema(description = "Approved settlement payout amount in local market currency", example = "4200.00")
    private BigDecimal settledAmount;

    @Schema(description = "Optional settlement remarks or payment breakdown", example = "Approved after deductible deduction.")
    private String note;
}
