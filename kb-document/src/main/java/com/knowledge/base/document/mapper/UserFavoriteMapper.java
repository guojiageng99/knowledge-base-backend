package com.knowledge.base.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.base.document.entity.UserFavorite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {
    @Select("SELECT uf.*, d.summary AS documentSummary, d.category_id AS documentCategoryId, " +
            "c.category_name AS documentCategoryName, d.author_name AS documentAuthorName, " +
            "d.author_id AS documentAuthorId, d.status AS documentStatus, d.view_count AS documentViewCount " +
            "FROM kb_user_favorite uf LEFT JOIN kb_document d ON uf.document_id = d.id AND d.deleted = 0 " +
            "LEFT JOIN kb_category c ON d.category_id = c.id AND c.deleted = 0 " +
            "WHERE uf.user_id = #{userId} AND uf.deleted = 0 ORDER BY uf.favorite_time DESC")
    List<UserFavorite> getUserFavorites(@Param("userId") Long userId);

    @Select("SELECT * FROM kb_user_favorite WHERE user_id = #{userId} AND document_id = #{documentId} AND deleted = 0")
    UserFavorite findByUserAndDocument(@Param("userId") Long userId, @Param("documentId") Long documentId);

    @Select("SELECT COUNT(*) FROM kb_user_favorite WHERE document_id = #{documentId} AND deleted = 0")
    Integer countByDocumentId(@Param("documentId") Long documentId);

    @Delete("DELETE FROM kb_user_favorite WHERE user_id = #{userId} AND document_id = #{documentId}")
    int physicalDelete(@Param("userId") Long userId, @Param("documentId") Long documentId);
}
