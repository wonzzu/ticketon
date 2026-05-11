package com.ticketing.event.domain;

import com.ticketing.venue.domain.SeatGrade;

public record GradePrice(SeatGrade seatGrade, int price) {}
