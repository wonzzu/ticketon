package com.ticketing.kafka.failure.messaging;

import com.ticketing.kafka.failure.domain.FailedKafkaEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class KafkaFailedEventReprocessor {

    private static final long SEND_TIMEOUT_SECONDS = 5L;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void reprocess(FailedKafkaEvent event) {
        ProducerRecord<String, String> record = new ProducerRecord<>(
                event.getOriginalTopic(),
                event.getOriginalPartition(),
                event.getMessageKey(),
                event.getPayload()
        );

        try {
            kafkaTemplate.send(record).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kafka 실패 이벤트 재발행 대기 중 중단됐습니다.", e);
        } catch (Exception e) {
            throw new IllegalStateException("Kafka 실패 이벤트 재발행에 실패했습니다.", e);
        }
    }
}
