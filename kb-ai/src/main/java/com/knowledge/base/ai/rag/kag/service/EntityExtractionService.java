package com.knowledge.base.ai.rag.kag.service;

import com.knowledge.base.ai.rag.kag.dto.ExtractionResult;

public interface EntityExtractionService {
    ExtractionResult extract(String content, String heading);
}
