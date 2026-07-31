package com.knowledge.base.document.service.impl;

import com.knowledge.base.document.entity.mongodb.DocumentContent;
import com.knowledge.base.document.service.DocumentContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DocumentContentServiceImpl implements DocumentContentService {

    private final MongoTemplate mongoTemplate;

    @Override
    public String saveContent(Long documentId, String content) {
        DocumentContent documentContent = new DocumentContent();
        documentContent.setDocumentId(documentId);
        documentContent.setContent(content);
        documentContent.setCreateTime(LocalDateTime.now());
        documentContent.setUpdateTime(documentContent.getCreateTime());
        return mongoTemplate.save(documentContent).getId();
    }

    @Override
    public DocumentContent getContentById(String contentId) {
        return mongoTemplate.findById(contentId, DocumentContent.class);
    }

    @Override
    public String updateContent(Long documentId, String content) {
        DocumentContent documentContent = mongoTemplate.findOne(
                Query.query(Criteria.where("documentId").is(documentId)), DocumentContent.class);
        if (documentContent == null) {
            return saveContent(documentId, content);
        }
        documentContent.setContent(content);
        documentContent.setUpdateTime(LocalDateTime.now());
        return mongoTemplate.save(documentContent).getId();
    }

    @Override
    public void deleteContent(String contentId) {
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(contentId)), DocumentContent.class);
    }
}
