package com.ticketing.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.notification.domain.Notification;
import com.ticketing.notification.domain.NotificationType;
import com.ticketing.notification.repository.NotificationRepository;
import com.ticketing.outbox.domain.OutboxConsumerType;
import com.ticketing.outbox.dto.EventEnvelope;
import com.ticketing.outbox.dto.PaymentCanceledOutboxPayload;
import com.ticketing.outbox.dto.PaymentCompletedOutboxPayload;
import com.ticketing.outbox.messaging.KafkaPaymentNotificationConsumer;
import com.ticketing.outbox.repository.ProcessedMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
@ActiveProfiles("test")
@Sql(
        scripts = "/truncate.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class PaymentNotificationConsumerIntegrationTest {

    @Autowired
    private KafkaPaymentNotificationConsumer consumer;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ProcessedMessageRepository processedMessageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("결제 완료와 취소 이벤트를 회원 알림으로 저장한다")
    void savePaymentNotifications() throws Exception {
        PaymentCompletedOutboxPayload completedPayload =
                new PaymentCompletedOutboxPayload(
                        10L,
                        100L,
                        1L,
                        2L,
                        3L,
                        4L,
                        50_000,
                        LocalDateTime.of(2026, 9, 24, 12, 0)
                );

        PaymentCanceledOutboxPayload canceledPayload =
                new PaymentCanceledOutboxPayload(
                        10L,
                        100L,
                        1L,
                        50_000,
                        LocalDateTime.of(2026, 9, 24, 13, 0),
                        2L,
                        3L,
                        LocalDate.of(2026, 9, 30),
                        LocalDate.of(2026, 9, 24)
                );

        consumer.consume(
                createMessage(
                        "payment-completed-message",
                        "PAYMENT_COMPLETED",
                        1L,
                        completedPayload
                ),
                "PAYMENT:10"
        );

        consumer.consume(
                createMessage(
                        "payment-canceled-message",
                        "PAYMENT_CANCELED",
                        2L,
                        canceledPayload
                ),
                "PAYMENT:10"
        );

        List<Notification> notifications = notificationRepository.findAll();

        assertThat(notifications)
                .extracting(
                        Notification::getType,
                        Notification::getMemberId,
                        Notification::getReferenceId
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                NotificationType.PAYMENT_COMPLETED,
                                1L,
                                100L
                        ),
                        tuple(
                                NotificationType.PAYMENT_CANCELED,
                                1L,
                                100L
                        )
                );

        assertThat(processedMessageRepository.findAll())
                .hasSize(2)
                .allSatisfy(processed ->
                        assertThat(processed.getConsumerType())
                                .isEqualTo(OutboxConsumerType.NOTIFICATION)
                );
    }

    @Test
    @DisplayName("동일한 결제 취소 메시지가 재전달되어도 알림은 한 번만 저장한다")
    void ignoreDuplicatedPaymentNotification() throws Exception {
        PaymentCanceledOutboxPayload payload =
                new PaymentCanceledOutboxPayload(
                        10L,
                        100L,
                        1L,
                        50_000,
                        LocalDateTime.of(2026, 9, 24, 13, 0),
                        2L,
                        3L,
                        LocalDate.of(2026, 9, 30),
                        LocalDate.of(2026, 9, 24)
                );

        String message = createMessage(
                "duplicated-message",
                "PAYMENT_CANCELED",
                2L,
                payload
        );

        consumer.consume(message, "PAYMENT:10");
        consumer.consume(message, "PAYMENT:10");

        assertThat(notificationRepository.count()).isEqualTo(1);
        assertThat(processedMessageRepository.count()).isEqualTo(1);
    }

    private String createMessage(
            String eventId,
            String eventType,
            long eventSequence,
            Object payload
    ) throws Exception {
        EventEnvelope envelope = new EventEnvelope(
                eventId,
                eventType,
                1,
                "PAYMENT",
                10L,
                eventSequence,
                LocalDateTime.of(2026, 9, 24, 13, 0),
                objectMapper.valueToTree(payload)
        );

        return objectMapper.writeValueAsString(envelope);
    }
}
