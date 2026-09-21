package com.ticketing.outbox.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.outbox.dto.EventEnvelope;
import com.ticketing.outbox.domain.OutboxEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Primary
@Component
@ConditionalOnProperty(name = "outbox.publisher.type", havingValue = "kafka")
public class KafkaOutboxMessagePublisher implements OutboxMessagePublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;
    private final long confirmTimeoutMs;

    public KafkaOutboxMessagePublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topic.payment-events}") String topic,
            @Value("${outbox.relay.confirm-timeout-ms:5000}") long confirmTimeoutMs
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
        this.confirmTimeoutMs = confirmTimeoutMs;
    }

    @Override
    public void publish(OutboxEvent event) {
        String partitionKey = event.getAggregateType() + ":" + event.getAggregateId();

        ProducerRecord<String, String> record = new ProducerRecord<>(
                topic,
                partitionKey,
                serializeEnvelope(event)
        );

        addHeader(record, "messageId", event.getMessageId());
        addHeader(record, "eventType", event.getEventType().name());
        addHeader(record, "eventVersion", event.getEventVersion().toString());
        addHeader(record, "eventSequence", Long.toString(event.getEventSequence()));
        addHeader(record, "aggregateType", event.getAggregateType());
        addHeader(record, "aggregateId", event.getAggregateId().toString());

        try {
            kafkaTemplate.send(record).get(confirmTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kafka 발행 확인 대기 중 스레드가 중단됐습니다.", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Kafka 이벤트 발행에 실패했습니다.", e);
        }
    }

    private String serializeEnvelope(OutboxEvent event) {
        try {
            JsonNode payload = objectMapper.readTree(event.getPayload());
            return objectMapper.writeValueAsString(EventEnvelope.from(event, payload));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Kafka 이벤트 Envelope 직렬화에 실패했습니다.", e);
        }
    }

    private void addHeader(ProducerRecord<String, String> record, String name, String value) {
        record.headers().add(name, value.getBytes(StandardCharsets.UTF_8));
    }
}
