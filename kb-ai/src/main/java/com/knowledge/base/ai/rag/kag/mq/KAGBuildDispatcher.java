package com.knowledge.base.ai.rag.kag.mq;

import com.knowledge.base.ai.rag.kag.service.GraphBuildService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class KAGBuildDispatcher {
    private final GraphBuildService graphBuildService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${kag.rabbit.enabled:false}")
    private boolean rabbitEnabled;

    public void build(Long documentId) { dispatch(KAGBuildMessage.Type.BUILD_BY_DOC_IDS, List.of(documentId), KAGRabbitConfig.BUILD_KEY); }
    public void buildBatch(List<Long> documentIds) { dispatch(KAGBuildMessage.Type.BUILD_BY_DOC_IDS, documentIds, KAGRabbitConfig.BUILD_KEY); }
    public void buildAll() { dispatch(KAGBuildMessage.Type.BUILD_ALL, List.of(), KAGRabbitConfig.BUILD_KEY); }
    public void delete(Long documentId) { dispatch(KAGBuildMessage.Type.DELETE_BY_DOC_IDS, List.of(documentId), KAGRabbitConfig.DELETE_KEY); }

    private void dispatch(KAGBuildMessage.Type type, List<Long> ids, String routingKey) {
        KAGBuildMessage message = KAGBuildMessage.builder().type(type).documentIds(ids).build();
        if (rabbitEnabled) rabbitTemplate.convertAndSend(KAGRabbitConfig.EXCHANGE, routingKey, message);
        else CompletableFuture.runAsync(() -> process(message));
    }

    void process(KAGBuildMessage message) {
        switch (message.getType()) {
            case BUILD_BY_DOC_IDS -> graphBuildService.buildBatch(message.getDocumentIds());
            case BUILD_ALL -> graphBuildService.buildAll();
            case DELETE_BY_DOC_IDS -> message.getDocumentIds().forEach(graphBuildService::deleteForDocument);
        }
    }
}
