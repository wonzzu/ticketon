package com.ticketing.kafka.failure.repository;

import com.ticketing.kafka.failure.domain.FailedKafkaEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface FailedKafkaEventRepository
        extends JpaRepository<FailedKafkaEvent, Long>, FailedKafkaEventRepositoryCustom {

    boolean existsByDltTopicAndDltPartitionAndDltOffset(
            String dltTopic,
            int dltPartition,
            long dltOffset
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update FailedKafkaEvent event
               set event.status = com.ticketing.kafka.failure.domain.FailedKafkaEventStatus.REPROCESSING,
                   event.reprocessCount = event.reprocessCount + 1,
                   event.lastReprocessedAt = :reprocessedAt
             where event.id = :id
               and event.status in (
                   com.ticketing.kafka.failure.domain.FailedKafkaEventStatus.PENDING,
                   com.ticketing.kafka.failure.domain.FailedKafkaEventStatus.FAILED
               )
            """)
    int tryStartReprocessing(
            @Param("id") Long id,
            @Param("reprocessedAt") LocalDateTime reprocessedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update FailedKafkaEvent event
               set event.status = com.ticketing.kafka.failure.domain.FailedKafkaEventStatus.RESOLVED
             where event.id = :id
               and event.status = com.ticketing.kafka.failure.domain.FailedKafkaEventStatus.REPROCESSING
            """)
    int markResolved(@Param("id") Long id);
}
