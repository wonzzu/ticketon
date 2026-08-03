package com.ticketing.settlement.batch;

import com.ticketing.settlement.domain.SettlementDirtyDate;
import com.ticketing.settlement.repository.SettlementDirtyDateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {

    private final JobLauncher jobLauncher;
    private final Job settlementJob;
    private final SettlementDirtyDateRepository dirtyRepository;
    private final RedissonClient redissonClient;

    @Scheduled(cron = "0 0 2 * * *")
    public void runDaily() {
        RLock lock = redissonClient.getLock("batch:settlement:daily:lock");
        if (!lock.tryLock()) {
            log.debug("정산 배치 락 획득 실패 - 다른 인스턴스가 처리 중");
            return;
        }
        try {
            run(LocalDate.now().minusDays(1));
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    @Scheduled(cron = "0 30 2 * * *")
    public void reaggregateDirty() {

        RLock lock = redissonClient.getLock("batch:settlement:reaggregate:lock");
        if (!lock.tryLock()) {
            log.debug("정산 재집계 락 획득 실패 - 다른 인스턴스가 처리 중");
            return;
        }

        try {
            List<SettlementDirtyDate> dirties = dirtyRepository.findAll();
            if (dirties.isEmpty()) {
                return;
            }

            Map<LocalDate, List<SettlementDirtyDate>> byDate = dirties.stream()
                    .collect(Collectors.groupingBy(SettlementDirtyDate::getSettlementDate));

            byDate.forEach((date, group) -> {
                if (run(date)) {dirtyRepository.deleteAll(group);}
            });

            log.info("정산 재집계 실행: dates={}", byDate.keySet());
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }


    public boolean run(LocalDate targetDate) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("targetDate", targetDate.toString())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(settlementJob, params);

            if (execution.getStatus() != BatchStatus.COMPLETED) {
                log.error("정산 배치 비정상 종료: targetDate={}, status={}", targetDate, execution.getStatus());
                return false;
            }
            log.info("정산 배치 실행 : targetDate ={}", targetDate);
            return true;
        } catch (Exception e) {
            log.error("정산 배치 실패 : targetDate = {}", targetDate, e);
            return false;
        }
    }
}