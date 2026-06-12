package com.ticketing.payment.domain;

import com.ticketing.global.entity.BaseEntity;
import com.ticketing.global.exception.BaseException;
import com.ticketing.reservation.domain.Reservation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static com.ticketing.global.baseresponse.BaseResponseStatus.PAYMENT_CANCEL_NOT_ALLOWED;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = true, length = 30)
    private String method;

    public static Payment paid(Reservation reservation, int amount) {
        return Payment.builder()
                .reservation(reservation)
                .amount(amount)
                .status(PaymentStatus.PAID)
                .method("MOCK")
                .build();
    }

    public void cancel() {
        if (this.status != PaymentStatus.PAID) {
            throw new BaseException(PAYMENT_CANCEL_NOT_ALLOWED);
        }
        this.status = PaymentStatus.CANCELED;
    }
}