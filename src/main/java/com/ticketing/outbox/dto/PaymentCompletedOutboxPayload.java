package com.ticketing.outbox.dto;

import java.time.LocalDateTime;

public record PaymentCompletedOutboxPayload(
        Long paymentId,
        Long reservationId,
        Long memberId,
        Long sellerId,
        Long performanceEventId,
        Long scheduleId,
        Integer paidAmount,
        LocalDateTime paidAt
) {
}
