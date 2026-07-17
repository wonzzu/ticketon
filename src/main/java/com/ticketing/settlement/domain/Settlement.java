package com.ticketing.settlement.domain;


import com.ticketing.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_settlement_seller_event_date",
                columnNames = {"seller_id", "event_id", "settlement_date"}
        )
)
public class Settlement extends BaseEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private LocalDate settlementDate;

    @Column(nullable = false)
    private Long grossAmount;

    @Column(nullable = false)
    private Long commission;

    @Column(nullable = false)
    private Long netAmount;

    public static Settlement of(Long sellerId, Long eventId, LocalDate settlementDate, long grossAmount,long commission,long netAmount) {

        return Settlement.builder()
                .sellerId(sellerId)
                .eventId(eventId)
                .settlementDate(settlementDate)
                .grossAmount(grossAmount)
                .commission(commission)
                .netAmount(netAmount)
                .build();
    }


}
