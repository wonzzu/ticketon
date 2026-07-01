package com.ticketing.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ticketing.event.dto.response.EventListResponseDto;
import com.ticketing.event.dto.response.EventResponseDto;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());                    // LocalDate/LocalDateTime 직렬화
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // 숫자 대신 ISO 문자열

        // 캐시별 "타입 고정" 직렬화 — defaultTyping(@class) 없이 순수 JSON.
        //  값 타입을 캐시마다 못 박으므로 컬렉션(List) 역직렬화가 깨지지 않는다.
        RedisCacheConfiguration eventConfig = cacheConfig(
                new Jackson2JsonRedisSerializer<>(mapper, EventResponseDto.class));

        JavaType listType = mapper.getTypeFactory()
                .constructCollectionType(List.class, EventListResponseDto.class);
        RedisCacheConfiguration eventsConfig = cacheConfig(
                new Jackson2JsonRedisSerializer<>(mapper, listType));

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration("event", eventConfig)     // 단건: EventResponseDto
                .withCacheConfiguration("events", eventsConfig)   // 목록: List<EventListResponseDto>
                .build();
    }

    private RedisCacheConfiguration cacheConfig(Jackson2JsonRedisSerializer<?> serializer) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer));
    }
}
