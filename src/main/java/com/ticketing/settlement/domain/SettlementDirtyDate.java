package com.ticketing.settlement.domain;

import com.ticketing.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_dirty_seller_event_date",
                columnNames = {"seller_id", "event_id", "settlement_date"}
        )
)
public class SettlementDirtyDate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private LocalDate settlementDate;

    public static SettlementDirtyDate of(Long sellerId, Long eventId, LocalDate settlementDate) {
        return SettlementDirtyDate.builder()
                .sellerId(sellerId)
                .eventId(eventId)
                .settlementDate(settlementDate)
                .build();
    }
}