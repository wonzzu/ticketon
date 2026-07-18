package com.ticketing.settlement.batch;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {

    private final JobLauncher jobLauncher;
    private final Job settlementJob;

    @Scheduled(cron = " 0 0 2 * * *")
    public void runDaily() {
        run(LocalDate.now().minusDays(1));
    }


    //수동 실행 용
    public void run(LocalDate targetDate) {

        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("targetDate", targetDate.toString())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(settlementJob, params);
            log.info("정산 배치 실행 : targetDate ={}", targetDate);
        } catch (Exception e) {
            log.error("정산 배치 실패 : targetDate = {}", targetDate,e);
        }
    }
}
