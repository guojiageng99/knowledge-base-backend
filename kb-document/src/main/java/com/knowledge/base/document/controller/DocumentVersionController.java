package com.knowledge.base.document.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.document.dto.DocumentVersionRestoreDTO;
import com.knowledge.base.document.service.DocumentVersionService;
import com.knowledge.base.document.vo.DocumentVersionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/documents/{documentId}/versions")
@RequiredArgsConstructor
public class DocumentVersionController {

    private final DocumentVersionService documentVersionService;

    @GetMapping
    @Operation(summary = "获取文档版本列表", description = "分页查询文档版本列表")
    public Result<IPage<DocumentVersionVO>> getVersions(
            @Parameter(description = "文档ID", required = true) @PathVariable Long documentId,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size) {
        return Result.success(documentVersionService.getVersionList(documentId, current, size));
    }

    @GetMapping("/{versionId}")
    @Operation(summary = "获取文档版本详情", description = "根据版本ID获取文档版本详情")
    public Result<DocumentVersionVO> getVersionDetail(
            @Parameter(description = "文档ID", required = true) @PathVariable Long documentId,
            @Parameter(description = "版本ID", required = true) @PathVariable Long versionId) {
        DocumentVersionVO version = documentVersionService.getVersionDetail(versionId);
        assertDocumentVersion(documentId, version);
        return Result.success(version);
    }

    @PostMapping("/restore")
    @Operation(summary = "恢复文档版本", description = "将文档恢复到指定版本")
    public Result<Boolean> restoreVersion(
            @Parameter(description = "文档ID", required = true) @PathVariable Long documentId,
            @Valid @RequestBody DocumentVersionRestoreDTO dto) {
        return Result.success("恢复版本成功", documentVersionService.restoreVersion(documentId, dto, 1L));
    }

    @GetMapping("/compare")
    @Operation(summary = "对比文档版本", description = "对比两个文档版本的差异")
    public Result<String> compareVersions(
            @Parameter(description = "文档ID", required = true) @PathVariable Long documentId,
            @Parameter(description = "版本1 ID", required = true) @RequestParam Long versionId1,
            @Parameter(description = "版本2 ID", required = true) @RequestParam Long versionId2) {
        assertDocumentVersion(documentId, documentVersionService.getVersionDetail(versionId1));
        assertDocumentVersion(documentId, documentVersionService.getVersionDetail(versionId2));
        return Result.success(documentVersionService.compareVersions(versionId1, versionId2));
    }

    private void assertDocumentVersion(Long documentId, DocumentVersionVO version) {
        if (!Objects.equals(documentId, version.getDocumentId())) {
            throw new BusinessException("Version does not belong to this document");
        }
    }
}
