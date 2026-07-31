package com.knowledge.base.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.base.document.entity.DocumentReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DocumentReviewMapper extends BaseMapper<DocumentReview> {

    List<DocumentReview> selectByDocumentId(@Param("documentId") Long documentId);

    List<DocumentReview> selectByReviewerId(@Param("reviewerId") Long reviewerId);

    List<DocumentReview> selectByDocumentIdAndRound(@Param("documentId") Long documentId,
                                                     @Param("reviewRound") Integer reviewRound);

    DocumentReview selectLatestByDocumentId(@Param("documentId") Long documentId);

    Integer countRoundsByDocumentId(@Param("documentId") Long documentId);

    Long countByReviewerId(@Param("reviewerId") Long reviewerId);

    List<DocumentReview> selectPendingReviews(@Param("reviewerId") Long reviewerId);
}
