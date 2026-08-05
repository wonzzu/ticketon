package com.ticketing.queue.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class QueueMultiInstanceAdmissionTest extends QueueTestSupport {

    @Test
    void 두_서버에서_동시에_승급해도_active는_정원을_넘지_않는다() throws InterruptedException {
        // given
        for (int i = 1; i <= 300; i++) {
            redis.opsForZSet().add("queue:wait:" + scheduleId, "u" + i, i);
        }
        redis.opsForSet().add("queue:schedules", scheduleId.toString());

        RedissonClient client1 = newClient();
        RedissonClient client2 = newClient();
        QueueService instance1 = new QueueService(redis, client1, queueEnterScript, queueAdmitScript);
        QueueService instance2 = new QueueService(redis, client2, queueEnterScript, queueAdmitScript);

        int rounds = 50;
        ExecutorService executor = Executors.newFixedThreadPool(rounds * 2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(rounds * 2);

        try {
            // when
            for (int i = 0; i < rounds; i++) {
                executor.submit(() -> admitAtTheSameTime(instance1, startLatch, endLatch));
                executor.submit(() -> admitAtTheSameTime(instance2, startLatch, endLatch));
            }

            startLatch.countDown();
            endLatch.await();

            // then
            Long activeCount = redis.opsForZSet().zCard("queue:active:" + scheduleId);
            Long waitingCount = redis.opsForZSet().zCard("queue:wait:" + scheduleId);

            assertThat(activeCount).isEqualTo((long) CAPACITY);
            assertThat(waitingCount).isEqualTo(200L);
        } finally {
            executor.shutdown();
            client1.shutdown();
            client2.shutdown();
        }
    }

    private void admitAtTheSameTime(QueueService queueService, CountDownLatch startLatch, CountDownLatch endLatch) {
        try {
            startLatch.await();
            queueService.admit();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            endLatch.countDown();
        }
    }
}
