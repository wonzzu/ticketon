package com.ticketing.ticket.dto;

import com.ticketing.ticket.domain.ElectronicTicket;
import com.ticketing.ticket.domain.ElectronicTicketStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ElectronicTicketResponseDto {

    private Long ticketId;
    private Long reservationSeatId;
    private String ticketToken;
    private ElectronicTicketStatus status;
    private LocalDateTime issuedAt;
    private LocalDateTime canceledAt;

    public static ElectronicTicketResponseDto from(ElectronicTicket ticket) {
        return ElectronicTicketResponseDto.builder()
                .ticketId(ticket.getId())
                .reservationSeatId(ticket.getReservationSeat().getId())
                .ticketToken(ticket.getTicketToken())
                .status(ticket.getStatus())
                .issuedAt(ticket.getIssuedAt())
                .canceledAt(ticket.getCanceledAt())
                .build();
    }
}
