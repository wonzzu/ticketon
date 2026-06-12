package com.ticketing.reservation.dto.request;

import com.ticketing.reservation.domain.CancelReason;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReservationCancelDto {

    @NotNull
    private CancelReason cancelReason;

    private String detail;

}
