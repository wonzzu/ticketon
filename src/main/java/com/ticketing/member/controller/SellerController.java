package com.ticketing.member.controller;

import com.ticketing.auth.CustomUserDetails;
import com.ticketing.event.dto.response.EventListResponseDto;
import com.ticketing.event.dto.response.EventResponseDto;
import com.ticketing.event.service.EventService;
import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.global.ratelimit.RateLimit;
import com.ticketing.member.dto.request.SellerSignupDto;
import com.ticketing.member.dto.request.SellerUpdateDto;
import com.ticketing.member.dto.response.SellerResponseDto;
import com.ticketing.member.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sellers")
public class SellerController {

    private final SellerService sellerService;
    private final EventService eventService;

    @RateLimit(key = RateLimit.KeyType.IP, limit = 20, windowSeconds = 60)
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<Void>> signup(@Validated @RequestBody SellerSignupDto dto) {
        sellerService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success());
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<SellerResponseDto>> getSeller(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(BaseResponse.success(sellerService.findById(user.getMemberId())));
    }

    @PatchMapping("/me")
    public ResponseEntity<BaseResponse<Void>> update(@AuthenticationPrincipal CustomUserDetails user, @Validated @RequestBody SellerUpdateDto dto) {
        sellerService.update(user.getMemberId(), dto);
        return ResponseEntity.ok(BaseResponse.success());
    }

    @DeleteMapping("/me")
    public ResponseEntity<BaseResponse<Void>> delete(@AuthenticationPrincipal CustomUserDetails user) {
        sellerService.delete(user.getMemberId());
        return ResponseEntity.ok(BaseResponse.success());
    }

    // ── 셀러 마이페이지 ─────────────────────────────────────

    @GetMapping("/me/events")
    public ResponseEntity<BaseResponse<List<EventListResponseDto>>> findMyEvents(
            @AuthenticationPrincipal CustomUserDetails seller) {
        return ResponseEntity.ok(BaseResponse.success(eventService.findMyEvents(seller.getMemberId())));
    }

    @GetMapping("/me/events/{id}")
    public ResponseEntity<BaseResponse<EventResponseDto>> findMyEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails seller) {
        return ResponseEntity.ok(BaseResponse.success(
                eventService.findMyEventDetail(id, seller.getMemberId())));
    }
}
