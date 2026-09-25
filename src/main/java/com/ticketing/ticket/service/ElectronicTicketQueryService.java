package com.ticketing.ticket.service;

import com.ticketing.ticket.dto.ElectronicTicketResponseDto;
import com.ticketing.ticket.repository.ElectronicTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ElectronicTicketQueryService {

    private final ElectronicTicketRepository electronicTicketRepository;

    public List<ElectronicTicketResponseDto> findMine(Long reservationId, Long memberId) {
        return electronicTicketRepository
                .findAllByReservationSeatReservationIdAndReservationSeatReservationMemberIdOrderByIdAsc(
                        reservationId,
                        memberId
                )
                .stream()
                .map(ElectronicTicketResponseDto::from)
                .toList();
    }
}
