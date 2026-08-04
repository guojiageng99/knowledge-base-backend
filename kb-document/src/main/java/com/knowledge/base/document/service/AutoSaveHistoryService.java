package com.knowledge.base.document.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledge.base.document.dto.AutoSaveHistoryQueryDTO;
import com.knowledge.base.document.vo.AutoSaveHistoryVO;

public interface AutoSaveHistoryService {
    void saveSnapshot(Long documentId, String title, String content, Long authorId);
    IPage<AutoSaveHistoryVO> pageHistory(AutoSaveHistoryQueryDTO query);
    AutoSaveHistoryVO getSnapshot(String snapshotId, Long documentId);
    void deleteByDocumentId(Long documentId);
}
