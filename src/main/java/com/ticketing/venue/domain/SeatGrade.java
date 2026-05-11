package com.ticketing.venue.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SeatGrade {

    VIP("VIP석"),
    R("R석"),
    S("S석"),
    A("A석");

    private final String description;
}
