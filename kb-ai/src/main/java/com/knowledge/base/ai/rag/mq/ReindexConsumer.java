package com.knowledge.base.ai.rag.mq;

import com.knowledge.base.ai.rag.service.impl.ReindexServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
@ConditionalOnProperty(name = "rag.rabbit.enabled", havingValue = "true")
public class ReindexConsumer {
    private final ReindexServiceImpl reindexService;
    @RabbitListener(queues = "#{@ragRabbitConfig.queueName()}")
    public void consume(ReindexMessage message) {
        reindexService.process(message);
    }
}
