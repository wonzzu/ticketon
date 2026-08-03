package com.ticketing.payment.dto;

import java.time.LocalDate;

public record PaymentCanceledEvent(Long sellerId, Long eventId, LocalDate settlementDate, LocalDate paidDate) {
}
