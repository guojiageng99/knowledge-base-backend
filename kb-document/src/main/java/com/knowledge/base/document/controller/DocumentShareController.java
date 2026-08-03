package com.knowledge.base.document.controller;

import com.knowledge.base.common.result.Result;
import com.knowledge.base.document.dto.ShareDTO;
import com.knowledge.base.document.service.DocumentShareService;
import com.knowledge.base.document.vo.DocumentVO;
import com.knowledge.base.document.vo.ShareVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/documents")
public class DocumentShareController {
    private final DocumentShareService documentShareService;

    @PostMapping("/share")
    public Result<ShareVO> create(@Valid @RequestBody ShareDTO dto) {
        return Result.success(documentShareService.createShare(dto));
    }

    @GetMapping("/share/{shareId}")
    public Result<ShareVO> get(@PathVariable String shareId) {
        return Result.success(documentShareService.getShareById(shareId));
    }

    @PostMapping("/share/{shareId}/access")
    public Result<Long> access(@PathVariable String shareId, @RequestParam(required = false) String password) {
        return Result.success(documentShareService.accessShare(shareId, password));
    }

    @GetMapping("/{documentId}/shares")
    public Result<List<ShareVO>> list(@PathVariable Long documentId) {
        return Result.success(documentShareService.getSharesByDocumentId(documentId));
    }

    @GetMapping("/share/my")
    public Result<List<ShareVO>> my() {
        return Result.success(documentShareService.getMyShares());
    }

    @DeleteMapping("/share/{shareId}")
    public Result<Boolean> delete(@PathVariable String shareId) {
        return Result.success(documentShareService.deleteShare(shareId));
    }

    @PutMapping("/share/{shareId}")
    public Result<Boolean> update(@PathVariable String shareId, @RequestBody ShareDTO dto) {
        return Result.success(documentShareService.updateShare(shareId, dto));
    }
}
