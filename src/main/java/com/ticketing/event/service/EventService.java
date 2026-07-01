package com.ticketing.event.service;

import com.ticketing.event.domain.Category;
import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.EventStatus;
import com.ticketing.event.dto.request.EventCreateDto;
import com.ticketing.event.dto.request.EventUpdateDto;
import com.ticketing.event.dto.response.EventListResponseDto;
import com.ticketing.event.dto.response.EventResponseDto;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.global.baseresponse.BaseResponseStatus;
import com.ticketing.global.exception.BaseException;
import com.ticketing.member.domain.Seller;
import com.ticketing.member.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ticketing.global.baseresponse.BaseResponseStatus.EVENT_NOT_OWNED;

@Slf4j
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
        log.info("공연 등록: eventId={}, sellerId={}, title={}", event.getId(), sellerId, dto.getTitle());
    }

    @Cacheable(cacheNames = "events", key = "'all'",
            condition = "#category == null and #keyword == null")
    public List<EventListResponseDto> search(Category category,String keyword) {
        return eventRepository.search(category, keyword)
                .stream().map(EventListResponseDto::from)
                .toList();
    }

    @Cacheable(cacheNames = "event", key = "#id")
    public EventResponseDto find(Long id) {

        Event event = eventRepository.findById(id).orElseThrow
                (() -> new BaseException(BaseResponseStatus.PERFORMANCE_NOT_FOUND));
        if (event.getStatus() != EventStatus.APPROVED) {
            throw new BaseException(BaseResponseStatus.PERFORMANCE_NOT_FOUND);
        }
        return EventResponseDto.from(event);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "events", key = "'all'"),
            @CacheEvict(cacheNames = "event", key = "#id")
    })
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


    public List<EventListResponseDto> findBySeller(Long sellerId) {
        return eventRepository.findBySellerIdOrderByCreatedAtDesc(sellerId)
                .stream().map(EventListResponseDto::from)
                .toList();
    }


    public EventResponseDto findBySeller(Long eventId, Long sellerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PERFORMANCE_NOT_FOUND));
        if (!event.isOwnedBy(sellerId)) {
            throw new BaseException(EVENT_NOT_OWNED);
        }
        return EventResponseDto.from(event);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "events", key = "'all'"),
            @CacheEvict(cacheNames = "event", key = "#id")
    })
    public void delete(Long id, Long sellerId) {
        Event event = eventRepository.findById(id).orElseThrow
                (() -> new BaseException(BaseResponseStatus.PERFORMANCE_NOT_FOUND));
        if (!event.isOwnedBy(sellerId)) {
            throw new BaseException(EVENT_NOT_OWNED);
        }

        event.delete();
        log.info("공연 삭제: eventId={}, sellerId={}", id, sellerId);
    }
}
