package com.ticketing.notification.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.notification.domain.NotificationType;
import com.ticketing.notification.event.NotificationCreatedEvent;
import com.ticketing.notification.sse.NotificationSseEmitterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.Message;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationRedisSubscriberTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final NotificationSseEmitterRegistry emitterRegistry =
            mock(NotificationSseEmitterRegistry.class);
    private final NotificationRedisSubscriber subscriber =
            new NotificationRedisSubscriber(objectMapper, emitterRegistry);

    @Test
    void deliverRedisMessageToMemberSseConnections() throws Exception {
        NotificationCreatedEvent event = new NotificationCreatedEvent(
                1L,
                10L,
                NotificationType.PAYMENT_CANCELED,
                "결제가 취소되었습니다.",
                LocalDateTime.of(2026, 9, 24, 18, 0)
        );

        Message message = mock(Message.class);
        when(message.getBody()).thenReturn(
                objectMapper.writeValueAsBytes(event)
        );

        subscriber.onMessage(message, null);

        verify(emitterRegistry).send(10L, event);
    }
}
