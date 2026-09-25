package com.ticketing.ticket;

import com.ticketing.outbox.dto.PaymentCanceledOutboxPayload;
import com.ticketing.outbox.dto.PaymentCompletedOutboxPayload;
import com.ticketing.outbox.repository.ProcessedMessageRepository;
import com.ticketing.payment.domain.Payment;
import com.ticketing.payment.domain.PaymentStatus;
import com.ticketing.payment.repository.PaymentRepository;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationSeat;
import com.ticketing.ticket.domain.ElectronicTicket;
import com.ticketing.ticket.domain.ElectronicTicketStatus;
import com.ticketing.ticket.repository.ElectronicTicketRepository;
import com.ticketing.ticket.service.TicketIssuanceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TicketIssuanceServiceTest {

    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final ProcessedMessageRepository processedMessageRepository = mock(ProcessedMessageRepository.class);
    private final ElectronicTicketRepository electronicTicketRepository = mock(ElectronicTicketRepository.class);
    private final TicketIssuanceService service = new TicketIssuanceService(
            paymentRepository,
            processedMessageRepository,
            electronicTicketRepository
    );

    @Test
    @DisplayName("결제 완료 이벤트를 받으면 예매 좌석마다 전자 티켓을 발급한다")
    void issueTicketForEachReservationSeat() {
        ReservationSeat seat1 = ReservationSeat.builder().id(101L).build();
        ReservationSeat seat2 = ReservationSeat.builder().id(102L).build();
        Reservation reservation = Reservation.builder()
                .id(20L)
                .reservationSeats(List.of(seat1, seat2))
                .build();
        Payment payment = Payment.builder()
                .id(10L)
                .reservation(reservation)
                .status(PaymentStatus.PAID)
                .build();

        when(paymentRepository.findByReservationIdForUpdate(20L))
                .thenReturn(Optional.of(payment));
        when(electronicTicketRepository.findAllByReservationSeatReservationIdOrderByIdAsc(20L))
                .thenReturn(List.of());

        service.handleCompleted("message-1", completedPayload());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ElectronicTicket>> captor = ArgumentCaptor.forClass(List.class);
        verify(electronicTicketRepository).saveAll(captor.capture());

        assertThat(captor.getValue())
                .hasSize(2)
                .allSatisfy(ticket -> {
                    assertThat(ticket.getStatus()).isEqualTo(ElectronicTicketStatus.ISSUED);
                    assertThat(ticket.getTicketToken()).isNotBlank();
                });
    }

    @Test
    @DisplayName("결제가 이미 취소됐다면 늦게 도착한 완료 이벤트로 티켓을 발급하지 않는다")
    void doNotIssueTicketForCanceledPayment() {
        Payment payment = Payment.builder()
                .id(10L)
                .reservation(Reservation.builder().id(20L).build())
                .status(PaymentStatus.CANCELED)
                .build();

        when(paymentRepository.findByReservationIdForUpdate(20L))
                .thenReturn(Optional.of(payment));

        service.handleCompleted("message-1", completedPayload());

        verify(electronicTicketRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("결제 취소 이벤트를 받으면 발급된 전자 티켓을 취소한다")
    void cancelIssuedTickets() {
        Payment payment = Payment.builder()
                .id(10L)
                .reservation(Reservation.builder().id(20L).build())
                .status(PaymentStatus.CANCELED)
                .build();
        ElectronicTicket ticket = ElectronicTicket.issue(
                ReservationSeat.builder().id(101L).build(),
                "ticket-token",
                LocalDateTime.of(2026, 9, 25, 10, 0)
        );

        when(paymentRepository.findByReservationIdForUpdate(20L))
                .thenReturn(Optional.of(payment));
        when(electronicTicketRepository.findAllByReservationSeatReservationIdOrderByIdAsc(20L))
                .thenReturn(List.of(ticket));

        service.handleCanceled("message-2", canceledPayload());

        assertThat(ticket.getStatus()).isEqualTo(ElectronicTicketStatus.CANCELED);
        assertThat(ticket.getCanceledAt())
                .isEqualTo(LocalDateTime.of(2026, 9, 25, 11, 0));
    }

    private PaymentCompletedOutboxPayload completedPayload() {
        return new PaymentCompletedOutboxPayload(
                10L, 20L, 30L, 40L, 50L, 60L, 100_000,
                LocalDateTime.of(2026, 9, 25, 10, 0)
        );
    }

    private PaymentCanceledOutboxPayload canceledPayload() {
        return new PaymentCanceledOutboxPayload(
                10L, 20L, 30L, 100_000,
                LocalDateTime.of(2026, 9, 25, 11, 0),
                40L, 50L,
                LocalDate.of(2026, 9, 30),
                LocalDate.of(2026, 9, 25)
        );
    }
}
