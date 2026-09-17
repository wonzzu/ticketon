package com.ticketing.outbox;

import com.ticketing.outbox.domain.OutboxEvent;
import com.ticketing.outbox.domain.OutboxEventStatus;
import com.ticketing.outbox.repository.OutboxEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("local")
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DisplayName("Outbox Relay Lease 선점")
class OutboxRelayClaimIntegrationTest {

    @Autowired OutboxEventRepository outboxEventRepository;

    @Test
    @DisplayName("세 Relay가 같은 PENDING 행을 선점해도 한 Relay만 성공한다")
    void onlyOneRelayClaimsPendingEvent() throws Exception {
        OutboxEvent event = outboxEventRepository.saveAndFlush(
                OutboxEvent.paymentCanceled(1L, "{\"paymentId\":1}"));
        CountDownLatch ready = new CountDownLatch(3);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(3);

        try {
            List<Future<Integer>> results = List.of("spring1", "spring2", "spring3").stream()
                    .map(workerId -> executor.submit(() -> {
                        ready.countDown();
                        start.await();
                        return outboxEventRepository.tryClaim(
                                event.getId(), workerId, LocalDateTime.now());
                    }))
                    .toList();

            ready.await();
            start.countDown();

            int claimedCount = 0;
            for (Future<Integer> result : results) {
                claimedCount += result.get();
            }

            OutboxEvent claimed = outboxEventRepository.findById(event.getId()).orElseThrow();
            assertThat(claimedCount).isEqualTo(1);
            assertThat(claimed.getStatus()).isEqualTo(OutboxEventStatus.PROCESSING);
            assertThat(claimed.getClaimedBy()).isIn("spring1", "spring2", "spring3");
            assertThat(claimed.getClaimedAt()).isNotNull();
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("Lease 시간이 지난 PROCESSING 행은 다시 PENDING으로 복구한다")
    void releaseExpiredClaim() {
        OutboxEvent event = outboxEventRepository.saveAndFlush(
                OutboxEvent.paymentCanceled(2L, "{\"paymentId\":2}"));
        LocalDateTime claimedAt = LocalDateTime.now().minusMinutes(1);
        assertThat(outboxEventRepository.tryClaim(event.getId(), "stopped-spring", claimedAt))
                .isEqualTo(1);

        int released = outboxEventRepository.releaseExpiredClaims(
                LocalDateTime.now().minusSeconds(30));

        OutboxEvent recovered = outboxEventRepository.findById(event.getId()).orElseThrow();
        assertThat(released).isEqualTo(1);
        assertThat(recovered.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(recovered.getClaimedBy()).isNull();
        assertThat(recovered.getClaimedAt()).isNull();
    }
}
