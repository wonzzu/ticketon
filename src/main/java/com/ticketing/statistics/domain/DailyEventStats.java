package com.ticketing.statistics.domain;

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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"stat_date", "event_id"}))
public class DailyEventStats extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate statDate;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Long orderCount;

    public static DailyEventStats of(LocalDate statDate, Long eventId, long orderCount) {
        return DailyEventStats.builder()
                .statDate(statDate)
                .eventId(eventId)
                .orderCount(orderCount)
                .build();
    }
}
