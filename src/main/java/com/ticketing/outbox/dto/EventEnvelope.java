package com.ticketing.outbox.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.ticketing.outbox.domain.OutboxEvent;

import java.time.LocalDateTime;

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
}
