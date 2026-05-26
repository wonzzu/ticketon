package com.ticketing.event.domain;

import com.ticketing.global.entity.BaseEntity;
import com.ticketing.member.domain.AdminMember;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private AdminMember reviewer;

    public static EventReviewHistory of(Event event, ReviewAction action, EventStatus prev, EventStatus next,
                                        String reason, AdminMember reviewer) {

        return EventReviewHistory.builder()
                .event(event)
                .action(action)
                .previousStatus(prev)
                .newStatus(next)
                .reason(reason)
                .reviewer(reviewer)
                .build();
    }
}
