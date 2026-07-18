package com.ticketing.settlement.batch;

import com.ticketing.settlement.domain.SettlementDetail;
import com.ticketing.settlement.dto.record.SettlementDetailRow;
import com.ticketing.settlement.service.SettlementSkipLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementSkipListener implements SkipListener<SettlementDetailRow, SettlementDetail> {

    private final SettlementSkipLogService skipLogService;

    @Override
    public void onSkipInProcess(SettlementDetailRow item, Throwable t) {
        log.warn("정산 스킵(Process): paymentId={}, sellerId={}, 사유={}",
                item.paymentId(), item.sellerId(), t.getMessage());
        skipLogService.save(item, t.getMessage());
    }

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("정산 스킵(Read) : 사유 = {}", t.getMessage());
        skipLogService.saveReadFailure("READ 실패: " + t.getMessage());
    }
}
