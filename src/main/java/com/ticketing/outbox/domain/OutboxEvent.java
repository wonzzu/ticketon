package com.ticketing.outbox.domain;

import com.ticketing.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_outbox_message_id",
                        columnNames = "message_id"
                ),
                @UniqueConstraint(
                        name = "uk_outbox_aggregate_sequence",
                        columnNames = {"aggregate_type", "aggregate_id", "event_sequence"}
                )
        }
)
public class OutboxEvent extends BaseEntity {

    private static final int INITIAL_EVENT_VERSION = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false, length = 36)
    private String messageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OutboxEventType eventType;

    @Column(name = "event_version", nullable = false)
    private Integer eventVersion;

    @Column(nullable = false, length = 50)
    private String aggregateType;

    @Column(nullable = false)
    private Long aggregateId;

    @Column(name = "event_sequence", nullable = false)
    private Long eventSequence;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxEventStatus status;

    @Column(length = 100)
    private String claimedBy;

    private LocalDateTime claimedAt;

    public static OutboxEvent reservationCreated(Long reservationId, String payload) {
        return create(
                "RESERVATION",
                reservationId,
                1L,
                OutboxEventType.RESERVATION_CREATED,
                payload
        );
    }

    public static OutboxEvent paymentCompleted(Long paymentId, long eventSequence, String payload) {
        return create(
                "PAYMENT",
                paymentId,
                eventSequence,
                OutboxEventType.PAYMENT_COMPLETED,
                payload
        );
    }

    public static OutboxEvent paymentCanceled(Long paymentId, long eventSequence, String payload) {
        return create(
                "PAYMENT",
                paymentId,
                eventSequence,
                OutboxEventType.PAYMENT_CANCELED,
                payload
        );
    }

    private static OutboxEvent create(
            String aggregateType,
            Long aggregateId,
            long eventSequence,
            OutboxEventType eventType,
            String payload
    ) {
        return OutboxEvent.builder()
                .messageId(UUID.randomUUID().toString())
                .eventType(eventType)
                .eventVersion(INITIAL_EVENT_VERSION)
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventSequence(eventSequence)
                .payload(payload)
                .status(OutboxEventStatus.PENDING)
                .build();
    }
}
