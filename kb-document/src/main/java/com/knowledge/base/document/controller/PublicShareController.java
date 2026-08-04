package com.knowledge.base.document.controller;

import com.knowledge.base.common.result.Result;
import com.knowledge.base.document.service.DocumentShareService;
import com.knowledge.base.document.vo.DocumentVO;
import com.knowledge.base.document.vo.ShareVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/share")
public class PublicShareController {
    private final DocumentShareService documentShareService;

    @GetMapping("/{shareId}")
    public Result<ShareVO> getInfo(@PathVariable String shareId) {
        ShareVO share = documentShareService.getShareById(shareId);
        share.setDocumentId(null);
        return Result.success(share);
    }

    @PostMapping("/{shareId}/verify")
    public Result<Boolean> verify(@PathVariable String shareId, @RequestParam(required = false) String password) {
        return Result.success(documentShareService.verifyShareAccess(shareId, password));
    }

    @PostMapping("/{shareId}/access")
    public Result<DocumentVO> access(@PathVariable String shareId, @RequestParam(required = false) String password) {
        return Result.success(documentShareService.getSharedDocument(shareId, password));
    }
}
