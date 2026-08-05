package com.knowledge.base.ai.rag.mq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import com.knowledge.base.common.config.InstanceIdentifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;

@Configuration
@ConditionalOnProperty(name = "rag.rabbit.enabled", havingValue = "true")
public class RagRabbitConfig {
    public static final String EXCHANGE = "rag.reindex.exchange";
    public static final String QUEUE_PREFIX = "rag.reindex.queue.";
    public static final String DEAD_LETTER_EXCHANGE = EXCHANGE + ".dlx";
    private final InstanceIdentifier instanceIdentifier;
    public RagRabbitConfig(InstanceIdentifier instanceIdentifier) { this.instanceIdentifier = instanceIdentifier; }
    public String queueName() { return QUEUE_PREFIX + instanceIdentifier.getId(); }
    public String routingKeyAll() { return "rag.reindex." + instanceIdentifier.getId() + ".all"; }
    public String routingKeyByIds() { return "rag.reindex." + instanceIdentifier.getId() + ".by_ids"; }
    public String routingKeyDelete() { return "rag.reindex." + instanceIdentifier.getId() + ".delete"; }
    public String deadLetterQueueName() { return queueName() + ".dlq"; }
    @Bean public TopicExchange ragExchange() { return new TopicExchange(EXCHANGE, true, false); }
    @Bean public DirectExchange ragDeadLetterExchange() { return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false); }
    @Bean public Queue ragQueue() { return QueueBuilder.durable(queueName()).withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE).build(); }
    @Bean public Queue ragDeadLetterQueue() { return QueueBuilder.durable(deadLetterQueueName()).build(); }
    @Bean public Binding ragDeadLetterBinding() { return BindingBuilder.bind(ragDeadLetterQueue()).to(ragDeadLetterExchange()).with(queueName()); }
    @Bean public Binding allBinding() { return BindingBuilder.bind(ragQueue()).to(ragExchange()).with(routingKeyAll()); }
    @Bean public Binding byIdsBinding() { return BindingBuilder.bind(ragQueue()).to(ragExchange()).with(routingKeyByIds()); }
    @Bean public Binding deleteBinding() { return BindingBuilder.bind(ragQueue()).to(ragExchange()).with(routingKeyDelete()); }
    @Bean public Jackson2JsonMessageConverter ragMessageConverter() { return new Jackson2JsonMessageConverter(); }
}
