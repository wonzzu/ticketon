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
        return dailySalesStatsRepository.findByStatDateBetweenOrderByStatDate(from, to)
                .stream().map(DailySalesStatsResponseDto::from).toList();
    }
}
