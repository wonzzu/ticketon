package com.ticketing.funnel.repository;

public interface ReservationFunnelSummaryProjection {

    Long getEnteredCount();

    Long getAdmittedCount();

    Long getReservedCount();

    Long getPaidCount();

    Long getEligibleReservationCount();

    Long getUnpaidDropOffCount();

    Double getAverageWaitMs();

    Double getAveragePaymentMs();
}
