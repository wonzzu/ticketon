package com.ticketing.kafka.failure.dto.response;

import com.ticketing.kafka.failure.domain.FailedKafkaEvent;
import com.ticketing.kafka.failure.domain.FailedKafkaEventStatus;
import com.ticketing.outbox.domain.OutboxConsumerType;

import java.time.LocalDateTime;

public record FailedKafkaEventResponse(
        Long id,
        String eventId,
        OutboxConsumerType consumerType,
        String originalTopic,
        int originalPartition,
        long originalOffset,
        String dltTopic,
        String exceptionClass,
        String exceptionMessage,
        FailedKafkaEventStatus status,
        int reprocessCount,
        LocalDateTime lastReprocessedAt,
        LocalDateTime createdAt
) {
    public static FailedKafkaEventResponse from(FailedKafkaEvent event) {
        return new FailedKafkaEventResponse(
                event.getId(),
                event.getEventId(),
                event.getConsumerType(),
                event.getOriginalTopic(),
                event.getOriginalPartition(),
                event.getOriginalOffset(),
                event.getDltTopic(),
                event.getExceptionClass(),
                event.getExceptionMessage(),
                event.getStatus(),
                event.getReprocessCount(),
                event.getLastReprocessedAt(),
                event.getCreatedAt()
        );
    }
}
