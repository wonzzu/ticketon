package com.ticketing.coupon.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiscountType {

    FIXED("정액 할인"),RATE("정률 할인");

    private final  String description;
}
