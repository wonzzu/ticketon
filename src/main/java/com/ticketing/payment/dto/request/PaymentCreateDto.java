package com.ticketing.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PaymentCreateDto {

    @NotNull
    private Long reservationId;

}
