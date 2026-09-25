package com.ticketing.notification.sse;

import com.ticketing.notification.event.NotificationCreatedEvent;
import com.ticketing.notification.domain.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationSseEmitterRegistryTest {

    @Test
    void 종료된_연결의_정리마저_실패해도_다른_연결에는_알림을_전송한다() throws IOException {
        NotificationSseEmitterRegistry registry =
                new NotificationSseEmitterRegistry();
        SseEmitter closedEmitter = mock(SseEmitter.class);
        SseEmitter activeEmitter = mock(SseEmitter.class);

        doThrow(new IOException("종료된 연결"))
                .when(closedEmitter)
                .send(any(SseEmitter.SseEventBuilder.class));
        doThrow(new IllegalStateException("이미 완료된 연결"))
                .when(closedEmitter)
                .completeWithError(any(Throwable.class));

        Map<Long, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();
        emitters.put(1L, ConcurrentHashMap.newKeySet());
        emitters.get(1L).add(closedEmitter);
        emitters.get(1L).add(activeEmitter);
        ReflectionTestUtils.setField(registry, "emitters", emitters);

        NotificationCreatedEvent event = new NotificationCreatedEvent(
                10L,
                1L,
                NotificationType.PAYMENT_CANCELED,
                "결제 취소 알림",
                LocalDateTime.of(2026, 9, 25, 12, 0)
        );

        assertDoesNotThrow(() -> registry.send(1L, event));
        verify(activeEmitter).send(any(SseEmitter.SseEventBuilder.class));
    }
}
