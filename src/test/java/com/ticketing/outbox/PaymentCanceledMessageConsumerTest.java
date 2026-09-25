package com.ticketing.outbox;

import com.ticketing.outbox.dto.PaymentCanceledOutboxPayload;
import com.ticketing.outbox.messaging.PaymentCanceledMessageConsumer;
import com.ticketing.outbox.service.PaymentCanceledMessageService;
import com.ticketing.outbox.exception.DuplicateMessageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("결제 취소 메시지 Consumer")
class PaymentCanceledMessageConsumerTest {

    private final PaymentCanceledMessageService messageService = mock(PaymentCanceledMessageService.class);
    private final PaymentCanceledMessageConsumer consumer = new PaymentCanceledMessageConsumer(messageService);

    @Test
    @DisplayName("자동 변환된 결제 취소 Payload를 Service에 전달한다")
    void delegatePayloadToHandler() {
        PaymentCanceledOutboxPayload payload = new PaymentCanceledOutboxPayload(
                10L, 20L, 30L, 100_000, LocalDateTime.of(2026, 8, 19, 12, 0),
                1L, 2L, LocalDate.of(2026, 8, 20), LocalDate.of(2026, 8, 19));

        consumer.consume(payload, "message-1");

        verify(messageService).handle("message-1", payload);
    }

    @Test
    @DisplayName("이미 처리한 메시지는 오류 없이 무시한다")
    void ignoreDuplicatedMessage() {
        PaymentCanceledOutboxPayload payload = new PaymentCanceledOutboxPayload(
                10L, 20L, 30L, 100_000, LocalDateTime.of(2026, 8, 19, 12, 0),
                1L, 2L, LocalDate.of(2026, 8, 20), LocalDate.of(2026, 8, 19));
        doNothing()
                .doThrow(new DuplicateMessageException("REAGGREGATION", "message-1", null))
                .when(messageService).handle("message-1", payload);

        consumer.consume(payload, "message-1");
        consumer.consume(payload, "message-1");

        verify(messageService, times(2)).handle("message-1", payload);
    }
}
