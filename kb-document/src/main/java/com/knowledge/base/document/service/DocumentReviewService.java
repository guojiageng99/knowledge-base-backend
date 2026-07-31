package com.knowledge.base.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.knowledge.base.common.result.PageResult;
import com.knowledge.base.document.dto.DocumentReviewDTO;
import com.knowledge.base.document.dto.ReviewQueryDTO;
import com.knowledge.base.document.entity.DocumentReview;
import com.knowledge.base.document.vo.DocumentReviewVO;

import java.util.List;

public interface DocumentReviewService extends IService<DocumentReview> {

    Boolean submitForReview(Long documentId);

    Boolean approveReview(DocumentReviewDTO dto);

    Boolean rejectReview(DocumentReviewDTO dto);

    PageResult<DocumentReviewVO> getPendingReviews(ReviewQueryDTO dto);

    List<DocumentReviewVO> getDocumentReviewHistory(Long documentId);
}
