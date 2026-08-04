package com.knowledge.base.document.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.document.dto.DocumentDTO;
import com.knowledge.base.document.dto.BatchExportRequest;
import com.knowledge.base.document.service.DocumentService;
import com.knowledge.base.document.service.PdfExportService;
import com.knowledge.base.document.service.UserFavoriteService;
import com.knowledge.base.document.utils.UserContext;
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
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final PdfExportService pdfExportService;
    private final UserFavoriteService userFavoriteService;

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

    @DeleteMapping("/{documentId}/like")
    public Result<Boolean> unlikeDocument(@PathVariable Long documentId) {
        return Result.success("Document unliked successfully", documentService.unlikeDocument(documentId));
    }

    /**
     * Kept for clients created before the dedicated /favorite endpoints.
     * It now writes the favorite record as well as the aggregate count.
     */
    @PostMapping("/{documentId}/favorite")
    public Result<Boolean> favoriteDocument(@PathVariable Long documentId) {
        return Result.success("Document favorited successfully",
                userFavoriteService.addFavorite(UserContext.getCurrentUserId(), documentId));
    }

    @GetMapping("/{documentId}/export-pdf")
    public Result<String> exportPdf(@PathVariable Long documentId) {
        return Result.success(pdfExportService.exportDocumentToPdf(documentId));
    }

    @GetMapping("/{documentId}/download-pdf")
    public void downloadPdf(@PathVariable Long documentId, HttpServletResponse response) throws IOException {
        DocumentVO document = documentService.getDocumentById(documentId);
        byte[] bytes = pdfExportService.exportDocumentToPdfBytes(documentId);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''"
                + URLEncoder.encode(pdfExportService.generatePdfFileName(documentId, document.getTitle()), StandardCharsets.UTF_8));
        response.getOutputStream().write(bytes);
    }

    @PostMapping("/batch-export")
    public void batchExportDocuments(@Valid @RequestBody BatchExportRequest request, HttpServletResponse response) throws IOException {
        byte[] bytes = pdfExportService.batchExportDocuments(request.getDocumentIds(), request.getFormat());
        String fileName = "documents_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".zip";
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''"
                + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20"));
        response.setContentLengthLong(bytes.length);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
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
