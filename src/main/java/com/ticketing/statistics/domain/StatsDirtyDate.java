package com.ticketing.statistics.domain;

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
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_stats_dirty_date", columnNames = "stat_date"))
public class StatsDirtyDate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate statDate;

    public static StatsDirtyDate of(LocalDate statDate) {
        return StatsDirtyDate.builder()
                .statDate(statDate)
                .build();
    }
}
