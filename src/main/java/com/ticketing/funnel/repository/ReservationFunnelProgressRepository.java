package com.ticketing.funnel.repository;

import com.ticketing.funnel.domain.ReservationFunnelProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ReservationFunnelProgressRepository
        extends JpaRepository<ReservationFunnelProgress, Long> {

    Optional<ReservationFunnelProgress> findByJourneyId(String journeyId);

    Optional<ReservationFunnelProgress> findByReservationId(Long reservationId);

    @Query("""
            select
                coalesce(sum(case when f.enteredAt is not null then 1 else 0 end), 0) as enteredCount,
                coalesce(sum(case when f.admittedAt is not null then 1 else 0 end), 0) as admittedCount,
                coalesce(sum(case when f.reservedAt is not null then 1 else 0 end), 0) as reservedCount,
                coalesce(sum(case when f.paidAt is not null then 1 else 0 end), 0) as paidCount,
                coalesce(sum(case
                    when f.reservedAt is not null
                     and (f.paidAt is not null or f.reservedAt <= :dropOffCutoff)
                    then 1 else 0 end), 0) as eligibleReservationCount,
                coalesce(sum(case
                    when f.reservedAt <= :dropOffCutoff and f.paidAt is null
                    then 1 else 0 end), 0) as unpaidDropOffCount,
                avg(f.waitDurationMs) as averageWaitMs,
                avg(f.paymentDurationMs) as averagePaymentMs
            from ReservationFunnelProgress f
            where f.scheduleId = :scheduleId
            """)
    ReservationFunnelSummaryProjection summarize(
            @Param("scheduleId") Long scheduleId,
            @Param("dropOffCutoff") LocalDateTime dropOffCutoff
    );
}
