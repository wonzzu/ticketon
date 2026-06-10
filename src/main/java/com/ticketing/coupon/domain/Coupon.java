package com.ticketing.coupon.domain;

import com.ticketing.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private Integer discountValue;

    @Column(nullable = false)
    private Integer totalQuantity;

    public static Coupon create(String name, DiscountType discountType, int value, int quantity) {
        return Coupon.builder()
                .name(name)
                .discountType(discountType)
                .discountValue(value)
                .totalQuantity(quantity)
                .build();
    }
}
