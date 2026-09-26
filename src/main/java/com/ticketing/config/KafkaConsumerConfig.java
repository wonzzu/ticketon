package com.ticketing.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object>
    reaggregationKafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> consumerFactory,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.retry.interval-ms}") long retryIntervalMs,
            @Value("${app.kafka.retry.max-retries}") long maxRetries
    ) {
        return createFactory(
                configurer,
                consumerFactory,
                kafkaTemplate,
                ".reaggregation.DLT",
                retryIntervalMs,
                maxRetries
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object>
    notificationKafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> consumerFactory,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.retry.interval-ms}") long retryIntervalMs,
            @Value("${app.kafka.retry.max-retries}") long maxRetries
    ) {
        return createFactory(
                configurer,
                consumerFactory,
                kafkaTemplate,
                ".notification.DLT",
                retryIntervalMs,
                maxRetries
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object>
    ticketIssuanceKafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> consumerFactory,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.retry.interval-ms}") long retryIntervalMs,
            @Value("${app.kafka.retry.max-retries}") long maxRetries
    ) {
        return createFactory(
                configurer,
                consumerFactory,
                kafkaTemplate,
                ".ticket-issuance.DLT",
                retryIntervalMs,
                maxRetries
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object>
    queueAnalyticsKafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> consumerFactory,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.retry.interval-ms}") long retryIntervalMs,
            @Value("${app.kafka.retry.max-retries}") long maxRetries
    ) {
        return createFactory(
                configurer,
                consumerFactory,
                kafkaTemplate,
                ".analytics.DLT",
                retryIntervalMs,
                maxRetries
        );
    }

    private ConcurrentKafkaListenerContainerFactory<Object, Object> createFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> consumerFactory,
            KafkaTemplate<String, String> kafkaTemplate,
            String dltSuffix,
            long retryIntervalMs,
            long maxRetries
    ) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        configurer.configure(factory, consumerFactory);
        factory.setCommonErrorHandler(
                createErrorHandler(kafkaTemplate, dltSuffix, retryIntervalMs, maxRetries)
        );

        return factory;
    }

    private DefaultErrorHandler createErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate,
            String dltSuffix,
            long retryIntervalMs,
            long maxRetries
    ) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) ->
                        new TopicPartition(record.topic() + dltSuffix, record.partition())
        );
        recoverer.setFailIfSendResultIsError(true);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(retryIntervalMs, maxRetries)
        );
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);

        return errorHandler;
    }
}
