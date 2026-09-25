package com.ticketing.ticket.service;

import com.ticketing.outbox.domain.OutboxConsumerType;
import com.ticketing.outbox.domain.ProcessedMessage;
import com.ticketing.outbox.dto.PaymentCanceledOutboxPayload;
import com.ticketing.outbox.dto.PaymentCompletedOutboxPayload;
import com.ticketing.outbox.exception.DuplicateMessageException;
import com.ticketing.outbox.repository.ProcessedMessageRepository;
import com.ticketing.payment.domain.Payment;
import com.ticketing.payment.domain.PaymentStatus;
import com.ticketing.payment.repository.PaymentRepository;
import com.ticketing.reservation.domain.ReservationSeat;
import com.ticketing.ticket.domain.ElectronicTicket;
import com.ticketing.ticket.repository.ElectronicTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketIssuanceService {

    private final PaymentRepository paymentRepository;
    private final ProcessedMessageRepository processedMessageRepository;
    private final ElectronicTicketRepository electronicTicketRepository;

    @Transactional
    public void handleCompleted(
            String messageId,
            PaymentCompletedOutboxPayload payload
    ) {
        Payment payment = findPaymentForUpdate(
                payload.paymentId(),
                payload.reservationId()
        );

        registerProcessedMessage(messageId);

        if (payment.getStatus() != PaymentStatus.PAID) {
            return;
        }

        List<ElectronicTicket> existingTickets = electronicTicketRepository
                .findAllByReservationSeatReservationIdOrderByIdAsc(
                        payload.reservationId()
                );

        Set<Long> issuedReservationSeatIds = existingTickets.stream()
                .map(ElectronicTicket::getReservationSeat)
                .map(ReservationSeat::getId)
                .collect(Collectors.toSet());

        LocalDateTime issuedAt = LocalDateTime.now();

        List<ElectronicTicket> newTickets = payment.getReservation()
                .getReservationSeats()
                .stream()
                .filter(reservationSeat ->
                        !issuedReservationSeatIds.contains(reservationSeat.getId())
                )
                .map(reservationSeat ->
                        ElectronicTicket.issue(
                                reservationSeat,
                                UUID.randomUUID().toString(),
                                issuedAt
                        )
                )
                .toList();

        electronicTicketRepository.saveAll(newTickets);
    }

    @Transactional
    public void handleCanceled(
            String messageId,
            PaymentCanceledOutboxPayload payload
    ) {
        Payment payment = findPaymentForUpdate(
                payload.paymentId(),
                payload.reservationId()
        );

        registerProcessedMessage(messageId);

        if (payment.getStatus() != PaymentStatus.CANCELED) {
            return;
        }

        electronicTicketRepository
                .findAllByReservationSeatReservationIdOrderByIdAsc(
                        payload.reservationId()
                )
                .forEach(ticket -> ticket.cancel(payload.canceledAt()));
    }

    private Payment findPaymentForUpdate(
            Long paymentId,
            Long reservationId
    ) {
        Payment payment = paymentRepository
                .findByReservationIdForUpdate(reservationId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "전자 티켓 처리 대상 결제를 찾을 수 없습니다. reservationId="
                                        + reservationId
                        )
                );

        if (!payment.getId().equals(paymentId)) {
            throw new IllegalStateException(
                    "전자 티켓 이벤트의 결제 정보가 일치하지 않습니다. paymentId="
                            + paymentId
            );
        }

        return payment;
    }

    private void registerProcessedMessage(String messageId) {
        try {
            processedMessageRepository.saveAndFlush(
                    ProcessedMessage.of(
                            OutboxConsumerType.TICKET_ISSUANCE,
                            messageId
                    )
            );
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateMessageException(
                    OutboxConsumerType.TICKET_ISSUANCE.name(),
                    messageId,
                    e
            );
        }
    }
}
