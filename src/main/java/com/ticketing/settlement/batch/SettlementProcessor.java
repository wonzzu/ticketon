package com.ticketing.settlement.batch;

import com.ticketing.member.domain.SellerGrade;
import com.ticketing.settlement.domain.Settlement;
import com.ticketing.settlement.dto.SettlementAggregateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class SettlementProcessor implements ItemProcessor<SettlementAggregateDto, Settlement> {


    @Override
    public Settlement process(SettlementAggregateDto dto) throws Exception {

        if(dto.grossAmount() == 0 ){
            log.debug("매출 0원이라 정산 제외 : sellerId ={}, eventId={}",dto.sellerId(),dto.eventId());
            return null;
        }


        if (dto.sellerId() == null || dto.eventId() == null) {
            throw new SettlementValidationException("정산 키 누락: sellerId=%s,eventId=%s".formatted(dto.sellerId(), dto.eventId()));
        }

        if (dto.grade() == null) {
            throw new SettlementValidationException("판매자 등급 없음: sellerId=%s".formatted(dto.sellerId()));
        }

        if (dto.grossAmount() < 0) {
            throw new SettlementValidationException("매출 음수 : sellerId=%s,gross=%d".formatted(dto.sellerId(), dto.grossAmount()));
        }

        if (dto.settlementDate().isAfter(LocalDate.now())) {
            throw new SettlementValidationException("미래 날자 정산 불가: date=%s".formatted(dto.settlementDate()));
        }


        SellerGrade grade = dto.grade();
        long commission = grade.calculateCommission(dto.grossAmount());
        long netAmount = dto.grossAmount() - commission;

        if (commission > dto.grossAmount() || netAmount < 0) {
            throw new SettlementValidationException("정산 금액 오류: gross=%d,commission=%d,net=%d".formatted(dto.grossAmount(), commission, netAmount));
        }

        return Settlement.of(dto.sellerId(), dto.eventId(), dto.settlementDate(), dto.grossAmount(), commission, netAmount);
    }


}
