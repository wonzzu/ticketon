package com.ticketing.outbox.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.outbox.dto.PaymentCanceledOutboxPayload;
import com.ticketing.outbox.exception.DuplicateMessageException;
import com.ticketing.outbox.service.PaymentCanceledMessageHandler;
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
    private final PaymentCanceledMessageHandler messageHandler;

    @KafkaListener(
            topics = "${app.kafka.topic.payment-events}",
            groupId = "ticketon-reaggregation"
    )
    public void consume(
            String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String messageId,
            @Header("eventType") String eventType
    ) {
        if (!"PAYMENT_CANCELED".equals(eventType)) {
            log.debug(
                    "재집계 Consumer 담당 이벤트가 아니므로 생략: eventType={}, messageId={}",
                    eventType,
                    messageId
            );
            return;
        }

        try {
            PaymentCanceledOutboxPayload payload = objectMapper.readValue(
                    message,
                    PaymentCanceledOutboxPayload.class
            );

            messageHandler.handle(messageId, payload);

            log.info("Kafka 결제 취소 이벤트 처리 완료: messageId={}", messageId);
        } catch (DuplicateMessageException e) {
            log.info("중복 Kafka 메시지 처리 생략: messageId={}", messageId);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Kafka 결제 취소 payload 역직렬화에 실패했습니다.",
                    e
            );
        }
    }
}
