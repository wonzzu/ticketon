package com.ticketing.queue.service;

import org.junit.jupiter.api.AfterEach;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    protected RedisScript<String> queueAdmitScript;

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @AfterEach
    void cleanUp() {
        redis.delete("queue:wait:" + scheduleId);
        redis.delete("queue:active:" + scheduleId);
        redis.delete("queue:seq:" + scheduleId);
        redis.delete("queue:entered:" + scheduleId);
        redis.opsForSet().remove("queue:schedules", scheduleId.toString());
    }

    protected RedissonClient newClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + redisHost + ":" + redisPort);
        return Redisson.create(config);
    }
}
