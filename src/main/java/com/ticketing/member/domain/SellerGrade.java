package com.ticketing.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SellerGrade {

    BRONZE("브론즈", 10),
    SILVER("실버", 8),
    GOLD("골드", 5);

    private final String description;
    private final int commissionPercent;

    public long calculateCommission(long grossAmount) {
        return grossAmount * commissionPercent / 100;
    }
}
