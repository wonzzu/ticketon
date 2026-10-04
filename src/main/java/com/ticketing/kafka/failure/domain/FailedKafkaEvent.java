package com.ticketing.kafka.failure.domain;

import com.ticketing.global.entity.BaseEntity;
import com.ticketing.outbox.domain.OutboxConsumerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_failed_kafka_event_dlt_position",
                columnNames = {"dlt_topic", "dlt_partition", "dlt_offset"}
        )
)
public class FailedKafkaEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", length = 36)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "consumer_type", nullable = false, length = 30)
    private OutboxConsumerType consumerType;

    @Column(name = "original_topic", nullable = false, length = 200)
    private String originalTopic;

    @Column(name = "original_partition", nullable = false)
    private int originalPartition;

    @Column(name = "original_offset", nullable = false)
    private long originalOffset;

    @Column(name = "dlt_topic", nullable = false, length = 200)
    private String dltTopic;

    @Column(name = "dlt_partition", nullable = false)
    private int dltPartition;

    @Column(name = "dlt_offset", nullable = false)
    private long dltOffset;

    @Column(name = "message_key", length = 500)
    private String messageKey;

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;

    @Column(name = "exception_class", length = 500)
    private String exceptionClass;

    @Column(name = "exception_message", length = 2000)
    private String exceptionMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FailedKafkaEventStatus status;

    @Column(name = "reprocess_count", nullable = false)
    private int reprocessCount;

    @Column(name = "last_reprocessed_at")
    private LocalDateTime lastReprocessedAt;

    public static FailedKafkaEvent create(
            String eventId,
            OutboxConsumerType consumerType,
            String originalTopic,
            int originalPartition,
            long originalOffset,
            String dltTopic,
            int dltPartition,
            long dltOffset,
            String messageKey,
            String payload,
            String exceptionClass,
            String exceptionMessage
    ) {
        return FailedKafkaEvent.builder()
                .eventId(eventId)
                .consumerType(consumerType)
                .originalTopic(originalTopic)
                .originalPartition(originalPartition)
                .originalOffset(originalOffset)
                .dltTopic(dltTopic)
                .dltPartition(dltPartition)
                .dltOffset(dltOffset)
                .messageKey(messageKey)
                .payload(payload)
                .exceptionClass(exceptionClass)
                .exceptionMessage(exceptionMessage)
                .status(FailedKafkaEventStatus.PENDING)
                .reprocessCount(0)
                .build();
    }
}
