package com.ticketing.event.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewAction {
    APPROVED("승인"),
    REJECTED("반려");

    private final String label;
}
