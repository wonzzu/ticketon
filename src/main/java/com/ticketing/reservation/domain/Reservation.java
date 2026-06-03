package com.ticketing.reservation.domain;

import com.ticketing.event.domain.EventSchedule;
import com.ticketing.global.entity.BaseEntity;
import com.ticketing.global.exception.BaseException;
import com.ticketing.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

import static com.ticketing.global.BaseResponseStatus.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private EventSchedule eventSchedule;

    @Column(unique = true, nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationSeat> reservationSeats = new ArrayList<>();


    public void addReservationSeat(ReservationSeat seat) {
        this.reservationSeats.add(seat);
        seat.setReservation(this);
    }

    public static Reservation create(Member member, EventSchedule eventSchedule, String idempotencyKey, int totalPrice) {

        return Reservation.builder()
                .member(member)
                .eventSchedule(eventSchedule)
                .idempotencyKey(idempotencyKey)
                .totalPrice(totalPrice)
                .status(ReservationStatus.PENDING)
                .build();
    }

    public void confirm() {
        if (this.status != ReservationStatus.PENDING) {
            throw new BaseException(INVALID_RESERVATION_STATUS);
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status == ReservationStatus.CANCEL) {
            throw new BaseException(INVALID_RESERVATION_STATUS);
        }
        this.status = ReservationStatus.CANCEL;
    }

    public boolean isOwnedBy(Long memberId) {
        return this.member.getId().equals(memberId);
    }

}
