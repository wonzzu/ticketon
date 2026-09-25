package com.ticketing.ticket.repository;

import com.ticketing.ticket.domain.ElectronicTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ElectronicTicketRepository extends JpaRepository<ElectronicTicket, Long> {

    Optional<ElectronicTicket> findByReservationSeatId(Long reservationSeatId);

    List<ElectronicTicket> findAllByReservationSeatReservationIdOrderByIdAsc(Long reservationId);

    List<ElectronicTicket> findAllByReservationSeatReservationIdAndReservationSeatReservationMemberIdOrderByIdAsc(
            Long reservationId,
            Long memberId
    );
}
