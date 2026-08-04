package com.knowledge.base.ai.rag.kag.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kag.rabbit.enabled", havingValue = "true")
public class KAGReindexConsumer {
    private final KAGBuildDispatcher dispatcher;

    @RabbitListener(queues = KAGRabbitConfig.QUEUE)
    public void onMessage(KAGBuildMessage message) {
        try {
            dispatcher.process(message);
        } catch (Exception exception) {
            log.error("Knowledge graph build message failed: {}", message, exception);
            throw exception;
        }
    }
}
