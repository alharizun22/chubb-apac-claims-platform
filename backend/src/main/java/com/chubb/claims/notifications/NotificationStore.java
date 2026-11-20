package com.chubb.claims.notifications;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class NotificationStore {

    private final List<NotificationRecord> notifications = new CopyOnWriteArrayList<>();

    public void add(NotificationRecord record) {
        notifications.add(0, record); // Keep newest first
        if (notifications.size() > 100) {
            notifications.remove(notifications.size() - 1);
        }
    }

    public List<NotificationRecord> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(notifications));
    }

    public List<NotificationRecord> findByClaimId(java.util.UUID claimId) {
        return notifications.stream()
                .filter(n -> claimId.equals(n.getClaimId()))
                .toList();
    }

    public void clear() {
        notifications.clear();
    }
}
