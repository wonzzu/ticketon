package com.ticketing.kafka.failure.repository;

import com.ticketing.kafka.failure.domain.FailedKafkaEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailedKafkaEventRepository extends JpaRepository<FailedKafkaEvent, Long> {

    boolean existsByDltTopicAndDltPartitionAndDltOffset(
            String dltTopic,
            int dltPartition,
            long dltOffset
    );
}
