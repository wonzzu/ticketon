package com.ticketing.payment.service;

import com.ticketing.event.service.SeatHoldService;
import com.ticketing.global.exception.BaseException;
import com.ticketing.payment.domain.Payment;
import com.ticketing.payment.domain.PaymentHistory;
import com.ticketing.payment.dto.PaymentCanceledEvent;
import com.ticketing.payment.dto.request.PaymentCreateDto;
import com.ticketing.payment.dto.response.PaymentResponseDto;
import com.ticketing.payment.repository.PaymentHistoryRepository;
import com.ticketing.payment.repository.PaymentRepository;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.reservation.service.ReservationConfirmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static com.ticketing.global.baseresponse.BaseResponseStatus.*;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final SeatHoldService seatHoldService;
    private final ReservationConfirmService reservationConfirmService;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PaymentResponseDto pay(Long memberId, PaymentCreateDto dto) {
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new BaseException(RESERVATION_NOT_FOUND));

        if (!reservation.isOwnedBy(memberId)) {
            throw new BaseException(RESERVATION_NOT_OWNED);
        }

        if (paymentRepository.existsByReservationId(dto.getReservationId())) {
            throw new BaseException(PAYMENT_ALREADY_COMPLETED);
        }

        Long scheduleId = reservation.getEventSchedule().getId();
        List<Long> seatIds = reservation.getReservationSeats().stream()
                .map(rs -> rs.getEventSeat().getId()).toList();

        if (!seatHoldService.isHeldByAll(scheduleId, seatIds, memberId)) {
            throw new BaseException(SEAT_HOLD_EXPIRED);
        }

        reservation.getReservationSeats().forEach(re -> re.getEventSeat().reserve());

        Payment payment = Payment.paid(reservation, reservation.getTotalPrice());
        paymentRepository.save(payment);
        paymentHistoryRepository.save(PaymentHistory.of(payment, null));

        reservationConfirmService.confirm(reservation.getId());

        registerSeatHoldRelease(scheduleId, seatIds, memberId);

        return PaymentResponseDto.from(payment);
    }

    @Transactional
    public void cancelByReservation(Long reservationId, String reason) {
        paymentRepository.findByReservationId(reservationId).ifPresent(payment -> {
            payment.cancel();
            paymentHistoryRepository.save(PaymentHistory.of(payment, reason));

            var event = payment.getReservation().getEventSchedule().getEvent();
            eventPublisher.publishEvent(new PaymentCanceledEvent(
                    event.getSeller().getId(), event.getId(), event.getEndDate(),
                    payment.getCreatedAt().toLocalDate()));

        });
    }

    private void registerSeatHoldRelease(Long scheduleId, List<Long> seatIds, Long memberId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    seatHoldService.releaseAll(scheduleId, seatIds, memberId);
                    log.info("결제 커밋 후 좌석 선점 해제: scheduleId={}, memberId={}, seatIds={}",
                            scheduleId, memberId, seatIds);
                } catch (RuntimeException e) {
                    log.error("결제 커밋 후 좌석 선점 해제 실패: scheduleId={}, memberId={}, seatIds={}",
                            scheduleId, memberId, seatIds, e);
                }
            }
        });
    }

}
