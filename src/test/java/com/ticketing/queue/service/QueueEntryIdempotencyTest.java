package com.ticketing.queue.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class QueueEntryIdempotencyTest extends QueueTestSupport {

    @Test
    void 동일_회원이_동시에_진입해도_대기순번은_한번만_발급된다() throws InterruptedException {
        // given
        long expireAt = System.currentTimeMillis() + 600_000;
        for (int i = 1; i <= CAPACITY; i++) {
            redis.opsForZSet().add("queue:active:" + scheduleId, "active-" + i, expireAt);
        }

        RedissonClient client = newClient();
        QueueService queueService = new QueueService(redis, client, queueEnterScript, queueAdmitScript);
        int requestCount = 100;
        Long memberId = 999L;
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch readyLatch = new CountDownLatch(requestCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(requestCount);
        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();

        try {
            // when
            for (int i = 0; i < requestCount; i++) {
                executor.submit(() -> {
                    readyLatch.countDown();
                    try {
                        startLatch.await();
                        queueService.enter(scheduleId, memberId);
                    } catch (Throwable e) {
                        errors.add(e);
                    } finally {
                        endLatch.countDown();
                    }
                });
            }

            readyLatch.await();
            startLatch.countDown();
            endLatch.await();

            // then
            Long waitingCount = redis.opsForZSet().zCard("queue:wait:" + scheduleId);
            String sequence = redis.opsForValue().get("queue:seq:" + scheduleId);

            assertThat(errors).isEmpty();
            assertThat(waitingCount).isEqualTo(1L);
            assertThat(sequence).isEqualTo("1");
        } finally {
            executor.shutdown();
            client.shutdown();
        }
    }
}
