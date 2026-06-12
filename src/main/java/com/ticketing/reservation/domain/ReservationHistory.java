package com.ticketing.reservation.domain;


import com.ticketing.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class ReservationHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reservationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    private CancelReason cancelReason;

    @Column(length = 500)
    private String reason;


    public static ReservationHistory of(Reservation reservation) {
        return ReservationHistory.builder()
                .reservationId(reservation.getId())
                .status(reservation.getStatus())
                .totalPrice(reservation.getTotalPrice())
                .build();
    }

    public static ReservationHistory ofCancel(Reservation reservation, CancelReason cancelReason, String detail) {
        return ReservationHistory.builder()
                .reservationId(reservation.getId())
                .status(reservation.getStatus())
                .totalPrice(reservation.getTotalPrice())
                .cancelReason(cancelReason)
                .reason(detail)
                .build();
            }

}
