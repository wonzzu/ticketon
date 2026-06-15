package com.ticketing.coupon.dto.response;

import com.ticketing.coupon.domain.Coupon;
import com.ticketing.coupon.domain.CouponIssue;
import com.ticketing.coupon.domain.DiscountType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MyCouponResponseDto {

    private Long couponId;
    private String name;
    private DiscountType discountType;
    private Integer discountValue;
    private LocalDateTime issueAt;

    public static MyCouponResponseDto from(CouponIssue issue) {
        Coupon coupon = issue.getCoupon();
        return MyCouponResponseDto.builder()
                .couponId(coupon.getId())
                .name(coupon.getName())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .issueAt(issue.getCreatedAt())
                .build();
    }
}
