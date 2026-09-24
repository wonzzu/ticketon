package com.ticketing.notification.event;

import com.ticketing.notification.domain.Notification;
import com.ticketing.notification.domain.NotificationType;

import java.time.LocalDateTime;

public record NotificationCreatedEvent(
        Long notificationId,
        Long memberId,
        NotificationType type,
        String title,
        LocalDateTime createdAt
) {

    public static NotificationCreatedEvent from(Notification notification) {
        return new NotificationCreatedEvent(
                notification.getId(),
                notification.getMemberId(),
                notification.getType(),
                notification.getTitle(),
                notification.getCreatedAt()
        );
    }
}
