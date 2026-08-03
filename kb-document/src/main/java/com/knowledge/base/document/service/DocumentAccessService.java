package com.knowledge.base.document.service;

import com.knowledge.base.document.vo.DocumentAccessVO;

import java.util.List;

public interface DocumentAccessService {
    void recordAccess(Long userId, Long documentId, String documentTitle);
    List<DocumentAccessVO> getRecentAccess(Integer limit);
    void deleteAccess(Long documentId);
    void clearAllAccess();
}
