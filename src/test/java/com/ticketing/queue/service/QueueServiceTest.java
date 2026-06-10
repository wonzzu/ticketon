package com.ticketing.queue.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QueueServiceTest {

    @Autowired
    StringRedisTemplate redis;

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
        //given
        for (int i = 1; i <= 300; i++) {
            redis.opsForZSet().add("queue:wait:" + scheduleId, "u" + i, i);
        }
        redis.opsForSet().add("queue:schedules", scheduleId.toString());

        RedissonClient c1 = newClient();
        RedissonClient c2 = newClient();
        QueueService instance1 = new QueueService(redis, c1);   // 같은 Redis, 다른 락 클라이언트
        QueueService instance2 = new QueueService(redis, c2);

        //when
        int rounds = 50;
        ExecutorService es = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(rounds * 2);
        for (int i = 0; i < rounds; i++) {
            es.submit(() -> { try { instance1.admit(); } finally { latch.countDown(); } });
            es.submit(() -> { try { instance2.admit(); } finally { latch.countDown(); } });
        }
        latch.await();
        es.shutdown();
        c1.shutdown();
        c2.shutdown();

        //then
        Long active = redis.opsForZSet().zCard("queue:active:" + scheduleId);
        assertThat(active).isLessThanOrEqualTo((long) CAPACITY);   // 100 초과 X
    }

}