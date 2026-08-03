package com.ticketing.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.sentinel.master:}")
    private String sentinelMaster;

    @Value("${spring.data.redis.sentinel.nodes:}")
    private List<String> sentinelNodes;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();

        if (sentinelMaster.isBlank()) {
            config.useSingleServer()
                    .setAddress("redis://" + host + ":" + port);
        } else {
            SentinelServersConfig sentinel = config.useSentinelServers()
                    .setMasterName(sentinelMaster)
                    .setCheckSentinelsList(false);

            sentinelNodes.forEach(node ->sentinel.addSentinelAddress("redis://" + node));

        }
        return Redisson.create(config);
    }
}
