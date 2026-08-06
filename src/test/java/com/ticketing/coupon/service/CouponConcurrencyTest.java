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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

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
@ActiveProfiles("test")
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)   // DB 정리
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

    private static final int STOCK = 5;
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
    void cleanupRedis() {   // DB는 @Sql(truncate.sql)이 정리, Redis만 여기서
        redis.delete("coupon:stock:" + couponId);
        redis.delete("coupon:issued:" + couponId);
    }

    @Test
    void 재고5개를_6명이_동시에_발급하면_5명만_성공() throws InterruptedException {
        // given : setup에서 재고 5 쿠폰 + 회원 6명

        // when : 6명이 동시에 발급 시도
        ExecutorService es = Executors.newFixedThreadPool(PEOPLE);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(PEOPLE);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        for (Long memberId : memberIds) {
            es.submit(() -> {
                try {
                    startLatch.await();
                    couponService.issue(couponId, memberId);
                    success.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        endLatch.await();
        es.shutdown();


        // then (검증값을 먼저 모아 출력·assert에 공용)
        String redisStock = redis.opsForValue().get("coupon:stock:" + couponId);
        long dbIssued = couponIssueRepository.count();
        long distinctMembers = couponIssueRepository.findAll().stream()   // 프록시 id는 LazyInit 안전
                .map(issue -> issue.getMember().getId())
                .distinct()
                .count();

        System.out.printf("%n========== [쿠폰 발급 동시성] ==========%n" +
                        "  재고    : %d · 동시 요청 : %d명%n" +
                        "  기대값  : 성공 %d / 실패 %d / Redis재고 0 / DB발급 %d / distinct %d%n" +
                        "  실제값  : 성공 %d / 실패 %d / Redis재고 %s / DB발급 %d / distinct %d%n" +
                        "=======================================%n",
                STOCK, PEOPLE,
                STOCK, PEOPLE - STOCK, STOCK, STOCK,
                success.get(), fail.get(), redisStock, dbIssued, distinctMembers);

        assertThat(success.get()).isEqualTo(STOCK);
        assertThat(fail.get()).isEqualTo(PEOPLE - STOCK);
        assertThat(redisStock).isEqualTo("0");
        assertThat(dbIssued).isEqualTo((long) STOCK);
        assertThat(distinctMembers).isEqualTo((long) STOCK);
    }
}
