package com.knowledge.base.document.service;

import java.util.List;

public interface PdfExportService {
    String exportDocumentToPdf(Long documentId);
    byte[] exportDocumentToPdfBytes(Long documentId);
    String generatePdfFileName(Long documentId, String title);
    byte[] batchExportDocuments(List<String> documentIds, String format);
}
