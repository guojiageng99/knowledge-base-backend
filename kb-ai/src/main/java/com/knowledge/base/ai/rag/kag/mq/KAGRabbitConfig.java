package com.knowledge.base.ai.rag.kag.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "kag.rabbit.enabled", havingValue = "true")
public class KAGRabbitConfig {
    public static final String EXCHANGE = "kag.graph.build.exchange";
    public static final String QUEUE = "kag.graph.build.queue";
    public static final String BUILD_KEY = "kag.graph.build";
    public static final String DELETE_KEY = "kag.graph.delete";

    @Bean public TopicExchange kagExchange() { return new TopicExchange(EXCHANGE, true, false); }
    @Bean public Queue kagQueue() { return QueueBuilder.durable(QUEUE).build(); }
    @Bean public Binding kagBuildBinding() { return BindingBuilder.bind(kagQueue()).to(kagExchange()).with(BUILD_KEY); }
    @Bean public Binding kagDeleteBinding() { return BindingBuilder.bind(kagQueue()).to(kagExchange()).with(DELETE_KEY); }
    @Bean public Jackson2JsonMessageConverter kagMessageConverter() { return new Jackson2JsonMessageConverter(); }
}
