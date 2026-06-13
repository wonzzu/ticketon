package com.ticketing.statistics.domain;


import com.ticketing.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class DailySalesStats extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate statDate;

    @Column(nullable = false)
    private Long orderCount;

    @Column(nullable = false)
    private Long salesAmount;

    public static DailySalesStats of(LocalDate statDate, long orderCount, long salesAmount) {
        return DailySalesStats.builder()
                .statDate(statDate)
                .orderCount(orderCount)
                .salesAmount(salesAmount)
                .build();
    }
}
