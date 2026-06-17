package com.ticketing.statistics.service;

import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.EventStatus;
import com.ticketing.event.dto.response.EventListResponseDto;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.payment.dto.PaymentSalesAggregate;
import com.ticketing.payment.repository.PaymentRepository;
import com.ticketing.review.repository.ReviewRepository;
import com.ticketing.statistics.domain.DailyEventStats;
import com.ticketing.statistics.domain.DailySalesStats;
import com.ticketing.statistics.dto.response.DailySalesStatsResponseDto;
import com.ticketing.statistics.repository.DailyEventStatsRepository;
import com.ticketing.statistics.repository.DailySalesStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private static final long MIN_REVIEWS = 10;

    private final PaymentRepository paymentRepository;
    private final DailySalesStatsRepository dailySalesStatsRepository;
    private final DailyEventStatsRepository dailyEventStatsRepository;
    private final EventRepository eventRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public void aggregateDaily(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        PaymentSalesAggregate agg = paymentRepository.aggregatePaid(start, end);

        dailySalesStatsRepository.deleteByStatDate(date);
        dailySalesStatsRepository.save(DailySalesStats.of(date, agg.orderCount(), agg.salesAmount()));

        dailyEventStatsRepository.deleteByStatDate(date);
        paymentRepository.aggregatePaidByEvent(start, end)
                .forEach(e -> dailyEventStatsRepository.save(
                        DailyEventStats.of(date, e.eventId(), e.orderCount())
                ));

        log.info("일별 통계 집계 완료: date={},주문수={},매출={}", date, agg.orderCount(), agg.salesAmount());
    }

    public List<DailySalesStatsResponseDto> findRange(LocalDate from, LocalDate to) {

        LocalDate today = LocalDate.now();
        LocalDate pastTo = to.isBefore(today) ? to : today.minusDays(1);

        ArrayList<DailySalesStatsResponseDto> result = new ArrayList<>();

        if (!from.isAfter(pastTo)) {
            dailySalesStatsRepository.findByStatDateBetweenOrderByStatDate(from, pastTo)
                    .forEach(s -> result.add(DailySalesStatsResponseDto.from(s)));
        }

        if (!to.isBefore(today) && !from.isAfter(today)) {
            PaymentSalesAggregate t = paymentRepository.aggregatePaid(
                    today.atStartOfDay(), today.plusDays(1).atStartOfDay());
            result.add(DailySalesStatsResponseDto.ofToday(today, t.orderCount(), t.salesAmount()));
        }

        return result;
    }

    public List<EventListResponseDto> getRanking(String sort, int days, int limit) {

        List<Long> ids = "rating".equals(sort)
                ? reviewRepository.findTopRatedEventIds(EventStatus.APPROVED, MIN_REVIEWS, limit)
                : dailyEventStatsRepository.findRankedEventIds(LocalDate.now().minusDays(days), PageRequest.of(0, limit));

        return toRanked(ids);
    }

    private List<EventListResponseDto> toRanked(List<Long> ids) {

        Map<Long, Event> eventMap = eventRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Event::getId, e -> e));

        return ids.stream()
                .map(eventMap::get)
                .filter(Objects::nonNull)
                .map(EventListResponseDto::from)
                .toList();
    }
}
