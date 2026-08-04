package com.knowledge.base.document.service;

import com.knowledge.base.document.dto.ShareDTO;
import com.knowledge.base.document.vo.DocumentVO;
import com.knowledge.base.document.vo.ShareVO;

import java.util.List;

public interface DocumentShareService {
    ShareVO createShare(ShareDTO dto);
    ShareVO getShareById(String shareId);
    boolean verifyShareAccess(String shareId, String password);
    Long accessShare(String shareId, String password);
    DocumentVO getSharedDocument(String shareId, String password);
    List<ShareVO> getSharesByDocumentId(Long documentId);
    List<ShareVO> getMyShares();
    boolean deleteShare(String shareId);
    boolean updateShare(String shareId, ShareDTO dto);
}
