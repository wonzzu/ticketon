package com.ticketing.review.domain;


import com.ticketing.event.domain.Event;
import com.ticketing.global.BaseResponseStatus;
import com.ticketing.global.entity.BaseEntity;
import com.ticketing.global.exception.BaseException;
import com.ticketing.member.domain.Member;
import com.ticketing.member.domain.NormalMember;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

import static com.ticketing.global.BaseResponseStatus.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_review_member_event",
        columnNames = {"member_id", "event_id"}
))
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@SQLRestriction("deleted_at IS NULL")
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column
    private LocalDateTime deletedAt;

    public static Review create(Event event, Member member, int rating, String content) {
        validateRating(rating);
        return Review.builder()
                .event(event)
                .member(member)
                .rating(rating)
                .content(content)
                .build();
    }

    private static void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new BaseException(INVALID_REVIEW_RATING);
        }
    }

    public boolean isOwnedBy(Long memberId) {
        return this.member.getId().equals(memberId);
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

}
