package com.knowledge.base.ai.rag.service;
import java.util.List;
public interface EmbeddingService { float[] embed(String text); List<float[]> embedBatch(List<String> texts); }
