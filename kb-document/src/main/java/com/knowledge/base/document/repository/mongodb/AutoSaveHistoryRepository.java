package com.knowledge.base.document.repository.mongodb;

import com.knowledge.base.document.entity.mongodb.AutoSaveHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AutoSaveHistoryRepository extends MongoRepository<AutoSaveHistory, String> {
    Page<AutoSaveHistory> findByDocumentIdAndDeletedFalseOrderBySavedAtDesc(Long documentId, Pageable pageable);
}
