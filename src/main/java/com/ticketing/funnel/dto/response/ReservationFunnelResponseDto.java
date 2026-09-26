package com.ticketing.funnel.dto.response;

import com.ticketing.funnel.repository.ReservationFunnelSummaryProjection;

public record ReservationFunnelResponseDto(
        Long scheduleId,
        long enteredCount,
        long admittedCount,
        long reservedCount,
        long paidCount,
        long unpaidDropOffCount,
        double admissionRate,
        double reservationRate,
        double paymentRate,
        double unpaidDropOffRate,
        Long averageWaitMs,
        Long averagePaymentMs
) {

    public static ReservationFunnelResponseDto from(
            Long scheduleId,
            ReservationFunnelSummaryProjection summary
    ) {
        long enteredCount = value(summary.getEnteredCount());
        long admittedCount = value(summary.getAdmittedCount());
        long reservedCount = value(summary.getReservedCount());
        long paidCount = value(summary.getPaidCount());
        long eligibleReservationCount = value(summary.getEligibleReservationCount());
        long unpaidDropOffCount = value(summary.getUnpaidDropOffCount());

        return new ReservationFunnelResponseDto(
                scheduleId,
                enteredCount,
                admittedCount,
                reservedCount,
                paidCount,
                unpaidDropOffCount,
                rate(admittedCount, enteredCount),
                rate(reservedCount, admittedCount),
                rate(paidCount, reservedCount),
                rate(unpaidDropOffCount, eligibleReservationCount),
                round(summary.getAverageWaitMs()),
                round(summary.getAveragePaymentMs())
        );
    }

    private static long value(Long value) {
        return value == null ? 0 : value;
    }

    private static Long round(Double value) {
        return value == null ? null : Math.round(value);
    }

    private static double rate(long numerator, long denominator) {
        if (denominator == 0) {
            return 0.0;
        }
        return Math.round(numerator * 1000.0 / denominator) / 10.0;
    }
}
