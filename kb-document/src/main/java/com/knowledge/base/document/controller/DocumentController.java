package com.knowledge.base.document.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.document.dto.DocumentDTO;
import com.knowledge.base.document.service.DocumentService;
import com.knowledge.base.document.vo.DocumentVO;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public Result<Long> createDocument(@Valid @RequestBody DocumentDTO documentDTO) {
        return Result.success("Document created successfully", documentService.createDocument(documentDTO));
    }

    @PutMapping
    public Result<Boolean> updateDocument(@Valid @RequestBody DocumentDTO documentDTO) {
        return Result.success("Document updated successfully", documentService.updateDocument(documentDTO));
    }

    @DeleteMapping("/{documentId}")
    public Result<Boolean> deleteDocument(@PathVariable Long documentId) {
        return Result.success("Document deleted successfully", documentService.deleteDocument(documentId));
    }

    @GetMapping("/{documentId}")
    public Result<DocumentVO> getDocumentById(@PathVariable Long documentId) {
        return Result.success(documentService.getDocumentById(documentId));
    }

    @GetMapping("/{documentId}/view")
    public Result<DocumentVO> viewDocument(@PathVariable Long documentId) {
        return Result.success(documentService.viewDocument(documentId));
    }

    @GetMapping("/page")
    public Result<IPage<DocumentVO>> pageDocuments(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(documentService.pageDocuments(current, size, categoryId, keyword, status));
    }

    @PostMapping("/upload")
    public Result<String> uploadDocumentFile(@RequestParam("file") MultipartFile file) {
        return Result.success("File uploaded successfully", documentService.uploadDocumentFile(file));
    }

    @PostMapping("/{documentId}/content")
    public Result<Boolean> saveDocumentContent(@PathVariable Long documentId, @RequestBody String content) {
        return Result.success(documentService.updateDocumentContent(documentId, content));
    }

    @GetMapping("/{documentId}/content")
    public Result<String> getDocumentContent(@PathVariable Long documentId) {
        return Result.success(documentService.getDocumentContent(documentId));
    }

    @PostMapping("/upload-image-from-url")
    public Result<String> uploadImageFromUrl(@RequestParam String imageUrl) {
        return Result.success(documentService.uploadImageFromUrl(imageUrl));
    }

    @PostMapping("/{documentId}/like")
    public Result<Boolean> likeDocument(@PathVariable Long documentId) {
        return Result.success("Document liked successfully", documentService.likeDocument(documentId));
    }

    @PostMapping("/{documentId}/favorite")
    public Result<Boolean> favoriteDocument(@PathVariable Long documentId) {
        return Result.success("Document favorited successfully", documentService.favoriteDocument(documentId));
    }

    @PutMapping("/{documentId}/publish")
    public Result<Boolean> publishDocument(@PathVariable Long documentId) {
        return Result.success("Document published successfully", documentService.publishDocument(documentId));
    }

    @PutMapping("/{documentId}/archive")
    public Result<Boolean> archiveDocument(@PathVariable Long documentId) {
        return Result.success("Document archived successfully", documentService.archiveDocument(documentId));
    }
}
