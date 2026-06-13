package com.ticketing.statistics.service;

import com.ticketing.payment.dto.PaymentSalesAggregate;
import com.ticketing.payment.repository.PaymentRepository;
import com.ticketing.statistics.domain.DailySalesStats;
import com.ticketing.statistics.dto.response.DailySalesStatsResponseDto;
import com.ticketing.statistics.repository.DailySalesStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final PaymentRepository paymentRepository;
    private final DailySalesStatsRepository dailySalesStatsRepository;

    @Transactional
    public void aggregateDaily(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        PaymentSalesAggregate agg = paymentRepository.aggregatePaid(start, end);

        dailySalesStatsRepository.deleteByStatDate(date);
        dailySalesStatsRepository.save(DailySalesStats.of(date, agg.orderCount(), agg.salesAmount()));

    }

    public List<DailySalesStatsResponseDto> findRange(LocalDate from, LocalDate to) {

        LocalDate today = LocalDate.now();
        LocalDate pastTo = to.isBefore(today) ? to : today.minusDays(1);

        ArrayList<DailySalesStatsResponseDto> result = new ArrayList<>();

        // 1) 과거(오늘 이전)는 통계 테이블에서
        if (!from.isAfter(pastTo)) {
            dailySalesStatsRepository.findByStatDateBetweenOrderByStatDate(from, pastTo)
                    .forEach(s -> result.add(DailySalesStatsResponseDto.from(s)));
        }

        // 2) 오늘이 기간에 포함되면 원본 즉석 집계 (to >= today && from <= today)
        if (!to.isBefore(today) && !from.isAfter(today)) {
            PaymentSalesAggregate t = paymentRepository.aggregatePaid(
                    today.atStartOfDay(), today.plusDays(1).atStartOfDay());
            result.add(DailySalesStatsResponseDto.ofToday(today, t.orderCount(), t.salesAmount()));
        }

        return result;
    }
}
