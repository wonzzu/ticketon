package com.ticketing.queue.analytics.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.outbox.dto.EventEnvelope;
import com.ticketing.outbox.exception.DuplicateMessageException;
import com.ticketing.queue.analytics.dto.QueueAnalyticsEvent;
import com.ticketing.queue.analytics.service.QueueStatsAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueAnalyticsConsumer {

    private static final String QUEUE_ENTERED = "QUEUE_ENTERED";
    private static final String QUEUE_ADMITTED = "QUEUE_ADMITTED";

    private final ObjectMapper objectMapper;
    private final QueueStatsAggregationService aggregationService;

    @KafkaListener(
            topics = "${app.kafka.topic.queue-events}",
            groupId = "queue-analytics-group"
    )
    public void consume(
            String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String partitionKey,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partitionNo
    ) {
        EventEnvelope envelope = deserializeEnvelope(message);

        if (!isQueueAnalyticsEvent(envelope.eventType())) {
            log.debug("대기열 분석 대상이 아니므로 생략: eventType={}, eventId={}",
                    envelope.eventType(), envelope.eventId());
            return;
        }

        validateEnvelope(envelope);

        try {
            QueueAnalyticsEvent event = objectMapper.treeToValue(
                    envelope.payload(), QueueAnalyticsEvent.class
            );
            aggregationService.handle(envelope.eventId(), envelope.eventType(), partitionNo, event);

            log.info("대기열 통계 처리 완료: eventId={}, eventType={}, partitionKey={}, partitionNo={}",
                    envelope.eventId(), envelope.eventType(), partitionKey, partitionNo);
        } catch (DuplicateMessageException e) {
            log.info("중복 대기열 이벤트 처리 생략: eventId={}", envelope.eventId());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("대기열 이벤트 payload 역직렬화에 실패했습니다.", e);
        }
    }

    private EventEnvelope deserializeEnvelope(String message) {
        try {
            return objectMapper.readValue(message, EventEnvelope.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Kafka 이벤트 Envelope 역직렬화에 실패했습니다.", e);
        }
    }

    private boolean isQueueAnalyticsEvent(String eventType) {
        return QUEUE_ENTERED.equals(eventType) || QUEUE_ADMITTED.equals(eventType);
    }

    private void validateEnvelope(EventEnvelope envelope) {
        if (!"QUEUE".equals(envelope.aggregateType())) {
            throw new IllegalArgumentException("대기열 이벤트의 aggregateType은 QUEUE여야 합니다.");
        }
        if (envelope.payload() == null) {
            throw new IllegalArgumentException("대기열 이벤트 payload가 없습니다.");
        }
    }
}
