package com.ticketing.kafka.failure.dto.request;

import com.ticketing.kafka.failure.domain.FailedKafkaEventStatus;
import com.ticketing.outbox.domain.OutboxConsumerType;

public record FailedKafkaEventSearchCondition(
        FailedKafkaEventStatus status,
        OutboxConsumerType consumerType,
        String originalTopic,
        String eventId
) {
}
