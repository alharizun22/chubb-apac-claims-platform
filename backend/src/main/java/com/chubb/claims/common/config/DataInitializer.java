package com.chubb.claims.common.config;

import com.chubb.claims.claims.model.*;
import com.chubb.claims.claims.repository.ClaimActivityRepository;
import com.chubb.claims.claims.repository.ClaimRepository;
import com.chubb.claims.common.auth.UserRole;
import com.chubb.claims.common.util.ReferenceNumberGenerator;
import com.chubb.claims.exposure.service.ExposureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ClaimRepository claimRepository;
    private final ClaimActivityRepository activityRepository;
    private final ReferenceNumberGenerator referenceNumberGenerator;
    private final ExposureService exposureService;

    public DataInitializer(ClaimRepository claimRepository,
                           ClaimActivityRepository activityRepository,
                           ReferenceNumberGenerator referenceNumberGenerator,
                           ExposureService exposureService) {
        this.claimRepository = claimRepository;
        this.activityRepository = activityRepository;
        this.referenceNumberGenerator = referenceNumberGenerator;
        this.exposureService = exposureService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (claimRepository.count() > 0) {
            log.info("Database already seeded with claims. Recalculating market exposure...");
            exposureService.recalculateAll();
            return;
        }

        log.info("Seeding initial Chubb APAC demo claims data...");

        Instant now = Instant.now();

        // 1. Unassigned claim in SUBMITTED (Malaysia - Motor)
        createClaim(
                Market.MY,
                ClaimType.MOTOR,
                "POL-MY-2024-1182",
                "Ahmad Razak",
                "claimant-my-01",
                LocalDate.now().minusDays(3),
                "Front bumper and headlight damage following vehicle collision along Jalan Sultan Ismail in heavy rain.",
                new BigDecimal("3500.00"),
                null,
                ClaimStatus.SUBMITTED,
                null,
                now.minus(2, ChronoUnit.DAYS),
                new ActivitySeed(ActionType.SUBMITTED, "claimant-my-01", UserRole.CLAIMANT, "First Notice of Loss submitted by policyholder.", now.minus(2, ChronoUnit.DAYS))
        );

        // 2. Claim in UNDER_REVIEW (Singapore - Property)
        createClaim(
                Market.SG,
                ClaimType.PROPERTY,
                "POL-SG-2023-9014",
                "Tan Wei Ming",
                "claimant-sg-01",
                LocalDate.now().minusDays(7),
                "Water burst pipe damage in commercial retail shop basement causing inventory loss.",
                new BigDecimal("18500.00"),
                null,
                ClaimStatus.UNDER_REVIEW,
                "officer-apac-01",
                now.minus(5, ChronoUnit.DAYS),
                new ActivitySeed(ActionType.SUBMITTED, "claimant-sg-01", UserRole.CLAIMANT, "Claim submitted with initial damage photos.", now.minus(5, ChronoUnit.DAYS)),
                new ActivitySeed(ActionType.PICKED_UP, "officer-apac-01", UserRole.OFFICER, "Claim assigned to officer-apac-01 for loss assessment.", now.minus(4, ChronoUnit.DAYS))
        );

        // 3. Claim in PENDING_INFO (Thailand - Motor)
        createClaim(
                Market.TH,
                ClaimType.MOTOR,
                "POL-TH-2025-4421",
                "Somchai Prasert",
                "claimant-th-01",
                LocalDate.now().minusDays(10),
                "Side impact collision at Bangkok Sukhumvit intersection.",
                new BigDecimal("65000.00"),
                null,
                ClaimStatus.PENDING_INFO,
                "officer-apac-02",
                now.minus(8, ChronoUnit.DAYS),
                new ActivitySeed(ActionType.SUBMITTED, "claimant-th-01", UserRole.CLAIMANT, "Claim submitted with third party details.", now.minus(8, ChronoUnit.DAYS)),
                new ActivitySeed(ActionType.PICKED_UP, "officer-apac-02", UserRole.OFFICER, "Claim picked up for assessment.", now.minus(7, ChronoUnit.DAYS)),
                new ActivitySeed(ActionType.INFO_REQUESTED, "officer-apac-02", UserRole.OFFICER, "Please provide certified police report and official garage repair estimate in THB.", now.minus(6, ChronoUnit.DAYS))
        );

        // 4. Claim SETTLED (Hong Kong - Property)
        createClaim(
                Market.HK,
                ClaimType.PROPERTY,
                "POL-HK-2024-7733",
                "Chan Wai Keung",
                "claimant-hk-01",
                LocalDate.now().minusDays(20),
                "Typhoon gale damage to commercial warehouse roof glazing.",
                new BigDecimal("120000.00"),
                new BigDecimal("115000.00"),
                ClaimStatus.SETTLED,
                "officer-apac-01",
                now.minus(18, ChronoUnit.DAYS),
                new ActivitySeed(ActionType.SUBMITTED, "claimant-hk-01", UserRole.CLAIMANT, "Typhoon damage claim submitted.", now.minus(18, ChronoUnit.DAYS)),
                new ActivitySeed(ActionType.PICKED_UP, "officer-apac-01", UserRole.OFFICER, "Loss adjuster appointed.", now.minus(17, ChronoUnit.DAYS)),
                new ActivitySeed(ActionType.INFO_REQUESTED, "officer-apac-01", UserRole.OFFICER, "Surveyor report required.", now.minus(15, ChronoUnit.DAYS)),
                new ActivitySeed(ActionType.INFO_PROVIDED, "claimant-hk-01", UserRole.CLAIMANT, "Provided official surveyor assessment report.", now.minus(12, ChronoUnit.DAYS)),
                new ActivitySeed(ActionType.SETTLED, "officer-apac-01", UserRole.OFFICER, "Claim settled for HKD 115,000.00 after deductible adjustment.", now.minus(10, ChronoUnit.DAYS))
        );

        // 5. Claim REJECTED (Australia - Motor)
        createClaim(
                Market.AU,
                ClaimType.MOTOR,
                "POL-AU-2024-5512",
                "Liam Smith",
                "claimant-au-01",
                LocalDate.now().minusDays(14),
                "Off-road track engine submergence during recreational driving.",
                new BigDecimal("8200.00"),
                null,
                ClaimStatus.REJECTED,
                "officer-apac-02",
                now.minus(12, ChronoUnit.DAYS),
                new ActivitySeed(ActionType.SUBMITTED, "claimant-au-01", UserRole.CLAIMANT, "Engine damage claim submitted.", now.minus(12, ChronoUnit.DAYS)),
                new ActivitySeed(ActionType.PICKED_UP, "officer-apac-02", UserRole.OFFICER, "Reviewing policy coverage terms.", now.minus(10, ChronoUnit.DAYS)),
                new ActivitySeed(ActionType.REJECTED, "officer-apac-02", UserRole.OFFICER, "Claim rejected. Reason: Policy section 4.2 excludes unsealed off-road track water damage.", now.minus(8, ChronoUnit.DAYS))
        );

        // Recalculate projections for all markets
        exposureService.recalculateAll();
        log.info("Demo data seeding completed successfully.");
    }

    private void createClaim(Market market,
                             ClaimType type,
                             String policyNumber,
                             String claimantName,
                             String claimantId,
                             LocalDate incidentDate,
                             String description,
                             BigDecimal estimatedAmount,
                             BigDecimal settledAmount,
                             ClaimStatus status,
                             String assignedOfficerId,
                             Instant createdAt,
                             ActivitySeed... activities) {
        String ref = referenceNumberGenerator.generateReferenceNumber(market);

        Claim claim = Claim.builder()
                .referenceNumber(ref)
                .market(market)
                .claimType(type)
                .policyNumber(policyNumber)
                .claimantName(claimantName)
                .claimantId(claimantId)
                .incidentDate(incidentDate)
                .description(description)
                .estimatedAmount(estimatedAmount)
                .settledAmount(settledAmount)
                .status(status)
                .assignedOfficerId(assignedOfficerId)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        Claim saved = claimRepository.save(claim);

        for (ActivitySeed act : activities) {
            ClaimActivity activity = ClaimActivity.builder()
                    .claimId(saved.getId())
                    .actorId(act.actorId)
                    .actorRole(act.actorRole)
                    .actionType(act.actionType)
                    .note(act.note)
                    .createdAt(act.createdAt)
                    .build();
            activityRepository.save(activity);
        }
    }

    private record ActivitySeed(ActionType actionType, String actorId, UserRole actorRole, String note, Instant createdAt) {}
}
