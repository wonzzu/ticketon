package com.ticketing.statistics.batch;

import com.ticketing.payment.dto.PaymentCanceledEvent;
import com.ticketing.statistics.service.StatsDirtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class StatsDirtyEventListener {

    private final StatsDirtyService statsDirtyService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentCanceledEvent event) {
        statsDirtyService.markDirtyIfAggregated(event.paidDate());
    }
}
