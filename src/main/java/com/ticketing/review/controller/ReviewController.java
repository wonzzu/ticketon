package com.ticketing.review.controller;


import com.ticketing.auth.CustomUserDetails;
import com.ticketing.global.BaseResponse;
import com.ticketing.review.dto.request.ReviewCreateDto;
import com.ticketing.review.dto.response.ReviewListResponseDto;
import com.ticketing.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/events/{eventId}/reviews")
    public ResponseEntity<BaseResponse<ReviewListResponseDto>> findAll(
            @PathVariable Long eventId, @RequestParam(defaultValue = "latest") String sort
    ) {
        ReviewListResponseDto reviewDto = reviewService.findByEvent(eventId, sort);

        return ResponseEntity.ok(BaseResponse.success(reviewDto));
    }

    @PostMapping("/events/{eventId}/reviews")
    public ResponseEntity<BaseResponse<Void>> create(
            @PathVariable Long eventId,
            @Validated @RequestBody ReviewCreateDto dto,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        reviewService.create(eventId, user.getMemberId(), dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success());
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<BaseResponse<Void>> delete(
            @PathVariable Long reviewId, @AuthenticationPrincipal CustomUserDetails user
    ) {
        reviewService.delete(reviewId, user.getMemberId());
        return ResponseEntity.ok(BaseResponse.success());
    }
}
