package com.ticketing.review.service;

import com.ticketing.event.domain.Event;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.global.exception.BaseException;
import com.ticketing.member.domain.Member;
import com.ticketing.member.repository.MemberRepository;
import com.ticketing.review.domain.Review;
import com.ticketing.review.dto.request.ReviewCreateDto;
import com.ticketing.review.dto.response.ReviewListResponseDto;
import com.ticketing.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ticketing.global.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void create(Long eventId, Long memberId, ReviewCreateDto dto) {
        if (reviewRepository.existsByEventIdAndMemberId(eventId, memberId)) {
            throw new BaseException(DUPLICATE_REVIEW);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BaseException(PERFORMANCE_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        Review review = Review.create(event, member, dto.getRating(), dto.getContent());

        reviewRepository.save(review);
    }

    public ReviewListResponseDto findByEvent(Long eventId, String sort) {
        List<Review> reviews = "rating".equals(sort)
                ? reviewRepository.findByEventIdOrderByRatingDesc(eventId)
                : reviewRepository.findByEventIdOrderByCreatedAtDesc(eventId);

        long count = reviewRepository.countByEventId(eventId);
        Double averageRating = reviewRepository.findAverageRating(eventId);

        return ReviewListResponseDto.of(count, averageRating, reviews);
    }

    @Transactional
    public void delete(Long reviewId, Long memberId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BaseException(REVIEW_NOT_FOUND));

        if (!review.isOwnedBy(memberId)) {
            throw new BaseException(REVIEW_NOT_OWNED);
        }

        review.delete();

    }
}
