package com.ticketing.settlement.dto.response;


import com.ticketing.settlement.domain.Settlement;
import lombok.*;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SettlementResponseDto {

    private Long settlementId;
    private Long eventId;
    private String eventTitle;
    private LocalDate settlementDate;
    private Long grossAmount;
    private Long commission;
    private Long netAmount;


    public static SettlementResponseDto of(Settlement s, String eventTitle) {
        return SettlementResponseDto.builder()
                .settlementId(s.getId())
                .eventId(s.getEventId())
                .eventTitle(eventTitle)
                .settlementDate(s.getSettlementDate())
                .grossAmount(s.getGrossAmount())
                .commission(s.getCommission())
                .netAmount(s.getNetAmount())
                .build();
    }
}
