package com.ticketing.kafka.failure.service;

import com.ticketing.global.exception.BaseException;
import com.ticketing.kafka.failure.domain.FailedKafkaEvent;
import com.ticketing.kafka.failure.dto.request.FailedKafkaEventSearchCondition;
import com.ticketing.kafka.failure.dto.response.FailedKafkaEventResponse;
import com.ticketing.kafka.failure.messaging.KafkaFailedEventReprocessor;
import com.ticketing.kafka.failure.repository.FailedKafkaEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.ticketing.global.baseresponse.BaseResponseStatus.DUPLICATE_REQUEST;

@Service
@RequiredArgsConstructor
public class FailedKafkaEventService {

    private final FailedKafkaEventRepository failedKafkaEventRepository;
    private final KafkaFailedEventReprocessor failedEventReprocessor;

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

    @Transactional(readOnly = true)
    public Page<FailedKafkaEventResponse> search(
            FailedKafkaEventSearchCondition condition,
            Pageable pageable
    ) {
        return failedKafkaEventRepository.search(condition, pageable)
                .map(FailedKafkaEventResponse::from);
    }

    @Transactional
    public void reprocess(Long failedEventId) {
        int claimed = failedKafkaEventRepository.tryStartReprocessing(
                failedEventId,
                LocalDateTime.now()
        );

        if (claimed == 0) {
            throw new BaseException(DUPLICATE_REQUEST);
        }

        FailedKafkaEvent failedEvent = failedKafkaEventRepository.findById(failedEventId)
                .orElseThrow(() -> new IllegalArgumentException("Kafka 실패 이벤트를 찾을 수 없습니다."));

        failedEventReprocessor.reprocess(failedEvent);

        if (failedKafkaEventRepository.markRepublished(failedEventId) != 1) {
            throw new IllegalStateException("Kafka 실패 이벤트 재발행 완료 상태를 저장하지 못했습니다.");
        }
    }
}
