package com.ticketing.settlement.batch;


import com.ticketing.payment.dto.PaymentCanceledEvent;
import com.ticketing.settlement.service.SettlementDirtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SettlementDirtyEventListener {

    private final SettlementDirtyService settlementDirtyService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentCanceledEvent event) {
        settlementDirtyService.markDirtyIfSettled(event.sellerId(), event.eventId(), event.settlementDate());
    }
}
