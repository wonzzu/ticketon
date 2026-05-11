package com.ticketing.event.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    CONCERT("콘서트"),
    MUSICAL("뮤지컬"),
    SPORTS("스포츠"),
    EXHIBITION("전시"),
    KIDS("아동공연");

    private final String description;
}
