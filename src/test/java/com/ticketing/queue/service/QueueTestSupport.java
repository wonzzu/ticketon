package com.ticketing.queue.service;

import org.junit.jupiter.api.AfterEach;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

abstract class QueueTestSupport {

    protected static final int CAPACITY = 100;
    protected final Long scheduleId = 7777L;

    @Autowired
    protected StringRedisTemplate redis;

    @Autowired
    protected RedisScript<Long> queueEnterScript;

    @Autowired
    protected RedisScript<Long> queueAdmitScript;

    @AfterEach
    void cleanUp() {
        redis.delete("queue:wait:" + scheduleId);
        redis.delete("queue:active:" + scheduleId);
        redis.delete("queue:seq:" + scheduleId);
        redis.opsForSet().remove("queue:schedules", scheduleId.toString());
    }

    protected RedissonClient newClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379");
        return Redisson.create(config);
    }
}
