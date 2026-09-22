package com.ticketing.notification.dto.response;

public record ReadAllNotificationResponseDto(int updatedCount) {

    public static ReadAllNotificationResponseDto of(int updatedCount) {
        return new ReadAllNotificationResponseDto(updatedCount);
    }
}
