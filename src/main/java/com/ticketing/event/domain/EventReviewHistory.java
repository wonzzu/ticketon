package com.ticketing.event.domain;

import com.ticketing.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class EventReviewHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus newStatus;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false)
    private Long reviewerId;

    public static EventReviewHistory of(Long eventId, ReviewAction action, EventStatus prev, EventStatus next,
                                        String reason, Long reviewerId) {

        return EventReviewHistory.builder()
                .eventId(eventId)
                .action(action)
                .previousStatus(prev)
                .newStatus(next)
                .reason(reason)
                .reviewerId(reviewerId)
                .build();
    }
}
