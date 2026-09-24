package com.ticketing.config;

import com.ticketing.notification.messaging.NotificationRedisPublisher;
import com.ticketing.notification.messaging.NotificationRedisSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class NotificationRedisConfig {

    @Bean
    public RedisMessageListenerContainer notificationRedisListenerContainer(
            RedisConnectionFactory connectionFactory,
            NotificationRedisSubscriber subscriber
    ) {
        RedisMessageListenerContainer container =
                new RedisMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(
                subscriber,
                new ChannelTopic(
                        NotificationRedisPublisher.NOTIFICATION_CREATED_CHANNEL
                )
        );

        return container;
    }
}
