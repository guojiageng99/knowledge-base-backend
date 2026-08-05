package com.knowledge.base.file.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.knowledge.base.common.config.InstanceIdentifier;

@Configuration
public class TranscodeRabbitConfig {
    public static final String EXCHANGE = "transcode.exchange";
    private final InstanceIdentifier instanceIdentifier;
    public TranscodeRabbitConfig(InstanceIdentifier instanceIdentifier) { this.instanceIdentifier = instanceIdentifier; }
    public String queueName() { return "transcode.queue." + instanceIdentifier.getId(); }
    public String routingKey() { return "transcode." + instanceIdentifier.getId(); }
    @Bean public DirectExchange transcodeExchange() { return new DirectExchange(EXCHANGE, true, false); }
    @Bean public Queue transcodeQueue() { return QueueBuilder.durable(queueName()).build(); }
    @Bean public Binding transcodeBinding() { return BindingBuilder.bind(transcodeQueue()).to(transcodeExchange()).with(routingKey()); }
    @Bean public Jackson2JsonMessageConverter transcodeMessageConverter() { return new Jackson2JsonMessageConverter(); }
}
