package com.ticketing.coupon.controller;

import com.ticketing.auth.CustomUserDetails;
import com.ticketing.coupon.dto.request.CouponCreateDto;
import com.ticketing.coupon.dto.response.CouponResponseDto;
import com.ticketing.coupon.dto.response.MyCouponResponseDto;
import com.ticketing.coupon.service.CouponService;
import com.ticketing.global.baseresponse.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "쿠폰")
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

        couponService.issue(id, user.getMemberId());
        return ResponseEntity.ok(BaseResponse.success());
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<CouponResponseDto>>> findAll() {
        List<CouponResponseDto> all = couponService.findAll();
        return ResponseEntity.ok(BaseResponse.success(all));
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<List<MyCouponResponseDto>>> findMyCoupon(@AuthenticationPrincipal CustomUserDetails user) {

        List<MyCouponResponseDto> myCoupon = couponService.findMyCoupon(user.getMemberId());

        return ResponseEntity.ok(BaseResponse.success(myCoupon));
    }
}
