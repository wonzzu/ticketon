package com.ticketing.coupon.service;

import com.ticketing.coupon.domain.Coupon;
import com.ticketing.coupon.domain.CouponIssue;
import com.ticketing.coupon.domain.DiscountType;
import com.ticketing.coupon.repository.CouponIssueRepository;
import com.ticketing.coupon.repository.CouponRepository;
import com.ticketing.global.entity.Address;
import com.ticketing.member.domain.Gender;
import com.ticketing.member.domain.NormalMember;
import com.ticketing.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CouponRollbackCompensationTest {

    @Autowired CouponService couponService;
    @Autowired CouponRepository couponRepository;
    @Autowired CouponIssueRepository couponIssueRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired StringRedisTemplate redis;

    private Long couponId;
    private Long memberId;

    @BeforeEach
    void setUp() {
        NormalMember member = NormalMember.create(
                "coupon-rollback@test.com", "pw", "회원", "coupon-member",
                LocalDate.of(2000, 1, 1), Gender.MALE, "01000000000",
                new Address("city", "street", "00000")
        );
        memberRepository.save(member);

        Coupon coupon = couponRepository.save(
                Coupon.create("보상 테스트 쿠폰", DiscountType.FIXED, 5_000, 1)
        );

        couponIssueRepository.saveAndFlush(CouponIssue.create(coupon, member));

        couponId = coupon.getId();
        memberId = member.getId();

        redis.opsForValue().set(stockKey(), "1");
        redis.delete(issuedKey());
    }

    @AfterEach
    void cleanUpRedis() {
        redis.delete(stockKey());
        redis.delete(issuedKey());
    }

    @Test
    void DB저장이_롤백되면_Redis발급기록과_재고를_복구한다() {
        assertThatThrownBy(() -> couponService.issue(couponId, memberId))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(redis.opsForValue().get(stockKey())).isEqualTo("1");
        assertThat(redis.opsForSet().isMember(issuedKey(), memberId.toString())).isFalse();
        assertThat(couponIssueRepository.count()).isEqualTo(1L);
    }

    private String stockKey() {
        return "coupon:stock:" + couponId;
    }

    private String issuedKey() {
        return "coupon:issued:" + couponId;
    }
}