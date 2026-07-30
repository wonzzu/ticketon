package com.ticketing.queue.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class QueueMetrics {

    private static final String SCHEDULES_KEY = "queue:schedules";

    private final StringRedisTemplate redisTemplate;
    private final MeterRegistry meterRegistry;

    @PostConstruct
    public void register() {
        Gauge.builder("ticketing.queue.waiting", this, m -> m.total("queue:wait:"))
                .description("대기 중인 인원")
                .register(meterRegistry);

        Gauge.builder("ticketing.queue.active", this, m -> m.total("queue:active:"))
                .description("입장한 인원")
                .register(meterRegistry);
    }

    private double total(String prefix) {
        Set<String> scheduleIds = redisTemplate.opsForSet().members(SCHEDULES_KEY);

        if (scheduleIds == null) return 0;

        long sum = 0;
        for (String id : scheduleIds) {
            Long count = redisTemplate.opsForZSet().zCard(prefix + id);
            if (count != null) sum += count;
        }
        return sum;
    }
}
