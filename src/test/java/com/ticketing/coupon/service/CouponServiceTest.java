package com.ticketing.coupon.service;

import com.ticketing.coupon.domain.Coupon;
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
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CouponConcurrencyTest {

    @Autowired
    CouponService couponService;
    @Autowired
    CouponRepository couponRepository;
    @Autowired
    CouponIssueRepository couponIssueRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    StringRedisTemplate redis;

    private static final int STOCK  = 5;
    private static final int PEOPLE = 6;

    private Long couponId;
    private final List<Long> memberIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        for (int i = 0; i < PEOPLE; i++) {
            NormalMember m = NormalMember.create(
                    "user" + i + "@test.com", "pw", "이름" + i, "nick" + i,
                    LocalDate.of(2000, 1, 1), Gender.MALE, "01000000000",
                    new Address("city", "street", "00000"));
            memberRepository.save(m);
            memberIds.add(m.getId());
        }

        Coupon coupon = couponRepository.save(
                Coupon.create("테스트쿠폰", DiscountType.FIXED, 5000, STOCK));
        couponId = coupon.getId();
        redis.opsForValue().set("coupon:stock:" + couponId, String.valueOf(STOCK));
    }

    @AfterEach
    void cleanUp() {
        redis.delete("coupon:stock:" + couponId);
        redis.delete("coupon:issued:" + couponId);
        couponIssueRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void 재고5개를_6명이_동시에_발급하면_5명만_성공() throws InterruptedException {
        // when
        ExecutorService es = Executors.newFixedThreadPool(PEOPLE);
        CountDownLatch latch = new CountDownLatch(PEOPLE);
        AtomicInteger success = new AtomicInteger();

        for (Long memberId : memberIds) {
            es.submit(() -> {
                try {
                    couponService.issue(couponId, memberId);
                    success.incrementAndGet();
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        es.shutdown();


        assertThat(success.get()).isEqualTo(STOCK);
        assertThat(redis.opsForValue().get("coupon:stock:" + couponId)).isEqualTo("0");
    }
}