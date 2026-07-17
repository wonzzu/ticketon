package com.ticketing.settlement.batch;

import com.ticketing.settlement.domain.Settlement;
import com.ticketing.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SettlementWriterConfig {

    private final SettlementRepository settlementRepository;

    @Bean
    public ItemWriter<Settlement> settlementItemWriter() {
        return chunk -> settlementRepository.saveAll(chunk.getItems());
    }
}
