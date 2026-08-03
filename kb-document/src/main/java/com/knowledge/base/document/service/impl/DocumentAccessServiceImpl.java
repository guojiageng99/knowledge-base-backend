package com.knowledge.base.document.service.impl;

import com.knowledge.base.document.entity.DocumentAccess;
import com.knowledge.base.document.mapper.DocumentAccessMapper;
import com.knowledge.base.document.service.DocumentAccessService;
import com.knowledge.base.document.utils.UserContext;
import com.knowledge.base.document.vo.DocumentAccessVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentAccessServiceImpl implements DocumentAccessService {

    private static final int DEFAULT_LIMIT = 20;
    private final DocumentAccessMapper documentAccessMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordAccess(Long userId, Long documentId, String documentTitle) {
        if (userId == null || documentId == null) return;
        documentAccessMapper.deleteByUserIdAndDocumentId(userId, documentId);
        DocumentAccess access = new DocumentAccess();
        access.setUserId(userId);
        access.setDocumentId(documentId);
        access.setDocumentTitle(documentTitle);
        access.setAccessTime(LocalDateTime.now());
        access.setCreatedBy(userId);
        access.setUpdatedBy(userId);
        documentAccessMapper.insert(access);
    }

    @Override
    public List<DocumentAccessVO> getRecentAccess(Integer limit) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) return List.of();
        int queryLimit = limit != null && limit > 0 ? Math.min(limit, 100) : DEFAULT_LIMIT;
        return documentAccessMapper.selectRecentAccessByUserId(userId, queryLimit).stream().map(this::toVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccess(Long documentId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId != null) documentAccessMapper.deleteByUserIdAndDocumentId(userId, documentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAllAccess() {
        Long userId = UserContext.getCurrentUserId();
        if (userId != null) documentAccessMapper.deleteAllByUserId(userId);
    }

    private DocumentAccessVO toVO(DocumentAccess access) {
        DocumentAccessVO vo = new DocumentAccessVO();
        vo.setId(access.getId());
        vo.setUserId(access.getUserId());
        vo.setDocumentId(access.getDocumentId());
        vo.setDocumentTitle(access.getDocumentTitle());
        vo.setSummary(access.getSummary());
        vo.setCategoryName(access.getCategoryName());
        vo.setAuthorName(access.getAuthorName());
        vo.setAccessTime(access.getAccessTime());
        vo.setStatus(access.getStatus());
        return vo;
    }
}
