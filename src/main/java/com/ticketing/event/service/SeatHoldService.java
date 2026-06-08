package com.ticketing.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SeatHoldService {

    private final StringRedisTemplate redisTemplate;
    private static final Duration HOLD_TTL = Duration.ofMinutes(7);

    private String key(Long scheduleId, Long eventSeatId) {
        return "seat:hold:" + scheduleId + ":" + eventSeatId;
    }

    public boolean holdAll(Long scheduleId, List<Long> eventSeatIds, Long memberId) {
        List<Long> heldByMe = new ArrayList<>();

        for (Long seatId : eventSeatIds) {
            Boolean ok = redisTemplate.opsForValue()
                    .setIfAbsent(key(scheduleId, seatId), memberId.toString(), HOLD_TTL);

            if (Boolean.TRUE.equals(ok)) {
                heldByMe.add(seatId);
            } else {
                releaseAll(scheduleId, heldByMe);
                return false;
            }
        }
        return true;
    }

    public void releaseAll(Long scheduleId, List<Long> eventSeatIds) {
        if (eventSeatIds.isEmpty()) return;

        List<String> keys = eventSeatIds.stream().map(id -> key(scheduleId, id)).toList();
        redisTemplate.delete(keys);
    }


    public Set<Long> findHeldSeatIds(Long scheduleId, List<Long> eventSeatIds) {
        HashSet<Long> heldSeat = new HashSet<>();

        if (eventSeatIds.isEmpty()) return heldSeat;

        List<String> keys = eventSeatIds.stream().map(id -> key(scheduleId, id)).toList();
        List<String> values = redisTemplate.opsForValue().multiGet(keys);

        if (values ==null) return heldSeat;

        for (int i = 0; i < eventSeatIds.size(); i++) {
            if (values.get(i) != null) {
                heldSeat.add(eventSeatIds.get(i));
            }
        }
        return heldSeat;
    }

}
