package com.chubb.claims.claims.dto;

import com.chubb.claims.claims.model.ActionType;
import com.chubb.claims.claims.model.ClaimActivity;
import com.chubb.claims.common.auth.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimActivityDto {
    private UUID id;
    private UUID claimId;
    private String actorId;
    private UserRole actorRole;
    private ActionType actionType;
    private String note;
    private Instant createdAt;

    public static ClaimActivityDto fromEntity(ClaimActivity entity) {
        return ClaimActivityDto.builder()
                .id(entity.getId())
                .claimId(entity.getClaimId())
                .actorId(entity.getActorId())
                .actorRole(entity.getActorRole())
                .actionType(entity.getActionType())
                .note(entity.getNote())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
