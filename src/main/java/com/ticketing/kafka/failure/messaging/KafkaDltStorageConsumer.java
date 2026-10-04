package com.ticketing.kafka.failure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.kafka.failure.domain.FailedKafkaEvent;
import com.ticketing.kafka.failure.service.FailedKafkaEventService;
import com.ticketing.outbox.domain.OutboxConsumerType;
import com.ticketing.outbox.dto.EventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaDltStorageConsumer {

    private static final String REAGGREGATION_DLT = ".reaggregation.DLT";
    private static final String NOTIFICATION_DLT = ".notification.DLT";
    private static final String TICKET_ISSUANCE_DLT = ".ticket-issuance.DLT";
    private static final String ANALYTICS_DLT = ".analytics.DLT";
    private static final String FUNNEL_DLT = ".funnel.DLT";

    private final ObjectMapper objectMapper;
    private final FailedKafkaEventService failedKafkaEventService;

    @KafkaListener(
            topics = {
                    "${app.kafka.topic.payment-events}.reaggregation.DLT",
                    "${app.kafka.topic.payment-events}.notification.DLT",
                    "${app.kafka.topic.payment-events}.ticket-issuance.DLT",
                    "${app.kafka.topic.payment-events}.funnel.DLT",
                    "${app.kafka.topic.queue-events}.analytics.DLT",
                    "${app.kafka.topic.queue-events}.funnel.DLT",
                    "${app.kafka.topic.reservation-events}.funnel.DLT"
            },
            groupId = "ticketon-dlt-storage",
            containerFactory = "dltStorageKafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> record) {
        FailedKafkaEvent failedEvent = FailedKafkaEvent.create(
                extractEventId(record.value()),
                resolveConsumerType(record.topic()),
                readRequiredStringHeader(record, KafkaHeaders.DLT_ORIGINAL_TOPIC),
                readIntHeader(record, KafkaHeaders.DLT_ORIGINAL_PARTITION),
                readLongHeader(record, KafkaHeaders.DLT_ORIGINAL_OFFSET),
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                record.value(),
                readStringHeader(record, KafkaHeaders.DLT_EXCEPTION_FQCN),
                readStringHeader(record, KafkaHeaders.DLT_EXCEPTION_MESSAGE)
        );

        failedKafkaEventService.saveIfAbsent(failedEvent);

        log.warn(
                "Kafka DLT 이벤트 저장: dltTopic={}, partition={}, offset={}, eventId={}, consumerType={}",
                record.topic(),
                record.partition(),
                record.offset(),
                failedEvent.getEventId(),
                failedEvent.getConsumerType()
        );
    }

    private String extractEventId(String message) {
        try {
            return objectMapper.readValue(message, EventEnvelope.class).eventId();
        } catch (Exception e) {
            return null;
        }
    }

    private OutboxConsumerType resolveConsumerType(String dltTopic) {
        if (dltTopic.endsWith(REAGGREGATION_DLT)) {
            return OutboxConsumerType.REAGGREGATION;
        }
        if (dltTopic.endsWith(NOTIFICATION_DLT)) {
            return OutboxConsumerType.NOTIFICATION;
        }
        if (dltTopic.endsWith(TICKET_ISSUANCE_DLT)) {
            return OutboxConsumerType.TICKET_ISSUANCE;
        }
        if (dltTopic.endsWith(ANALYTICS_DLT)) {
            return OutboxConsumerType.QUEUE_ANALYTICS;
        }
        if (dltTopic.endsWith(FUNNEL_DLT)) {
            return OutboxConsumerType.FUNNEL_ANALYTICS;
        }

        throw new IllegalArgumentException("지원하지 않는 Kafka DLT Topic입니다: " + dltTopic);
    }

    private String readRequiredStringHeader(
            ConsumerRecord<String, String> record,
            String headerName
    ) {
        Header header = requireHeader(record, headerName);
        return new String(header.value(), StandardCharsets.UTF_8);
    }

    private String readStringHeader(
            ConsumerRecord<String, String> record,
            String headerName
    ) {
        Header header = record.headers().lastHeader(headerName);
        if (header == null) {
            return null;
        }

        return new String(header.value(), StandardCharsets.UTF_8);
    }

    private int readIntHeader(
            ConsumerRecord<String, String> record,
            String headerName
    ) {
        return ByteBuffer.wrap(requireHeader(record, headerName).value()).getInt();
    }

    private long readLongHeader(
            ConsumerRecord<String, String> record,
            String headerName
    ) {
        return ByteBuffer.wrap(requireHeader(record, headerName).value()).getLong();
    }

    private Header requireHeader(
            ConsumerRecord<String, String> record,
            String headerName
    ) {
        Header header = record.headers().lastHeader(headerName);
        if (header == null) {
            throw new IllegalArgumentException("Kafka DLT 필수 Header가 없습니다: " + headerName);
        }

        return header;
    }
}
