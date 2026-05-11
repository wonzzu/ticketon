package com.ticketing.event.domain;

import com.ticketing.global.entity.BaseEntity;
import com.ticketing.venue.domain.Seat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class EventSeat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private EventSchedule eventSchedule;


    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventSeatStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    void setSchedule(EventSchedule schedule) {
        this.eventSchedule = schedule;
    }

    public void reserve() {
        if (this.status != EventSeatStatus.AVAILABLE) {
            throw new IllegalStateException("예매할 수 없는 좌석입니다.");
        }
        this.status = EventSeatStatus.RESERVED;
    }

    public void cancel() {
        this.status = EventSeatStatus.AVAILABLE;
    }

    public static EventSeat create(EventSchedule eventSchedule, Seat seat, int price) {
        return EventSeat.builder()
                .eventSchedule(eventSchedule)
                .seat(seat)
                .price(price)
                .status(EventSeatStatus.AVAILABLE)
                .build();
    }
}
