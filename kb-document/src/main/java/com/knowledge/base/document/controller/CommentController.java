package com.knowledge.base.document.controller;

import com.knowledge.base.common.annotation.OperationLog;
import com.knowledge.base.common.result.PageResult;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.document.dto.CommentCreateDTO;
import com.knowledge.base.document.dto.CommentQueryDTO;
import com.knowledge.base.document.service.CommentService;
import com.knowledge.base.document.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Tag(name = "评论管理", description = "评论管理相关接口")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "创建评论", description = "创建文档评论")
    @OperationLog(module = "评论管理", operation = "创建评论", description = "创建文档评论")
    public Result<Long> createComment(@Valid @RequestBody CommentCreateDTO dto) {
        log.info("创建评论请求：documentId={}, parentId={}", dto.getDocumentId(), dto.getParentId());
        return Result.success(commentService.createComment(dto));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "删除评论", description = "删除指定评论")
    @OperationLog(module = "评论管理", operation = "删除评论", description = "删除评论")
    public Result<Boolean> deleteComment(@PathVariable Long commentId) {
        return Result.success(commentService.deleteComment(commentId));
    }

    @PostMapping("/{commentId}/like")
    @Operation(summary = "点赞评论", description = "点赞指定评论")
    @OperationLog(module = "评论管理", operation = "点赞评论", description = "点赞评论")
    public Result<Boolean> likeComment(@PathVariable Long commentId) {
        return Result.success(commentService.likeComment(commentId));
    }

    @DeleteMapping("/{commentId}/like")
    @Operation(summary = "取消点赞评论", description = "取消点赞评论")
    @OperationLog(module = "评论管理", operation = "取消点赞", description = "取消点赞评论")
    public Result<Boolean> unlikeComment(@PathVariable Long commentId) {
        return Result.success(commentService.unlikeComment(commentId));
    }

    @PostMapping("/document/{documentId}")
    @Operation(summary = "分页查询文档评论", description = "分页查询文档评论列表")
    public Result<PageResult<CommentVO>> pageDocumentComments(@PathVariable Long documentId,
                                                                @RequestBody(required = false) CommentQueryDTO dto) {
        return Result.success(commentService.pageDocumentComments(documentId, dto));
    }

    @GetMapping("/{parentCommentId}/replies")
    @Operation(summary = "获取评论回复", description = "获取评论的回复列表")
    public Result<List<CommentVO>> getCommentReplies(@PathVariable Long parentCommentId) {
        return Result.success(commentService.getCommentReplies(parentCommentId));
    }
}
