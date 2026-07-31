package com.knowledge.base.document.service;

import com.knowledge.base.document.entity.mongodb.DocumentContent;

public interface DocumentContentService {

    String saveContent(Long documentId, String content);

    DocumentContent getContentById(String contentId);

    String updateContent(Long documentId, String content);

    void deleteContent(String contentId);
}
