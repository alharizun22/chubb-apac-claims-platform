package com.chubb.claims.assessment.controller;

import com.chubb.claims.assessment.dto.ProvideInfoRequest;
import com.chubb.claims.assessment.dto.RejectClaimRequest;
import com.chubb.claims.assessment.dto.RequestInfoRequest;
import com.chubb.claims.assessment.dto.SettleClaimRequest;
import com.chubb.claims.assessment.service.AssessmentService;
import com.chubb.claims.claims.dto.ClaimDetailResponse;
import com.chubb.claims.common.auth.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/claims/{id}")
@CrossOrigin(origins = "*")
@Tag(name = "Assessment", description = "Claims officer assessment workflows and claimant information exchange")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping("/pickup")
    @Operation(summary = "Pick up an unassigned claim", description = "Officer claims ownership of a SUBMITTED claim. Transitions status to UNDER_REVIEW.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claim successfully picked up"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Caller is not an OFFICER"),
            @ApiResponse(responseCode = "404", description = "Claim not found"),
            @ApiResponse(responseCode = "409", description = "Conflict: Claim is not in SUBMITTED status")
    })
    public ResponseEntity<ClaimDetailResponse> pickupClaim(
            @Parameter(description = "Claim UUID", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(assessmentService.pickupClaim(id, UserContextHolder.getContext()));
    }

    @PostMapping("/request-info")
    @Operation(summary = "Request additional information", description = "Officer requests documentation or clarification. Transitions status from UNDER_REVIEW to PENDING_INFO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Information request logged"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Caller is not an OFFICER"),
            @ApiResponse(responseCode = "404", description = "Claim not found"),
            @ApiResponse(responseCode = "409", description = "Conflict: Claim is not in UNDER_REVIEW status")
    })
    public ResponseEntity<ClaimDetailResponse> requestInfo(
            @Parameter(description = "Claim UUID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody RequestInfoRequest request) {
        return ResponseEntity.ok(assessmentService.requestInfo(id, request, UserContextHolder.getContext()));
    }

    @PostMapping("/provide-info")
    @Operation(summary = "Provide requested information", description = "Claimant provides requested details. Transitions status from PENDING_INFO back to UNDER_REVIEW.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Information recorded and claim returned to review"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Caller is not a CLAIMANT"),
            @ApiResponse(responseCode = "404", description = "Claim not found"),
            @ApiResponse(responseCode = "409", description = "Conflict: Claim is not in PENDING_INFO status")
    })
    public ResponseEntity<ClaimDetailResponse> provideInfo(
            @Parameter(description = "Claim UUID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody ProvideInfoRequest request) {
        return ResponseEntity.ok(assessmentService.provideInfo(id, request, UserContextHolder.getContext()));
    }

    @PostMapping("/settle")
    @Operation(summary = "Settle claim with payout amount", description = "Officer approves and settles claim. Transitions status from UNDER_REVIEW to SETTLED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claim settled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid settlement amount"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Caller is not an OFFICER"),
            @ApiResponse(responseCode = "404", description = "Claim not found"),
            @ApiResponse(responseCode = "409", description = "Conflict: Claim is not in UNDER_REVIEW status")
    })
    public ResponseEntity<ClaimDetailResponse> settleClaim(
            @Parameter(description = "Claim UUID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody SettleClaimRequest request) {
        return ResponseEntity.ok(assessmentService.settleClaim(id, request, UserContextHolder.getContext()));
    }

    @PostMapping("/reject")
    @Operation(summary = "Reject claim with reason", description = "Officer rejects claim with formal justification. Transitions status from UNDER_REVIEW to REJECTED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claim rejected successfully"),
            @ApiResponse(responseCode = "400", description = "Rejection reason missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Caller is not an OFFICER"),
            @ApiResponse(responseCode = "404", description = "Claim not found"),
            @ApiResponse(responseCode = "409", description = "Conflict: Claim is not in UNDER_REVIEW status")
    })
    public ResponseEntity<ClaimDetailResponse> rejectClaim(
            @Parameter(description = "Claim UUID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody RejectClaimRequest request) {
        return ResponseEntity.ok(assessmentService.rejectClaim(id, request, UserContextHolder.getContext()));
    }
}
