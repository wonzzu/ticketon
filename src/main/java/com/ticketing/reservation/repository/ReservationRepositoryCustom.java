package com.ticketing.reservation.repository;

import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.dto.request.ReservationSearchCond;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationRepositoryCustom {

    Page<Reservation> search(Long memberId, ReservationSearchCond cond, Pageable pageable);
}
