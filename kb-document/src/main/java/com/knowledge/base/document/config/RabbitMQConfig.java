package com.knowledge.base.document.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.knowledge.base.common.config.InstanceIdentifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {
    public static final String NOTIFICATION_EXCHANGE = "kb.notification.exchange";
    public static final String STATISTICS_EXCHANGE = "kb.statistics.exchange";
    private final InstanceIdentifier instanceIdentifier;

    public RabbitMQConfig(InstanceIdentifier instanceIdentifier) {
        this.instanceIdentifier = instanceIdentifier;
    }

    @Bean
    public TopicExchange notificationExchange() { return new TopicExchange(NOTIFICATION_EXCHANGE, true, false); }

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
    public TopicExchange statisticsExchange() { return new TopicExchange(STATISTICS_EXCHANGE, true, false); }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("com.knowledge.base.common.event", "com.knowledge.base.document.dto", "java.util", "java.lang");
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(mapper);
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setMandatory(true);
        template.setReturnsCallback(returned -> log.warn("RabbitMQ message was not routed: exchange={}, routingKey={}",
                returned.getExchange(), returned.getRoutingKey()));
        return template;
    }

    public String reviewRoutingKey(String eventType) {
        return "notification.review." + instanceIdentifier.getId() + "." + eventType;
    }

    public String statisticsRoutingKey(String eventType) {
        return "statistics." + eventType + "." + instanceIdentifier.getId() + ".record";
    }
}
