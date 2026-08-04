package com.knowledge.base.document.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.document.dto.AutoSaveHistoryQueryDTO;
import com.knowledge.base.document.entity.mongodb.AutoSaveHistory;
import com.knowledge.base.document.repository.mongodb.AutoSaveHistoryRepository;
import com.knowledge.base.document.service.AutoSaveHistoryService;
import com.knowledge.base.document.vo.AutoSaveHistoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoSaveHistoryServiceImpl implements AutoSaveHistoryService {
    private final AutoSaveHistoryRepository repository;

    @Override
    public void saveSnapshot(Long documentId, String title, String content, Long authorId) {
        if (content == null || content.isBlank()) return;
        try {
            repository.save(AutoSaveHistory.builder()
                    .documentId(documentId).title(title).content(content)
                    .contentPreview(content.substring(0, Math.min(content.length(), 200)))
                    .contentLength(content.length()).authorId(authorId)
                    .savedAt(LocalDateTime.now()).build());
        } catch (RuntimeException exception) {
            log.warn("Automatic-save snapshot failed for document {}", documentId, exception);
        }
    }

    @Override
    public IPage<AutoSaveHistoryVO> pageHistory(AutoSaveHistoryQueryDTO query) {
        long current = Math.max(1, query.getCurrent());
        long size = Math.min(100, Math.max(1, query.getSize()));
        org.springframework.data.domain.Page<AutoSaveHistory> result = repository
                .findByDocumentIdAndDeletedFalseOrderBySavedAtDesc(query.getDocumentId(), PageRequest.of((int) current - 1, (int) size));
        Page<AutoSaveHistoryVO> page = new Page<>(current, size, result.getTotalElements());
        page.setRecords(result.getContent().stream().map(this::toVO).toList());
        return page;
    }

    @Override
    public AutoSaveHistoryVO getSnapshot(String snapshotId, Long documentId) {
        AutoSaveHistory snapshot = repository.findById(snapshotId)
                .orElseThrow(() -> new BusinessException("Automatic-save snapshot does not exist"));
        if (!documentId.equals(snapshot.getDocumentId()) || Boolean.TRUE.equals(snapshot.getDeleted())) {
            throw new BusinessException("Automatic-save snapshot does not belong to this document");
        }
        AutoSaveHistoryVO vo = toVO(snapshot);
        vo.setContent(snapshot.getContent());
        return vo;
    }

    @Override
    public void deleteByDocumentId(Long documentId) {
        repository.findByDocumentIdAndDeletedFalseOrderBySavedAtDesc(documentId, PageRequest.of(0, 1000))
                .forEach(snapshot -> { snapshot.setDeleted(true); repository.save(snapshot); });
    }

    private AutoSaveHistoryVO toVO(AutoSaveHistory source) {
        return AutoSaveHistoryVO.builder().id(source.getId()).documentId(source.getDocumentId()).title(source.getTitle())
                .contentPreview(source.getContentPreview()).contentLength(source.getContentLength()).savedAt(source.getSavedAt()).build();
    }
}
