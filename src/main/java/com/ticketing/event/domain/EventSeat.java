package com.ticketing.event.domain;

import com.ticketing.global.entity.BaseEntity;
import com.ticketing.global.exception.BaseException;
import com.ticketing.venue.domain.Seat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static com.ticketing.global.baseresponse.BaseResponseStatus.SEAT_ALREADY_RESERVED;

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

    // Redis 선점이 1차 방어선, 이 낙관적 락이 최후 방어선.
    // 선점 TTL이 만료되는 순간 두 트랜잭션이 같이 AVAILABLE을 읽으면 reserve()의 상태 검사를 둘 다 통과하는데,
    // 커밋 시점에 버전이 어긋나 한쪽만 성공한다.
    @Version
    private Long version;

    void setSchedule(EventSchedule schedule) {
        this.eventSchedule = schedule;
    }

    public void reserve() {
        if (this.status != EventSeatStatus.AVAILABLE) {
            throw new BaseException(SEAT_ALREADY_RESERVED);
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
