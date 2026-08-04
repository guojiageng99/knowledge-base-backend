package com.knowledge.base.document.controller;

import com.knowledge.base.common.annotation.OperationLog;
import com.knowledge.base.common.result.PageResult;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.document.dto.DocumentReviewDTO;
import com.knowledge.base.document.dto.ReviewActionDTO;
import com.knowledge.base.document.dto.BatchReviewDTO;
import com.knowledge.base.document.dto.ReviewQueryDTO;
import com.knowledge.base.document.service.DocumentReviewService;
import com.knowledge.base.document.vo.DocumentReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/document-reviews", "/review"})
@RequiredArgsConstructor
@Tag(name = "文档审核", description = "文档审核相关接口")
public class DocumentReviewController {

    private final DocumentReviewService reviewService;

    @PostMapping("/submit/{documentId}")
    @Operation(summary = "提交文档审核", description = "提交文档进行审核")
    @OperationLog(module = "文档审核", operation = "提交审核", description = "提交文档审核")
    public Result<Boolean> submitForReview(@PathVariable Long documentId) {
        return Result.success(reviewService.submitForReview(documentId));
    }

    @PostMapping("/approve")
    @Operation(summary = "审核通过", description = "审核通过文档")
    @OperationLog(module = "文档审核", operation = "审核通过", description = "审核通过文档")
    public Result<Boolean> approveReview(@Valid @RequestBody DocumentReviewDTO dto) {
        return Result.success(reviewService.approveReview(dto));
    }

    @PostMapping("/reject")
    @Operation(summary = "审核驳回", description = "审核驳回文档")
    @OperationLog(module = "文档审核", operation = "审核驳回", description = "审核驳回文档")
    public Result<Boolean> rejectReview(@Valid @RequestBody DocumentReviewDTO dto) {
        return Result.success(reviewService.rejectReview(dto));
    }

    @PostMapping("/pending")
    @Operation(summary = "获取待审核文档", description = "获取待审核文档列表")
    public Result<PageResult<DocumentReviewVO>> getPendingReviews(@RequestBody(required = false) ReviewQueryDTO dto) {
        return Result.success(reviewService.getPendingReviews(dto));
    }

    @GetMapping("/history/{documentId}")
    @Operation(summary = "获取审核历史", description = "获取文档的审核历史")
    public Result<List<DocumentReviewVO>> getDocumentReviewHistory(@PathVariable Long documentId) {
        return Result.success(reviewService.getDocumentReviewHistory(documentId));
    }

    @GetMapping("/documents/{documentId}/history")
    public Result<List<DocumentReviewVO>> getDocumentReviewHistoryByDocument(@PathVariable Long documentId) {
        return Result.success(reviewService.getDocumentReviewHistory(documentId));
    }

    @GetMapping("/documents/{documentId}/current")
    public Result<DocumentReviewVO> getCurrentReviewTask(@PathVariable Long documentId) {
        return Result.success(reviewService.getCurrentReviewTask(documentId));
    }

    @GetMapping("/tasks")
    public Result<PageResult<DocumentReviewVO>> getReviewTasks(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long authorId) {
        ReviewQueryDTO query = new ReviewQueryDTO();
        query.setCurrent(page.longValue()); query.setSize(pageSize.longValue());
        query.setKeyword(keyword); query.setAuthorId(authorId);
        query.setStatus("pending".equals(status) ? 0 : "approved".equals(status) ? 1 : "rejected".equals(status) ? 2 : null);
        return Result.success(reviewService.getPendingReviews(query));
    }

    @GetMapping("/tasks/pending-count")
    public Result<Long> getPendingCount() { return Result.success(reviewService.getPendingCount()); }

    @GetMapping("/tasks/stats")
    public Result<Map<String, Long>> getReviewStats() { return Result.success(reviewService.getReviewStats()); }

    @PostMapping("/tasks/{taskId}/review")
    public Result<Boolean> reviewTask(@PathVariable Long taskId, @Valid @RequestBody ReviewActionDTO action) {
        DocumentReviewDTO dto = new DocumentReviewDTO();
        dto.setReviewId(taskId); dto.setReviewComment(action.getComment());
        if ("approved".equalsIgnoreCase(action.getStatus())) { dto.setReviewResult(1); return Result.success(reviewService.approveReview(dto)); }
        if ("rejected".equalsIgnoreCase(action.getStatus())) { dto.setReviewResult(2); return Result.success(reviewService.rejectReview(dto)); }
        return Result.error("无效的审核结果：" + action.getStatus());
    }

    @PostMapping("/tasks/batch-review")
    public Result<String> batchReview(@Valid @RequestBody BatchReviewDTO action) {
        reviewService.batchReview(action.getTaskIds(), action.getStatus(), action.getComment());
        return Result.success("批量审核完成");
    }
}
