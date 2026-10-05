package com.ticketing.kafka.failure.domain;

public enum FailedKafkaEventStatus {
    PENDING,
    REPROCESSING,
    REPUBLISHED,
    FAILED
}
