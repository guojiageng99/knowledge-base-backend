package com.knowledge.base.document.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledge.base.document.dto.DocumentVersionRestoreDTO;
import com.knowledge.base.document.vo.DocumentVersionVO;

public interface DocumentVersionService {

    boolean createVersion(Long documentId, String changeDescription, Long userId);

    IPage<DocumentVersionVO> getVersionList(Long documentId, Long current, Long size);

    DocumentVersionVO getVersionDetail(Long versionId);

    boolean restoreVersion(Long documentId, DocumentVersionRestoreDTO restoreDTO, Long userId);

    String compareVersions(Long versionId1, Long versionId2);

    boolean deleteVersion(Long versionId, Long userId);
}
