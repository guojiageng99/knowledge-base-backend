package com.knowledge.base.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.common.event.StatisticsEventDTO;
import com.knowledge.base.common.result.PageResult;
import com.knowledge.base.common.utils.SnowflakeIdGenerator;
import com.knowledge.base.document.dto.CommentCreateDTO;
import com.knowledge.base.document.dto.CommentQueryDTO;
import com.knowledge.base.document.config.RabbitMQConfig;
import com.knowledge.base.document.entity.Comment;
import com.knowledge.base.document.entity.Document;
import com.knowledge.base.document.mapper.CommentMapper;
import com.knowledge.base.document.mapper.DocumentMapper;
import com.knowledge.base.document.service.CommentService;
import com.knowledge.base.document.utils.UserContext;
import com.knowledge.base.document.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private static final int COMMENT_TARGET_TYPE = 2;

    private final CommentMapper commentMapper;
    private final DocumentMapper documentMapper;
    private final JdbcTemplate jdbcTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQConfig rabbitMQConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(CommentCreateDTO dto) {
        Document document = requireCommentableDocument(dto.getDocumentId());
        Comment parent = null;
        if (dto.getParentId() != null && dto.getParentId() > 0) {
            parent = requireComment(dto.getParentId());
            if (!Objects.equals(parent.getDocumentId(), dto.getDocumentId())) {
                throw new BusinessException("父评论不属于该文档");
            }
        }

        Comment comment = new Comment();
        comment.setId(SnowflakeIdGenerator.nextId());
        comment.setDocumentId(dto.getDocumentId());
        comment.setParentId(parent == null ? 0L : parent.getId());
        comment.setRootId(parent == null ? 0L : (parent.getRootId() == null || parent.getRootId() == 0 ? parent.getId() : parent.getRootId()));
        comment.setContent(dto.getContent());
        comment.setCommenterId(currentUserId());
        comment.setCommenterName(currentUserName());
        comment.setCommenterAvatar("/avatar/default.png");
        comment.setReplyToUserId(dto.getReplyToUserId());
        comment.setReplyToUserName(parent == null ? null : parent.getCommenterName());
        comment.setStatus(1);
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        comment.setDeleted(0);
        if (commentMapper.insert(comment) <= 0) {
            throw new BusinessException("创建评论失败");
        }

        if (parent != null && commentMapper.incrementReplyCount(parent.getId()) <= 0) {
            throw new BusinessException("更新父评论回复数失败");
        }
        if (documentMapper.incrementCommentCount(document.getId()) <= 0) {
            throw new BusinessException("更新文档评论数失败");
        }
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.STATISTICS_EXCHANGE,
                    rabbitMQConfig.statisticsRoutingKey("comment"),
                    StatisticsEventDTO.builder().eventType("comment").documentId(document.getId())
                            .userId(currentUserId()).timestamp(LocalDateTime.now()).build());
        } catch (RuntimeException exception) {
            log.warn("Failed to publish comment statistics event for document {}", document.getId(), exception);
        }
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteComment(Long commentId) {
        Comment comment = requireComment(commentId);
        long childCount = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getParentId, commentId)
                .eq(Comment::getDeleted, 0));
        if (childCount > 0) {
            throw new BusinessException("该评论下有回复，不能删除");
        }
        boolean deleted = commentMapper.deleteById(commentId) > 0;
        if (!deleted) {
            return false;
        }
        if (comment.getParentId() != null && comment.getParentId() > 0) {
            commentMapper.decrementReplyCount(comment.getParentId());
        }
        jdbcTemplate.update("UPDATE kb_document SET comment_count = GREATEST(comment_count - 1, 0) WHERE id = ? AND deleted = 0", comment.getDocumentId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean likeComment(Long commentId) {
        requireComment(commentId);
        int inserted = jdbcTemplate.update("INSERT IGNORE INTO tb_like (id, target_id, target_type, user_id, created_at) VALUES (?, ?, ?, ?, NOW())",
                SnowflakeIdGenerator.nextId(), commentId, COMMENT_TARGET_TYPE, currentUserId());
        if (inserted == 0) {
            throw new BusinessException("已经点赞过了");
        }
        if (commentMapper.incrementLikeCount(commentId) <= 0) {
            throw new BusinessException("更新评论点赞数失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unlikeComment(Long commentId) {
        requireComment(commentId);
        int deleted = jdbcTemplate.update("DELETE FROM tb_like WHERE target_id = ? AND user_id = ? AND target_type = ?", commentId, currentUserId(), COMMENT_TARGET_TYPE);
        if (deleted > 0) {
            commentMapper.decrementLikeCount(commentId);
            return true;
        }
        return false;
    }

    @Override
    public PageResult<CommentVO> pageDocumentComments(Long documentId, CommentQueryDTO dto) {
        requireCommentableDocument(documentId);
        CommentQueryDTO query = dto == null ? new CommentQueryDTO() : dto;
        long current = query.getCurrent() == null || query.getCurrent() < 1 ? 1 : query.getCurrent();
        long size = query.getSize() == null || query.getSize() < 1 ? 10 : query.getSize();
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getDocumentId, documentId)
                .eq(Comment::getParentId, 0L)
                .eq(Comment::getStatus, 1)
                .eq(Comment::getDeleted, 0);
        if ("like_count".equals(query.getSortBy())) {
            wrapper.orderByDesc(Comment::getLikeCount).orderByDesc(Comment::getCreatedAt);
        } else if ("asc".equalsIgnoreCase(query.getSortOrder())) {
            wrapper.orderByAsc(Comment::getCreatedAt);
        } else {
            wrapper.orderByDesc(Comment::getCreatedAt);
        }
        IPage<Comment> page = commentMapper.selectPage(new Page<Comment>(current, size), wrapper);
        List<CommentVO> records = page.getRecords().stream().map(comment -> {
            CommentVO vo = toVO(comment);
            vo.setIsLiked(isLiked(comment.getId()));
            vo.setReplies(getCommentReplies(comment.getId()));
            return vo;
        }).toList();
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    public List<CommentVO> getCommentReplies(Long parentCommentId) {
        if (parentCommentId == null || parentCommentId <= 0) {
            return List.of();
        }
        return commentMapper.selectByParentId(parentCommentId).stream().map(comment -> {
            CommentVO vo = toVO(comment);
            vo.setIsLiked(isLiked(comment.getId()));
            return vo;
        }).toList();
    }

    private Document requireCommentableDocument(Long documentId) {
        if (documentId == null) {
            throw new BusinessException("文档ID不能为空");
        }
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        if (!Integer.valueOf(1).equals(document.getAllowComment())) {
            throw new BusinessException("该文档不允许评论");
        }
        return document;
    }

    private Comment requireComment(Long commentId) {
        if (commentId == null) {
            throw new BusinessException("评论ID不能为空");
        }
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        return comment;
    }

    private boolean isLiked(Long commentId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_like WHERE target_id = ? AND target_type = ? AND user_id = ?",
                Integer.class, commentId, COMMENT_TARGET_TYPE, currentUserId());
        return count != null && count > 0;
    }

    private Long currentUserId() {
        Long userId = UserContext.getCurrentUserId();
        return userId == null ? 1L : userId;
    }

    private String currentUserName() {
        String username = UserContext.getCurrentUserName();
        return username == null ? "Test user" : username;
    }

    private CommentVO toVO(Comment comment) {
        return CommentVO.builder()
                .id(comment.getId()).documentId(comment.getDocumentId()).parentId(comment.getParentId()).rootId(comment.getRootId())
                .content(comment.getContent()).commenterId(comment.getCommenterId()).commenterName(comment.getCommenterName())
                .commenterAvatar(comment.getCommenterAvatar()).replyToUserId(comment.getReplyToUserId()).replyToUserName(comment.getReplyToUserName())
                .status(comment.getStatus()).likeCount(comment.getLikeCount() == null ? 0 : comment.getLikeCount())
                .replyCount(comment.getReplyCount() == null ? 0 : comment.getReplyCount()).createdAt(comment.getCreatedAt()).build();
    }
}
