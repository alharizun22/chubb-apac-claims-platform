package com.chubb.claims.claims.model;

public enum ClaimStatus {
    SUBMITTED,
    UNDER_REVIEW,
    PENDING_INFO,
    SETTLED,
    REJECTED;

    public boolean isTerminal() {
        return this == SETTLED || this == REJECTED;
    }

    public boolean isOpen() {
        return !isTerminal();
    }
}
