package com.ticketing.queue.service;

import com.ticketing.queue.dto.response.QueueStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

    private final StringRedisTemplate redisTemplate;

    private static final int CAPACITY = 100;
    private static final long ACTIVE_TTL_MS = 10 * 60 * 1000;
    private static final String SCHEDULES_KEY = "queue:schedules";
    private static final String ADMIT_LOCK = "queue:admit:lock";
    private final RedissonClient redissonClient;
    private final RedisScript<Long> queueEnterScript;
    private final RedisScript<Long> queueAdmitScript;


    public QueueStatusResponse enter(Long scheduleId, Long memberId) {
        String member = memberId.toString();
        long now = System.currentTimeMillis();

        Long result = redisTemplate.execute(
                queueEnterScript,
                List.of(
                        activeKey(scheduleId),
                        waitKey(scheduleId),
                        seqKey(scheduleId),
                        SCHEDULES_KEY
                ),
                member,
                String.valueOf(now + ACTIVE_TTL_MS),
                String.valueOf(CAPACITY),
                String.valueOf(now),
                scheduleId.toString()
        );

        if (Long.valueOf(1L).equals(result)) {
            return QueueStatusResponse.admitted();
        }

        return status(scheduleId, memberId);
    }

    public QueueStatusResponse status(Long scheduleId, Long memberId) {
        String member = memberId.toString();
        if (isActive(scheduleId, member)) return QueueStatusResponse.admitted();

        Long rank = redisTemplate.opsForZSet().rank(waitKey(scheduleId), member);
        if (rank == null) return QueueStatusResponse.expired();

        Long total = redisTemplate.opsForZSet().zCard(waitKey(scheduleId));
        return QueueStatusResponse.waiting(rank, total == null ? 0 : total);
    }

    public boolean isAdmitted(Long scheduleId, Long memberId) {
        return isActive(scheduleId, memberId.toString());
    }

    public void leave(Long scheduleId, Long memberId) {
        redisTemplate.opsForZSet().remove(activeKey(scheduleId), memberId.toString());
    }

    @Scheduled(fixedDelay = 3000)
    public void admit() {
        RLock lock = redissonClient.getLock(ADMIT_LOCK);
        if (!lock.tryLock()) {
            log.debug("대기열 승급 락 획득 실패 - 다른 인스턴스가 처리 중.");
            return;
        }

        try {
            doAdmit();
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }


    public void doAdmit() {
        Set<String> scheduleIds = redisTemplate.opsForSet().members(SCHEDULES_KEY);
        if (scheduleIds == null || scheduleIds.isEmpty()) return;

        long now = System.currentTimeMillis();
        long expireAt = now + ACTIVE_TTL_MS;

        for (String sid : scheduleIds) {
            Long scheduleId = Long.valueOf(sid);

            Long admittedCount = redisTemplate.execute(
                    queueAdmitScript,
                    List.of(activeKey(scheduleId), waitKey(scheduleId)),
                    String.valueOf(now),
                    String.valueOf(expireAt),
                    String.valueOf(CAPACITY)
            );

            if (admittedCount != null && admittedCount > 0) {
                log.info("대기열 승급: scheduleId={}, {}명 입장", scheduleId, admittedCount);
            }
        }
    }


    private boolean isActive(Long scheduleId, String member) {
        Double expireAt = redisTemplate.opsForZSet().score(activeKey(scheduleId), member);

        if (expireAt == null) return false;

        if (expireAt < System.currentTimeMillis()) {
            redisTemplate.opsForZSet().remove(activeKey(scheduleId), member);
            return false;
        }
        return true;
    }


    private String waitKey(Long s) {
        return "queue:wait:" + s;
    }

    private String activeKey(Long s) {
        return "queue:active:" + s;
    }

    private String seqKey(Long s) {
        return "queue:seq:" + s;
    }


}
