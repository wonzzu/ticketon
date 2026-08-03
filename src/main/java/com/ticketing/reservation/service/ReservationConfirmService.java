package com.ticketing.reservation.service;

import com.ticketing.global.baseresponse.BaseResponseStatus;
import com.ticketing.global.exception.BaseException;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationHistory;
import com.ticketing.reservation.repository.ReservationHistoryRepository;
import com.ticketing.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ticketing.global.baseresponse.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class ReservationConfirmService {

    private final ReservationRepository reservationRepository;
    private final ReservationHistoryRepository reservationHistoryRepository;

    @Transactional
    public void confirm(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BaseException(RESERVATION_NOT_FOUND));

        reservation.confirm();
        reservationHistoryRepository.save(ReservationHistory.of(reservation));
    }
}
