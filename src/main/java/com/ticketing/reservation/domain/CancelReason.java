package com.ticketing.reservation.domain;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CancelReason {

    CHANGE_OF_MIND("단순 변심"),
    SCHEDULE_CONFLICT("일정 변경"),
    DUPLICATE_BOOKING("중복 예매"),
    EVENT_CHANGED("공연 정보 변경"),
    OTHER("기타");

    private final String description;
}
