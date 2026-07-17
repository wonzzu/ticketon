package com.ticketing.settlement.domain;

import com.ticketing.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLUpdate;

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

    private Long sellerId;

    private Long eventId;

    private LocalDate settlementDate;

    private Long grossAmount;

    @Column(length = 500, nullable = false)
    private String reason;


    public static SettlementSkipLog of(Long sellerId, Long eventId, LocalDate settlementDate, Long grossAmount, String reason) {

        return SettlementSkipLog.builder()
                .sellerId(sellerId)
                .eventId(eventId)
                .settlementDate(settlementDate)
                .grossAmount(grossAmount)
                .reason(reason)
                .build();
    }
}
