package com.knowledge.base.document.service;

public interface PdfExportService {
    String exportDocumentToPdf(Long documentId);
    byte[] exportDocumentToPdfBytes(Long documentId);
    String generatePdfFileName(Long documentId, String title);
}
