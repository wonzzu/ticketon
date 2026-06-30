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
        // when
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


        // 1) 성공/실패 수가 전체 요청과 일치
        assertThat(success.get()).isEqualTo(STOCK);            // 재고만큼만 성공
        assertThat(fail.get()).isEqualTo(PEOPLE - STOCK);      // 나머지는 실패

        // 2) Redis 재고 소진
        assertThat(redis.opsForValue().get("coupon:stock:" + couponId)).isEqualTo("0");

        // 3) DB 발급 내역 == 재고 차감량
        //    (보상 트랜잭션 정확성: Redis만 줄고 DB 저장이 누락되는 불일치를 잡는다)
        assertThat(couponIssueRepository.count()).isEqualTo((long) STOCK);

        // 4) 중복 발급 없음 (1인1매 — SADD 검증): 발급받은 회원이 모두 서로 다름
        //    ※ 프록시 id는 초기화 없이 접근 가능해 LazyInit 안전
        long distinctMembers = couponIssueRepository.findAll().stream()
                .map(issue -> issue.getMember().getId())
                .distinct()
                .count();
        assertThat(distinctMembers).isEqualTo((long) STOCK);
    }
}