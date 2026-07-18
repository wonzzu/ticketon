package com.ticketing.settlement.batch;

import com.ticketing.settlement.domain.Settlement;
import com.ticketing.settlement.domain.SettlementDetail;
import com.ticketing.settlement.dto.record.SettlementDetailRow;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class SettlementJobConfig {

    private final JdbcTemplate jdbcTemplate;
    private static final int CHUNK_SIZE = 1000;

    @Bean
    public Job settlementJob(JobRepository jobRepository, Step settlementDeleteStep,
                             Step settlementDetailStep, Step settlementAggregateStep) {
        return new JobBuilder("settlementJob", jobRepository)
                .start(settlementDeleteStep)
                .next(settlementDetailStep)
                .next(settlementAggregateStep)
                .build();
    }

    // Step0 — 멱등 (해당 날짜 정산·명세 삭제)
    @Bean
    public Step settlementDeleteStep(JobRepository jobRepository, PlatformTransactionManager tx,
                                     Tasklet settlementDeleteTasklet) {
        return new StepBuilder("settlementDeleteStep", jobRepository)
                .tasklet(settlementDeleteTasklet, tx)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet settlementDeleteTasklet(@Value("#{jobParameters['targetDate']}") String targetDate) {
        return (contribution, chunkContext) -> {
            LocalDate date = LocalDate.parse(targetDate);
            jdbcTemplate.update("DELETE FROM settlement_detail WHERE settlement_date = ?", date);
            jdbcTemplate.update("DELETE FROM settlement WHERE settlement_date = ?", date);
            return RepeatStatus.FINISHED;
        };
    }

    // Step1 — 건별 명세
    @Bean
    public Step settlementDetailStep(JobRepository jobRepository, PlatformTransactionManager tx,
                                     JdbcCursorItemReader<SettlementDetailRow> settlementReader,
                                     ItemProcessor<SettlementDetailRow, SettlementDetail> settlementProcessor,
                                     ItemWriter<SettlementDetail> settlementWriter,
                                     SettlementSkipListener settlementSkipListener) {
        return new StepBuilder("settlementDetailStep", jobRepository)
                .<SettlementDetailRow, SettlementDetail>chunk(CHUNK_SIZE, tx)
                .reader(settlementReader)
                .processor(settlementProcessor)
                .writer(settlementWriter)
                .faultTolerant()
                .skip(SettlementValidationException.class)
                .skipLimit(100)
                .listener(settlementSkipListener)
                .build();
    }

    // Step2 — 집계 (Processor 없음)
    @Bean
    public Step settlementAggregateStep(JobRepository jobRepository, PlatformTransactionManager tx,
                                        JdbcCursorItemReader<Settlement> settlementAggregateReader,
                                        ItemWriter<Settlement> settlementAggregateWriter) {
        return new StepBuilder("settlementAggregateStep", jobRepository)
                .<Settlement, Settlement>chunk(CHUNK_SIZE, tx)
                .reader(settlementAggregateReader)
                .writer(settlementAggregateWriter)
                .build();
    }
}
