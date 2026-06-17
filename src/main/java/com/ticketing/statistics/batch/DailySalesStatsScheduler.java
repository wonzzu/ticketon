package com.ticketing.statistics.batch;

import com.ticketing.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailySalesStatsScheduler {

    private final StatisticsService statisticService;

    @Scheduled(cron = "0 0 4 * * *")
    public void aggregateYesterday() {
        LocalDate target = LocalDate.now().minusDays(1);
        log.info("[배치] 일별 통계 집계 시작: date={}", target);
        try {
            statisticService.aggregateDaily(target);
            log.info("[배치] 일별 통계 집계 완료: date={}", target);
        } catch (Exception e) {
            log.error("[배치] 일별 통계 집계 실패: date={}", target, e);
        }
    }
}
