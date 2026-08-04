package com.knowledge.base.ai.rag.service.impl;

import com.knowledge.base.ai.rag.client.DocumentFeignClient;
import com.knowledge.base.ai.rag.config.RagProperties;
import com.knowledge.base.ai.rag.entity.DocumentChunk;
import com.knowledge.base.ai.rag.mq.RagRabbitConfig;
import com.knowledge.base.ai.rag.mq.ReindexMessage;
import com.knowledge.base.ai.rag.service.*;
import com.knowledge.base.ai.rag.vo.ReindexProgressVO;
import com.knowledge.base.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.*;

@Service @RequiredArgsConstructor
public class ReindexServiceImpl implements ReindexService {
    private final DocumentFeignClient documents; private final ChunkingService chunking; private final EmbeddingService embeddings; private final VectorIndexService index;
    private final RabbitTemplate rabbitTemplate;
    private final RagProperties properties;
    private final Map<String, ReindexProgressVO> progress = new ConcurrentHashMap<>();
    @Override public String reindexByDocId(Long id){return submitMessage(List.of(id), ReindexMessage.ReindexType.BY_DOC_IDS, RagRabbitConfig.ROUTING_KEY_BY_IDS);}
    @Override public String reindexBatch(List<Long> ids){return submitMessage(ids, ReindexMessage.ReindexType.BY_DOC_IDS, RagRabbitConfig.ROUTING_KEY_BY_IDS);}
    @Override public String reindexAll(){return submitMessage(List.of(), ReindexMessage.ReindexType.ALL, RagRabbitConfig.ROUTING_KEY_ALL);}
    @Override public String deleteByDocId(Long id){return submitMessage(List.of(id), ReindexMessage.ReindexType.DELETE_BY_DOC_IDS, RagRabbitConfig.ROUTING_KEY_DELETE);}
    @Override public ReindexProgressVO getProgress(String id){return progress.getOrDefault(id,ReindexProgressVO.builder().taskId(id).status("NOT_FOUND").build());}
    @Override public void process(ReindexMessage message) {
        String taskId = message.getTaskId();
        try {
            if (message.getType() == ReindexMessage.ReindexType.DELETE_BY_DOC_IDS) {
                for (Long id : message.getDocumentIds()) index.deleteByDocId(id);
                done(taskId, message.getDocumentIds().size());
                return;
            }
            if (message.getType() == ReindexMessage.ReindexType.BY_DOC_IDS) {
                for (Long id : message.getDocumentIds()) one(id, taskId);
                complete(taskId);
                return;
            }
            long pageNo = 1;
            int total = 0;
            while (true) {
                Result<Map<String,Object>> page = documents.pageDocuments(pageNo, 100L, 1);
                if (page == null || page.getData() == null) break;
                Object recordsValue = page.getData().get("records");
                if (!(recordsValue instanceof List<?> records) || records.isEmpty()) break;
                for (Object record : records) if (record instanceof Map<?,?> item) {
                    Long id = number(item.get("id")); if (id != null) { one(id, taskId); total++; }
                }
                Object pages = page.getData().get("pages");
                if (pages instanceof Number count && pageNo >= count.longValue()) break;
                pageNo++;
            }
            progress.put(taskId, ReindexProgressVO.builder().taskId(taskId).status("COMPLETED").total(total).completed(total).build());
        } catch (Exception e) { failed(taskId, e); }
    }
    private String submit(List<Long> ids,boolean ignored){String task=task(ids==null?0:ids.size());CompletableFuture.runAsync(()->{try{if(ids!=null)for(Long id:ids)one(id,task);progress.computeIfPresent(task,(k,p)->ReindexProgressVO.builder().taskId(k).status("COMPLETED").total(p.getTotal()).completed(p.getTotal()).build());}catch(Exception e){failed(task,e);}});return task;}
    private void one(Long id,String task){Result<Map<String,Object>> result=documents.getDocument(id);if(result==null||result.getData()==null)throw new IllegalArgumentException("Document not found: "+id);Map<String,Object>d=result.getData();String content=String.valueOf(d.getOrDefault("content",""));List<DocumentChunk> chunks=chunking.chunk(content,id,String.valueOf(d.getOrDefault("title","")),number(d.get("categoryId")),number(d.get("authorId")),null,integer(d.get("status")));chunks.forEach(c->c.setEmbedding(embeddings.embed(c.getContent())));index.deleteByDocId(id);index.indexChunks(chunks);progress.computeIfPresent(task,(k,p)->ReindexProgressVO.builder().taskId(k).status("RUNNING").total(p.getTotal()).completed(p.getCompleted()+1).build());}
    private String task(int total){String id=UUID.randomUUID().toString();progress.put(id,ReindexProgressVO.builder().taskId(id).status("RUNNING").total(total).completed(0).build());return id;}
    private String submitMessage(List<Long> ids, ReindexMessage.ReindexType type, String routingKey) {
        String taskId = task(type == ReindexMessage.ReindexType.ALL ? 0 : ids.size());
        ReindexMessage message = ReindexMessage.builder().taskId(taskId).type(type).documentIds(ids).build();
        if (properties.getRabbit().isEnabled()) rabbitTemplate.convertAndSend(RagRabbitConfig.EXCHANGE, routingKey, message);
        else CompletableFuture.runAsync(() -> process(message));
        return taskId;
    }
    private void complete(String taskId) { ReindexProgressVO current = progress.get(taskId); if (current != null) progress.put(taskId, ReindexProgressVO.builder().taskId(taskId).status("COMPLETED").total(current.getTotal()).completed(current.getTotal()).build()); }
    private void done(String id,int count){progress.put(id,ReindexProgressVO.builder().taskId(id).status("COMPLETED").total(count).completed(count).build());}
    private void failed(String id,Exception e){progress.computeIfPresent(id,(k,p)->ReindexProgressVO.builder().taskId(k).status("FAILED").total(p.getTotal()).completed(p.getCompleted()).error(e.getMessage()).build());}
    private Long number(Object value){return value instanceof Number n?n.longValue():value==null?null:Long.valueOf(value.toString());}
    private Integer integer(Object value){return value instanceof Number n?n.intValue():value==null?null:Integer.valueOf(value.toString());}
}
