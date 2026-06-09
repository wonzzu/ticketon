package com.ticketing.event.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
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
        int threadCount=100;
        ExecutorService es = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        //when
        for (int i = 0; i < threadCount; i++) {
            long memberId =1;
            es.submit(
                    ()->{try {
                        boolean ok = seatHoldService.holdAll(scheduleId, List.of(seatId), memberId);
                        if (ok) successCount.incrementAndGet();
                    }finally {
                        latch.countDown();
                    }
                    }
            );
        }
        latch.await();
        es.shutdown();

        //then
        Assertions.assertThat(successCount.get()).isEqualTo(1);
    }
}