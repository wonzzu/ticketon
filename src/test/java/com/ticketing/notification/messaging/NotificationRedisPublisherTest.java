package com.ticketing.notification.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.notification.domain.NotificationType;
import com.ticketing.notification.event.NotificationCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationRedisPublisherTest {

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private final NotificationRedisPublisher publisher =
            new NotificationRedisPublisher(redisTemplate, objectMapper);

    @Test
    void publishNotificationCreatedEvent() throws JsonProcessingException {
        NotificationCreatedEvent event = createEvent();
        String message = "{\"notificationId\":1}";

        when(objectMapper.writeValueAsString(event)).thenReturn(message);
        when(redisTemplate.convertAndSend(
                NotificationRedisPublisher.NOTIFICATION_CREATED_CHANNEL,
                message
        )).thenReturn(2L);

        publisher.publish(event);

        verify(redisTemplate).convertAndSend(
                NotificationRedisPublisher.NOTIFICATION_CREATED_CHANNEL,
                message
        );
    }

    @Test
    void doNotPropagateRedisFailure() throws JsonProcessingException {
        NotificationCreatedEvent event = createEvent();
        String message = "{\"notificationId\":1}";

        when(objectMapper.writeValueAsString(event)).thenReturn(message);
        when(redisTemplate.convertAndSend(
                NotificationRedisPublisher.NOTIFICATION_CREATED_CHANNEL,
                message
        )).thenThrow(new RedisConnectionFailureException("Redis 연결 실패"));

        assertThatCode(() -> publisher.publish(event))
                .doesNotThrowAnyException();
    }

    private NotificationCreatedEvent createEvent() {
        return new NotificationCreatedEvent(
                1L,
                10L,
                NotificationType.PAYMENT_CANCELED,
                "결제가 취소되었습니다.",
                LocalDateTime.of(2026, 9, 24, 18, 0)
        );
    }
}
