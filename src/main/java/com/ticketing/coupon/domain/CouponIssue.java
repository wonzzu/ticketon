package com.ticketing.coupon.domain;


import com.ticketing.global.entity.BaseEntity;
import com.ticketing.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Table(
        name = "coupon_issue",
        uniqueConstraints = @UniqueConstraint(name = "uk_coupon_issue_coupon_member",
                columnNames = {"coupon_id", "member_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class CouponIssue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static CouponIssue create(Coupon coupon, Member member) {
        return CouponIssue.builder()
                .coupon(coupon)
                .member(member)
                .build();
    }
}
