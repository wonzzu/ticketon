package com.ticketing.notification.dto.response;

public record UnreadNotificationCountResponseDto(long count) {

    public static UnreadNotificationCountResponseDto of(long count) {
        return new UnreadNotificationCountResponseDto(count);
    }
}
