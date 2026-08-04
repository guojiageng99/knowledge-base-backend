package com.knowledge.base.file.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "file.transcode.rabbit.enabled", havingValue = "true")
public class TranscodeRabbitConfig {
    public static final String EXCHANGE = "transcode.exchange";
    public static final String QUEUE = "transcode.queue";
    public static final String ROUTING_KEY = "transcode";
    @Bean public DirectExchange transcodeExchange() { return new DirectExchange(EXCHANGE, true, false); }
    @Bean public Queue transcodeQueue() { return QueueBuilder.durable(QUEUE).build(); }
    @Bean public Binding transcodeBinding() { return BindingBuilder.bind(transcodeQueue()).to(transcodeExchange()).with(ROUTING_KEY); }
    @Bean public Jackson2JsonMessageConverter transcodeMessageConverter() { return new Jackson2JsonMessageConverter(); }
}
