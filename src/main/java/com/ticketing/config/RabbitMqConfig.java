package com.ticketing.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.outbox.messaging.RabbitOutboxMessagePublisher;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class RabbitMqConfig {

    public static final String PAYMENT_CANCELED_QUEUE = "ticketon.payment-canceled.reaggregation";
    public static final String DEAD_LETTER_EXCHANGE = "ticketon.events.dlx";
    public static final String PAYMENT_CANCELED_DLQ = "ticketon.payment-canceled.reaggregation.dlq";
    public static final String PAYMENT_CANCELED_DLQ_ROUTING_KEY = "payment.canceled.reaggregation.failed";

    @Bean
    public TopicExchange ticketonEventExchange() {
        return new TopicExchange(RabbitOutboxMessagePublisher.EXCHANGE, true, false);
    }

    @Bean
    public Queue paymentCanceledQueue() {
        return QueueBuilder.durable(PAYMENT_CANCELED_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(PAYMENT_CANCELED_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public TopicExchange ticketonDeadLetterExchange() {
        return new TopicExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue paymentCanceledDeadLetterQueue() {
        return QueueBuilder.durable(PAYMENT_CANCELED_DLQ).build();
    }

    @Bean
    public Binding paymentCanceledBinding(
            @Qualifier("ticketonEventExchange") TopicExchange ticketonEventExchange,
            @Qualifier("paymentCanceledQueue") Queue paymentCanceledQueue
    ) {
        return BindingBuilder.bind(paymentCanceledQueue)
                .to(ticketonEventExchange)
                .with(RabbitOutboxMessagePublisher.PAYMENT_CANCELED_ROUTING_KEY);
    }

    @Bean
    public Binding paymentCanceledDeadLetterBinding(
            @Qualifier("ticketonDeadLetterExchange") TopicExchange ticketonDeadLetterExchange,
            @Qualifier("paymentCanceledDeadLetterQueue") Queue paymentCanceledDeadLetterQueue
    ) {
        return BindingBuilder.bind(paymentCanceledDeadLetterQueue)
                .to(ticketonDeadLetterExchange)
                .with(PAYMENT_CANCELED_DLQ_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
