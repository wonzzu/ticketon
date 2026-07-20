package com.ticketing.settlement.batch;

import com.ticketing.member.domain.SellerGrade;
import com.ticketing.settlement.dto.record.SettlementDetailRow;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class SettlementReaderConfig {

    private final DataSource dataSource;


    private static final String DETAIL_SQL = """
            SELECT p.id            AS payment_id,
                   r.id            AS reservation_id,
                   e.seller_id     AS seller_id,
                   e.id            AS event_id,
                   s.grade         AS grade,
                   p.amount        AS gross_amount,
                   p.created_at    AS paid_at
            FROM payment p
            JOIN reservation r     ON p.reservation_id = r.id
            JOIN event_schedule es ON r.schedule_id = es.id
            JOIN event e           ON es.event_id = e.id
            JOIN seller s          ON e.seller_id = s.id
            WHERE p.status = 'PAID'
              AND e.end_date = ?
            """;

    @Bean
    @StepScope
    public JdbcCursorItemReader<SettlementDetailRow> settlementReader(
            @Value("#{jobParameters['targetDate']}") String targetDate) {

        LocalDate date = LocalDate.parse(targetDate);

        return new JdbcCursorItemReaderBuilder<SettlementDetailRow>()
                .name("settlementReader")
                .dataSource(dataSource)
                .fetchSize(1000)
                .sql(DETAIL_SQL)
                .preparedStatementSetter(ps -> ps.setString(1, targetDate))
                .rowMapper((rs, rowNum) -> new SettlementDetailRow(
                        rs.getLong("payment_id"),
                        rs.getLong("reservation_id"),
                        rs.getLong("seller_id"),
                        rs.getLong("event_id"),
                        SellerGrade.valueOf(rs.getString("grade")),
                        rs.getLong("gross_amount"),
                        date,
                        rs.getTimestamp("paid_at").toLocalDateTime()
                ))
                .build();
    }
}