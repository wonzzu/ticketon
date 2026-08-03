package com.ticketing.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SeatHoldService {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> seatReleaseScript;
    private static final Duration HOLD_TTL = Duration.ofMinutes(7);

    private String key(Long scheduleId, Long eventSeatId) {
        return "seat:hold:" + scheduleId + ":" + eventSeatId;
    }

    // 선택 좌석 전부를 선점 시도 -> 하나라도 실패하면 롤백 후 false
    public boolean holdAll(Long scheduleId, List<Long> eventSeatIds, Long memberId) {
        List<Long> heldByMe = new ArrayList<>();

        for (Long seatId : eventSeatIds) {
            Boolean ok = redisTemplate.opsForValue()
                    .setIfAbsent(key(scheduleId, seatId), memberId.toString(), HOLD_TTL);

            if (Boolean.TRUE.equals(ok)) {
                heldByMe.add(seatId);
            } else {
                releaseAll(scheduleId, heldByMe,memberId);
                return false;
            }
        }
        return true;
    }

    public void releaseAll(Long scheduleId, List<Long> eventSeatIds,Long memberId) {
        if (eventSeatIds.isEmpty()) return;

        List<String> keys = eventSeatIds.stream().map(id -> key(scheduleId, id)).toList();

        redisTemplate.execute(seatReleaseScript, keys, memberId.toString());
    }


    // 이미 선점 중인 좌석 찾기.(좌석 전체에서)
    public Set<Long> findHeldSeatIds(Long scheduleId, List<Long> eventSeatIds) {
        HashSet<Long> heldSeat = new HashSet<>();

        if (eventSeatIds.isEmpty()) return heldSeat;

        List<String> keys = eventSeatIds.stream().map(id -> key(scheduleId, id)).toList();
        List<String> values = redisTemplate.opsForValue().multiGet(keys);

        if (values == null) return heldSeat;

        for (int i = 0; i < eventSeatIds.size(); i++) {
            if (values.get(i) != null) {
                heldSeat.add(eventSeatIds.get(i));
            }
        }
        return heldSeat;
    }

    // 결제 직전에 전부 내 선점 상태인지 한번 더 체크 -> TTL 만료 됐는지 체크.
    public boolean isHeldByAll(Long scheduleId, List<Long> eventSeatIds, Long memberId) {

        List<String> keys = eventSeatIds.stream().map(id -> key(scheduleId, id)).toList();
        List<String> values = redisTemplate.opsForValue().multiGet(keys);

        if (values==null) return false;

        String me = memberId.toString();

        return values.size() == eventSeatIds.size() && values.stream().allMatch(me::equals);
    }
}

