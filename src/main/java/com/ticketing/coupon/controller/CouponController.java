package com.ticketing.coupon.controller;

import com.ticketing.auth.CustomUserDetails;
import com.ticketing.coupon.dto.request.CouponCreateDto;
import com.ticketing.coupon.service.CouponService;
import com.ticketing.global.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    public ResponseEntity<BaseResponse<Long>> create(@Validated @RequestBody CouponCreateDto dto) {

        Long coupon = couponService.createCoupon(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(coupon));
    }


    @PostMapping("/{id}/issue")
    public ResponseEntity<BaseResponse<Void>> issue(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user) {

        couponService.issue(id,user.getMemberId());
        return ResponseEntity.ok(BaseResponse.success());
    }
}
