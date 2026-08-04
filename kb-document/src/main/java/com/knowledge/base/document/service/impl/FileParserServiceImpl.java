package com.knowledge.base.document.service.impl;

import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.document.service.FileUploadService;
import com.knowledge.base.document.service.FileParserService;
import com.knowledge.base.document.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFGroupShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTableRow;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileParserServiceImpl implements FileParserService {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md");
    private final FileUploadService fileUploadService;

    @Override
    public String parse(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) throw new BusinessException("File must not be empty");
        String extension = extensionOf(file.getOriginalFilename());
        if (!isSupported(extension)) throw new BusinessException("Unsupported file format: " + extension);
        return switch (extension) {
            case "pdf" -> parsePdf(file);
            case "docx" -> parseDocx(file);
            case "doc" -> parseDoc(file);
            case "xlsx", "xls" -> parseSpreadsheet(file);
            case "pptx" -> parsePptx(file);
            case "ppt" -> parsePpt(file);
            default -> parsePlainText(file);
        };
    }

    @Override
    public boolean isSupported(String extension) {
        return StringUtils.hasText(extension) && SUPPORTED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT));
    }

    private String parsePdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            StringBuilder markdown = new StringBuilder();
            CollectingPdfStripper stripper = new CollectingPdfStripper();
            for (int pageNumber = 1; pageNumber <= document.getNumberOfPages(); pageNumber++) {
                stripper.setStartPage(pageNumber);
                stripper.setEndPage(pageNumber);
                stripper.getText(document);
                if (document.getNumberOfPages() > 1) markdown.append("## Page ").append(pageNumber).append("\n\n");
                markdown.append(buildPdfMarkdown(stripper.consumeLines())).append("\n\n");
                appendPdfImageMarkers(document.getPage(pageNumber - 1), markdown);
            }
            return markdown.toString().trim();
        }
    }

    private void appendPdfImageMarkers(PDPage page, StringBuilder markdown) throws IOException {
        Map<org.apache.pdfbox.cos.COSName, PDImageXObject> images = new LinkedHashMap<>();
        collectPdfImages(page.getResources(), images);
        for (Map.Entry<org.apache.pdfbox.cos.COSName, PDImageXObject> entry : images.entrySet()) {
            String url = uploadPdfImage(entry.getValue(), entry.getKey().getName());
            if (StringUtils.hasText(url)) markdown.append("\n![](").append(url).append(")\n");
        }
    }

    private void collectPdfImages(org.apache.pdfbox.pdmodel.PDResources resources,
                                  Map<org.apache.pdfbox.cos.COSName, PDImageXObject> images) throws IOException {
        if (resources == null) return;
        for (org.apache.pdfbox.cos.COSName name : resources.getXObjectNames()) {
            PDXObject object = resources.getXObject(name);
            if (object instanceof PDImageXObject image) images.putIfAbsent(name, image);
            else if (object instanceof PDFormXObject form) collectPdfImages(form.getResources(), images);
        }
    }

    private String uploadPdfImage(PDImageXObject image, String objectName) throws IOException {
        if (fileUploadService == null) return null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!javax.imageio.ImageIO.write(image.getImage(), "png", output)) return null;
        Long userId = UserContext.getCurrentUserId() == null ? 1L : UserContext.getCurrentUserId();
        MultipartFile upload = new ByteArrayMultipartFile("file", "pdf-image-" + objectName + ".png", "image/png", output.toByteArray());
        return fileUploadService.uploadDocumentFile(upload, userId);
    }

    private byte[] toPngBytes(PDImageXObject image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image.getImage(), "png", output);
        return output.toByteArray();
    }

    private static final class ByteArrayMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] bytes;

        private ByteArrayMultipartFile(String name, String originalFilename, String contentType, byte[] bytes) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.bytes = bytes;
        }

        @Override public String getName() { return name; }
        @Override public String getOriginalFilename() { return originalFilename; }
        @Override public String getContentType() { return contentType; }
        @Override public boolean isEmpty() { return bytes.length == 0; }
        @Override public long getSize() { return bytes.length; }
        @Override public byte[] getBytes() { return bytes.clone(); }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(bytes); }
        @Override public void transferTo(java.io.File destination) throws IOException { java.nio.file.Files.write(destination.toPath(), bytes); }
    }

    private String normalizePdfText(String text) {
        if (!StringUtils.hasText(text)) return "";
        StringBuilder result = new StringBuilder();
        for (String line : text.replace('\r', '\n').split("\\n+")) {
            String trimmed = line.trim().replace('•', '-');
            if (!trimmed.isEmpty()) result.append(trimmed).append("\n\n");
        }
        return result.toString().trim();
    }

    private String buildPdfMarkdown(List<PdfLine> lines) {
        if (lines.isEmpty()) return "";
        Map<Integer, String> tables = detectPdfTables(lines);
        List<Float> sizes = lines.stream().map(line -> line.fontSize).filter(size -> size > 0).sorted().toList();
        float baseSize = sizes.isEmpty() ? 10f : sizes.get(sizes.size() / 2);
        StringBuilder markdown = new StringBuilder();
        for (int index = 0; index < lines.size(); index++) {
            String table = tables.get(index);
            if (table != null) {
                if (!table.isEmpty()) markdown.append(table).append("\n\n");
                continue;
            }
            PdfLine line = lines.get(index);
            String text = line.text.trim();
            if (text.isEmpty()) continue;
            if (text.matches("^[•*\\-]\\s+.*")) markdown.append("- ").append(text.replaceFirst("^[•*\\-]\\s+", "")).append('\n');
            else if (text.matches("^\\d+[.)、]\\s+.*")) markdown.append("1. ").append(text.replaceFirst("^\\d+[.)、]\\s+", "")).append('\n');
            else {
                int heading = line.fontSize >= baseSize * 1.8f ? 1 : line.fontSize >= baseSize * 1.4f ? 2
                        : line.bold && text.length() <= 80 ? 3 : 0;
                if (heading > 0) markdown.append("#".repeat(heading)).append(' ').append(text).append("\n\n");
                else markdown.append(text).append(line.paragraphBreak ? "\n\n" : "\n");
            }
        }
        return cleanMarkdown(markdown.toString());
    }

    private Map<Integer, String> detectPdfTables(List<PdfLine> lines) {
        Map<Integer, String> result = new LinkedHashMap<>();
        for (int start = 0; start < lines.size(); start++) {
            PdfLine first = lines.get(start);
            if (first.columns == null || first.columns.length < 2) continue;
            int end = start + 1;
            while (end < lines.size() && sameTableColumns(first, lines.get(end))) end++;
            if (end - start < 2) continue;
            StringBuilder table = new StringBuilder();
            for (int row = start; row < end; row++) {
                table.append('|');
                for (String cell : lines.get(row).columns) table.append(' ').append(escapeCell(cell)).append(" |");
                table.append('\n');
                if (row == start) appendDivider(table, first.columns.length);
                result.put(row, row == start ? table.toString() : "");
            }
            result.put(start, table.toString().trim());
            start = end - 1;
        }
        return result;
    }

    private boolean sameTableColumns(PdfLine reference, PdfLine candidate) {
        if (candidate.columns == null || candidate.columns.length != reference.columns.length) return false;
        for (int index = 0; index < reference.columnX.length; index++) {
            if (Math.abs(reference.columnX[index] - candidate.columnX[index]) > 16f) return false;
        }
        return true;
    }

    private String cleanMarkdown(String markdown) {
        return markdown.replaceAll("[ \\t]+\\n", "\n").replaceAll("\\n{3,}", "\n\n").trim();
    }

    private static final class PdfLine {
        private final String text;
        private final float fontSize;
        private final boolean bold;
        private final float y;
        private final boolean paragraphBreak;
        private final String[] columns;
        private final float[] columnX;

        private PdfLine(String text, float fontSize, boolean bold, float y, boolean paragraphBreak,
                        String[] columns, float[] columnX) {
            this.text = text;
            this.fontSize = fontSize;
            this.bold = bold;
            this.y = y;
            this.paragraphBreak = paragraphBreak;
            this.columns = columns;
            this.columnX = columnX;
        }
    }

    private static final class CollectingPdfStripper extends PDFTextStripper {
        private final List<PdfLine> lines = new ArrayList<>();
        private final StringBuilder currentText = new StringBuilder();
        private final List<TextPosition> currentPositions = new ArrayList<>();
        private float previousY = Float.NaN;

        private CollectingPdfStripper() throws IOException {
            setSortByPosition(true);
            setSuppressDuplicateOverlappingText(true);
        }

        @Override
        protected void writeString(String text, List<TextPosition> positions) {
            String value = text == null ? "" : text.trim();
            if (value.isEmpty() || positions == null || positions.isEmpty()) return;
            if (!currentPositions.isEmpty()
                    && Math.abs(positions.getFirst().getYDirAdj() - currentPositions.getFirst().getYDirAdj()) > 2f) {
                flushCurrentLine();
            }
            if (!currentText.isEmpty()) currentText.append(' ');
            currentText.append(value);
            currentPositions.addAll(positions);
        }

        @Override
        protected void writeLineSeparator() {
            flushCurrentLine();
        }

        private void flushCurrentLine() {
            if (currentPositions.isEmpty()) return;
            String value = currentText.toString().trim();
            if (value.isEmpty()) {
                currentText.setLength(0);
                currentPositions.clear();
                return;
            }
            float fontSize = 0;
            boolean bold = false;
            float y = currentPositions.getFirst().getYDirAdj();
            List<TextPosition> sorted = new ArrayList<>(currentPositions);
            sorted.sort(Comparator.comparingDouble(TextPosition::getXDirAdj));
            for (TextPosition position : sorted) {
                fontSize = Math.max(fontSize, position.getFontSizeInPt());
                String font = position.getFont() == null ? "" : position.getFont().getName().toLowerCase(Locale.ROOT);
                bold |= font.contains("bold") || font.contains("semibold") || font.contains("heavy") || font.contains("black");
            }
            List<Integer> columnStarts = new ArrayList<>();
            float previousEnd = Float.NEGATIVE_INFINITY;
            for (int index = 0; index < sorted.size(); index++) {
                TextPosition position = sorted.get(index);
                if (index > 0 && position.getXDirAdj() - previousEnd > 12f) columnStarts.add(index);
                previousEnd = Math.max(previousEnd, position.getXDirAdj() + position.getWidthDirAdj());
            }
            String[] columns = null;
            float[] columnX = null;
            if (!columnStarts.isEmpty()) {
                List<StringBuilder> cells = new ArrayList<>();
                List<Float> starts = new ArrayList<>();
                int column = 0;
                cells.add(new StringBuilder());
                starts.add(sorted.getFirst().getXDirAdj());
                for (int index = 0; index < sorted.size(); index++) {
                    if (column < columnStarts.size() && index == columnStarts.get(column)) {
                        column++;
                        cells.add(new StringBuilder());
                        starts.add(sorted.get(index).getXDirAdj());
                    }
                    cells.get(column).append(sorted.get(index).getUnicode());
                }
                columns = cells.stream().map(cell -> cell.toString().trim()).toArray(String[]::new);
                columnX = new float[starts.size()];
                for (int index = 0; index < starts.size(); index++) columnX[index] = starts.get(index);
            }
            boolean paragraphBreak = !Float.isNaN(previousY) && y - previousY > Math.max(14f, fontSize * 1.8f);
            lines.add(new PdfLine(value, fontSize, bold, y, paragraphBreak, columns, columnX));
            previousY = y;
            currentText.setLength(0);
            currentPositions.clear();
        }

        private List<PdfLine> consumeLines() {
            flushCurrentLine();
            List<PdfLine> result = new ArrayList<>(lines);
            lines.clear();
            previousY = Float.NaN;
            return result;
        }
    }

    private String parseDocx(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            StringBuilder markdown = new StringBuilder();
            for (IBodyElement element : document.getBodyElements()) {
                if (element instanceof XWPFParagraph paragraph) appendDocxParagraph(paragraph, markdown);
                else if (element instanceof XWPFTable table) markdown.append(formatDocxTable(table));
            }
            return markdown.toString().trim();
        }
    }

    private String parseDoc(MultipartFile file) throws IOException {
        try (HWPFDocument document = new HWPFDocument(file.getInputStream()); WordExtractor extractor = new WordExtractor(document)) {
            StringBuilder markdown = new StringBuilder();
            for (String paragraph : extractor.getParagraphText()) {
                String value = paragraph == null ? "" : paragraph.trim();
                if (!value.isEmpty()) markdown.append(value).append("\n\n");
            }
            return markdown.toString().trim();
        }
    }

    private void appendDocxParagraph(XWPFParagraph paragraph, StringBuilder markdown) {
        String rawText = paragraph.getText() == null ? "" : paragraph.getText().trim();
        if (rawText.isEmpty()) return;
        int headingLevel = getDocxHeadingLevel(paragraph);
        String text = buildDocxParagraphText(paragraph);
        if (headingLevel > 0) markdown.append("#".repeat(headingLevel)).append(' ').append(text).append("\n\n");
        else if (paragraph.getNumID() != null || rawText.matches("^[•*-]\\s+.*")) markdown.append("- ").append(text.replaceFirst("^[•*-]\\s+", "")).append("\n");
        else if (rawText.matches("^\\d+[.)]\\s+.*")) markdown.append("1. ").append(text.replaceFirst("^\\d+[.)]\\s+", "")).append("\n");
        else markdown.append(text).append("\n\n");
    }

    private int getDocxHeadingLevel(XWPFParagraph paragraph) {
        String style = paragraph.getStyleID();
        if (style != null && style.matches("(?i)heading\\d+")) return Math.min(6, Integer.parseInt(style.replaceAll("(?i)heading", "")));
        CTPPr properties = paragraph.getCTP().getPPr();
        if (properties != null && properties.getOutlineLvl() != null) return Math.min(6, properties.getOutlineLvl().getVal().intValue() + 1);
        return 0;
    }

    private String buildDocxParagraphText(XWPFParagraph paragraph) {
        StringBuilder text = new StringBuilder();
        for (XWPFRun run : paragraph.getRuns()) text.append(formatDocxRun(run, run.text()));
        return text.toString().trim();
    }

    private String formatDocxRun(XWPFRun run, String text) {
        if (!StringUtils.hasText(text)) return "";
        String result = text;
        if (run.isBold()) result = "**" + result + "**";
        if (run.isItalic()) result = "*" + result + "*";
        if (run.getUnderline() != UnderlinePatterns.NONE) result = "<u>" + result + "</u>";
        return result;
    }

    private String formatDocxTable(XWPFTable table) {
        StringBuilder markdown = new StringBuilder("\n");
        List<XWPFTableRow> rows = table.getRows();
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            markdown.append('|');
            for (XWPFTableCell cell : rows.get(rowIndex).getTableCells()) markdown.append(' ').append(escapeCell(cell.getText())).append(" |");
            markdown.append('\n');
            if (rowIndex == 0) appendDivider(markdown, rows.get(rowIndex).getTableCells().size());
        }
        return markdown.append('\n').toString();
    }

    private String parseSpreadsheet(MultipartFile file) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            DataFormatter formatter = new DataFormatter();
            StringBuilder markdown = new StringBuilder();
            for (Sheet sheet : workbook) {
                markdown.append("## ").append(sheet.getSheetName()).append("\n\n");
                int maxColumns = 0;
                for (Row row : sheet) maxColumns = Math.max(maxColumns, Math.max(0, row.getLastCellNum()));
                if (maxColumns == 0) continue;
                boolean firstRow = true;
                for (Row row : sheet) {
                    markdown.append('|');
                    for (int column = 0; column < maxColumns; column++) markdown.append(' ').append(escapeCell(formatter.formatCellValue(row.getCell(column)))).append(" |");
                    markdown.append('\n');
                    if (firstRow) { appendDivider(markdown, maxColumns); firstRow = false; }
                }
                markdown.append('\n');
            }
            return markdown.toString().trim();
        }
    }

    private String parsePptx(MultipartFile file) throws IOException {
        try (XMLSlideShow presentation = new XMLSlideShow(file.getInputStream())) {
            StringBuilder markdown = new StringBuilder();
            int index = 1;
            for (XSLFSlide slide : presentation.getSlides()) {
                markdown.append("## Slide ").append(index++).append("\n\n");
                for (XSLFShape shape : slide.getShapes()) appendPptxShape(shape, markdown);
                markdown.append('\n');
            }
            return markdown.toString().trim();
        }
    }

    private void appendPptxShape(XSLFShape shape, StringBuilder markdown) {
        if (shape instanceof XSLFGroupShape group) {
            for (XSLFShape child : group.getShapes()) appendPptxShape(child, markdown);
        } else if (shape instanceof XSLFTable table) {
            List<XSLFTableRow> rows = table.getRows();
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                markdown.append('|');
                for (XSLFTableCell cell : rows.get(rowIndex).getCells()) markdown.append(' ').append(escapeCell(cell.getText())).append(" |");
                markdown.append('\n');
                if (rowIndex == 0) appendDivider(markdown, rows.get(rowIndex).getCells().size());
            }
            markdown.append('\n');
        } else if (shape instanceof XSLFTextShape textShape) {
            StringBuilder text = new StringBuilder();
            for (XSLFTextParagraph paragraph : textShape.getTextParagraphs()) {
                for (XSLFTextRun run : paragraph.getTextRuns()) {
                    String fragment = run.getRawText();
                    if (!StringUtils.hasText(fragment)) continue;
                    if (run.isBold()) fragment = "**" + fragment + "**";
                    if (run.isItalic()) fragment = "*" + fragment + "*";
                    text.append(fragment);
                }
                text.append('\n');
            }
            if (!text.toString().isBlank()) markdown.append(text.toString().trim()).append("\n\n");
        }
    }

    private String parsePpt(MultipartFile file) throws IOException {
        try (HSLFSlideShow presentation = new HSLFSlideShow(file.getInputStream())) {
            StringBuilder markdown = new StringBuilder();
            int index = 1;
            for (HSLFSlide slide : presentation.getSlides()) {
                markdown.append("## Slide ").append(index++).append("\n\n");
                slide.getShapes().stream().filter(HSLFTextShape.class::isInstance).map(HSLFTextShape.class::cast)
                        .map(HSLFTextShape::getText).filter(StringUtils::hasText).forEach(text -> markdown.append(text.trim()).append("\n\n"));
            }
            return markdown.toString().trim();
        }
    }

    private String parsePlainText(MultipartFile file) throws IOException {
        return new String(file.getBytes(), StandardCharsets.UTF_8).trim();
    }

    private void appendDivider(StringBuilder markdown, int columns) {
        markdown.append('|');
        for (int column = 0; column < columns; column++) markdown.append(" --- |");
        markdown.append('\n');
    }

    private String escapeCell(String value) {
        return value == null ? "" : value.replace("|", "\\|").replaceAll("[\\r\\n]+", " ").trim();
    }

    private String extensionOf(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
