package com.knowledge.base.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.base.document.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DocumentMapper extends BaseMapper<Document> {

    int incrementViewCount(@Param("documentId") Long documentId);

    int incrementLikeCount(@Param("documentId") Long documentId);

    int decrementLikeCount(@Param("documentId") Long documentId);

    int incrementFavoriteCount(@Param("documentId") Long documentId);

    int decrementFavoriteCount(@Param("documentId") Long documentId);

    int incrementCommentCount(@Param("documentId") Long documentId);
}
