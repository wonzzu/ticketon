package com.ticketing.kafka.failure.repository;

import com.ticketing.kafka.failure.domain.FailedKafkaEvent;
import com.ticketing.kafka.failure.dto.request.FailedKafkaEventSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FailedKafkaEventRepositoryCustom {

    Page<FailedKafkaEvent> search(
            FailedKafkaEventSearchCondition condition,
            Pageable pageable
    );
}
