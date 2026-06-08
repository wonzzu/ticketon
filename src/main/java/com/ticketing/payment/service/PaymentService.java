package com.ticketing.payment.service;

import com.ticketing.event.service.SeatHoldService;
import com.ticketing.global.exception.BaseException;
import com.ticketing.payment.domain.Payment;
import com.ticketing.payment.dto.request.PaymentCreateDto;
import com.ticketing.payment.dto.response.PaymentResponseDto;
import com.ticketing.payment.repository.PaymentRepository;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ticketing.global.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final SeatHoldService seatHoldService;

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

        reservation.confirm();

        seatHoldService.releaseAll(scheduleId, seatIds);

        return PaymentResponseDto.from(payment);
    }

}
