package com.ticketing.event.service;

import com.ticketing.event.dto.request.EventUpdateDto;
import com.ticketing.event.dto.response.EventListResponseDto;
import com.ticketing.event.domain.Event;
import com.ticketing.event.dto.request.EventCreateDto;
import com.ticketing.event.dto.response.EventResponseDto;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.global.BaseResponseStatus;
import com.ticketing.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    @Transactional
    public void save(EventCreateDto dto) {

        Event event = Event.create(dto.getTitle(),
                dto.getDescription(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getRunningTime(),
                dto.getCast(),
                dto.getAgeLimit(),
                dto.getCategory());

        eventRepository.save(event);
    }


    public List<EventListResponseDto> findAll() {
        return eventRepository.findAll()
                .stream().map(EventListResponseDto::from)
                .toList();
    }

    public EventResponseDto find(Long id) {

        Event event = eventRepository.findById(id).orElseThrow
                (() -> new BaseException(BaseResponseStatus.PERFORMANCE_NOT_FOUND));
        return EventResponseDto.from(event);
    }

    @Transactional
    public void update(Long id, EventUpdateDto dto) {
        Event event = eventRepository.findById(id).orElseThrow
                (() -> new BaseException(BaseResponseStatus.PERFORMANCE_NOT_FOUND));
        event.update(dto.getTitle(),
                dto.getDescription(),
                dto.getCast(),
                dto.getRunningTime(),
                dto.getStartDate(),
                dto.getEndDate());
    }

    @Transactional
    public void delete(Long id) {
        Event event = eventRepository.findById(id).orElseThrow
                (() -> new BaseException(BaseResponseStatus.PERFORMANCE_NOT_FOUND));
        event.delete();
    }
}
