package com.ticketing.settlement.batch;

import com.ticketing.member.domain.SellerGrade;
import com.ticketing.settlement.domain.SettlementDetail;
import com.ticketing.settlement.dto.record.SettlementDetailRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class SettlementProcessor implements ItemProcessor<SettlementDetailRow, SettlementDetail> {

    @Override
    public SettlementDetail process(SettlementDetailRow row) {


        if (row.grossAmount() == 0) {
            log.debug("매출 0원 제외: paymentId={}", row.paymentId());
            return null;
        }


        validate(row);


        SellerGrade grade = row.grade();
        long commission = grade.calculateCommission(row.grossAmount());
        long netAmount = row.grossAmount() - commission;


        if (commission > row.grossAmount() || netAmount < 0) {
            throw new SettlementValidationException(
                    "정산 금액 오류: paymentId=%s, gross=%d, commission=%d, net=%d"
                            .formatted(row.paymentId(), row.grossAmount(), commission, netAmount));
        }


        return SettlementDetail.of(
                row.paymentId(), row.reservationId(), row.sellerId(), row.eventId(),
                row.settlementDate(), row.grossAmount(), commission, netAmount,
                grade, grade.getCommissionPercent());
    }

    private void validate(SettlementDetailRow row) {
        if (row.paymentId() == null || row.sellerId() == null || row.eventId() == null) {
            throw new SettlementValidationException("정산 키 누락: paymentId=%s, sellerId=%s, eventId=%s"
                    .formatted(row.paymentId(), row.sellerId(), row.eventId()));
        }
        if (row.grade() == null) {
            throw new SettlementValidationException("판매자 등급 없음: sellerId=%s".formatted(row.sellerId()));
        }
        if (row.grossAmount() < 0) {
            throw new SettlementValidationException("매출 음수: paymentId=%s, gross=%d"
                    .formatted(row.paymentId(), row.grossAmount()));
        }
        if (row.settlementDate().isAfter(LocalDate.now())) {
            throw new SettlementValidationException("미래 날짜 정산 불가: date=%s".formatted(row.settlementDate()));
        }
    }
}
