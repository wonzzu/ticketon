package com.ticketing.settlement.batch;

import com.ticketing.settlement.domain.SettlementDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
public class SettlementWriterConfig {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL = """
            INSERT INTO settlement_detail
                (payment_id, reservation_id, seller_id, event_id, settlement_date, paid_at,
                 gross_amount, commission, net_amount, applied_grade, commission_rate,
                 created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
            """;


    @Bean
    public ItemWriter<SettlementDetail> settlementWriter() {
        return chunk -> jdbcTemplate.batchUpdate(INSERT_SQL, chunk.getItems(), chunk.size(),
                (ps, d) -> {
                    ps.setLong(1, d.getPaymentId());
                    ps.setLong(2, d.getReservationId());
                    ps.setLong(3, d.getSellerId());
                    ps.setLong(4, d.getEventId());
                    ps.setObject(5, d.getSettlementDate());
                    ps.setObject(6, d.getPaidAt());
                    ps.setLong(7, d.getGrossAmount());
                    ps.setLong(8, d.getCommission());
                    ps.setLong(9, d.getNetAmount());
                    ps.setString(10, d.getAppliedGrade().name());
                    ps.setInt(11, d.getCommissionRate());
                });
    }
}
