package com.ticketing.reservation.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationStatus {
    PENDING("결제 대기"),
    CONFIRMED("결제 완료"),
    CANCEL("예매 취소");

    private final String description;
}
