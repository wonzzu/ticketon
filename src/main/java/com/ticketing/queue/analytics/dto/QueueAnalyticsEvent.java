package com.ticketing.queue.analytics.dto;

import java.time.LocalDateTime;

public record QueueAnalyticsEvent(
        String journeyId,
        Long memberId,
        Long scheduleId,
        LocalDateTime enteredAt,
        LocalDateTime admittedAt
) {
}
