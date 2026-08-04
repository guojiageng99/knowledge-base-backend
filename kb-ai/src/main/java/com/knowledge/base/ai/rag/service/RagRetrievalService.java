package com.knowledge.base.ai.rag.service;
import com.knowledge.base.ai.rag.vo.RagSearchResultVO;
import java.util.List;
public interface RagRetrievalService { List<RagSearchResultVO> retrieve(String query, int topK, boolean enableRerank); }
