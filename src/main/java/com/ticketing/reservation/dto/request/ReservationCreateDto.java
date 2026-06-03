package com.ticketing.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class ReservationCreateDto {

    @NotNull
    private Long scheduleId;

    @NotEmpty
    private List<Long> eventSeatIds;

    @NotBlank
    private String idempotencyKey;
}
