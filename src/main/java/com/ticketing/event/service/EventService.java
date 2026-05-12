package com.ticketing.event.service;

import com.ticketing.event.domain.EventStatus;
import com.ticketing.event.dto.request.EventUpdateDto;
import com.ticketing.event.dto.response.EventListResponseDto;
import com.ticketing.event.domain.Event;
import com.ticketing.event.dto.request.EventCreateDto;
import com.ticketing.event.dto.response.EventResponseDto;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.global.BaseResponseStatus;
import com.ticketing.global.exception.BaseException;
import com.ticketing.member.domain.Seller;
import com.ticketing.member.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ticketing.global.BaseResponseStatus.EVENT_NOT_OWNED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final SellerRepository sellerRepository;

    @Transactional
    public void save(EventCreateDto dto, Long sellerId) {

        Seller seller = sellerRepository.findById(sellerId).orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        Event event = Event.create(dto.getTitle(),
                dto.getDescription(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getRunningTime(),
                dto.getCast(),
                dto.getAgeLimit(),
                dto.getCategory(),
                dto.getPosterUrl(),
                seller);

        eventRepository.save(event);
    }


    public List<EventListResponseDto> findAll() {
        return eventRepository.findByEventStatus(EventStatus.APPROVED)
                .stream().map(EventListResponseDto::from)
                .toList();
    }

    public EventResponseDto find(Long id) {

        Event event = eventRepository.findById(id).orElseThrow
                (() -> new BaseException(BaseResponseStatus.PERFORMANCE_NOT_FOUND));
        return EventResponseDto.from(event);
    }

    @Transactional
    public void update(Long id, EventUpdateDto dto, Long sellerId) {
        Event event = eventRepository.findById(id).orElseThrow
                (() -> new BaseException(BaseResponseStatus.PERFORMANCE_NOT_FOUND));
        if (!event.isOwnedBy(sellerId)) {
            throw new BaseException(EVENT_NOT_OWNED);
        }

        event.update(dto.getTitle(),
                dto.getDescription(),
                dto.getCast(),
                dto.getRunningTime(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getPosterUrl());
    }

    @Transactional
    public void delete(Long id,Long sellerId) {
        Event event = eventRepository.findById(id).orElseThrow
                (() -> new BaseException(BaseResponseStatus.PERFORMANCE_NOT_FOUND));
        if (!event.isOwnedBy(sellerId)) {
            throw new BaseException(EVENT_NOT_OWNED);
        }

        event.delete();
    }
}
