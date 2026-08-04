package com.knowledge.base.document.service.impl;

import com.knowledge.base.document.entity.Document;
import com.knowledge.base.document.mapper.CategoryMapper;
import com.knowledge.base.document.service.DocumentService;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PdfExportServiceImplTest {

    @Test
    void batchMarkdownExportCreatesZipEntriesForStringDocumentIds() throws IOException {
        DocumentService documentService = mock(DocumentService.class);
        CategoryMapper categoryMapper = mock(CategoryMapper.class);
        PdfExportServiceImpl service = new PdfExportServiceImpl(documentService, categoryMapper);

        Document first = document(900000000000000001L, "First document");
        Document second = document(900000000000000002L, "First document");
        when(documentService.getById(first.getId())).thenReturn(first);
        when(documentService.getById(second.getId())).thenReturn(second);
        when(documentService.getDocumentContent(first.getId())).thenReturn("# First");
        when(documentService.getDocumentContent(second.getId())).thenReturn(null);

        byte[] archive = service.batchExportDocuments(
                List.of(first.getId().toString(), second.getId().toString()), "markdown");

        try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(archive))) {
            ZipEntry firstEntry = input.getNextEntry();
            assertNotNull(firstEntry);
            assertEquals("First document.md", firstEntry.getName());
            assertEquals("# First", new String(input.readAllBytes(), StandardCharsets.UTF_8));

            ZipEntry secondEntry = input.getNextEntry();
            assertNotNull(secondEntry);
            assertEquals("First document_2.md", secondEntry.getName());
            assertEquals("", new String(input.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private Document document(Long id, String title) {
        Document document = new Document();
        document.setId(id);
        document.setTitle(title);
        return document;
    }
}
