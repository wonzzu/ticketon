package com.ticketing.review.controller;


import com.ticketing.auth.CustomUserDetails;
import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.review.dto.request.ReviewCreateDto;
import com.ticketing.review.dto.response.MyReviewResponseDto;
import com.ticketing.review.dto.response.ReviewListResponseDto;
import com.ticketing.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "리뷰")
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "공연 리뷰 목록")
    @GetMapping("/events/{eventId}/reviews")
    public ResponseEntity<BaseResponse<ReviewListResponseDto>> findAll(
            @PathVariable Long eventId,
            @RequestParam(name = "sortType", defaultValue = "latest") String sort,
            @PageableDefault(size = 10) Pageable pageable
    ) {

        ReviewListResponseDto reviewDto = reviewService.findByEvent(eventId, sort,pageable);

        return ResponseEntity.ok(BaseResponse.success(reviewDto));
    }

    @Operation(summary = "리뷰 작성")
    @PostMapping("/events/{eventId}/reviews")
    public ResponseEntity<BaseResponse<Void>> create(
            @PathVariable Long eventId,
            @Validated @RequestBody ReviewCreateDto dto,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        reviewService.create(eventId, user.getMemberId(), dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success());
    }

    @Operation(summary = "리뷰 삭제")
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<BaseResponse<Void>> delete(
            @PathVariable Long reviewId, @AuthenticationPrincipal CustomUserDetails user
    ) {
        reviewService.delete(reviewId, user.getMemberId());
        return ResponseEntity.ok(BaseResponse.success());
    }

    @Operation(summary = "내 리뷰 목록")
    @GetMapping("/me/reviews")
    public ResponseEntity<BaseResponse<List<MyReviewResponseDto>>> findMyReviews(@AuthenticationPrincipal CustomUserDetails user) {
        List<MyReviewResponseDto> myReviews = reviewService.findMyReviews(user.getMemberId());

        return ResponseEntity.ok(BaseResponse.success(myReviews));
    }
}
