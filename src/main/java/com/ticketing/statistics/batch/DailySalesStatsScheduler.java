package com.ticketing.statistics.batch;

import com.ticketing.statistics.domain.StatsDirtyDate;
import com.ticketing.statistics.repository.StatsDirtyDateRepository;
import com.ticketing.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailySalesStatsScheduler {

    private final StatisticsService statisticService;
    private final StatsDirtyDateRepository dirtyRepository;
    private final RedissonClient redissonClient;

    @Scheduled(cron = "0 0 4 * * *")
    public void aggregateYesterday() {

        RLock lock = redissonClient.getLock("batch:statistics:daily:lock");

        if (!lock.tryLock()) {
            log.debug("통계 배치 락 획득 실패 - 다른 인스턴스가 처리 중");
            return;
        }
        try {
            LocalDate target = LocalDate.now().minusDays(1);
            log.info("[배치] 일별 통계 집계 시작: date={}", target);
            try {
                statisticService.aggregateDaily(target);
                log.info("[배치] 일별 통계 집계 완료: date={}", target);
            } catch (Exception e) {
                log.error("[배치] 일별 통계 집계 실패: date={}", target, e);
            }
        }finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }


    }

    @Scheduled(cron = "0 30 4 * * *")
    public void reaggregateDirty() {

        RLock lock = redissonClient.getLock("batch:statistics:reaggregate:lock");

        if (!lock.tryLock()) {
            log.debug("통계 재집계 락 획득 실패 - 다른 인스턴스가 처리 중");
            return;
        }
        try {
            List<StatsDirtyDate> dirties = dirtyRepository.findAll();
            if (dirties.isEmpty()) {
                return;
            }

            dirties.forEach(dirty -> {
                statisticService.aggregateDaily(dirty.getStatDate());
                dirtyRepository.delete(dirty);
            });

            log.info("통계 재집계 실행: dates={}",
                    dirties.stream().map(StatsDirtyDate::getStatDate).toList());
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }
}
