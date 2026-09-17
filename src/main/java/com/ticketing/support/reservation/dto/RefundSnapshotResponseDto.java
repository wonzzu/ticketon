package com.ticketing.support.reservation.dto;

import com.ticketing.payment.domain.Payment;
import com.ticketing.payment.domain.PaymentStatus;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RefundSnapshotResponseDto {

    private Long reservationId;
    private ReservationStatus reservationStatus;
    private LocalDateTime reservedAt;
    private LocalDateTime performanceAt;
    private Integer paidAmount;
    private PaymentStatus paymentStatus;

    public static RefundSnapshotResponseDto of(Reservation reservation, Payment payment) {
        return RefundSnapshotResponseDto.builder()
                .reservationId(reservation.getId())
                .reservationStatus(reservation.getStatus())
                .reservedAt(reservation.getCreatedAt())
                .performanceAt(reservation.getEventSchedule().getShowDateTime())
                .paidAmount(payment.getAmount())
                .paymentStatus(payment.getStatus())
                .build();
    }
}
