package com.ticketing.funnel.repository;

import com.ticketing.funnel.domain.ReservationFunnelProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationFunnelProgressRepository
        extends JpaRepository<ReservationFunnelProgress, Long> {

    Optional<ReservationFunnelProgress> findByJourneyId(String journeyId);

    Optional<ReservationFunnelProgress> findByReservationId(Long reservationId);
}
