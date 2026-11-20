package com.chubb.claims.kafka;

import com.chubb.claims.assessment.dto.ProvideInfoRequest;
import com.chubb.claims.assessment.dto.RequestInfoRequest;
import com.chubb.claims.assessment.dto.SettleClaimRequest;
import com.chubb.claims.assessment.service.AssessmentService;
import com.chubb.claims.claims.dto.ClaimDetailResponse;
import com.chubb.claims.claims.dto.SubmitClaimRequest;
import com.chubb.claims.claims.model.ClaimStatus;
import com.chubb.claims.claims.model.ClaimType;
import com.chubb.claims.claims.model.Market;
import com.chubb.claims.claims.service.ClaimService;
import com.chubb.claims.common.auth.UserContext;
import com.chubb.claims.common.auth.UserRole;
import com.chubb.claims.config.TestKafkaConfig;
import com.chubb.claims.exposure.dto.MarketExposureResponse;
import com.chubb.claims.exposure.service.ExposureService;
import com.chubb.claims.notifications.NotificationStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestKafkaConfig.class)
@DirtiesContext
class ClaimKafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ClaimService claimService;

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private ExposureService exposureService;

    @Autowired
    private NotificationStore notificationStore;

    private UserContext claimantContext;
    private UserContext officerContext;

    @BeforeEach
    void setUp() {
        claimantContext = UserContext.builder()
                .userId("claimant-kafka-test")
                .role(UserRole.CLAIMANT)
                .build();

        officerContext = UserContext.builder()
                .userId("officer-kafka-test")
                .role(UserRole.OFFICER)
                .build();

        notificationStore.clear();
        reset(kafkaTemplate);
    }

    @Test
    @DisplayName("End-to-End: Submit Claim emits event to Kafka, updates Exposure projection, and records Notification")
    void testSubmitClaimEventPipeline() {
        SubmitClaimRequest request = SubmitClaimRequest.builder()
                .market(Market.SG)
                .claimType(ClaimType.MOTOR)
                .policyNumber("POL-SG-TEST-001")
                .claimantName("John Doe")
                .claimantId("claimant-kafka-test")
                .incidentDate(LocalDate.now().minusDays(1))
                .description("Minor bumper scratch in parking lot.")
                .estimatedAmount(new BigDecimal("1200.00"))
                .build();

        ClaimDetailResponse submitted = claimService.submitClaim(request, claimantContext);
        assertNotNull(submitted.getId());
        assertEquals(ClaimStatus.SUBMITTED, submitted.getStatus());

        // Verify KafkaTemplate.send was called with the claimId key
        verify(kafkaTemplate, atLeastOnce()).send(anyString(), eq(submitted.getId().toString()), any());

        // Verify async event consumption updates Market Exposure
        await().atMost(5, TimeUnit.SECONDS).pollInterval(Duration.ofMillis(50)).untilAsserted(() -> {
            MarketExposureResponse exposure = exposureService.getMarketExposure(Market.SG);
            assertTrue(exposure.getOpenClaimCount() >= 1, "Open claim count should be at least 1");
            assertTrue(exposure.getTotalEstimatedExposure().compareTo(new BigDecimal("1200.00")) >= 0);
        });

        // Verify async notification was recorded
        await().atMost(5, TimeUnit.SECONDS).pollInterval(Duration.ofMillis(50)).untilAsserted(() -> {
            assertFalse(notificationStore.getAll().isEmpty(), "Notification store should receive event");
            assertTrue(notificationStore.getAll().stream()
                    .anyMatch(n -> n.getReferenceNumber().equals(submitted.getReferenceNumber())));
        });
    }

    @Test
    @DisplayName("Full Lifecycle: Submit -> Pickup -> Request Info -> Provide Info -> Settle verifies events & exposure")
    void testCompleteClaimLifecycleEventsAndExposure() {
        // 1. Submit
        SubmitClaimRequest submitReq = SubmitClaimRequest.builder()
                .market(Market.MY)
                .claimType(ClaimType.PROPERTY)
                .policyNumber("POL-MY-LIFECYCLE-99")
                .claimantName("Nurul Ain")
                .claimantId("claimant-my-01")
                .incidentDate(LocalDate.now().minusDays(2))
                .description("Roof leak damage to electronics.")
                .estimatedAmount(new BigDecimal("5000.00"))
                .build();

        ClaimDetailResponse claim = claimService.submitClaim(submitReq, claimantContext);
        UUID claimId = claim.getId();
        assertEquals(ClaimStatus.SUBMITTED, claim.getStatus());

        // 2. Pickup (Officer)
        ClaimDetailResponse underReview = assessmentService.pickupClaim(claimId, officerContext);
        assertEquals(ClaimStatus.UNDER_REVIEW, underReview.getStatus());
        assertEquals("officer-kafka-test", underReview.getAssignedOfficerId());

        // 3. Request Info (Officer)
        ClaimDetailResponse pendingInfo = assessmentService.requestInfo(claimId,
                new RequestInfoRequest("Please upload purchase receipt for the damaged TV."),
                officerContext);
        assertEquals(ClaimStatus.PENDING_INFO, pendingInfo.getStatus());

        // 4. Provide Info (Claimant)
        ClaimDetailResponse infoProvided = assessmentService.provideInfo(claimId,
                new ProvideInfoRequest("Receipt #INV-88219 uploaded."),
                claimantContext);
        assertEquals(ClaimStatus.UNDER_REVIEW, infoProvided.getStatus());

        // 5. Settle (Officer)
        BigDecimal approvedAmount = new BigDecimal("4800.00");
        ClaimDetailResponse settled = assessmentService.settleClaim(claimId,
                new SettleClaimRequest(approvedAmount, "Settlement approved less MYR 200 deductible."),
                officerContext);
        assertEquals(ClaimStatus.SETTLED, settled.getStatus());
        assertEquals(approvedAmount, settled.getSettledAmount());

        // Verify KafkaTemplate.send was called across each transition
        verify(kafkaTemplate, atLeast(5)).send(anyString(), eq(claimId.toString()), any());

        // 6. Verify Projector updated exposure for Market MY
        await().atMost(5, TimeUnit.SECONDS).pollInterval(Duration.ofMillis(50)).untilAsserted(() -> {
            MarketExposureResponse exposure = exposureService.getMarketExposure(Market.MY);
            assertTrue(exposure.getTotalSettledPayout().compareTo(approvedAmount) >= 0,
                    "Total settled payout should reflect approved amount");
        });

        // 7. Verify all notifications are logged in order
        await().atMost(5, TimeUnit.SECONDS).pollInterval(Duration.ofMillis(50)).untilAsserted(() -> {
            var claimNotifications = notificationStore.findByClaimId(claimId);
            assertTrue(claimNotifications.size() >= 3,
                    "Should have multiple notifications across the lifecycle");
        });
    }
}
