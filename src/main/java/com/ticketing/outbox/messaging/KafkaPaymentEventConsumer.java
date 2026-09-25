package com.ticketing.outbox.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.outbox.dto.EventEnvelope;
import com.ticketing.outbox.dto.PaymentCanceledOutboxPayload;
import com.ticketing.outbox.exception.DuplicateMessageException;
import com.ticketing.outbox.service.PaymentCanceledMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPaymentEventConsumer {

    private final ObjectMapper objectMapper;
    private final PaymentCanceledMessageService messageService;

    @KafkaListener(
            topics = "${app.kafka.topic.payment-events}",
            groupId = "ticketon-reaggregation"
    )
    public void consume(
            String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String partitionKey
    ) {
        EventEnvelope envelope = deserializeEnvelope(message);

        if (!"PAYMENT_CANCELED".equals(envelope.eventType())) {
            log.debug(
                    "재집계 Consumer 담당 이벤트가 아니므로 생략: eventType={}, eventId={}, partitionKey={}",
                    envelope.eventType(),
                    envelope.eventId(),
                    partitionKey
            );
            return;
        }

        try {
            PaymentCanceledOutboxPayload payload = objectMapper.treeToValue(
                    envelope.payload(),
                    PaymentCanceledOutboxPayload.class
            );

            messageService.handle(envelope.eventId(), payload);

            log.info(
                    "Kafka 결제 취소 이벤트 처리 완료: eventId={}, partitionKey={}, eventSequence={}",
                    envelope.eventId(),
                    partitionKey,
                    envelope.eventSequence()
            );
        } catch (DuplicateMessageException e) {
            log.info("중복 Kafka 메시지 처리 생략: eventId={}", envelope.eventId());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Kafka 결제 취소 payload 역직렬화에 실패했습니다.",
                    e
            );
        }
    }

    private EventEnvelope deserializeEnvelope(String message) {
        try {
            return objectMapper.readValue(message, EventEnvelope.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Kafka 이벤트 Envelope 역직렬화에 실패했습니다.", e);
        }
    }
}
