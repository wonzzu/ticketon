package com.ticketing.outbox;

import com.ticketing.outbox.domain.OutboxEvent;
import com.ticketing.outbox.domain.OutboxEventStatus;
import com.ticketing.outbox.domain.OutboxEventType;
import com.ticketing.outbox.messaging.OutboxMessagePublisher;
import com.ticketing.outbox.repository.OutboxEventRepository;
import com.ticketing.outbox.service.OutboxRelayService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@DisplayName("Outbox Relay")
class OutboxRelayServiceTest {

    private final OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);
    private final OutboxMessagePublisher messagePublisher = mock(OutboxMessagePublisher.class);
    private final OutboxRelayService outboxRelayService =
            new OutboxRelayService(
                    outboxEventRepository, messagePublisher, 20,
                    Duration.ofSeconds(30), "relay-1");

    @Test
    @DisplayName("RabbitMQ 발행에 성공한 메시지만 PUBLISHED로 변경한다")
    void markPublishedOnlyAfterPublishSuccess() {
        OutboxEvent outboxEvent = OutboxEvent.paymentCanceled(1L, 1L, "{\"paymentId\":1}");
        when(outboxEventRepository.findByStatusAndEventTypeOrderByIdAsc(
                OutboxEventStatus.PENDING, OutboxEventType.PAYMENT_CANCELED, PageRequest.of(0, 20)))
                .thenReturn(List.of(outboxEvent));
        when(outboxEventRepository.tryClaim(any(), anyString(), any())).thenReturn(1);
        when(outboxEventRepository.markPublished(any(), anyString())).thenReturn(1);

        int publishedCount = outboxRelayService.publishPending();

        assertThat(publishedCount).isEqualTo(1);
        verify(messagePublisher).publish(outboxEvent);
        verify(outboxEventRepository).markPublished(outboxEvent.getId(), "relay-1");
    }

    @Test
    @DisplayName("RabbitMQ 발행에 실패한 메시지는 PENDING으로 유지한다")
    void keepPendingWhenPublishFails() {
        OutboxEvent outboxEvent = OutboxEvent.paymentCanceled(1L, 1L, "{\"paymentId\":1}");
        when(outboxEventRepository.findByStatusAndEventTypeOrderByIdAsc(
                OutboxEventStatus.PENDING, OutboxEventType.PAYMENT_CANCELED, PageRequest.of(0, 20)))
                .thenReturn(List.of(outboxEvent));
        when(outboxEventRepository.tryClaim(any(), anyString(), any())).thenReturn(1);
        doThrow(new IllegalStateException("RabbitMQ 연결 실패")).when(messagePublisher).publish(outboxEvent);

        assertThatThrownBy(outboxRelayService::publishPending)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("RabbitMQ 연결 실패");
        verify(outboxEventRepository).releaseClaim(outboxEvent.getId(), "relay-1");
    }

    @Test
    @DisplayName("다른 Relay가 먼저 선점한 메시지는 발행하지 않는다")
    void skipEventClaimedByAnotherRelay() {
        OutboxEvent outboxEvent = OutboxEvent.paymentCanceled(1L, 1L, "{\"paymentId\":1}");
        when(outboxEventRepository.findByStatusAndEventTypeOrderByIdAsc(
                OutboxEventStatus.PENDING, OutboxEventType.PAYMENT_CANCELED, PageRequest.of(0, 20)))
                .thenReturn(List.of(outboxEvent));
        when(outboxEventRepository.tryClaim(any(), anyString(), any())).thenReturn(0);

        int publishedCount = outboxRelayService.publishPending();

        assertThat(publishedCount).isZero();
        verify(messagePublisher, never()).publish(any());
    }
}
