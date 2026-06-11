package com.ticketing.event.service;

import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.EventSchedule;
import com.ticketing.event.domain.EventSeat;
import com.ticketing.event.domain.GradePrice;
import com.ticketing.event.dto.request.EventScheduleCreateDto;
import com.ticketing.event.dto.response.EventScheduleResponseDto;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.event.repository.EventScheduleRepository;
import com.ticketing.event.repository.EventSeatRepository;
import com.ticketing.global.exception.BaseException;
import com.ticketing.venue.domain.Seat;
import com.ticketing.venue.domain.SeatGrade;
import com.ticketing.venue.domain.Venue;
import com.ticketing.venue.repository.SeatRepository;
import com.ticketing.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ticketing.global.baseresponse.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventScheduleService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final EventScheduleRepository eventScheduleRepository;
    private final EventSeatRepository eventSeatRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public void create(Long eventId, EventScheduleCreateDto dto, Long sellerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BaseException(PERFORMANCE_NOT_FOUND));
        if (!event.isOwnedBy(sellerId)) {
            throw new BaseException(EVENT_NOT_OWNED);
        }
        Venue venue = venueRepository.findById(dto.getVenueId())
                .orElseThrow(() -> new BaseException(VENUE_NOT_FOUND));

        int nextRound = eventScheduleRepository.countByEventIdAndVenueId(eventId, venue.getId()) + 1;

        EventSchedule schedule = EventSchedule.create(venue, dto.getShowDateTime(), nextRound);
        event.addSchedule(schedule);
        eventScheduleRepository.save(schedule);

        List<Seat> seats = seatRepository.findByVenueId(venue.getId());
        Map<SeatGrade, Integer> priceMap = dto.getGradePrices().stream()
                .collect(Collectors.toMap(GradePrice::seatGrade, GradePrice::price));

        validateAllGradesPriced(seats, priceMap);

        List<EventSeat> eventSeats = seats.stream()
                .map(seat -> EventSeat.create(schedule, seat, priceMap.get(seat.getSeatGrade())))
                .toList();
        eventSeats.forEach(schedule::addEventSeat);

        eventSeatRepository.saveAll(eventSeats);
    }

    public List<EventScheduleResponseDto> findByEvent(Long eventId) {
        return eventScheduleRepository.findByEventIdOrderByShowDateTimeAsc(eventId)
                .stream()
                .map(EventScheduleResponseDto::from)
                .toList();
    }


    private void validateAllGradesPriced(List<Seat> seats, Map<SeatGrade, Integer> priceMap) {
        Set<SeatGrade> grades = seats.stream()
                .map(Seat::getSeatGrade)
                .collect(Collectors.toSet());
        for (SeatGrade grade : grades) {
            if (!priceMap.containsKey(grade)) {
                throw new IllegalArgumentException("등급 " + grade + " 의 가격이 누락되었습니다.");
            }
        }
    }
}
