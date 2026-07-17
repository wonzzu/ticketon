package com.ticketing.settlement.batch;

import com.ticketing.settlement.dto.SettlementAggregateDto;
import com.ticketing.settlement.service.SettlementSkipLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementSkipListener implements SkipListener<SettlementAggregateDto, Object> {

    private final SettlementSkipLogService skipLogService;

    @Override
    public void onSkipInProcess(SettlementAggregateDto item, Throwable t) {
        log.warn("정산 스킵(Process): sellerId={}, eventId={}, 사유={}",
                item.sellerId(), item.eventId(), t.getMessage());
        skipLogService.save(item, t.getMessage());
    }

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("정산 스킵(Read) : 사유 = {}", t.getMessage());
        skipLogService.saveReadFailure("READ 실패: " + t.getMessage());
    }
}
