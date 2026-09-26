package com.ticketing.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic paymentEventsTopic(
            @Value("${app.kafka.topic.payment-events}") String topicName,
            @Value("${app.kafka.topic.payment-events-partitions}") int partitions,
            @Value("${app.kafka.topic.replication-factor}") int replicationFactor
    ) {
        return TopicBuilder.name(topicName)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    public NewTopic queueEventsTopic(
            @Value("${app.kafka.topic.queue-events}") String topicName,
            @Value("${app.kafka.topic.queue-events-partitions}") int partitions,
            @Value("${app.kafka.topic.replication-factor}") int replicationFactor
    ) {
        return createTopic(topicName, partitions, replicationFactor);
    }

    @Bean
    public NewTopic paymentReaggregationDltTopic(
            @Value("${app.kafka.topic.payment-events}") String topicName,
            @Value("${app.kafka.topic.payment-events-partitions}") int partitions,
            @Value("${app.kafka.topic.replication-factor}") int replicationFactor
    ) {
        return createTopic(topicName + ".reaggregation.DLT", partitions, replicationFactor);
    }

    @Bean
    public NewTopic paymentNotificationDltTopic(
            @Value("${app.kafka.topic.payment-events}") String topicName,
            @Value("${app.kafka.topic.payment-events-partitions}") int partitions,
            @Value("${app.kafka.topic.replication-factor}") int replicationFactor
    ) {
        return createTopic(topicName + ".notification.DLT", partitions, replicationFactor);
    }

    @Bean
    public NewTopic paymentTicketIssuanceDltTopic(
            @Value("${app.kafka.topic.payment-events}") String topicName,
            @Value("${app.kafka.topic.payment-events-partitions}") int partitions,
            @Value("${app.kafka.topic.replication-factor}") int replicationFactor
    ) {
        return createTopic(topicName + ".ticket-issuance.DLT", partitions, replicationFactor);
    }

    @Bean
    public NewTopic queueAnalyticsDltTopic(
            @Value("${app.kafka.topic.queue-events}") String topicName,
            @Value("${app.kafka.topic.queue-events-partitions}") int partitions,
            @Value("${app.kafka.topic.replication-factor}") int replicationFactor
    ) {
        return createTopic(topicName + ".analytics.DLT", partitions, replicationFactor);
    }

    private NewTopic createTopic(String topicName, int partitions, int replicationFactor) {
        return TopicBuilder.name(topicName)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }
}
