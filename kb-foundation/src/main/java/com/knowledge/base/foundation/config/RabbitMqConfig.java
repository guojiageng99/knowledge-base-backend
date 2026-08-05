package com.knowledge.base.foundation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.knowledge.base.common.config.InstanceIdentifier;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String NOTIFICATION_EXCHANGE = "kb.notification.exchange";
    public static final String CONFIG_EXCHANGE = "kb.config.exchange";
    public static final String OPERATION_LOG_EXCHANGE = "kb.operationlog.exchange";

    private final InstanceIdentifier instanceIdentifier;

    public RabbitMqConfig(InstanceIdentifier instanceIdentifier) {
        this.instanceIdentifier = instanceIdentifier;
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange configExchange() {
        return new DirectExchange(CONFIG_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange operationLogExchange() {
        return new TopicExchange(OPERATION_LOG_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable("kb.notification.queue." + instanceIdentifier.getId()).build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue()).to(notificationExchange())
                .with("notification." + instanceIdentifier.getId() + ".#");
    }

    @Bean
    public Queue reviewNotificationQueue() {
        return QueueBuilder.durable("kb.notification.review.queue." + instanceIdentifier.getId()).build();
    }

    @Bean
    public Binding reviewNotificationBinding() {
        return BindingBuilder.bind(reviewNotificationQueue()).to(notificationExchange())
                .with("notification.review." + instanceIdentifier.getId() + ".*");
    }

    @Bean
    public Queue configQueue() {
        return QueueBuilder.durable("kb.config.queue." + instanceIdentifier.getId()).build();
    }

    @Bean
    public Binding configBinding() {
        return BindingBuilder.bind(configQueue()).to(configExchange())
                .with("config." + instanceIdentifier.getId() + ".update");
    }

    @Bean
    public Queue operationLogQueue() {
        return QueueBuilder.durable("kb.operationlog.queue." + instanceIdentifier.getId()).build();
    }

    @Bean
    public Binding operationLogBinding() {
        return BindingBuilder.bind(operationLogQueue()).to(operationLogExchange())
                .with("operationlog." + instanceIdentifier.getId() + ".#");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("com.knowledge.base.common.event", "com.knowledge.base.foundation.dto", "java.util", "java.lang");
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(mapper);
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
