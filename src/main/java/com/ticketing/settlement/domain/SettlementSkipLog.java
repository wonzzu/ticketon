package com.ticketing.settlement.domain;

import com.ticketing.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class SettlementSkipLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long paymentId;

    private Long sellerId;

    private Long eventId;

    private LocalDate settlementDate;

    private Long grossAmount;

    @Column(length = 500, nullable = false)
    private String reason;

    public static SettlementSkipLog of(Long paymentId, Long sellerId, Long eventId,
                                       LocalDate settlementDate, Long grossAmount, String reason) {
        return SettlementSkipLog.builder()
                .paymentId(paymentId)
                .sellerId(sellerId)
                .eventId(eventId)
                .settlementDate(settlementDate)
                .grossAmount(grossAmount)
                .reason(reason)
                .build();
    }
}
