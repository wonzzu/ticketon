package com.ticketing.outbox.domain;

public enum OutboxEventType {
    RESERVATION_CREATED,
    PAYMENT_COMPLETED,
    PAYMENT_CANCELED
}
