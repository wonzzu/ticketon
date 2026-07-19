package com.ticketing.settlement.dto.response;

import com.ticketing.member.domain.SellerGrade;
import com.ticketing.settlement.domain.SettlementDetail;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SettlementDetailLineResponseDto {

    private Long paymentId;
    private Long reservationId;
    private Long grossAmount;
    private Long commission;
    private Long netAmount;
    private SellerGrade appliedGrade;
    private Integer commissionRate;

    public static SettlementDetailLineResponseDto from(SettlementDetail d) {
        return SettlementDetailLineResponseDto.builder()
                .paymentId(d.getPaymentId())
                .reservationId(d.getReservationId())
                .grossAmount(d.getGrossAmount())
                .commission(d.getCommission())
                .netAmount(d.getNetAmount())
                .appliedGrade(d.getAppliedGrade())
                .commissionRate(d.getCommissionRate())
                .build();
    }
}
