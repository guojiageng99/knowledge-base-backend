package com.knowledge.base.document.controller;

import com.knowledge.base.common.result.Result;
import com.knowledge.base.document.service.DocumentAccessService;
import com.knowledge.base.document.utils.UserContext;
import com.knowledge.base.document.vo.DocumentAccessVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/access")
@RequiredArgsConstructor
public class DocumentAccessController {
    private final DocumentAccessService documentAccessService;

    @PostMapping("/record")
    public Result<Boolean> record(@RequestParam Long documentId, @RequestParam String documentTitle) {
        documentAccessService.recordAccess(UserContext.getCurrentUserId(), documentId, documentTitle);
        return Result.success(true);
    }

    @GetMapping("/recent")
    public Result<List<DocumentAccessVO>> recent(@RequestParam(required = false) Integer limit) {
        return Result.success(documentAccessService.getRecentAccess(limit));
    }

    @DeleteMapping("/remove/{documentId}")
    public Result<Boolean> remove(@PathVariable Long documentId) {
        documentAccessService.deleteAccess(documentId);
        return Result.success(true);
    }

    @DeleteMapping("/clear")
    public Result<Boolean> clear() {
        documentAccessService.clearAllAccess();
        return Result.success(true);
    }
}
