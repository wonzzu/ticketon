package com.ticketing.queue.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class QueueEntryAdmissionRaceTest extends QueueTestSupport {

    @Test
    void 진입과_승급이_경쟁해도_active는_정원을_넘지_않는다() throws Exception {
        // given
        RedissonClient client = newClient();
        QueueService queueService = new QueueService(redis, client, queueEnterScript, queueAdmitScript);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        int rounds = 100;
        boolean capacityExceeded = false;
        Long actualActiveCount = null;

        try {
            for (int round = 0; round < rounds; round++) {
                redis.delete("queue:wait:" + scheduleId);
                redis.delete("queue:active:" + scheduleId);
                redis.delete("queue:seq:" + scheduleId);

                long expireAt = System.currentTimeMillis() + 600_000;
                for (int i = 1; i < CAPACITY; i++) {
                    redis.opsForZSet().add("queue:active:" + scheduleId, "active-" + i, expireAt);
                }
                redis.opsForZSet().add("queue:wait:" + scheduleId, "waiting-member", 1);
                redis.opsForSet().add("queue:schedules", scheduleId.toString());

                Long enteringMemberId = 100_000L + round;
                CountDownLatch startLatch = new CountDownLatch(1);

                // when
                Future<?> admitFuture = executor.submit(() -> {
                    await(startLatch);
                    queueService.doAdmit();
                });
                Future<?> enterFuture = executor.submit(() -> {
                    await(startLatch);
                    queueService.enter(scheduleId, enteringMemberId);
                });

                startLatch.countDown();
                admitFuture.get();
                enterFuture.get();

                actualActiveCount = redis.opsForZSet().zCard("queue:active:" + scheduleId);
                if (actualActiveCount != null && actualActiveCount > CAPACITY) {
                    capacityExceeded = true;
                    break;
                }
            }
        } finally {
            executor.shutdown();
            client.shutdown();
        }

        // then
        assertThat(capacityExceeded)
                .as("진입과 승급이 경쟁해도 active는 정원 %d명을 넘으면 안 된다. 실제 active=%s", CAPACITY, actualActiveCount)
                .isFalse();
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
