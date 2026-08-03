package com.knowledge.base.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.base.document.entity.DocumentAccess;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DocumentAccessMapper extends BaseMapper<DocumentAccess> {
    @Select("SELECT da.*, d.summary, c.category_name AS categoryName, d.author_name AS authorName, d.status " +
            "FROM kb_document_access da LEFT JOIN kb_document d ON da.document_id = d.id AND d.deleted = 0 " +
            "LEFT JOIN kb_category c ON d.category_id = c.id AND c.deleted = 0 " +
            "WHERE da.user_id = #{userId} AND da.deleted = 0 AND d.id IS NOT NULL " +
            "ORDER BY da.access_time DESC LIMIT #{limit}")
    List<DocumentAccess> selectRecentAccessByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    @Delete("DELETE FROM kb_document_access WHERE user_id = #{userId} AND document_id = #{documentId}")
    int deleteByUserIdAndDocumentId(@Param("userId") Long userId, @Param("documentId") Long documentId);

    @Delete("DELETE FROM kb_document_access WHERE user_id = #{userId}")
    int deleteAllByUserId(@Param("userId") Long userId);
}
