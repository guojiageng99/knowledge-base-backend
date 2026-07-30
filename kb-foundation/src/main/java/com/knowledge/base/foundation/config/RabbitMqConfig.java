package com.knowledge.base.foundation.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String NOTIFICATION_EXCHANGE = "kb.notification.exchange";
    public static final String NOTIFICATION_QUEUE = "kb.notification.queue";
    public static final String CONFIG_EXCHANGE = "kb.config.exchange";
    public static final String CONFIG_QUEUE = "kb.config.queue";

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue()).to(notificationExchange()).with("notification.#");
    }

    @Bean
    public DirectExchange configExchange() {
        return new DirectExchange(CONFIG_EXCHANGE, true, false);
    }

    @Bean
    public Queue configQueue() {
        return QueueBuilder.durable(CONFIG_QUEUE).build();
    }

    @Bean
    public Binding configBinding() {
        return BindingBuilder.bind(configQueue()).to(configExchange()).with("config.update");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }
}
