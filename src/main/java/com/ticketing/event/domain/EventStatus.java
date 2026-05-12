package com.ticketing.event.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventStatus {

    PENDING("검수 대기"),
    APPROVED("게시 중"),
    REJECTED("반려"),
    CLOSED("종료");

    private final String label;
}
