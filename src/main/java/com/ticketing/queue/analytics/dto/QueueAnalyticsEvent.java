package com.ticketing.queue.analytics.dto;

import java.time.LocalDateTime;

public record QueueAnalyticsEvent(
        Long memberId,
        Long scheduleId,
        LocalDateTime enteredAt,
        LocalDateTime admittedAt
) {
}
