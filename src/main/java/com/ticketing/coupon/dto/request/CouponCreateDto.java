package com.ticketing.coupon.dto.request;

import com.ticketing.coupon.domain.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class CouponCreateDto {

    @NotBlank
    private String name;

    @NotNull
    private DiscountType discountType;

    @NotNull
    @Positive
    private Integer discountValue;

    @NotNull
    @Positive
    private Integer totalQuantity;


}
