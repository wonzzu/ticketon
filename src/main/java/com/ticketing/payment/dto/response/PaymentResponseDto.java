package com.ticketing.payment.dto.response;

import com.ticketing.payment.domain.Payment;
import com.ticketing.payment.domain.PaymentStatus;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PaymentResponseDto {

    private Long id;
    private Long reservationId;
    private Integer amount;
    private PaymentStatus status;
    private String statusLabel;
    private String method;

    public static PaymentResponseDto from(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .reservationId(payment.getReservation().getId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .statusLabel(payment.getStatus().getDescription())
                .method(payment.getMethod())
                .build();
    }
}
