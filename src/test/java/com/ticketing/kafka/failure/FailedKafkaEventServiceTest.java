package com.ticketing.kafka.failure;

import com.ticketing.global.exception.BaseException;
import com.ticketing.kafka.failure.domain.FailedKafkaEvent;
import com.ticketing.kafka.failure.messaging.KafkaFailedEventReprocessor;
import com.ticketing.kafka.failure.repository.FailedKafkaEventRepository;
import com.ticketing.kafka.failure.service.FailedKafkaEventService;
import com.ticketing.outbox.domain.OutboxConsumerType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static com.ticketing.global.baseresponse.BaseResponseStatus.DUPLICATE_REQUEST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FailedKafkaEventServiceTest {

    @Mock
    private FailedKafkaEventRepository failedKafkaEventRepository;

    @Mock
    private KafkaFailedEventReprocessor failedEventReprocessor;

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

    @Test
    void 재처리_선점에_성공한_이벤트만_다시_발행한다() {
        FailedKafkaEvent failedEvent = createFailedEvent();
        when(failedKafkaEventRepository.tryStartReprocessing(
                eq(1L),
                any()
        )).thenReturn(1);
        when(failedKafkaEventRepository.findById(1L)).thenReturn(Optional.of(failedEvent));
        when(failedKafkaEventRepository.markResolved(1L)).thenReturn(1);

        failedKafkaEventService.reprocess(1L);

        verify(failedEventReprocessor).reprocess(failedEvent);
        verify(failedKafkaEventRepository).markResolved(1L);
    }

    @Test
    void 이미_선점된_이벤트는_중복_발행하지_않는다() {
        when(failedKafkaEventRepository.tryStartReprocessing(eq(1L), any()))
                .thenReturn(0);

        BaseException exception = assertThrows(
                BaseException.class,
                () -> failedKafkaEventService.reprocess(1L)
        );

        assertThat(exception.getBaseResponseStatus()).isEqualTo(DUPLICATE_REQUEST);
        verify(failedEventReprocessor, never()).reprocess(any());
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
