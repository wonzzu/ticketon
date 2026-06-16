package com.ticketing.review.dto.response;

import com.ticketing.review.domain.Review;
import lombok.*;
import org.springframework.data.domain.Page;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReviewListResponseDto {

    private long reviewCount;
    private double avgRating;
    private Page<ReviewResponseDto> reviews;

    public static ReviewListResponseDto of(long count, Double avg, Page<Review> reviews) {
        return ReviewListResponseDto.builder()
                .reviewCount(count)
                .avgRating(avg == null ? 0.0 : Math.round(avg * 10) / 10.0)
                .reviews(reviews.map(ReviewResponseDto::from))
                .build();
    }
}
