package com.chubb.claims.common.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {
    private String userId;
    private UserRole role;

    public boolean isOfficer() {
        return role == UserRole.OFFICER;
    }

    public boolean isClaimant() {
        return role == UserRole.CLAIMANT;
    }

    public boolean isSystem() {
        return role == UserRole.SYSTEM;
    }
}
