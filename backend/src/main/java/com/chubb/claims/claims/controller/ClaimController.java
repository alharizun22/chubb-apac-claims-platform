package com.chubb.claims.claims.controller;

import com.chubb.claims.claims.dto.ClaimDetailResponse;
import com.chubb.claims.claims.dto.ClaimResponse;
import com.chubb.claims.claims.dto.SubmitClaimRequest;
import com.chubb.claims.claims.model.ClaimStatus;
import com.chubb.claims.claims.model.Market;
import com.chubb.claims.claims.service.ClaimService;
import com.chubb.claims.common.auth.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = "*")
@Tag(name = "Claims", description = "Claim submission, retrieval, and lifecycle queries")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    @Operation(summary = "Submit a new claim (FNOL)", description = "Claimant submits a First Notice of Loss. Generates an immutable reference number.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Claim successfully submitted"),
            @ApiResponse(responseCode = "400", description = "Validation error in input fields")
    })
    public ResponseEntity<ClaimDetailResponse> submitClaim(@Valid @RequestBody SubmitClaimRequest request) {
        ClaimDetailResponse response = claimService.submitClaim(request, UserContextHolder.getContext());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get claim details by ID", description = "Returns full claim attributes along with the chronological activity audit timeline.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claim found"),
            @ApiResponse(responseCode = "404", description = "Claim not found")
    })
    public ResponseEntity<ClaimDetailResponse> getClaim(
            @Parameter(description = "Claim UUID", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(claimService.getClaimDetail(id));
    }

    @GetMapping
    @Operation(summary = "List and filter claims", description = "Query claims by market, status, claimant ID, unassigned queue, or assigned officer.")
    @ApiResponse(responseCode = "200", description = "Filtered list of claims")
    public ResponseEntity<List<ClaimResponse>> listClaims(
            @RequestParam(required = false) Market market,
            @RequestParam(required = false) ClaimStatus status,
            @RequestParam(required = false) String claimantId,
            @RequestParam(required = false) Boolean unassignedOnly,
            @RequestParam(required = false) String assignedOfficerId) {
        return ResponseEntity.ok(claimService.listClaims(market, status, claimantId, unassignedOnly, assignedOfficerId));
    }
}
