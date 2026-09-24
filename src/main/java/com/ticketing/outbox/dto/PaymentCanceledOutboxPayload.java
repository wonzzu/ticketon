package com.ticketing.outbox.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PaymentCanceledOutboxPayload(
        Long paymentId,
        Long reservationId,
        Long memberId,
        Integer canceledAmount,
        LocalDateTime canceledAt,
        Long sellerId,
        Long performanceEventId,
        LocalDate settlementDate,
        LocalDate paidDate
) {
}
