package com.ticketing.settlement.dto.response;

import com.ticketing.member.domain.SellerGrade;
import com.ticketing.settlement.domain.SettlementDetail;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SettlementDetailSearchResponseDto {

    private Long paymentId;
    private Long reservationId;
    private Long eventId;
    private String eventTitle;
    private LocalDate settlementDate;
    private LocalDateTime paidAt;
    private Long grossAmount;
    private Long commission;
    private Long netAmount;
    private SellerGrade appliedGrade;
    private Integer commissionRate;

    public static SettlementDetailSearchResponseDto of(SettlementDetail d, String eventTitle) {
        return SettlementDetailSearchResponseDto.builder()
                .paymentId(d.getPaymentId())
                .reservationId(d.getReservationId())
                .eventId(d.getEventId())
                .eventTitle(eventTitle)
                .settlementDate(d.getSettlementDate())
                .paidAt(d.getPaidAt())
                .grossAmount(d.getGrossAmount())
                .commission(d.getCommission())
                .netAmount(d.getNetAmount())
                .appliedGrade(d.getAppliedGrade())
                .commissionRate(d.getCommissionRate())
                .build();
    }
}
