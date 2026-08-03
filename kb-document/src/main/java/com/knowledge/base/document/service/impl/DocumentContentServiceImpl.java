package com.knowledge.base.document.service.impl;

import com.knowledge.base.document.entity.mongodb.DocumentContent;
import com.knowledge.base.document.repository.mongodb.DocumentContentRepository;
import com.knowledge.base.document.service.DocumentContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DocumentContentServiceImpl implements DocumentContentService {

    private final DocumentContentRepository documentContentRepository;

    @Override
    public String saveContent(Long documentId, String content) {
        DocumentContent documentContent = new DocumentContent();
        documentContent.setDocumentId(documentId);
        documentContent.setContent(content);
        documentContent.setWordCount(content == null ? 0 : content.length());
        documentContent.setCreatedAt(LocalDateTime.now());
        documentContent.setUpdatedAt(documentContent.getCreatedAt());
        return documentContentRepository.save(documentContent).getId();
    }

    @Override
    public DocumentContent getContentById(String contentId) {
        return documentContentRepository.findById(contentId).orElse(null);
    }

    @Override
    public String updateContent(Long documentId, String content) {
        DocumentContent documentContent = documentContentRepository.findByDocumentId(documentId).orElse(null);
        if (documentContent == null) {
            return saveContent(documentId, content);
        }
        documentContent.setContent(content);
        documentContent.setWordCount(content == null ? 0 : content.length());
        documentContent.setUpdatedAt(LocalDateTime.now());
        return documentContentRepository.save(documentContent).getId();
    }

    @Override
    public void deleteContent(String contentId) {
        documentContentRepository.deleteById(contentId);
    }
}
