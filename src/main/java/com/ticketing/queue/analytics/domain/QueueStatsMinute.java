package com.ticketing.queue.analytics.domain;

import com.ticketing.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "queue_stats_minute",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_queue_stats_minute",
                columnNames = {"schedule_id", "bucket_time", "partition_no"}
        )
)
public class QueueStatsMinute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "bucket_time", nullable = false)
    private LocalDateTime bucketTime;

    @Column(name = "partition_no", nullable = false)
    private int partitionNo;

    @Column(nullable = false)
    private long enteredCount;

    @Column(nullable = false)
    private long admittedCount;

    @Column(nullable = false)
    private long totalWaitMs;

    @Column(nullable = false)
    private long maxWaitMs;

    @Column(nullable = false)
    private long waitUnder10sCount;

    @Column(nullable = false)
    private long wait10To30sCount;

    @Column(nullable = false)
    private long wait30To60sCount;

    @Column(nullable = false)
    private long wait60To180sCount;

    @Column(nullable = false)
    private long waitOver180sCount;

    public static QueueStatsMinute create(Long scheduleId, LocalDateTime bucketTime, int partitionNo) {
        return QueueStatsMinute.builder()
                .scheduleId(scheduleId)
                .bucketTime(bucketTime)
                .partitionNo(partitionNo)
                .enteredCount(0)
                .admittedCount(0)
                .totalWaitMs(0)
                .maxWaitMs(0)
                .waitUnder10sCount(0)
                .wait10To30sCount(0)
                .wait30To60sCount(0)
                .wait60To180sCount(0)
                .waitOver180sCount(0)
                .build();
    }

    public void recordEntered() {
        enteredCount++;
    }

    public void recordAdmitted(long waitMs) {
        admittedCount++;
        totalWaitMs += waitMs;
        maxWaitMs = Math.max(maxWaitMs, waitMs);

        if (waitMs < 10_000) {
            waitUnder10sCount++;
        } else if (waitMs < 30_000) {
            wait10To30sCount++;
        } else if (waitMs < 60_000) {
            wait30To60sCount++;
        } else if (waitMs < 180_000) {
            wait60To180sCount++;
        } else {
            waitOver180sCount++;
        }
    }
}
