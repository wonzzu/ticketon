package com.ticketing.queue.analytics.repository;

import com.ticketing.queue.analytics.domain.QueueStatsMinute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface QueueStatsMinuteRepository extends JpaRepository<QueueStatsMinute, Long> {

    Optional<QueueStatsMinute> findByScheduleIdAndBucketTimeAndPartitionNo(
            Long scheduleId,
            LocalDateTime bucketTime,
            int partitionNo
    );
}
