package com.ticketing.statistics.batch;

import com.ticketing.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DailySalesStatsScheduler {

    private final StatisticsService statisticService;

    @Scheduled(cron = "0 0 4 * * *")
    public void aggregateYesterday() {
        statisticService.aggregateDaily(LocalDate.now().minusDays(1));
    }
}
