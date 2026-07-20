package com.ticketing.settlement.domain;

import com.ticketing.global.entity.BaseEntity;
import com.ticketing.member.domain.SellerGrade;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class SettlementDetail extends BaseEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long paymentId;

    @Column(nullable = false)
    private Long reservationId;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private LocalDate settlementDate;

    @Column(nullable = false)
    private LocalDateTime paidAt;   // 고객 결제 시각 (payment.created_at 스냅샷)

    @Column(nullable = false)
    private Long grossAmount;

    @Column(nullable = false)
    private Long commission;

    @Column(nullable = false)
    private Long netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SellerGrade appliedGrade;

    @Column(nullable = false)
    private Integer commissionRate;

    public static SettlementDetail of(Long paymentId, Long reservationId, Long sellerId, Long eventId, LocalDate settlementDate,
                                      LocalDateTime paidAt, Long grossAmount, Long commission, Long netAmount, SellerGrade grade, int commissionRate) {

        return SettlementDetail.builder()
                .paymentId(paymentId)
                .reservationId(reservationId)
                .sellerId(sellerId)
                .eventId(eventId)
                .settlementDate(settlementDate)
                .paidAt(paidAt)
                .grossAmount(grossAmount)
                .commission(commission)
                .netAmount(netAmount)
                .appliedGrade(grade)
                .commissionRate(commissionRate)
                .build();
    }

}
