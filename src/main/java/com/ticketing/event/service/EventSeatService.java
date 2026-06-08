package com.ticketing.event.service;


import com.ticketing.event.domain.EventSeat;
import com.ticketing.event.domain.EventSeatStatus;
import com.ticketing.event.dto.response.EventSeatResponseDto;
import com.ticketing.event.repository.EventSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventSeatService {

    private final EventSeatRepository eventSeatRepository;
    private final SeatHoldService seatHoldService;

    public List<EventSeatResponseDto> findByScheduleId(Long scheduleId) {
        List<EventSeat> seats = eventSeatRepository.findByEventScheduleId(scheduleId);
        List<Long> seatIds = seats.stream().map(EventSeat::getId).toList();
        Set<Long> heldIds = seatHoldService.findHeldSeatIds(scheduleId, seatIds);

        return seats.stream()
                .map(seat -> {
                    EventSeatStatus status = seat.getStatus();
                    if (status == EventSeatStatus.AVAILABLE && heldIds.contains(seat.getId())) {
                        status = EventSeatStatus.HELD;
                    }
                    return EventSeatResponseDto.from(seat, status);
                }).toList();
    }
}
