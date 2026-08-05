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
import com.knowledge.base.common.config.InstanceIdentifier;

@Configuration
@ConditionalOnProperty(name = "kag.rabbit.enabled", havingValue = "true")
public class KAGRabbitConfig {
    public static final String EXCHANGE = "kag.graph.build.exchange";
    private final InstanceIdentifier instanceIdentifier;
    public KAGRabbitConfig(InstanceIdentifier instanceIdentifier) { this.instanceIdentifier = instanceIdentifier; }
    public String queueName() { return "kag.graph.build.queue." + instanceIdentifier.getId(); }
    public String buildKey() { return "kag.graph.build." + instanceIdentifier.getId() + ".all"; }
    public String buildByIdsKey() { return "kag.graph.build." + instanceIdentifier.getId() + ".by_ids"; }
    public String deleteKey() { return "kag.graph.delete." + instanceIdentifier.getId(); }

    @Bean public TopicExchange kagExchange() { return new TopicExchange(EXCHANGE, true, false); }
    @Bean public Queue kagQueue() { return QueueBuilder.durable(queueName()).build(); }
    @Bean public Binding kagBuildBinding() { return BindingBuilder.bind(kagQueue()).to(kagExchange()).with(buildKey()); }
    @Bean public Binding kagBuildByIdsBinding() { return BindingBuilder.bind(kagQueue()).to(kagExchange()).with(buildByIdsKey()); }
    @Bean public Binding kagDeleteBinding() { return BindingBuilder.bind(kagQueue()).to(kagExchange()).with(deleteKey()); }
    @Bean public Jackson2JsonMessageConverter kagMessageConverter() { return new Jackson2JsonMessageConverter(); }
}
