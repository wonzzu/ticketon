package com.ticketing.coupon.dto.response;

import com.ticketing.coupon.domain.Coupon;
import com.ticketing.coupon.domain.DiscountType;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CouponResponseDto {

    private Long id;
    private String name;
    private DiscountType discountType;
    private Integer discountValue;

    public static CouponResponseDto from(Coupon coupon) {
        return CouponResponseDto.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .build();
    }
}
