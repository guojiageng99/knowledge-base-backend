package com.knowledge.base.statistics.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.knowledge.base.common.config.InstanceIdentifier;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatisticsMQConfig {
    public static final String STATISTICS_EXCHANGE = "kb.statistics.exchange";
    private final InstanceIdentifier instanceIdentifier;
    public StatisticsMQConfig(InstanceIdentifier instanceIdentifier) { this.instanceIdentifier = instanceIdentifier; }
    public String queueName(String type) { return "kb.statistics." + type + ".queue." + instanceIdentifier.getId(); }
    public String routingKey(String type) { return "statistics." + type + "." + instanceIdentifier.getId() + ".record"; }
    @Bean public TopicExchange statisticsExchange() { return new TopicExchange(STATISTICS_EXCHANGE, true, false); }
    @Bean public Queue statisticsViewQueue() { return QueueBuilder.durable(queueName("view")).build(); }
    @Bean public Queue statisticsLikeQueue() { return QueueBuilder.durable(queueName("like")).build(); }
    @Bean public Queue statisticsCommentQueue() { return QueueBuilder.durable(queueName("comment")).build(); }
    @Bean public Binding statisticsViewBinding() { return BindingBuilder.bind(statisticsViewQueue()).to(statisticsExchange()).with(routingKey("view")); }
    @Bean public Binding statisticsLikeBinding() { return BindingBuilder.bind(statisticsLikeQueue()).to(statisticsExchange()).with(routingKey("like")); }
    @Bean public Binding statisticsCommentBinding() { return BindingBuilder.bind(statisticsCommentQueue()).to(statisticsExchange()).with(routingKey("comment")); }
    @Bean public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("com.knowledge.base.common.event", "java.util", "java.lang");
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(mapper);
        converter.setClassMapper(classMapper);
        return converter;
    }
}
