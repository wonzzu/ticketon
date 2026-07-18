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

        // filter — 0원 결제는 정산 명세 제외 (null 반환 = 정상 제외)
        if (row.grossAmount() == 0) {
            log.debug("매출 0원 제외: paymentId={}", row.paymentId());
            return null;
        }

        // skip — 검증 실패 (예외 = 오류 제외, SkipListener가 DB 기록)
        validate(row);

        // 등급별 수수료 계산 — enum이 정책 소유 (정수 연산)
        SellerGrade grade = row.grade();
        long commission = grade.calculateCommission(row.grossAmount());
        long netAmount = row.grossAmount() - commission;

        // 계산 정합성 재검산
        if (commission > row.grossAmount() || netAmount < 0) {
            throw new SettlementValidationException(
                    "정산 금액 오류: paymentId=%s, gross=%d, commission=%d, net=%d"
                            .formatted(row.paymentId(), row.grossAmount(), commission, netAmount));
        }

        // 명세 생성 — grade·rate 스냅샷 포함 (정책 변경 대응)
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
