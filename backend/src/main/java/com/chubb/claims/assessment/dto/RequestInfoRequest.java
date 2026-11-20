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
public class RequestInfoRequest {

    @NotBlank(message = "Note / details of required information is required")
    @Size(min = 5, max = 2000, message = "Note must be between 5 and 2000 characters")
    @Schema(description = "Details of supplementary documentation or clarification requested from claimant",
            example = "Please provide official police report and repair workshop damage assessment.")
    private String note;
}
