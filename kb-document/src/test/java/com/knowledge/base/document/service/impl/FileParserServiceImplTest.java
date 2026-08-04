package com.knowledge.base.document.service.impl;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileParserServiceImplTest {

    private final FileParserServiceImpl parser = new FileParserServiceImpl(null);

    @Test
    void parsesPlainText() throws Exception {
        String parsed = parser.parse(new MockMultipartFile("file", "notes.txt", "text/plain", "Hello knowledge base".getBytes()));
        assertEquals("Hello knowledge base", parsed);
    }

    @Test
    void parsesDocxWithHeadingAndTable() throws Exception {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            XWPFParagraph heading = document.createParagraph();
            heading.setStyle("Heading1");
            heading.createRun().setText("Overview");
            document.createParagraph().createRun().setText("Document content");
            document.createTable(2, 2).getRow(0).getCell(0).setText("Name");
            document.getTables().getFirst().getRow(0).getCell(1).setText("Value");
            document.getTables().getFirst().getRow(1).getCell(0).setText("A");
            document.getTables().getFirst().getRow(1).getCell(1).setText("1");
            document.write(output);
            String parsed = parser.parse(new MockMultipartFile("file", "guide.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", output.toByteArray()));
            assertTrue(parsed.contains("# Overview"));
            assertTrue(parsed.contains("| Name | Value |"));
        }
    }

    @Test
    void parsesXlsxAsMarkdownTable() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Scores");
            sheet.createRow(0).createCell(0).setCellValue("Name");
            sheet.getRow(0).createCell(1).setCellValue("Score");
            sheet.createRow(1).createCell(0).setCellValue("Ada");
            sheet.getRow(1).createCell(1).setCellValue(100);
            workbook.write(output);
            String parsed = parser.parse(new MockMultipartFile("file", "scores.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", output.toByteArray()));
            assertTrue(parsed.contains("## Scores"));
            assertTrue(parsed.contains("| Ada | 100 |"));
        }
    }

    @Test
    void parsesPptxText() throws Exception {
        try (XMLSlideShow show = new XMLSlideShow(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            XSLFSlide slide = show.createSlide();
            XSLFTextBox box = slide.createTextBox();
            box.setText("Release plan");
            show.write(output);
            String parsed = parser.parse(new MockMultipartFile("file", "plan.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", output.toByteArray()));
            assertTrue(parsed.contains("## Slide 1"));
            assertTrue(parsed.contains("Release plan"));
        }
    }

    @Test
    void parsesPdfText() throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 16);
                content.newLineAtOffset(72, 720);
                content.showText("PDF overview");
                content.endText();
            }
            document.save(output);
            String parsed = parser.parse(new MockMultipartFile("file", "guide.pdf", "application/pdf", output.toByteArray()));
            assertTrue(parsed.contains("PDF overview"));
        }
    }

    @Test
    void rejectsUnsupportedFileTypes() {
        assertTrue(!parser.isSupported("zip"));
    }
}
