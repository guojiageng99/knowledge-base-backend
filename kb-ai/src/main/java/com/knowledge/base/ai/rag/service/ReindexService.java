package com.knowledge.base.ai.rag.service;
import com.knowledge.base.ai.rag.vo.ReindexProgressVO;
import com.knowledge.base.ai.rag.mq.ReindexMessage;
import java.util.List;
public interface ReindexService { String reindexByDocId(Long id); String reindexBatch(List<Long> ids); String reindexAll(); String deleteByDocId(Long id); ReindexProgressVO getProgress(String taskId); void process(ReindexMessage message); }
