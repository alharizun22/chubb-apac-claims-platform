package com.chubb.claims.notifications;

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
public class NotificationRecord {
    private UUID id;
    private UUID claimId;
    private String referenceNumber;
    private String recipient;
    private String title;
    private String message;
    private String channel; // SMS, EMAIL, IN_APP
    private Instant sentAt;
}
