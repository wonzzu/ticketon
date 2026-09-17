package com.ticketing.support.reservation;

import com.ticketing.event.domain.EventSchedule;
import com.ticketing.global.exception.BaseException;
import com.ticketing.member.domain.NormalMember;
import com.ticketing.payment.domain.Payment;
import com.ticketing.payment.domain.PaymentStatus;
import com.ticketing.payment.repository.PaymentRepository;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationStatus;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.support.reservation.dto.RefundSnapshotResponseDto;
import com.ticketing.support.reservation.service.RefundSnapshotService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.ticketing.global.baseresponse.BaseResponseStatus.RESERVATION_NOT_OWNED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AI 고객지원 환불 스냅샷")
class RefundSnapshotServiceTest {

    private final ReservationRepository reservationRepository = mock(ReservationRepository.class);
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final RefundSnapshotService refundSnapshotService =
            new RefundSnapshotService(reservationRepository, paymentRepository);

    @Test
    @DisplayName("본인 예매의 환불 계산에 필요한 실제 결제 정보를 반환한다")
    void returnRefundSnapshotForOwner() {
        long memberId = 1L;
        long reservationId = 10L;
        LocalDateTime reservedAt = LocalDateTime.of(2026, 9, 1, 10, 0);
        LocalDateTime performanceAt = LocalDateTime.of(2026, 9, 4, 20, 0);

        NormalMember member = NormalMember.builder().id(memberId).build();
        EventSchedule schedule = EventSchedule.builder()
                .id(20L)
                .showDateTime(performanceAt)
                .build();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .member(member)
                .eventSchedule(schedule)
                .idempotencyKey("reservation-key")
                .totalPrice(55_000)
                .status(ReservationStatus.CONFIRMED)
                .createdAt(reservedAt)
                .build();
        Payment payment = Payment.builder()
                .id(30L)
                .reservation(reservation)
                .amount(50_000)
                .status(PaymentStatus.PAID)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(reservationId)).thenReturn(Optional.of(payment));

        RefundSnapshotResponseDto result = refundSnapshotService.getSnapshot(reservationId, memberId);

        assertThat(result.getReservationId()).isEqualTo(reservationId);
        assertThat(result.getReservationStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(result.getReservedAt()).isEqualTo(reservedAt);
        assertThat(result.getPerformanceAt()).isEqualTo(performanceAt);
        assertThat(result.getPaidAmount()).isEqualTo(50_000);
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("타인 예매이면 결제 정보를 조회하기 전에 차단한다")
    void rejectSnapshotForOtherMember() {
        long ownerId = 1L;
        long requesterId = 2L;
        long reservationId = 10L;

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .member(NormalMember.builder().id(ownerId).build())
                .build();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> refundSnapshotService.getSnapshot(reservationId, requesterId))
                .isInstanceOfSatisfying(BaseException.class,
                        exception -> assertThat(exception.getBaseResponseStatus())
                                .isEqualTo(RESERVATION_NOT_OWNED));
        verify(paymentRepository, never()).findByReservationId(reservationId);
    }
}
