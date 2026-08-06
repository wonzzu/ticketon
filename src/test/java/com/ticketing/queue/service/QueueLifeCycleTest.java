package com.ticketing.queue.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class QueueLifeCycleTest extends QueueTestSupport {

    @Test
    void active가_있으면_유지하고_만료후_빈_대기열은_정리한다() {
        // given
        String activeKey = "queue:active:" + scheduleId;
        String seqKey = "queue:seq:" + scheduleId;

        redis.opsForZSet().add(activeKey, "member-1", System.currentTimeMillis() + 600_000);
        redis.opsForValue().set(seqKey, "10");
        redis.opsForSet().add("queue:schedules", scheduleId.toString());

        RedissonClient client = newClient();
        QueueService queueService = new QueueService(redis, client, queueEnterScript, queueAdmitScript);

        try {
            // when: 유효한 active 회원이 있는 상태
            queueService.doAdmit();

            // then: 스케줄러 처리 대상과 sequence를 유지
            assertThat(redis.opsForSet().isMember("queue:schedules", scheduleId.toString())).isTrue();
            assertThat(redis.hasKey(seqKey)).isTrue();

            // when: active 회원이 만료된 뒤 다시 승급 작업 실행
            redis.opsForZSet().add(activeKey, "member-1", System.currentTimeMillis() - 1);
            queueService.doAdmit();

            // then: active와 waiting이 모두 비었으므로 메타데이터 정리
            assertThat(redis.hasKey(activeKey)).isFalse();
            assertThat(redis.hasKey(seqKey)).isFalse();
            assertThat(redis.opsForSet().isMember("queue:schedules", scheduleId.toString())).isFalse();
        } finally {
            client.shutdown();
        }
    }
}