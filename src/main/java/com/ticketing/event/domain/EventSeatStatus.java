package com.ticketing.event.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventSeatStatus {
    AVAILABLE("예매 가능"),
    RESERVED("예매 완료"),
    HELD("임시 점유");

    private final String description;
}
