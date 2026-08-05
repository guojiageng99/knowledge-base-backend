package com.knowledge.base.statistics.mq;

import com.knowledge.base.common.event.StatisticsEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StatisticsMQListener {
    @RabbitListener(queues = "#{@statisticsViewQueue.name}")
    public void handleViewEvent(StatisticsEventDTO event) { log.debug("Received view statistics event: {}", event); }
    @RabbitListener(queues = "#{@statisticsLikeQueue.name}")
    public void handleLikeEvent(StatisticsEventDTO event) { log.debug("Received like statistics event: {}", event); }
    @RabbitListener(queues = "#{@statisticsCommentQueue.name}")
    public void handleCommentEvent(StatisticsEventDTO event) { log.debug("Received comment statistics event: {}", event); }
}
