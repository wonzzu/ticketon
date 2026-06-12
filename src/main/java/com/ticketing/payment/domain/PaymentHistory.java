package com.ticketing.payment.domain;

import com.ticketing.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class PaymentHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private Integer amount;

    @Column(length = 500)
    private String reason;

    public static PaymentHistory of(Payment payment, String reason) {

        return PaymentHistory.builder()
                .paymentId(payment.getId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .reason(reason)
                .build();
    }
}
