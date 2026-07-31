package com.knowledge.base.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.base.document.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    List<Comment> selectByDocumentId(@Param("documentId") Long documentId);

    List<Comment> selectByParentId(@Param("parentId") Long parentId);

    List<Comment> selectByCommenterId(@Param("commenterId") Long commenterId);

    int incrementLikeCount(@Param("commentId") Long commentId);

    int decrementLikeCount(@Param("commentId") Long commentId);

    int incrementReplyCount(@Param("commentId") Long commentId);

    int decrementReplyCount(@Param("commentId") Long commentId);

    int updateStatus(@Param("commentId") Long commentId, @Param("status") Integer status);

    Long countByDocumentId(@Param("documentId") Long documentId);
}
