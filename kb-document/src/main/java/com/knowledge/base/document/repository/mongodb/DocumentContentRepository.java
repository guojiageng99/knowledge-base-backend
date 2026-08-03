package com.knowledge.base.document.repository.mongodb;

import com.knowledge.base.document.entity.mongodb.DocumentContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentContentRepository extends MongoRepository<DocumentContent, String> {

    Optional<DocumentContent> findByDocumentId(Long documentId);
}
