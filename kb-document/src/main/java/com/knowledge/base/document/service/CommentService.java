package com.knowledge.base.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.knowledge.base.common.result.PageResult;
import com.knowledge.base.document.dto.CommentCreateDTO;
import com.knowledge.base.document.dto.CommentQueryDTO;
import com.knowledge.base.document.entity.Comment;
import com.knowledge.base.document.vo.CommentVO;

import java.util.List;

public interface CommentService extends IService<Comment> {

    Long createComment(CommentCreateDTO dto);

    Boolean deleteComment(Long commentId);

    Boolean likeComment(Long commentId);

    Boolean unlikeComment(Long commentId);

    PageResult<CommentVO> pageDocumentComments(Long documentId, CommentQueryDTO dto);

    List<CommentVO> getCommentReplies(Long parentCommentId);
}
