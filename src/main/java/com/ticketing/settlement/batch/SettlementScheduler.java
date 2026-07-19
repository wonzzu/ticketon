package com.ticketing.settlement.batch;

import com.ticketing.settlement.domain.SettlementDirtyDate;
import com.ticketing.settlement.repository.SettlementDirtyDateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Scheduled(cron = "0 0 2 * * *")
    public void runDaily() {
        run(LocalDate.now().minusDays(1));
    }

    // 일일 정산(2:00) 이후 재집계 — 취소로 더러워진 날짜만 멱등 재실행
    @Scheduled(cron = "0 30 2 * * *")
    public void reaggregateDirty() {
        List<SettlementDirtyDate> dirties = dirtyRepository.findAll();
        if (dirties.isEmpty()) {
            return;
        }

        // 정산 Job은 '날짜' 단위 멱등 재실행이므로 날짜로 묶는다
        Map<LocalDate, List<SettlementDirtyDate>> byDate = dirties.stream()
                .collect(Collectors.groupingBy(SettlementDirtyDate::getSettlementDate));

        byDate.forEach((date, group) -> {
            if (run(date)) {                       // 멱등 DELETE&INSERT → CANCELED 자연 제외
                dirtyRepository.deleteAll(group);  // 성공한 날짜만 큐에서 제거 (실패 시 다음 실행에 재시도)
            }
        });
        log.info("정산 재집계 실행: dates={}", byDate.keySet());
    }

    // 수동/재집계 공용. 성공여부 반환 → 재집계에서 dirty 삭제 판단에 사용
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