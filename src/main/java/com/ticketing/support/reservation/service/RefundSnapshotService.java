package com.ticketing.support.reservation.service;

import com.ticketing.global.exception.BaseException;
import com.ticketing.payment.domain.Payment;
import com.ticketing.payment.repository.PaymentRepository;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.support.reservation.dto.RefundSnapshotResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ticketing.global.baseresponse.BaseResponseStatus.PAYMENT_NOT_FOUND;
import static com.ticketing.global.baseresponse.BaseResponseStatus.RESERVATION_NOT_FOUND;
import static com.ticketing.global.baseresponse.BaseResponseStatus.RESERVATION_NOT_OWNED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefundSnapshotService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    public RefundSnapshotResponseDto getSnapshot(Long reservationId, Long memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BaseException(RESERVATION_NOT_FOUND));

        if (!reservation.isOwnedBy(memberId)) {
            throw new BaseException(RESERVATION_NOT_OWNED);
        }

        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new BaseException(PAYMENT_NOT_FOUND));

        return RefundSnapshotResponseDto.of(reservation, payment);
    }
}
