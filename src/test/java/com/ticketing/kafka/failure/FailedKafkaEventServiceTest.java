package com.ticketing.kafka.failure;

import com.ticketing.kafka.failure.domain.FailedKafkaEvent;
import com.ticketing.kafka.failure.repository.FailedKafkaEventRepository;
import com.ticketing.kafka.failure.service.FailedKafkaEventService;
import com.ticketing.outbox.domain.OutboxConsumerType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FailedKafkaEventServiceTest {

    @Mock
    private FailedKafkaEventRepository failedKafkaEventRepository;

    @InjectMocks
    private FailedKafkaEventService failedKafkaEventService;

    @Test
    void 이미_저장된_DLT_위치라면_중복_저장하지_않는다() {
        FailedKafkaEvent failedEvent = createFailedEvent();
        when(failedKafkaEventRepository.existsByDltTopicAndDltPartitionAndDltOffset(
                failedEvent.getDltTopic(),
                failedEvent.getDltPartition(),
                failedEvent.getDltOffset()
        )).thenReturn(true);

        failedKafkaEventService.saveIfAbsent(failedEvent);

        verify(failedKafkaEventRepository, never()).save(failedEvent);
    }

    private FailedKafkaEvent createFailedEvent() {
        return FailedKafkaEvent.create(
                "3e6f2ed8-eefe-4fb1-9e92-4f79df5541a0",
                OutboxConsumerType.REAGGREGATION,
                "payment-events",
                1,
                15L,
                "payment-events.reaggregation.DLT",
                1,
                4L,
                "PAYMENT:1",
                "{\"eventType\":\"PAYMENT_CANCELED\"}",
                "java.lang.IllegalStateException",
                "테스트 처리 실패"
        );
    }
}
