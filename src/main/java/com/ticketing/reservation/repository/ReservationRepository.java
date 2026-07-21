package com.ticketing.reservation.repository;

import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long>,ReservationRepositoryCustom {

    Optional<Reservation> findByIdempotencyKey(String idempotencyKey);

    boolean existsByMemberIdAndEventScheduleEventIdAndStatus(Long memberId, Long eventId, ReservationStatus status);


}
