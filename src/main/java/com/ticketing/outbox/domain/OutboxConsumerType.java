package com.ticketing.outbox.domain;

public enum OutboxConsumerType {
    REAGGREGATION,
    NOTIFICATION,
    QUEUE_ANALYTICS,
    TICKET_ISSUANCE,
    FUNNEL_ANALYTICS
}
