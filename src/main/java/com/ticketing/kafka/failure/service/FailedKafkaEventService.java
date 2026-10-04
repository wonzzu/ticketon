package com.ticketing.kafka.failure.service;

import com.ticketing.kafka.failure.domain.FailedKafkaEvent;
import com.ticketing.kafka.failure.repository.FailedKafkaEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FailedKafkaEventService {

    private final FailedKafkaEventRepository failedKafkaEventRepository;

    @Transactional
    public void saveIfAbsent(FailedKafkaEvent failedEvent) {
        boolean alreadySaved = failedKafkaEventRepository
                .existsByDltTopicAndDltPartitionAndDltOffset(
                        failedEvent.getDltTopic(),
                        failedEvent.getDltPartition(),
                        failedEvent.getDltOffset()
                );

        if (alreadySaved) {
            return;
        }

        failedKafkaEventRepository.save(failedEvent);
    }
}
