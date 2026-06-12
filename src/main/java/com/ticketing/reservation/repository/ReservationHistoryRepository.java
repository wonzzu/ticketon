package com.ticketing.reservation.repository;

import com.ticketing.reservation.domain.ReservationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationHistoryRepository extends JpaRepository<ReservationHistory, Long> {

    List<ReservationHistory> findByReservationIdOrderByCreatedAtAsc(Long reservationId);
}
