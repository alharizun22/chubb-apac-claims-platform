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
public class ProvideInfoRequest {

    @NotBlank(message = "Response note / details cannot be blank")
    @Size(min = 5, max = 2000, message = "Note must be between 5 and 2000 characters")
    @Schema(description = "Information and document references provided by claimant",
            example = "Police report #P-99214 attached. Workshop estimate is MYR 4,200.00.")
    private String note;
}
