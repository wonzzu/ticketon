package com.ticketing.member.controller;

import com.ticketing.auth.CustomUserDetails;
import com.ticketing.event.dto.response.EventListResponseDto;
import com.ticketing.event.dto.response.EventResponseDto;
import com.ticketing.event.service.EventService;
import com.ticketing.global.BaseResponse;
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

    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<Void>> signup(@Validated @RequestBody SellerSignupDto dto) {
        sellerService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<SellerResponseDto>> getSeller(@PathVariable Long id) {
        return ResponseEntity.ok(BaseResponse.success(sellerService.findById(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> update(@PathVariable Long id, @Validated @RequestBody SellerUpdateDto dto) {
        sellerService.update(id, dto);
        return ResponseEntity.ok(BaseResponse.success());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        sellerService.delete(id);
        return ResponseEntity.ok(BaseResponse.success());
    }

    // ── 셀러 마이페이지 ─────────────────────────────────────

    @GetMapping("/me/events")
    public ResponseEntity<BaseResponse<List<EventListResponseDto>>> findMyEvents(
            @AuthenticationPrincipal CustomUserDetails seller) {
        return ResponseEntity.ok(BaseResponse.success(eventService.findBySeller(seller.getMemberId())));
    }

    @GetMapping("/me/events/{id}")
    public ResponseEntity<BaseResponse<EventResponseDto>> findMyEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails seller) {
        return ResponseEntity.ok(BaseResponse.success(
                eventService.findBySeller(id, seller.getMemberId())));
    }
}
