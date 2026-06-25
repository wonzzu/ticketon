package com.ticketing.queue.service;

import com.ticketing.queue.dto.response.QueueStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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


    public QueueStatusResponse enter(Long scheduleId, Long memberId) {
        String member = memberId.toString();

        if (isActive(scheduleId, member)) {
            return QueueStatusResponse.admitted();
        }

        // 이미 대기열에 진입 한지 체크.
        boolean already = redisTemplate.opsForZSet().score(waitKey(scheduleId), member) != null;

        if (!already) {
            Long seq = redisTemplate.opsForValue().increment(seqKey(scheduleId));
            redisTemplate.opsForZSet().add(waitKey(scheduleId), member, seq);
            redisTemplate.opsForSet().add(SCHEDULES_KEY, scheduleId.toString());
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
        if (scheduleIds == null) return;
        long now = System.currentTimeMillis();

        for (String sid : scheduleIds) {
            Long scheduleId = Long.valueOf(sid);

            redisTemplate.opsForZSet().removeRangeByScore(activeKey(scheduleId), 0, now);

            Long activeCount = redisTemplate.opsForZSet().zCard(activeKey(scheduleId));
            long slots = CAPACITY - (activeCount == null ? 0 : activeCount);
            if (slots <= 0) continue;

            Set<String> front = redisTemplate.opsForZSet().range(waitKey(scheduleId), 0, slots - 1);

            if (front == null || front.isEmpty()) continue;

            long expireAt = now + ACTIVE_TTL_MS;

            for (String member : front) {
                redisTemplate.opsForZSet().remove(waitKey(scheduleId), member);
                redisTemplate.opsForZSet().add(activeKey(scheduleId), member, expireAt);
            }
            log.info("대기열 승급: scheduleId={}, {}명 입장", scheduleId, front.size());
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
