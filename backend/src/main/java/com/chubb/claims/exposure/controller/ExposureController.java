package com.chubb.claims.exposure.controller;

import com.chubb.claims.claims.model.Market;
import com.chubb.claims.exposure.dto.MarketExposureResponse;
import com.chubb.claims.exposure.service.ExposureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exposure")
@CrossOrigin(origins = "*")
@Tag(name = "Market Exposure", description = "Regional claims liability exposure and aggregate settlement analytics")
public class ExposureController {

    private final ExposureService exposureService;

    public ExposureController(ExposureService exposureService) {
        this.exposureService = exposureService;
    }

    @GetMapping
    @Operation(summary = "Get regional exposure summary across all markets",
            description = "Returns open claim counts, total estimated exposure, and settled payout sums for all 6 Chubb APAC markets.")
    @ApiResponse(responseCode = "200", description = "Regional exposure projections list")
    public ResponseEntity<List<MarketExposureResponse>> getAllExposures() {
        return ResponseEntity.ok(exposureService.getAllExposures());
    }

    @GetMapping("/{market}")
    @Operation(summary = "Get exposure metrics for a specific market",
            description = "Returns current exposure projections for the selected market code (MY, SG, TH, HK, AU, JP).")
    @ApiResponse(responseCode = "200", description = "Market exposure projection")
    public ResponseEntity<MarketExposureResponse> getMarketExposure(
            @Parameter(description = "Chubb APAC market code", required = true)
            @PathVariable Market market) {
        return ResponseEntity.ok(exposureService.getMarketExposure(market));
    }
}
