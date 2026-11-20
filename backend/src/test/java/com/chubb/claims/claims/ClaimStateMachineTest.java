package com.chubb.claims.claims;

import com.chubb.claims.claims.model.ClaimStatus;
import com.chubb.claims.claims.service.ClaimStateMachine;
import com.chubb.claims.common.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class ClaimStateMachineTest {

    private ClaimStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new ClaimStateMachine();
    }

    @Nested
    @DisplayName("Valid State Transitions")
    class ValidTransitions {

        @Test
        @DisplayName("SUBMITTED -> UNDER_REVIEW (Officer pickup)")
        void submittedToUnderReview() {
            assertDoesNotThrow(() ->
                    stateMachine.validateTransition(ClaimStatus.SUBMITTED, ClaimStatus.UNDER_REVIEW));
        }

        @Test
        @DisplayName("UNDER_REVIEW -> PENDING_INFO (Officer requests info)")
        void underReviewToPendingInfo() {
            assertDoesNotThrow(() ->
                    stateMachine.validateTransition(ClaimStatus.UNDER_REVIEW, ClaimStatus.PENDING_INFO));
        }

        @Test
        @DisplayName("PENDING_INFO -> UNDER_REVIEW (Claimant provides info)")
        void pendingInfoToUnderReview() {
            assertDoesNotThrow(() ->
                    stateMachine.validateTransition(ClaimStatus.PENDING_INFO, ClaimStatus.UNDER_REVIEW));
        }

        @Test
        @DisplayName("UNDER_REVIEW -> SETTLED (Officer settles claim)")
        void underReviewToSettled() {
            assertDoesNotThrow(() ->
                    stateMachine.validateTransition(ClaimStatus.UNDER_REVIEW, ClaimStatus.SETTLED));
        }

        @Test
        @DisplayName("UNDER_REVIEW -> REJECTED (Officer rejects claim)")
        void underReviewToRejected() {
            assertDoesNotThrow(() ->
                    stateMachine.validateTransition(ClaimStatus.UNDER_REVIEW, ClaimStatus.REJECTED));
        }
    }

    @Nested
    @DisplayName("Invalid State Transitions (Enforces 409 Conflict)")
    class InvalidTransitions {

        @Test
        @DisplayName("SUBMITTED cannot jump directly to SETTLED")
        void submittedToSettledThrows() {
            InvalidStateTransitionException ex = assertThrows(InvalidStateTransitionException.class, () ->
                    stateMachine.validateTransition(ClaimStatus.SUBMITTED, ClaimStatus.SETTLED));
            assertTrue(ex.getMessage().contains("SUBMITTED"));
        }

        @Test
        @DisplayName("SUBMITTED cannot jump directly to REJECTED")
        void submittedToRejectedThrows() {
            assertThrows(InvalidStateTransitionException.class, () ->
                    stateMachine.validateTransition(ClaimStatus.SUBMITTED, ClaimStatus.REJECTED));
        }

        @Test
        @DisplayName("SUBMITTED cannot jump directly to PENDING_INFO")
        void submittedToPendingInfoThrows() {
            assertThrows(InvalidStateTransitionException.class, () ->
                    stateMachine.validateTransition(ClaimStatus.SUBMITTED, ClaimStatus.PENDING_INFO));
        }

        @Test
        @DisplayName("PENDING_INFO cannot transition directly to SETTLED")
        void pendingInfoToSettledThrows() {
            assertThrows(InvalidStateTransitionException.class, () ->
                    stateMachine.validateTransition(ClaimStatus.PENDING_INFO, ClaimStatus.SETTLED));
        }

        @Test
        @DisplayName("PENDING_INFO cannot transition directly to REJECTED")
        void pendingInfoToRejectedThrows() {
            assertThrows(InvalidStateTransitionException.class, () ->
                    stateMachine.validateTransition(ClaimStatus.PENDING_INFO, ClaimStatus.REJECTED));
        }

        @ParameterizedTest
        @EnumSource(value = ClaimStatus.class, names = {"SUBMITTED", "UNDER_REVIEW", "PENDING_INFO", "SETTLED", "REJECTED"})
        @DisplayName("SETTLED is a terminal state - cannot transition anywhere")
        void settledCannotTransition(ClaimStatus target) {
            assertThrows(InvalidStateTransitionException.class, () ->
                    stateMachine.validateTransition(ClaimStatus.SETTLED, target));
        }

        @ParameterizedTest
        @EnumSource(value = ClaimStatus.class, names = {"SUBMITTED", "UNDER_REVIEW", "PENDING_INFO", "SETTLED", "REJECTED"})
        @DisplayName("REJECTED is a terminal state - cannot transition anywhere")
        void rejectedCannotTransition(ClaimStatus target) {
            assertThrows(InvalidStateTransitionException.class, () ->
                    stateMachine.validateTransition(ClaimStatus.REJECTED, target));
        }

        @ParameterizedTest
        @EnumSource(ClaimStatus.class)
        @DisplayName("Self transition (current == target) is illegal")
        void selfTransitionThrows(ClaimStatus status) {
            assertThrows(InvalidStateTransitionException.class, () ->
                    stateMachine.validateTransition(status, status));
        }
    }
}
