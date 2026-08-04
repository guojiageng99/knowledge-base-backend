package com.knowledge.base.ai.rag.service.impl;
import com.knowledge.base.ai.rag.config.RagProperties;
import com.knowledge.base.ai.config.ModelProvider;
import com.knowledge.base.ai.rag.service.*;
import com.knowledge.base.ai.rag.vo.RagSearchResultVO;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
@Service @RequiredArgsConstructor public class RagRetrievalServiceImpl implements RagRetrievalService {
 private final EmbeddingService embeddingService; private final VectorIndexService vectorIndexService; private final RagProperties properties; private final ModelProvider modelProvider;
 @Override public List<RagSearchResultVO> retrieve(String query,int topK,boolean rerank){
  vectorIndexService.createIndexIfNotExists(); int candidateK=Math.max(topK*2,properties.getRetrieval().getHybridTopK());
  List<RagSearchResultVO> candidates=vectorIndexService.searchHybrid(query,embeddingService.embed(query),candidateK,properties.getRetrieval().getHybridTopK(),properties.getRetrieval().getRrfC());
  return rerank && properties.getRerank().isEnabled() && candidates.size()>topK ? rerank(candidates,query,topK) : candidates.stream().limit(topK).toList();
 }
 private List<RagSearchResultVO> rerank(List<RagSearchResultVO> candidates,String query,int topK){
  ChatLanguageModel model=modelProvider.getModel(properties.getRerank().getModel()); List<Scored> scored=new ArrayList<>();
  for(RagSearchResultVO candidate:candidates){
   String content=candidate.getContent()==null?"":candidate.getContent(); String prompt=String.format("你是搜索相关性评估专家。请仅返回1到10的整数评分。用户查询：%s\n文档片段：%s",query,content.substring(0,Math.min(1000,content.length())));
   int score=parseScore(model.generate(UserMessage.from(prompt)).content().text()); scored.add(new Scored(candidate,score));
  }
  scored.sort(Comparator.comparingInt(Scored::score).reversed()); return scored.stream().limit(topK).map(item->{item.result().setScore(item.score());return item.result();}).toList();
 }
 private int parseScore(String value){try{java.util.regex.Matcher m=java.util.regex.Pattern.compile("\\b(10|[1-9])\\b").matcher(value==null?"":value);return m.find()?Integer.parseInt(m.group(1)):0;}catch(Exception ignored){return 0;}}
 private record Scored(RagSearchResultVO result,int score){}
}
