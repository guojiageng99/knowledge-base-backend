package com.knowledge.base.document.service.impl;

import cn.hutool.core.io.FileUtil;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.document.entity.Document;
import com.knowledge.base.document.mapper.CategoryMapper;
import com.knowledge.base.document.service.DocumentService;
import com.knowledge.base.document.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfExportServiceImpl implements PdfExportService {

    private static final float MARGIN = 50;
    private static final float BODY_SIZE = 11;
    private static final float LINE_HEIGHT = 17;
    private final DocumentService documentService;
    private final CategoryMapper categoryMapper;

    @Value("${pdf.font.path:}")
    private String configuredFontPath;

    @Override
    public String exportDocumentToPdf(Long documentId) {
        requireDocument(documentId);
        return "/api/document/documents/" + documentId + "/download-pdf";
    }

    @Override
    public byte[] exportDocumentToPdfBytes(Long documentId) {
        Document document = requireDocument(documentId);
        String content = documentService.getDocumentContent(documentId);
        var categoryEntity = document.getCategoryId() == null ? null : categoryMapper.selectById(document.getCategoryId());
        String category = categoryEntity == null ? null : categoryEntity.getCategoryName();
        try (PDDocument pdf = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDFont font = loadFont(pdf);
            List<TextLine> lines = new ArrayList<>();
            addWrapped(lines, document.getTitle(), font, 18, 25);
            String subtitle = "Author: " + (document.getAuthorName() == null ? "Unknown" : document.getAuthorName())
                    + "  |  Category: " + (category == null ? "Uncategorized" : category);
            addWrapped(lines, subtitle, font, 9, LINE_HEIGHT);
            if (document.getSummary() != null && !document.getSummary().isBlank()) addWrapped(lines, document.getSummary(), font, 10, LINE_HEIGHT);
            for (String line : (content == null ? "" : content).split("\\R", -1)) {
                if (line.trim().startsWith("```")) continue;
                String plain = line.replaceAll("^#{1,6}\\s+", "").replaceAll("[*`]", "");
                if (plain.isBlank()) lines.add(new TextLine("", BODY_SIZE, LINE_HEIGHT / 2));
                else addWrapped(lines, plain, font, BODY_SIZE, LINE_HEIGHT);
            }
            int index = 0;
            while (index < lines.size()) {
                PDPage page = new PDPage(PDRectangle.A4);
                pdf.addPage(page);
                float y = page.getMediaBox().getHeight() - MARGIN;
                try (PDPageContentStream stream = new PDPageContentStream(pdf, page)) {
                    while (index < lines.size() && y - lines.get(index).height() >= MARGIN) {
                        TextLine line = lines.get(index++);
                        if (!line.text().isEmpty()) {
                            stream.beginText();
                            stream.setFont(font, line.size());
                            stream.newLineAtOffset(MARGIN, y);
                            stream.showText(line.text());
                            stream.endText();
                        }
                        y -= line.height();
                    }
                }
            }
            pdf.save(output);
            return output.toByteArray();
        } catch (IOException e) {
            log.error("PDF generation failed for document {}", documentId, e);
            throw new BusinessException("PDF export failed");
        }
    }

    @Override
    public String generatePdfFileName(Long documentId, String title) {
        String safeTitle = FileUtil.cleanInvalid(title == null ? "document" : title);
        if (safeTitle.length() > 50) safeTitle = safeTitle.substring(0, 50);
        return safeTitle + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".pdf";
    }

    private void addWrapped(List<TextLine> target, String text, PDFont font, float size, float height) throws IOException {
        for (String line : wrap(text, font, size, PDRectangle.A4.getWidth() - MARGIN * 2)) {
            target.add(new TextLine(line, size, height));
        }
    }

    private List<String> wrap(String text, PDFont font, float size, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        String source = text == null ? "" : text;
        for (char c : source.toCharArray()) {
            String candidate = current + String.valueOf(c);
            if (font.getStringWidth(candidate) / 1000 * size > maxWidth && !current.isEmpty()) {
                lines.add(current.toString());
                current.setLength(0);
            }
            current.append(c);
        }
        if (!current.isEmpty()) lines.add(current.toString());
        return lines;
    }

    private PDFont loadFont(PDDocument pdf) throws IOException {
        String[] paths = { configuredFontPath, "C:\\Windows\\Fonts\\simhei.ttf", "C:\\Windows\\Fonts\\simsun.ttc", "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc" };
        for (String path : paths) {
            java.nio.file.Path fontPath = java.nio.file.Path.of(path);
            if (java.nio.file.Files.exists(fontPath)) return PDType0Font.load(pdf, fontPath.toFile());
        }
        try (InputStream stream = getClass().getResourceAsStream("/fonts/NotoSansCJK-Regular.ttc")) {
            if (stream != null) return PDType0Font.load(pdf, stream);
        }
        log.warn("No CJK font was found. Configure pdf.font.path or add /fonts/NotoSansCJK-Regular.ttc for Chinese PDF output.");
        return PDType1Font.HELVETICA;
    }

    private Document requireDocument(Long documentId) {
        Document document = documentService.getById(documentId);
        if (document == null) throw new BusinessException("Document does not exist");
        return document;
    }

    private record TextLine(String text, float size, float height) { }
}
