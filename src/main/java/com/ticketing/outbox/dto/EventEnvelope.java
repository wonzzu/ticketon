package com.ticketing.outbox.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.ticketing.outbox.domain.OutboxEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventEnvelope(
        String eventId,
        String eventType,
        int eventVersion,
        String aggregateType,
        Long aggregateId,
        long eventSequence,
        LocalDateTime occurredAt,
        JsonNode payload
) {

    public static EventEnvelope from(OutboxEvent outboxEvent, JsonNode payload) {
        return new EventEnvelope(
                outboxEvent.getMessageId(),
                outboxEvent.getEventType().name(),
                outboxEvent.getEventVersion(),
                outboxEvent.getAggregateType(),
                outboxEvent.getAggregateId(),
                outboxEvent.getEventSequence(),
                outboxEvent.getCreatedAt(),
                payload
        );
    }

    public static EventEnvelope queueEntered(
            Long scheduleId,
            LocalDateTime occurredAt,
            JsonNode payload
    ) {
        return new EventEnvelope(
                UUID.randomUUID().toString(),
                "QUEUE_ENTERED",
                1,
                "QUEUE",
                scheduleId,
                0,
                occurredAt,
                payload
        );
    }
}
