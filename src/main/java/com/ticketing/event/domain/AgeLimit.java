package com.ticketing.event.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgeLimit {

    ALL("전체 관람가"),
    AGE_12("12세 이상"),
    AGE_15("15세 이상"),
    AGE_18("18세 이상");

    private final String description;
}
