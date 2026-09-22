package com.ticketing.notification.dto.response;

import com.ticketing.notification.domain.Notification;
import com.ticketing.notification.domain.NotificationReferenceType;
import com.ticketing.notification.domain.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponseDto(
        Long id,
        NotificationType type,
        String title,
        String content,
        NotificationReferenceType referenceType,
        Long referenceId,
        boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {

    public static NotificationResponseDto from(Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.isRead(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}
