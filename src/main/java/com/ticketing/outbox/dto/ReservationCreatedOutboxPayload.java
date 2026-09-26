package com.ticketing.outbox.dto;

import java.time.LocalDateTime;

public record ReservationCreatedOutboxPayload(
        String journeyId,
        Long reservationId,
        Long memberId,
        Long scheduleId,
        int seatCount,
        int totalPrice,
        LocalDateTime reservedAt
) {
}
