package com.ticketing.reservation.dto.request;

import com.ticketing.reservation.domain.ReservationStatus;

import java.time.LocalDate;

public record ReservationSearchCond(ReservationStatus status, LocalDate from, LocalDate to) {


}
