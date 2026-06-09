package com.ticketing.queue.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum QueueStatus {

    WAITING("대기중"), ADMITTED("입장"), EXPIRED("만료");

    private final String description;
}
