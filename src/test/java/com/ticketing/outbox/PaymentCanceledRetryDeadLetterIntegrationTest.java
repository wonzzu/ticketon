package com.ticketing.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.config.RabbitMqConfig;
import com.ticketing.outbox.domain.OutboxEvent;
import com.ticketing.outbox.dto.PaymentCanceledOutboxPayload;
import com.ticketing.outbox.messaging.RabbitOutboxMessagePublisher;
import com.ticketing.outbox.service.PaymentCanceledMessageHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Tag("local")
@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=true")
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("결제 취소 메시지 Retry·DLQ 통합 테스트")
class PaymentCanceledRetryDeadLetterIntegrationTest {

    @Autowired RabbitOutboxMessagePublisher messagePublisher;
    @Autowired RabbitAdmin rabbitAdmin;
    @Autowired RabbitTemplate rabbitTemplate;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean PaymentCanceledMessageHandler messageHandler;

    @BeforeEach
    void purgeQueues() {
        rabbitAdmin.purgeQueue(RabbitMqConfig.PAYMENT_CANCELED_QUEUE, false);
        rabbitAdmin.purgeQueue(RabbitMqConfig.PAYMENT_CANCELED_DLQ, false);
    }

    @Test
    @DisplayName("처리가 계속 실패하면 세 번 시도한 뒤 메시지를 DLQ로 이동한다")
    void moveToDeadLetterQueueAfterRetries() throws JsonProcessingException {
        doThrow(new IllegalStateException("DB 일시 장애"))
                .when(messageHandler).handle(anyString(), any(PaymentCanceledOutboxPayload.class));
        PaymentCanceledOutboxPayload payload = new PaymentCanceledOutboxPayload(
                1L, 2L, LocalDate.of(2026, 8, 20), LocalDate.of(2026, 8, 19));
        OutboxEvent outboxEvent = OutboxEvent.paymentCanceled(
                10L, objectMapper.writeValueAsString(payload));

        messagePublisher.publish(outboxEvent);

        Message deadLetter = rabbitTemplate.receive(RabbitMqConfig.PAYMENT_CANCELED_DLQ, 8_000);

        assertThat(deadLetter).isNotNull();
        assertThat((String) deadLetter.getMessageProperties().getHeader("messageId"))
                .isEqualTo(outboxEvent.getMessageId());
        verify(messageHandler, times(3)).handle(anyString(), any(PaymentCanceledOutboxPayload.class));
    }
}
