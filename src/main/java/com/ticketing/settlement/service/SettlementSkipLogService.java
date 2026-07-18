package com.ticketing.settlement.service;

import com.ticketing.settlement.domain.SettlementSkipLog;
import com.ticketing.settlement.dto.record.SettlementDetailRow;
import com.ticketing.settlement.repository.SettlementSkipLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementSkipLogService {

    private final SettlementSkipLogRepository settlementSkipLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(SettlementDetailRow row, String reason) {
        settlementSkipLogRepository.save(
                SettlementSkipLog.of(row.paymentId(), row.sellerId(), row.eventId(),
                        row.settlementDate(), row.grossAmount(), reason));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveReadFailure(String reason) {
        settlementSkipLogRepository.save(
                SettlementSkipLog.of(null, null, null, null, null, reason));
    }
}
