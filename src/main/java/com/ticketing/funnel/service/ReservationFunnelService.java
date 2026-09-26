package com.ticketing.funnel.service;

import com.ticketing.funnel.domain.ReservationFunnelProgress;
import com.ticketing.funnel.repository.ReservationFunnelProgressRepository;
import com.ticketing.outbox.domain.OutboxConsumerType;
import com.ticketing.outbox.domain.ProcessedMessage;
import com.ticketing.outbox.dto.PaymentCompletedOutboxPayload;
import com.ticketing.outbox.dto.ReservationCreatedOutboxPayload;
import com.ticketing.outbox.exception.DuplicateMessageException;
import com.ticketing.outbox.repository.ProcessedMessageRepository;
import com.ticketing.queue.analytics.dto.QueueAnalyticsEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationFunnelService {

    private final ReservationFunnelProgressRepository funnelRepository;
    private final ProcessedMessageRepository processedMessageRepository;

    @Transactional
    public void handleQueue(String messageId, String eventType, QueueAnalyticsEvent event) {
        registerProcessedMessage(messageId);

        ReservationFunnelProgress progress = funnelRepository.findByJourneyId(event.journeyId())
                .orElseGet(() -> ReservationFunnelProgress.start(
                        event.journeyId(), event.memberId(), event.scheduleId()
                ));

        switch (eventType) {
            case "QUEUE_ENTERED" -> progress.recordEntered(event.enteredAt());
            case "QUEUE_ADMITTED" -> progress.recordAdmitted(event.enteredAt(), event.admittedAt());
            default -> throw new IllegalArgumentException("지원하지 않는 대기열 퍼널 이벤트입니다: " + eventType);
        }

        funnelRepository.save(progress);
    }

    @Transactional
    public void handleReservation(String messageId, ReservationCreatedOutboxPayload event) {
        registerProcessedMessage(messageId);

        Optional<ReservationFunnelProgress> journeyProgress =
                funnelRepository.findByJourneyId(event.journeyId());
        Optional<ReservationFunnelProgress> reservationProgress =
                funnelRepository.findByReservationId(event.reservationId());

        ReservationFunnelProgress progress;
        if (journeyProgress.isPresent()
                && reservationProgress.isPresent()
                && !journeyProgress.get().getId().equals(reservationProgress.get().getId())) {
            progress = journeyProgress.get();
            progress.mergePaymentFrom(reservationProgress.get());
            funnelRepository.delete(reservationProgress.get());
            funnelRepository.flush();
        } else if (journeyProgress.isPresent()) {
            progress = journeyProgress.get();
        } else if (reservationProgress.isPresent()) {
            progress = reservationProgress.get();
        } else {
            progress = ReservationFunnelProgress.start(
                    event.journeyId(), event.memberId(), event.scheduleId()
            );
        }

        progress.recordReservation(
                event.journeyId(),
                event.reservationId(),
                event.memberId(),
                event.scheduleId(),
                event.reservedAt()
        );
        funnelRepository.save(progress);
    }

    @Transactional
    public void handlePayment(String messageId, PaymentCompletedOutboxPayload event) {
        registerProcessedMessage(messageId);

        ReservationFunnelProgress progress = funnelRepository.findByReservationId(event.reservationId())
                .orElseGet(() -> ReservationFunnelProgress.paymentFirst(
                        event.reservationId(), event.memberId(), event.scheduleId(), event.paidAt()
                ));

        progress.recordPaid(event.memberId(), event.scheduleId(), event.paidAt());
        funnelRepository.save(progress);
    }

    private void registerProcessedMessage(String messageId) {
        try {
            processedMessageRepository.saveAndFlush(
                    ProcessedMessage.of(OutboxConsumerType.FUNNEL_ANALYTICS, messageId)
            );
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateMessageException(
                    OutboxConsumerType.FUNNEL_ANALYTICS.name(), messageId, e
            );
        }
    }
}
