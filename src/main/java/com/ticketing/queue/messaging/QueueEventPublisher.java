package com.ticketing.queue.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ticketing.outbox.dto.EventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic.queue-events}")
    private String topic;

    public void publishEntered(Long scheduleId, Long memberId, String journeyId, long enteredAtMillis) {
        LocalDateTime enteredAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(enteredAtMillis),
                ZoneId.of("Asia/Seoul")
        );

        ObjectNode payload = objectMapper.createObjectNode()
                .put("journeyId", journeyId)
                .put("memberId", memberId)
                .put("scheduleId", scheduleId)
                .put("enteredAt", enteredAt.toString());

        EventEnvelope envelope = EventEnvelope.queueEntered(scheduleId, enteredAt, payload);

        try {
            send(scheduleId, memberId, journeyId, envelope);
        } catch (JsonProcessingException e) {
            log.warn("queue.entered 직렬화 실패: scheduleId={}, memberId={}, journeyId={}",
                    scheduleId, memberId, journeyId, e);
        }
    }

    public void publishAdmitted(Long scheduleId, Long memberId, String journeyId, Long enteredAtMillis, long admittedAtMillis) {
        LocalDateTime enteredAt = toLocalDateTime(enteredAtMillis == null ? admittedAtMillis : enteredAtMillis);
        LocalDateTime admittedAt = toLocalDateTime(admittedAtMillis);
        ObjectNode payload = objectMapper.createObjectNode()
                .put("journeyId", journeyId)
                .put("memberId", memberId)
                .put("scheduleId", scheduleId)
                .put("enteredAt", enteredAt.toString())
                .put("admittedAt", admittedAt.toString());
        EventEnvelope envelope = EventEnvelope.queueAdmitted(scheduleId, admittedAt, payload);
        try {
            send(scheduleId, memberId, journeyId, envelope);
        } catch (JsonProcessingException e) {
            log.warn("queue.admitted 직렬화 실패: scheduleId={}, memberId={}, journeyId={}",
                    scheduleId, memberId, journeyId, e);
        }
    }

    private void send(Long scheduleId, Long memberId, String journeyId, EventEnvelope envelope)
            throws JsonProcessingException {
        ProducerRecord<String, String> record = new ProducerRecord<>(
                topic,
                journeyId,
                objectMapper.writeValueAsString(envelope)
        );
        record.headers().add("eventId", envelope.eventId().getBytes(StandardCharsets.UTF_8));
        record.headers().add("eventType", envelope.eventType().getBytes(StandardCharsets.UTF_8));
        record.headers().add("journeyId", journeyId.getBytes(StandardCharsets.UTF_8));

        kafkaTemplate.send(record).whenComplete((result, error) -> {
            if (error != null) {
                log.warn("대기열 분석 이벤트 발행 실패: eventType={}, scheduleId={}, memberId={}, journeyId={}",
                        envelope.eventType(), scheduleId, memberId, journeyId, error);
            }
        });
    }

    private LocalDateTime toLocalDateTime(Long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.of("Asia/Seoul"));
    }
}
