package com.ticketing.queue.analytics.service;

import com.ticketing.outbox.domain.OutboxConsumerType;
import com.ticketing.outbox.domain.ProcessedMessage;
import com.ticketing.outbox.exception.DuplicateMessageException;
import com.ticketing.outbox.repository.ProcessedMessageRepository;
import com.ticketing.queue.analytics.domain.QueueStatsMinute;
import com.ticketing.queue.analytics.dto.QueueAnalyticsEvent;
import com.ticketing.queue.analytics.repository.QueueStatsMinuteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QueueStatsAggregationService {

    private final QueueStatsMinuteRepository queueStatsRepository;
    private final ProcessedMessageRepository processedMessageRepository;

    @Transactional
    public void handle(String eventId, String eventType, int partitionNo, QueueAnalyticsEvent event) {
        registerProcessedMessage(eventId);

        LocalDateTime eventTime = resolveEventTime(eventType, event);
        LocalDateTime bucketTime = eventTime.withSecond(0).withNano(0);

        QueueStatsMinute stats = queueStatsRepository
                .findByScheduleIdAndBucketTimeAndPartitionNo(event.scheduleId(), bucketTime, partitionNo)
                .orElseGet(() -> QueueStatsMinute.create(event.scheduleId(), bucketTime, partitionNo));

        if ("QUEUE_ENTERED".equals(eventType)) {
            stats.recordEntered();
        } else if ("QUEUE_ADMITTED".equals(eventType)) {
            stats.recordAdmitted(calculateWaitMs(event));
        } else {
            throw new IllegalArgumentException("지원하지 않는 대기열 이벤트입니다: " + eventType);
        }

        queueStatsRepository.save(stats);
    }

    private void registerProcessedMessage(String eventId) {
        try {
            processedMessageRepository.saveAndFlush(
                    ProcessedMessage.of(OutboxConsumerType.QUEUE_ANALYTICS, eventId)
            );
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateMessageException(OutboxConsumerType.QUEUE_ANALYTICS.name(), eventId, e);
        }
    }

    private LocalDateTime resolveEventTime(String eventType, QueueAnalyticsEvent event) {
        if ("QUEUE_ENTERED".equals(eventType)) {
            if (event.enteredAt() == null) {
                throw new IllegalArgumentException("QUEUE_ENTERED에는 enteredAt이 필요합니다.");
            }
            return event.enteredAt();
        }

        if ("QUEUE_ADMITTED".equals(eventType)) {
            if (event.admittedAt() == null) {
                throw new IllegalArgumentException("QUEUE_ADMITTED에는 admittedAt이 필요합니다.");
            }
            return event.admittedAt();
        }

        throw new IllegalArgumentException("지원하지 않는 대기열 이벤트입니다: " + eventType);
    }

    private long calculateWaitMs(QueueAnalyticsEvent event) {
        if (event.enteredAt() == null || event.admittedAt() == null) {
            throw new IllegalArgumentException("대기시간 계산에는 enteredAt과 admittedAt이 필요합니다.");
        }

        long waitMs = Duration.between(event.enteredAt(), event.admittedAt()).toMillis();
        if (waitMs < 0) {
            throw new IllegalArgumentException("admittedAt은 enteredAt보다 빠를 수 없습니다.");
        }
        return waitMs;
    }
}
