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

    public void publishEntered(Long scheduleId, Long memberId, long enteredAtMillis) {
        LocalDateTime enteredAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(enteredAtMillis),
                ZoneId.of("Asia/Seoul")
        );

        ObjectNode payload = objectMapper.createObjectNode()
                .put("queueToken", memberId.toString())
                .put("scheduleId", scheduleId)
                .put("enteredAt", enteredAt.toString());

        EventEnvelope envelope = EventEnvelope.queueEntered(scheduleId, enteredAt, payload);

        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    topic,
                    memberId.toString(),
                    objectMapper.writeValueAsString(envelope)
            );
            record.headers().add("eventId", envelope.eventId().getBytes(StandardCharsets.UTF_8));
            record.headers().add("eventType", envelope.eventType().getBytes(StandardCharsets.UTF_8));

            // 분석 이벤트 발행 실패가 Redis 대기열 응답을 막지 않도록 비동기로 보낸다.
            kafkaTemplate.send(record).whenComplete((result, error) -> {
                if (error != null) {
                    log.warn("queue.entered 발행 실패: scheduleId={}, memberId={}",
                            scheduleId, memberId, error);
                }
            });
        } catch (JsonProcessingException e) {
            log.warn("queue.entered 직렬화 실패: scheduleId={}, memberId={}",
                    scheduleId, memberId, e);
        }
    }
}
