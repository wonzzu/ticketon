package com.ticketing.outbox.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.outbox.dto.EventEnvelope;
import com.ticketing.outbox.dto.PaymentCanceledOutboxPayload;
import com.ticketing.outbox.dto.PaymentCompletedOutboxPayload;
import com.ticketing.outbox.exception.DuplicateMessageException;
import com.ticketing.outbox.service.PaymentNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPaymentNotificationConsumer {

    private static final String PAYMENT_COMPLETED = "PAYMENT_COMPLETED";
    private static final String PAYMENT_CANCELED = "PAYMENT_CANCELED";

    private final ObjectMapper objectMapper;
    private final PaymentNotificationService notificationService;

    @KafkaListener(
            topics = "${app.kafka.topic.payment-events}",
            groupId = "ticketon-notification",
            containerFactory = "notificationKafkaListenerContainerFactory"
    )
    public void consume(
            String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String partitionKey
    ) {
        EventEnvelope envelope = deserializeEnvelope(message);

        try {
            switch (envelope.eventType()) {
                case PAYMENT_COMPLETED -> handleCompleted(envelope);
                case PAYMENT_CANCELED -> handleCanceled(envelope);
                default -> {
                    log.debug(
                            "알림 Consumer 담당 이벤트가 아니므로 생략: eventType={}, eventId={}",
                            envelope.eventType(),
                            envelope.eventId()
                    );
                    return;
                }
            }

            log.info(
                    "Kafka 결제 알림 이벤트 처리 완료: eventType={}, eventId={}, partitionKey={}",
                    envelope.eventType(),
                    envelope.eventId(),
                    partitionKey
            );
        } catch (DuplicateMessageException e) {
            log.info(
                    "중복 Kafka 알림 메시지 처리 생략: eventId={}",
                    envelope.eventId()
            );
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Kafka 결제 알림 payload 역직렬화에 실패했습니다.",
                    e
            );
        }
    }

    private void handleCompleted(
            EventEnvelope envelope
    ) throws JsonProcessingException {
        PaymentCompletedOutboxPayload payload = objectMapper.treeToValue(
                envelope.payload(),
                PaymentCompletedOutboxPayload.class
        );

        notificationService.handleCompleted(
                envelope.eventId(),
                payload
        );
    }

    private void handleCanceled(
            EventEnvelope envelope
    ) throws JsonProcessingException {
        PaymentCanceledOutboxPayload payload = objectMapper.treeToValue(
                envelope.payload(),
                PaymentCanceledOutboxPayload.class
        );

        notificationService.handleCanceled(
                envelope.eventId(),
                payload
        );
    }

    private EventEnvelope deserializeEnvelope(String message) {
        try {
            return objectMapper.readValue(
                    message,
                    EventEnvelope.class
            );
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Kafka 알림 이벤트 Envelope 역직렬화에 실패했습니다.",
                    e
            );
        }
    }
}
