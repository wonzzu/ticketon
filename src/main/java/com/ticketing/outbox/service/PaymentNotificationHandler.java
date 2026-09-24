package com.ticketing.outbox.service;

import com.ticketing.notification.domain.Notification;
import com.ticketing.notification.domain.NotificationReferenceType;
import com.ticketing.notification.domain.NotificationType;
import com.ticketing.notification.event.NotificationCreatedEvent;
import com.ticketing.notification.repository.NotificationRepository;
import com.ticketing.outbox.domain.OutboxConsumerType;
import com.ticketing.outbox.domain.ProcessedMessage;
import com.ticketing.outbox.dto.PaymentCanceledOutboxPayload;
import com.ticketing.outbox.dto.PaymentCompletedOutboxPayload;
import com.ticketing.outbox.exception.DuplicateMessageException;
import com.ticketing.outbox.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentNotificationHandler {

    private final ProcessedMessageRepository processedMessageRepository;
    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void handleCompleted(
            String messageId,
            PaymentCompletedOutboxPayload payload
    ) {
        registerProcessedMessage(messageId);

        Notification notification = Notification.create(
                payload.memberId(),
                NotificationType.PAYMENT_COMPLETED,
                "결제가 완료되었습니다.",
                "예매 결제가 완료되었습니다. 결제 금액은 %,d원입니다."
                        .formatted(payload.paidAmount()),
                NotificationReferenceType.RESERVATION,
                payload.reservationId()
        );

        Notification savedNotification = notificationRepository.save(notification);

        eventPublisher.publishEvent(
                NotificationCreatedEvent.from(savedNotification)
        );
    }

    @Transactional
    public void handleCanceled(
            String messageId,
            PaymentCanceledOutboxPayload payload
    ) {
        registerProcessedMessage(messageId);

        Notification notification = Notification.create(
                payload.memberId(),
                NotificationType.PAYMENT_CANCELED,
                "결제가 취소되었습니다.",
                "결제 취소가 완료되었습니다. 취소 금액은 %,d원입니다."
                        .formatted(payload.canceledAmount()),
                NotificationReferenceType.RESERVATION,
                payload.reservationId()
        );

        Notification savedNotification = notificationRepository.save(notification);

        eventPublisher.publishEvent(
                NotificationCreatedEvent.from(savedNotification)
        );
    }

    private void registerProcessedMessage(String messageId) {
        try {
            processedMessageRepository.saveAndFlush(
                    ProcessedMessage.of(
                            OutboxConsumerType.NOTIFICATION,
                            messageId
                    )
            );
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateMessageException(
                    OutboxConsumerType.NOTIFICATION.name(),
                    messageId,
                    e
            );
        }
    }
}
