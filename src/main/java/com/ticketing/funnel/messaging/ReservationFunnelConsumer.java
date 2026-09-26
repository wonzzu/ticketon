package com.ticketing.funnel.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.funnel.service.ReservationFunnelService;
import com.ticketing.outbox.dto.EventEnvelope;
import com.ticketing.outbox.dto.PaymentCompletedOutboxPayload;
import com.ticketing.outbox.dto.ReservationCreatedOutboxPayload;
import com.ticketing.outbox.exception.DuplicateMessageException;
import com.ticketing.queue.analytics.dto.QueueAnalyticsEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationFunnelConsumer {

    private static final String QUEUE_ENTERED = "QUEUE_ENTERED";
    private static final String QUEUE_ADMITTED = "QUEUE_ADMITTED";
    private static final String RESERVATION_CREATED = "RESERVATION_CREATED";
    private static final String PAYMENT_COMPLETED = "PAYMENT_COMPLETED";

    private final ObjectMapper objectMapper;
    private final ReservationFunnelService funnelService;

    @KafkaListener(
            topics = {
                    "${app.kafka.topic.queue-events}",
                    "${app.kafka.topic.reservation-events}",
                    "${app.kafka.topic.payment-events}"
            },
            groupId = "reservation-funnel-analytics",
            containerFactory = "funnelKafkaListenerContainerFactory"
    )
    public void consume(
            String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        EventEnvelope envelope = deserializeEnvelope(message);

        try {
            switch (envelope.eventType()) {
                case QUEUE_ENTERED, QUEUE_ADMITTED -> handleQueue(envelope);
                case RESERVATION_CREATED -> handleReservation(envelope);
                case PAYMENT_COMPLETED -> handlePayment(envelope);
                default -> {
                    log.debug("퍼널 분석 대상이 아니므로 생략: topic={}, eventType={}, eventId={}",
                            topic, envelope.eventType(), envelope.eventId());
                    return;
                }
            }

            log.info("퍼널 이벤트 처리 완료: topic={}, eventType={}, eventId={}",
                    topic, envelope.eventType(), envelope.eventId());
        } catch (DuplicateMessageException e) {
            log.info("중복 퍼널 이벤트 처리 생략: eventId={}", envelope.eventId());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("퍼널 이벤트 payload 역직렬화에 실패했습니다.", e);
        }
    }

    private void handleQueue(EventEnvelope envelope) throws JsonProcessingException {
        QueueAnalyticsEvent event = objectMapper.treeToValue(
                envelope.payload(), QueueAnalyticsEvent.class
        );
        funnelService.handleQueue(envelope.eventId(), envelope.eventType(), event);
    }

    private void handleReservation(EventEnvelope envelope) throws JsonProcessingException {
        ReservationCreatedOutboxPayload event = objectMapper.treeToValue(
                envelope.payload(), ReservationCreatedOutboxPayload.class
        );
        funnelService.handleReservation(envelope.eventId(), event);
    }

    private void handlePayment(EventEnvelope envelope) throws JsonProcessingException {
        PaymentCompletedOutboxPayload event = objectMapper.treeToValue(
                envelope.payload(), PaymentCompletedOutboxPayload.class
        );
        funnelService.handlePayment(envelope.eventId(), event);
    }

    private EventEnvelope deserializeEnvelope(String message) {
        try {
            return objectMapper.readValue(message, EventEnvelope.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("퍼널 이벤트 Envelope 역직렬화에 실패했습니다.", e);
        }
    }
}
