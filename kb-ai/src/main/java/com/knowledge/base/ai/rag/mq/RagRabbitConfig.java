package com.knowledge.base.ai.rag.mq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;

@Configuration
@ConditionalOnProperty(name = "rag.rabbit.enabled", havingValue = "true")
public class RagRabbitConfig {
    public static final String EXCHANGE = "rag.reindex.exchange";
    public static final String QUEUE = "rag.reindex.queue";
    public static final String ROUTING_KEY_ALL = "rag.reindex.all";
    public static final String ROUTING_KEY_BY_IDS = "rag.reindex.by_ids";
    public static final String ROUTING_KEY_DELETE = "rag.reindex.delete";
    public static final String DEAD_LETTER_EXCHANGE = EXCHANGE + ".dlx";
    public static final String DEAD_LETTER_QUEUE = QUEUE + ".dlq";
    @Bean public TopicExchange ragExchange() { return new TopicExchange(EXCHANGE, true, false); }
    @Bean public DirectExchange ragDeadLetterExchange() { return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false); }
    @Bean public Queue ragQueue() { return QueueBuilder.durable(QUEUE).withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE).build(); }
    @Bean public Queue ragDeadLetterQueue() { return QueueBuilder.durable(DEAD_LETTER_QUEUE).build(); }
    @Bean public Binding ragDeadLetterBinding() { return BindingBuilder.bind(ragDeadLetterQueue()).to(ragDeadLetterExchange()).with(QUEUE); }
    @Bean public Binding allBinding() { return BindingBuilder.bind(ragQueue()).to(ragExchange()).with(ROUTING_KEY_ALL); }
    @Bean public Binding byIdsBinding() { return BindingBuilder.bind(ragQueue()).to(ragExchange()).with(ROUTING_KEY_BY_IDS); }
    @Bean public Binding deleteBinding() { return BindingBuilder.bind(ragQueue()).to(ragExchange()).with(ROUTING_KEY_DELETE); }
    @Bean public Jackson2JsonMessageConverter ragMessageConverter() { return new Jackson2JsonMessageConverter(); }
}
