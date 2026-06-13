package com.ticketing.statistics.dto.response;

import com.ticketing.statistics.domain.DailySalesStats;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DailySalesStatsResponseDto {

    private LocalDate statDate;
    private Long orderCount;
    private Long salesAmount;
    private LocalDateTime aggregatedAt;

    public static DailySalesStatsResponseDto from(DailySalesStats s) {
        return DailySalesStatsResponseDto.builder()
                .statDate(s.getStatDate())
                .orderCount(s.getOrderCount())
                .salesAmount(s.getSalesAmount())
                .aggregatedAt(s.getCreatedAt())
                .build();
    }

    public static DailySalesStatsResponseDto ofToday(LocalDate date, long orderCount, long salesAmount) {
        return DailySalesStatsResponseDto.builder()
                .statDate(date)
                .orderCount(orderCount)
                .salesAmount(salesAmount)
                .aggregatedAt(LocalDateTime.now())
                .build();
    }
}
