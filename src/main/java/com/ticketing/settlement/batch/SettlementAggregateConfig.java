package com.ticketing.settlement.batch;

import com.ticketing.settlement.domain.Settlement;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class SettlementAggregateConfig {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    private static final String AGGREGATE_SQL = """
            SELECT seller_id, event_id, settlement_date,
                   SUM(gross_amount) AS gross_amount,
                   SUM(commission)   AS commission,
                   SUM(net_amount)   AS net_amount
            FROM settlement_detail
            WHERE settlement_date = ?
            GROUP BY seller_id, event_id, settlement_date
            """;

    @Bean
    @StepScope
    public JdbcCursorItemReader<Settlement> settlementAggregateReader(
            @Value("#{jobParameters['targetDate']}") String targetDate) {

        return new JdbcCursorItemReaderBuilder<Settlement>()
                .name("settlementAggregateReader")
                .dataSource(dataSource)
                .sql(AGGREGATE_SQL)
                .preparedStatementSetter(ps -> ps.setString(1, targetDate))
                .rowMapper((rs, rowNum) -> Settlement.of(
                        rs.getLong("seller_id"),
                        rs.getLong("event_id"),
                        rs.getObject("settlement_date", LocalDate.class),
                        rs.getLong("gross_amount"),
                        rs.getLong("commission"),
                        rs.getLong("net_amount")))
                .build();
    }

    private static final String SETTLEMENT_INSERT_SQL = """
            INSERT INTO settlement
                (seller_id, event_id, settlement_date, gross_amount, commission, net_amount, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())
            """;

    @Bean
    public ItemWriter<Settlement> settlementAggregateWriter() {
        return chunk -> jdbcTemplate.batchUpdate(SETTLEMENT_INSERT_SQL, chunk.getItems(), chunk.size(),
                (ps, s) -> {
                    ps.setLong(1, s.getSellerId());
                    ps.setLong(2, s.getEventId());
                    ps.setObject(3, s.getSettlementDate());
                    ps.setLong(4, s.getGrossAmount());
                    ps.setLong(5, s.getCommission());
                    ps.setLong(6, s.getNetAmount());
                });
    }
}
