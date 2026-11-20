package com.chubb.claims.notifications.controller;

import com.chubb.claims.notifications.NotificationRecord;
import com.chubb.claims.notifications.NotificationStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
@Tag(name = "Notifications", description = "Simulated notifications dispatched by Kafka event consumers")
public class NotificationController {

    private final NotificationStore notificationStore;

    public NotificationController(NotificationStore notificationStore) {
        this.notificationStore = notificationStore;
    }

    @GetMapping
    @Operation(summary = "Get all dispatched notifications", description = "Returns recent notifications dispatched asynchronously via Kafka event consumers.")
    public ResponseEntity<List<NotificationRecord>> getNotifications(
            @RequestParam(required = false) UUID claimId) {
        if (claimId != null) {
            return ResponseEntity.ok(notificationStore.findByClaimId(claimId));
        }
        return ResponseEntity.ok(notificationStore.getAll());
    }
}
