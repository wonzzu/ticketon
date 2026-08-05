package com.ticketing.event.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SeatHoldServiceTest {

    @Autowired SeatHoldService seatHoldService;
    @Autowired StringRedisTemplate redisTemplate;

    private final Long scheduleId = 9999L;
    private final Long seatId = 1L;

    @AfterEach
    void cleanUp() {
        redisTemplate.delete("seat:hold:" + scheduleId + ":" + seatId);
    }


    @Test
    void 동시에_100명이_같은좌석_선점시_한명만성공() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService es = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when : 서로 다른 100명이 같은 좌석을 동시에 선점 시도
        for (int i = 0; i < threadCount; i++) {
            long memberId = i + 1;
            es.submit(() -> {
                try {
                    startLatch.await();   // 전원 대기 → 진짜 동시 출발
                    boolean ok = seatHoldService.holdAll(scheduleId, List.of(seatId), memberId);
                    if (ok) successCount.incrementAndGet();
                    else failCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        endLatch.await();
        es.shutdown();

        // then : 단 1명만 선점 성공, 나머지는 실패
        boolean holdKeyExists = redisTemplate.hasKey("seat:hold:" + scheduleId + ":" + seatId);
        System.out.printf("%n========== [좌석 선점 동시성] ==========%n" +
                        "  시도    : %d명 동시 · 같은 좌석 1개%n" +
                        "  기대값  : 성공 1 / 실패 %d / 선점키 존재(true)%n" +
                        "  실제값  : 성공 %d / 실패 %d / 선점키 %s%n" +
                        "=======================================%n",
                threadCount, threadCount - 1,
                successCount.get(), failCount.get(), holdKeyExists);

        Assertions.assertThat(successCount.get()).isEqualTo(1);
        Assertions.assertThat(failCount.get()).isEqualTo(threadCount - 1);
        Assertions.assertThat(holdKeyExists).isTrue();
    }

    @Test
    void 다른_회원이_소유한_좌석은_해제하지_않는다() {
        // given
        Long ownerId = 2L;
        Long otherMemberId = 1L;

        boolean held = seatHoldService.holdAll(scheduleId, List.of(seatId), ownerId);
        Assertions.assertThat(held).isTrue();

        // when
        seatHoldService.releaseAll(scheduleId, List.of(seatId), otherMemberId);

        // then
        String holder = redisTemplate.opsForValue()
                .get("seat:hold:" + scheduleId + ":" + seatId);

        Assertions.assertThat(holder).isEqualTo(ownerId.toString());
    }
}