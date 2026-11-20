package com.chubb.claims.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectClaimRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(min = 5, max = 2000, message = "Reason must be between 5 and 2000 characters")
    @Schema(description = "Justification for claim rejection based on policy terms",
            example = "Loss incident occurred outside policy coverage period.")
    private String reason;
}
