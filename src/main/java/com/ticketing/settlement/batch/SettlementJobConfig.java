package com.ticketing.settlement.batch;

import com.ticketing.settlement.domain.Settlement;
import com.ticketing.settlement.dto.SettlementAggregateDto;
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

    private static final int CHUNK_SIZE = 100;

    @Bean
    public Job settlementJob(JobRepository jobRepository, Step settlementDeleteStep,Step settlementStep) {

        return new JobBuilder("settlementJob",jobRepository)
                .start(settlementDeleteStep)
                .next(settlementStep)
                .build();
    }

    @Bean
    public Step settlementDeleteStep(JobRepository jobRepository, PlatformTransactionManager tx, Tasklet settlementDeleteTasklet) {

        return new StepBuilder("settlementDeleteStep",jobRepository)
                .tasklet(settlementDeleteTasklet,tx)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet settlementDeleteTasklet(@Value("#{jobParameters['targetDate']}") String targetDate) {
        return (contribution, chunkContext) -> {
            int deleted = jdbcTemplate.update(
                    "DELETE FROM settlement WHERE settlement_date = ?", LocalDate.parse(targetDate));
            contribution.incrementWriteCount(deleted);
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step settlementStep(JobRepository jobRepository, PlatformTransactionManager tx,
                               JdbcCursorItemReader<SettlementAggregateDto> settlementReader,
                               ItemProcessor<SettlementAggregateDto, Settlement> settlementProcessor,
                               ItemWriter<Settlement> settlementWriter, SettlementSkipListener settlementSkipListener) {

        return new StepBuilder("settlementStep",jobRepository)
                .<SettlementAggregateDto,Settlement> chunk(CHUNK_SIZE,tx)
                .reader(settlementReader)
                .processor(settlementProcessor)
                .writer(settlementWriter)
                .faultTolerant()
                .skip(SettlementValidationException.class)
                .skipLimit(10)
                .listener(settlementSkipListener)
                .build();
    }
}
