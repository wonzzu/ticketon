package com.ticketing.ticket.domain;

import com.ticketing.global.entity.BaseEntity;
import com.ticketing.reservation.domain.ReservationSeat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "electronic_ticket",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_electronic_ticket_reservation_seat",
                        columnNames = "reservation_seat_id"
                ),
                @UniqueConstraint(
                        name = "uk_electronic_ticket_token",
                        columnNames = "ticket_token"
                )
        }
)
public class ElectronicTicket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "reservation_seat_id", nullable = false)
    private ReservationSeat reservationSeat;

    @Column(name = "ticket_token", nullable = false, length = 100)
    private String ticketToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ElectronicTicketStatus status;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    public static ElectronicTicket issue(
            ReservationSeat reservationSeat,
            String ticketToken,
            LocalDateTime issuedAt
    ) {
        return ElectronicTicket.builder()
                .reservationSeat(Objects.requireNonNull(reservationSeat))
                .ticketToken(Objects.requireNonNull(ticketToken))
                .status(ElectronicTicketStatus.ISSUED)
                .issuedAt(Objects.requireNonNull(issuedAt))
                .build();
    }

    public void cancel(LocalDateTime canceledAt) {
        if (status == ElectronicTicketStatus.CANCELED) {
            return;
        }

        this.status = ElectronicTicketStatus.CANCELED;
        this.canceledAt = Objects.requireNonNull(canceledAt);
    }

    public boolean isUsable() {
        return status == ElectronicTicketStatus.ISSUED;
    }
}
