package com.ticketing.settlement.batch;

import com.ticketing.member.domain.SellerGrade;
import com.ticketing.settlement.dto.SettlementAggregateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class SettlementReaderConfig {

    private final DataSource dataSource;


    private static final String AGGREGATE_SQL = """
            SELECT e.seller_id   AS seller_id,
                   es.event_id   AS event_id,
                   s.grade       AS grade,
                   SUM(p.amount) AS gross_amount
            FROM payment p
            JOIN reservation r     ON p.reservation_id = r.id
            JOIN event_schedule es ON r.schedule_id = es.id
            JOIN event e           ON es.event_id = e.id
            JOIN seller s          ON e.seller_id = s.id
            WHERE p.status = 'PAID'
              AND p.created_at >= ?
              AND p.created_at <  ?
            GROUP BY e.seller_id, es.event_id, s.grade
            """;

    @Bean
    @StepScope
    public JdbcCursorItemReader<SettlementAggregateDto> settlementReader(
            @Value("#{jobParameters['targetDate']}") String targetDate) {

        LocalDate date = LocalDate.parse(targetDate);

        return new JdbcCursorItemReaderBuilder<SettlementAggregateDto>()
                .name("settlementReader")
                .dataSource(dataSource)
                .sql(AGGREGATE_SQL)
                .preparedStatementSetter(ps -> {
                    ps.setTimestamp(1, Timestamp.valueOf(date.atStartOfDay()));
                    ps.setTimestamp(2, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
                })
                .rowMapper((rs, rowNum) -> new SettlementAggregateDto(
                        rs.getLong("seller_id"),
                        rs.getLong("event_id"),
                        date,
                        rs.getLong("gross_amount"),
                        SellerGrade.valueOf(rs.getString("grade"))
                ))
                .build();
    }
}