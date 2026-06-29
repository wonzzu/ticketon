package com.ticketing.reservation.domain;

import com.ticketing.event.domain.EventSeat;
import com.ticketing.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class ReservationSeat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    //OneToOne 이면 예매 취소 시 다른 사람의 에매가 막히기 때문에 다대일
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_seat_id", nullable = false)
    private EventSeat eventSeat;

    @Column(nullable = false)
    private Integer price;

    void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public static ReservationSeat create(EventSeat eventSeat, int price) {
        return ReservationSeat.builder()
                .eventSeat(eventSeat)
                .price(price)
                .build();
    }
}
