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
        //given
        int threadCount = 100;
        ExecutorService es = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);          // 동시 출발 신호
        CountDownLatch endLatch = new CountDownLatch(threadCount);  // 전부 끝날 때까지 대기
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        //when - 서로 다른 100명이 같은 좌석을 "동시에" 선점 시도
        for (int i = 0; i < threadCount; i++) {
            long memberId = i + 1;
            es.submit(() -> {
                try {
                    startLatch.await();   // 모든 스레드가 여기서 대기 → 진짜 동시 출발
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
        startLatch.countDown();   // 출발 신호 → 100명 동시 경쟁
        endLatch.await();
        es.shutdown();

        //then
        Assertions.assertThat(successCount.get()).isEqualTo(1);                  // 1명만 성공
        Assertions.assertThat(failCount.get()).isEqualTo(threadCount - 1);       // 나머지 99명 실패
        Assertions.assertThat(redisTemplate.hasKey("seat:hold:" + scheduleId + ":" + seatId)).isTrue();
    }
}