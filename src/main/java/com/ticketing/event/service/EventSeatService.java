package com.ticketing.event.service;


import com.ticketing.event.dto.response.EventSeatResponseDto;
import com.ticketing.event.repository.EventSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventSeatService {

    private final EventSeatRepository eventSeatRepository;

    public List<EventSeatResponseDto> findByScheduleId(Long scheduleId) {
        return eventSeatRepository.findByEventScheduleId(scheduleId)
                .stream()
                .map(EventSeatResponseDto::from)
                .toList();
    }
}
