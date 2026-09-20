package com.ticketing.outbox.service;

import com.ticketing.outbox.domain.OutboxEvent;
import com.ticketing.outbox.domain.OutboxEventStatus;
import com.ticketing.outbox.messaging.OutboxMessagePublisher;
import com.ticketing.outbox.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxRelayService {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxMessagePublisher messagePublisher;
    private final int batchSize;
    private final Duration leaseTimeout;
    private final String workerId;

    public OutboxRelayService(OutboxEventRepository outboxEventRepository,
                              OutboxMessagePublisher messagePublisher,
                              @Value("${outbox.relay.batch-size:20}") int batchSize,
                              @Value("${outbox.relay.lease-timeout:30s}") Duration leaseTimeout,
                              @Value("${outbox.relay.worker-id:${random.uuid}}") String workerId) {
        this.outboxEventRepository = outboxEventRepository;
        this.messagePublisher = messagePublisher;
        this.batchSize = batchSize;
        this.leaseTimeout = leaseTimeout;
        this.workerId = workerId;
    }

    public int publishPending() {
        LocalDateTime now = LocalDateTime.now();
        outboxEventRepository.releaseExpiredClaims(now.minus(leaseTimeout));

        List<OutboxEvent> pendingEvents = outboxEventRepository
                .findByStatusOrderByIdAsc(
                        OutboxEventStatus.PENDING,
                        PageRequest.of(0, batchSize)
                );

        int publishedCount = 0;
        for (OutboxEvent pendingEvent : pendingEvents) {
            int claimed = outboxEventRepository.tryClaim(
                    pendingEvent.getId(), workerId, LocalDateTime.now());
            if (claimed == 0) {
                continue;
            }

            try {
                messagePublisher.publish(pendingEvent);
                if (outboxEventRepository.markPublished(pendingEvent.getId(), workerId) == 1) {
                    publishedCount++;
                }
            } catch (RuntimeException e) {
                outboxEventRepository.releaseClaim(pendingEvent.getId(), workerId);
                throw e;
            }
        }

        return publishedCount;
    }
}
