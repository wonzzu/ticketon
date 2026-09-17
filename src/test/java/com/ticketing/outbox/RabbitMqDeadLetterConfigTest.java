package com.ticketing.outbox;

import com.ticketing.config.RabbitMqConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RabbitMQ Dead Letter 설정")
class RabbitMqDeadLetterConfigTest {

    private final RabbitMqConfig rabbitMqConfig = new RabbitMqConfig();

    @Test
    @DisplayName("결제 취소 Queue는 재시도 소진 메시지를 Dead Letter Exchange로 전달한다")
    void configureDeadLetterRouting() {
        Queue queue = rabbitMqConfig.paymentCanceledQueue();

        assertThat(queue.getArguments())
                .containsEntry("x-dead-letter-exchange", RabbitMqConfig.DEAD_LETTER_EXCHANGE)
                .containsEntry("x-dead-letter-routing-key", RabbitMqConfig.PAYMENT_CANCELED_DLQ_ROUTING_KEY);
        assertThat(rabbitMqConfig.paymentCanceledDeadLetterQueue().getName())
                .isEqualTo(RabbitMqConfig.PAYMENT_CANCELED_DLQ);
    }
}
