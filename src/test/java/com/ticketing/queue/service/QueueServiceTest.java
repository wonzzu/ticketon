package com.ticketing.queue.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class QueueServiceTest {

    @Autowired
    StringRedisTemplate redis;

    @Autowired
    RedisScript<Long> queueFastPathScript;

    private final Long scheduleId = 7777L;
    private static final int CAPACITY = 100;

    @AfterEach
    void cleanUp() {
        redis.delete("queue:wait:" + scheduleId);
        redis.delete("queue:active:" + scheduleId);
        redis.delete("queue:seq:" + scheduleId);
        redis.opsForSet().remove("queue:schedules", scheduleId.toString());
    }

    private RedissonClient newClient() {
        Config cfg = new Config();
        cfg.useSingleServer().setAddress("redis://localhost:6379");
        return Redisson.create(cfg);
    }

    @Test
    void 두_서버에서_동시에_진입해도_active는_정원을_안넘는다() throws InterruptedException {
        // given : 대기열 300명 + 두 서버(같은 Redis, 다른 락 클라이언트)
        for (int i = 1; i <= 300; i++) {
            redis.opsForZSet().add("queue:wait:" + scheduleId, "u" + i, i);
        }
        redis.opsForSet().add("queue:schedules", scheduleId.toString());

        RedissonClient c1 = newClient();
        RedissonClient c2 = newClient();
        QueueService instance1 = new QueueService(redis, c1, queueFastPathScript);
        QueueService instance2 = new QueueService(redis, c2, queueFastPathScript);

        // when : 두 서버가 동시에 admit 경쟁
        int rounds = 50;
        ExecutorService es = Executors.newFixedThreadPool(rounds * 2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(rounds * 2);
        for (int i = 0; i < rounds; i++) {
            es.submit(() -> {
                try { startLatch.await(); instance1.admit(); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                finally { endLatch.countDown(); }
            });
            es.submit(() -> {
                try { startLatch.await(); instance2.admit(); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                finally { endLatch.countDown(); }
            });
        }
        startLatch.countDown();   // 동시 출발
        endLatch.await();
        es.shutdown();
        c1.shutdown();
        c2.shutdown();

        // then : active는 정원(100)을 안 넘고, wait는 승급된 100명만큼 감소
        Long active = redis.opsForZSet().zCard("queue:active:" + scheduleId);
        Long wait = redis.opsForZSet().zCard("queue:wait:" + scheduleId);

        System.out.printf("%n========== [대기열 정원 제어] ==========%n" +
                        "  초기 대기 : 300명 · 정원 : %d · 2서버 동시 admit%n" +
                        "  기대값    : active %d / wait 200%n" +
                        "  실제값    : active %d / wait %d%n" +
                        "=======================================%n",
                CAPACITY, CAPACITY, active, wait);

        assertThat(active).isEqualTo((long) CAPACITY);
        assertThat(wait).isEqualTo(200L);
    }

}