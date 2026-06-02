package com.ticketing.review.dto.response;

import com.ticketing.review.domain.Review;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MyReviewResponseDto {

    private Long id;
    private Long eventId;
    private String eventTitle;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;

    public static MyReviewResponseDto from(Review review) {

        return MyReviewResponseDto.builder()
                .id(review.getId())
                .eventId(review.getEvent().getId())
                .eventTitle(review.getEvent().getTitle())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
