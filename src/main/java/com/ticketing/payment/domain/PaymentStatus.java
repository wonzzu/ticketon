package com.ticketing.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    PAID("결제 완료"),
    FAILED("결제 실패"),
    CANCELED("결제 취소");

    private final String description;
}
