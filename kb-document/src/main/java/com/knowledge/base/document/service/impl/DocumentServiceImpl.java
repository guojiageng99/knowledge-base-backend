package com.knowledge.base.document.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.common.event.StatisticsEventDTO;
import com.knowledge.base.common.utils.SnowflakeIdGenerator;
import com.knowledge.base.document.dto.DocumentDTO;
import com.knowledge.base.document.dto.AutoSaveDTO;
import com.knowledge.base.document.config.RabbitMQConfig;
import com.knowledge.base.document.entity.Document;
import com.knowledge.base.document.entity.DocumentTag;
import com.knowledge.base.document.entity.Tag;
import com.knowledge.base.document.entity.mongodb.DocumentContent;
import com.knowledge.base.document.mapper.DocumentTagMapper;
import com.knowledge.base.document.mapper.DocumentMapper;
import com.knowledge.base.document.mapper.TagMapper;
import com.knowledge.base.document.service.DocumentService;
import com.knowledge.base.document.service.DocumentVersionService;
import com.knowledge.base.document.service.DocumentContentService;
import com.knowledge.base.document.service.DocumentAccessService;
import com.knowledge.base.document.service.AutoSaveHistoryService;
import com.knowledge.base.document.service.FileParserService;
import com.knowledge.base.document.utils.UserContext;
import com.knowledge.base.document.vo.AuthorVO;
import com.knowledge.base.document.vo.DocumentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {

    private static final int DOCUMENT_TARGET_TYPE = 1;

    private final DocumentMapper documentMapper;
    private final DocumentTagMapper documentTagMapper;
    private final TagMapper tagMapper;
    private final DocumentVersionService documentVersionService;
    private final DocumentContentService documentContentService;
    private final DocumentAccessService documentAccessService;
    private final com.knowledge.base.document.service.FileUploadService fileUploadService;
    private final FileParserService fileParserService;
    private final com.knowledge.base.document.feign.RagFeignClient ragFeignClient;
    private final com.knowledge.base.document.feign.KAGFeignClient kagFeignClient;
    private final com.knowledge.base.document.feign.SearchFeignClient searchFeignClient;
    private final JdbcTemplate jdbcTemplate;
    private final AutoSaveHistoryService autoSaveHistoryService;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQConfig rabbitMQConfig;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.upload.max-size:104857600}")
    private long maxFileSize;

    @Value("${file.upload.allowed-types:pdf,doc,docx,xls,xlsx,ppt,pptx,txt,md}")
    private List<String> allowedFileTypes;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDocument(DocumentDTO dto) {
        Document document = BeanUtil.copyProperties(dto, Document.class);
        document.setId(SnowflakeIdGenerator.nextId());
        document.setDocumentType(defaultValue(document.getDocumentType(), 1));
        document.setStatus(defaultValue(document.getStatus(), 0));
        document.setIsPublic(defaultValue(document.getIsPublic(), 1));
        document.setIsTop(defaultValue(document.getIsTop(), 0));
        document.setIsRecommend(defaultValue(document.getIsRecommend(), 0));
        document.setSource(defaultValue(document.getSource(), 1));
        document.setAllowComment(defaultValue(document.getAllowComment(), 1));
        document.setSort(defaultValue(document.getSort(), 0));
        document.setViewCount(0L);
        document.setLikeCount(0L);
        document.setFavoriteCount(0L);
        document.setCommentCount(0L);
        document.setAuthorId(UserContext.getCurrentUserId() == null ? 1L : UserContext.getCurrentUserId());
        document.setAuthorName(UserContext.getCurrentUserName() == null ? "System Administrator" : UserContext.getCurrentUserName());
        if (Objects.equals(document.getStatus(), 1)) {
            document.setPublishTime(LocalDateTime.now());
        }
        if (documentMapper.insert(document) <= 0) {
            throw new BusinessException("Failed to create document");
        }
        saveContent(document, dto.getContent());
        if (dto.getTagIds() != null) {
            addTagsToDocument(document.getId(), dto.getTagIds());
        }
        documentVersionService.createVersion(document.getId(), null, 1L);
        triggerRagReindex(document.getId());
        if (Objects.equals(document.getStatus(), 1)) triggerSearchIndex(document.getId());
        if (Objects.equals(document.getStatus(), 1)) triggerGraphBuild(document.getId());
        return document.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDocument(DocumentDTO dto) {
        if (dto.getId() == null) throw new BusinessException("Document ID is required");
        Document existing = requireDocument(dto.getId());
        Document document = BeanUtil.copyProperties(dto, Document.class);
        updateContent(existing, document, dto.getContent());
        if (Objects.equals(existing.getStatus(), 0) && Objects.equals(dto.getStatus(), 1)) {
            document.setPublishTime(LocalDateTime.now());
        }
        boolean updated = documentMapper.updateById(document) > 0;
        if (dto.getTagIds() != null) {
            addTagsToDocument(dto.getId(), dto.getTagIds());
        }
        if (updated) {
            documentVersionService.createVersion(dto.getId(), buildChangeDescription(existing, document), 1L);
            triggerRagReindex(dto.getId());
            if (Objects.equals(dto.getStatus(), 1)) triggerSearchIndex(dto.getId());
            if (Objects.equals(dto.getStatus(), 1)) triggerGraphBuild(dto.getId());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long autoSaveDocument(AutoSaveDTO dto) {
        Long authorId = currentUserId();
        String authorName = UserContext.getCurrentUserName() == null ? "System Administrator" : UserContext.getCurrentUserName();
        Document document;
        if (dto.getId() != null) {
            document = requireDocument(dto.getId());
            if (!authorId.equals(document.getAuthorId())) throw new BusinessException("You do not have permission to save this document");
        } else {
            document = findRecentAutoSaveDraft(authorId);
            if (document == null) {
                document = new Document();
                document.setId(SnowflakeIdGenerator.nextId());
                document.setDocumentType(1);
                document.setContent("");
                document.setStatus(0);
                document.setIsPublic(1);
                document.setIsTop(0);
                document.setIsRecommend(0);
                document.setSource(1);
                document.setAllowComment(1);
                document.setSort(0);
                document.setViewCount(0L);
                document.setLikeCount(0L);
                document.setFavoriteCount(0L);
                document.setCommentCount(0L);
                document.setAuthorId(authorId);
                document.setAuthorName(authorName);
                document.setAutoSaveDismissed(0);
                applyAutoSaveFields(document, dto, true);
                if (documentMapper.insert(document) <= 0) throw new BusinessException("Failed to create automatic-save draft");
            }
        }

        applyAutoSaveFields(document, dto, false);
        document.setStatus(0);
        if (documentMapper.updateById(document) <= 0) throw new BusinessException("Failed to save draft");
        if (dto.getContent() != null) {
            updateDocumentContent(document.getId(), dto.getContent());
        }
        autoSaveHistoryService.saveSnapshot(document.getId(), document.getTitle(), dto.getContent(), authorId);
        return document.getId();
    }

    @Override
    public void dismissAutoSaveDrafts() {
        Long userId = currentUserId();
        documentMapper.update(null, new LambdaUpdateWrapper<Document>()
                .eq(Document::getAuthorId, userId)
                .eq(Document::getStatus, 0)
                .set(Document::getAutoSaveDismissed, 1));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteDocument(Long documentId) {
        if (documentId == null) throw new BusinessException("Document ID is required");
        Document document = requireDocument(documentId);
        deleteContent(document);
        autoSaveHistoryService.deleteByDocumentId(documentId);
        clearDocumentTags(documentId);
        boolean deleted = documentMapper.deleteById(documentId) > 0;
        if (deleted) { triggerRagDelete(documentId); triggerGraphDelete(documentId); triggerSearchDelete(documentId); }
        return deleted;
    }

    @Override
    public DocumentVO getDocumentById(Long documentId) {
        return toVO(requireDocument(documentId), true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentVO viewDocument(Long documentId) {
        Document document = requireDocument(documentId);
        documentMapper.incrementViewCount(documentId);
        publishStatistics("view", documentId, UserContext.getCurrentUserId());
        document.setViewCount(document.getViewCount() + 1);
        Long userId = UserContext.getCurrentUserId();
        String documentTitle = document.getTitle();
        CompletableFuture.runAsync(() -> {
            try {
                documentAccessService.recordAccess(userId, documentId, documentTitle);
            } catch (RuntimeException exception) {
                log.warn("Failed to record document access for document {}", documentId, exception);
            }
        });
        return toVO(document, true);
    }

    @Override
    public IPage<DocumentVO> pageDocuments(Long current, Long size, Long categoryId, String keyword, Integer status) {
        LambdaQueryWrapper<Document> query = new LambdaQueryWrapper<Document>()
                .eq(categoryId != null, Document::getCategoryId, categoryId)
                .eq(status != null, Document::getStatus, status)
                .and(StringUtils.hasText(keyword), wrapper -> wrapper.like(Document::getTitle, keyword)
                        .or().like(Document::getSummary, keyword)
                        .or().like(Document::getContent, keyword)
                        .or().like(Document::getTags, keyword))
                .orderByDesc(Document::getIsTop).orderByDesc(Document::getSort).orderByDesc(Document::getPublishTime);
        return documentMapper.selectPage(new Page<Document>(current, size), query)
                .convert(document -> toVO(document, false));
    }

    @Override
    public String uploadDocumentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("File must not be empty");
        if (file.getSize() > maxFileSize) throw new BusinessException("File size exceeds the limit");
        String extension = FileUtil.extName(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        if (!StrUtil.isNotBlank(extension)) throw new BusinessException("Unsupported file type");
        if (!allowedFileTypes.contains(extension)) throw new BusinessException("Unsupported file type");
        return fileUploadService.uploadDocumentFile(file, currentUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> uploadAndCreateDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("File must not be empty");
        if (file.getSize() > maxFileSize) throw new BusinessException("File size exceeds the limit");
        String extension = FileUtil.extName(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        if (!fileParserService.isSupported(extension)) {
            throw new BusinessException("Unsupported file type: " + extension + ". Supported: pdf, doc, docx, xls, xlsx, ppt, pptx, txt, md");
        }
        String content;
        try {
            content = fileParserService.parse(file);
        } catch (Exception exception) {
            log.warn("Failed to parse document import file {}", file.getOriginalFilename(), exception);
            throw new BusinessException("File parsing failed: " + exception.getMessage());
        }
        if (!StringUtils.hasText(content)) throw new BusinessException("File parsing produced no text content");

        String fileUrl = uploadDocumentFile(file);
        String originalName = file.getOriginalFilename();
        String title = StringUtils.hasText(originalName) && originalName.lastIndexOf('.') > 0
                ? originalName.substring(0, originalName.lastIndexOf('.')) : "Imported document";
        DocumentDTO dto = new DocumentDTO();
        dto.setTitle(title.length() > 200 ? title.substring(0, 200) : title);
        dto.setContent(content);
        dto.setDocumentType(2);
        dto.setFilePath(fileUrl);
        dto.setFileSize(file.getSize());
        dto.setFileExtension(extension);
        dto.setMimeType(file.getContentType());
        dto.setStatus(0);
        Long documentId = createDocument(dto);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("documentId", documentId);
        result.put("title", dto.getTitle());
        result.put("fileUrl", fileUrl);
        result.put("fileSize", file.getSize());
        result.put("contentLength", content.length());
        result.put("contentPreview", content.substring(0, Math.min(200, content.length())));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean likeDocument(Long documentId) {
        requireDocument(documentId);
        Long userId = currentUserId();
        int inserted = jdbcTemplate.update(
                "INSERT IGNORE INTO tb_like (id, target_id, target_type, user_id, created_at) VALUES (?, ?, ?, ?, NOW())",
                SnowflakeIdGenerator.nextId(), documentId, DOCUMENT_TARGET_TYPE, userId);
        if (inserted == 0) {
            return true;
        }
        if (documentMapper.incrementLikeCount(documentId) <= 0) {
            throw new BusinessException("Failed to update document like count");
        }
        publishStatistics("like", documentId, userId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unlikeDocument(Long documentId) {
        requireDocument(documentId);
        int deleted = jdbcTemplate.update(
                "DELETE FROM tb_like WHERE target_id = ? AND target_type = ? AND user_id = ?",
                documentId, DOCUMENT_TARGET_TYPE, currentUserId());
        if (deleted == 0) {
            return false;
        }
        if (documentMapper.decrementLikeCount(documentId) <= 0) {
            throw new BusinessException("Failed to update document like count");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean favoriteDocument(Long documentId) { requireDocument(documentId); return documentMapper.incrementFavoriteCount(documentId) > 0; }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publishDocument(Long documentId) { boolean published = updateStatus(documentId, 1, true); if (published) { triggerGraphBuild(documentId); triggerSearchIndex(documentId); } return published; }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean archiveDocument(Long documentId) { return updateStatus(documentId, 2, false); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addTagsToDocument(Long documentId, List<Long> tagIds) {
        requireDocument(documentId);
        replaceDocumentTags(documentId, tagIds);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDocumentContent(Long documentId, String content) {
        Document existing = requireDocument(documentId);
        Document update = new Document();
        update.setId(documentId);
        update.setContent(content);
        update.setContentLength(content == null ? 0 : content.length());
        try {
            String contentId = StringUtils.hasText(existing.getContentId())
                    ? documentContentService.updateContent(documentId, content)
                    : documentContentService.saveContent(documentId, content);
            update.setContentId(contentId);
            update.setContentLength(content == null ? 0 : content.length());
        } catch (RuntimeException exception) {
            log.warn("MongoDB content update failed for document {}. MySQL content remains available.", documentId);
        }
        return documentMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateSummary(Long documentId, String summary) {
        if (documentId == null) throw new BusinessException("Document ID is required");
        requireDocument(documentId);
        Document update = new Document();
        update.setId(documentId);
        update.setSummary(summary);
        return documentMapper.updateById(update) > 0;
    }

    @Override
    public String getDocumentContent(Long documentId) {
        Document document = requireDocument(documentId);
        if (StringUtils.hasText(document.getContentId())) {
            try {
                DocumentContent content = documentContentService.getContentById(document.getContentId());
                if (content != null) return content.getContent();
            } catch (RuntimeException exception) {
                log.warn("MongoDB content lookup failed for document {}.", documentId);
            }
        }
        return document.getContent();
    }

    @Override
    public String uploadImageFromUrl(String imageUrl) {
        return fileUploadService.uploadImageFromUrl(imageUrl);
    }

    private Document requireDocument(Long documentId) {
        if (documentId == null) throw new BusinessException("Document ID is required");
        Document document = documentMapper.selectById(documentId);
        if (document == null) throw new BusinessException("Document does not exist");
        return document;
    }

    private Boolean updateStatus(Long documentId, int status, boolean publish) {
        requireDocument(documentId);
        Document document = new Document();
        document.setId(documentId);
        document.setStatus(status);
        if (publish) document.setPublishTime(LocalDateTime.now());
        return documentMapper.updateById(document) > 0;
    }

    private Integer defaultValue(Integer value, int defaultValue) { return value == null ? defaultValue : value; }

    private void triggerGraphBuild(Long documentId) {
        CompletableFuture.runAsync(() -> {
            try { kagFeignClient.buildGraph(documentId); }
            catch (RuntimeException exception) { log.warn("Failed to trigger graph build for document {}", documentId, exception); }
        });
    }

    private void triggerGraphDelete(Long documentId) {
        CompletableFuture.runAsync(() -> {
            try { kagFeignClient.deleteGraph(documentId); }
            catch (RuntimeException exception) { log.warn("Failed to trigger graph cleanup for document {}", documentId, exception); }
        });
    }

    private void triggerSearchIndex(Long documentId) {
        CompletableFuture.runAsync(() -> {
            try { searchFeignClient.indexDocument(toSearchData(getDocumentById(documentId))); }
            catch (RuntimeException exception) { log.warn("Failed to index document {} for search", documentId, exception); }
        });
    }

    private void triggerSearchDelete(Long documentId) {
        CompletableFuture.runAsync(() -> {
            try { searchFeignClient.deleteDocument(documentId); }
            catch (RuntimeException exception) { log.warn("Failed to remove document {} from search", documentId, exception); }
        });
    }

    private java.util.Map<String, Object> toSearchData(DocumentVO document) {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("id", document.getId()); data.put("title", document.getTitle()); data.put("summary", document.getSummary()); data.put("content", document.getContent());
        data.put("categoryId", document.getCategoryId()); data.put("categoryName", document.getCategoryName()); data.put("tags", document.getTags()); data.put("authorId", document.getAuthorId());
        data.put("authorName", document.getAuthorName()); data.put("status", document.getStatus()); data.put("viewCount", document.getViewCount()); data.put("likeCount", document.getLikeCount());
        data.put("commentCount", document.getCommentCount()); data.put("isPublic", document.getIsPublic()); data.put("publishTime", document.getPublishTime()); data.put("createTime", document.getCreateTime()); data.put("updateTime", document.getUpdateTime());
        return data;
    }

    private void saveContent(Document document, String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }
        try {
            String contentId = documentContentService.saveContent(document.getId(), content);
            Document update = new Document();
            update.setId(document.getId());
            update.setContentId(contentId);
            // Keep the relational column for historical versions and MongoDB outage fallback.
            update.setContent(content);
            documentMapper.updateById(update);
            document.setContentId(contentId);
        } catch (RuntimeException exception) {
            log.warn("MongoDB content save failed for document {}. MySQL content remains available.", document.getId());
        }
    }

    private Document findRecentAutoSaveDraft(Long authorId) {
        return documentMapper.selectOne(new LambdaQueryWrapper<Document>()
                .eq(Document::getAuthorId, authorId).eq(Document::getStatus, 0)
                .eq(Document::getAutoSaveDismissed, 0)
                .ge(Document::getCreateTime, LocalDateTime.now().minusMinutes(5))
                .orderByDesc(Document::getCreateTime).last("LIMIT 1"));
    }

    private void applyAutoSaveFields(Document document, AutoSaveDTO dto, boolean creating) {
        if (creating || dto.getTitle() != null) document.setTitle(StringUtils.hasText(dto.getTitle()) ? dto.getTitle().trim() : "Untitled document");
        if (dto.getSummary() != null) document.setSummary(dto.getSummary());
        if (dto.getCategoryId() != null) document.setCategoryId(dto.getCategoryId());
        if (dto.getTeamId() != null) document.setTeamId(dto.getTeamId());
        if (dto.getTags() != null) document.setTags(dto.getTags());
    }

    private void updateContent(Document existing, Document update, String content) {
        if (content == null) {
            return;
        }
        try {
            if (StringUtils.hasText(existing.getContentId())) {
                update.setContentId(documentContentService.updateContent(existing.getId(), content));
            } else {
                String contentId = documentContentService.saveContent(existing.getId(), content);
                update.setContentId(contentId);
            }
        } catch (RuntimeException exception) {
            log.warn("MongoDB content update failed for document {}. MySQL content remains available.", existing.getId());
        }
        update.setContent(content);
        update.setContentLength(content == null ? 0 : content.length());
    }

    private void deleteContent(Document document) {
        if (StringUtils.hasText(document.getContentId())) {
            try {
                documentContentService.deleteContent(document.getContentId());
            } catch (RuntimeException exception) {
                log.warn("MongoDB content deletion failed for document {}.", document.getId());
            }
        }
    }

    private void triggerRagReindex(Long documentId) {
        CompletableFuture.runAsync(() -> {
            try { ragFeignClient.reindexDocument(documentId); }
            catch (Exception exception) { log.warn("RAG reindex trigger failed for document {}", documentId, exception); }
        });
    }

    private void triggerRagDelete(Long documentId) {
        CompletableFuture.runAsync(() -> {
            try { ragFeignClient.removeFromIndex(documentId); }
            catch (Exception exception) { log.warn("RAG delete trigger failed for document {}", documentId, exception); }
        });
    }

    private DocumentVO toVO(Document document, boolean includeContent) {
        DocumentVO result = BeanUtil.copyProperties(document, DocumentVO.class);
        result.setIsLiked(isDocumentLiked(document.getId()));
        result.setAuthor(buildAuthorVO(document));
        if (includeContent && StringUtils.hasText(document.getContentId())) {
            try {
                DocumentContent content = documentContentService.getContentById(document.getContentId());
                if (content != null) {
                    result.setContent(content.getContent());
                }
            } catch (RuntimeException exception) {
                log.warn("MongoDB content lookup failed for document {}. Falling back to MySQL content.", document.getId());
            }
        }
        return result;
    }

    private boolean isDocumentLiked(Long documentId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tb_like WHERE target_id = ? AND target_type = ? AND user_id = ?",
                Integer.class, documentId, DOCUMENT_TARGET_TYPE, currentUserId());
        return count != null && count > 0;
    }

    private Long currentUserId() {
        Long userId = UserContext.getCurrentUserId();
        return userId == null ? 1L : userId;
    }

    private void publishStatistics(String eventType, Long documentId, Long userId) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.STATISTICS_EXCHANGE,
                    rabbitMQConfig.statisticsRoutingKey(eventType),
                    StatisticsEventDTO.builder().eventType(eventType).documentId(documentId)
                            .userId(userId).timestamp(LocalDateTime.now()).build());
        } catch (RuntimeException exception) {
            log.warn("Failed to publish {} statistics event for document {}", eventType, documentId, exception);
        }
    }

    private AuthorVO buildAuthorVO(Document document) {
        AuthorVO author = new AuthorVO();
        author.setId(document.getAuthorId());
        author.setUsername(document.getAuthorName());
        author.setEmail("");
        author.setAvatar("");
        author.setPosition("");
        return author;
    }

    private String buildChangeDescription(Document oldDocument, Document newDocument) {
        List<String> changes = new ArrayList<>();
        if (newDocument.getTitle() != null && !Objects.equals(oldDocument.getTitle(), newDocument.getTitle())) {
            changes.add("标题变更");
        }
        if (newDocument.getContent() != null && !Objects.equals(oldDocument.getContent(), newDocument.getContent())) {
            changes.add("内容更新");
        }
        if (newDocument.getCategoryId() != null && !Objects.equals(oldDocument.getCategoryId(), newDocument.getCategoryId())) {
            changes.add("分类调整");
        }
        return changes.isEmpty() ? "文档更新" : String.join("、", changes);
    }

    private void replaceDocumentTags(Long documentId, List<Long> requestedTagIds) {
        Set<Long> newTagIds = requestedTagIds == null ? Set.of() : new LinkedHashSet<>(requestedTagIds);
        List<DocumentTag> existingRelations = documentTagMapper.selectList(new LambdaQueryWrapper<DocumentTag>()
                .eq(DocumentTag::getDocumentId, documentId));
        Set<Long> existingTagIds = existingRelations.stream().map(DocumentTag::getTagId).collect(java.util.stream.Collectors.toSet());

        for (Long tagId : newTagIds) {
            Tag tag = tagMapper.selectById(tagId);
            if (tag == null || !Integer.valueOf(1).equals(tag.getStatus())) {
                throw new BusinessException("Tag does not exist or is disabled: " + tagId);
            }
        }
        for (DocumentTag relation : existingRelations) {
            if (!newTagIds.contains(relation.getTagId())) {
                documentTagMapper.deleteById(relation.getId());
                tagMapper.decrementDocumentCount(relation.getTagId());
            }
        }
        for (Long tagId : newTagIds) {
            if (!existingTagIds.contains(tagId)) {
                DocumentTag relation = new DocumentTag();
                relation.setId(SnowflakeIdGenerator.nextId());
                relation.setDocumentId(documentId);
                relation.setTagId(tagId);
                relation.setCreateTime(LocalDateTime.now());
                documentTagMapper.insert(relation);
                tagMapper.incrementDocumentCount(tagId);
            }
        }
        updateDocumentTagNames(documentId, newTagIds);
    }

    private void clearDocumentTags(Long documentId) {
        List<DocumentTag> relations = documentTagMapper.selectList(new LambdaQueryWrapper<DocumentTag>()
                .eq(DocumentTag::getDocumentId, documentId));
        for (DocumentTag relation : relations) {
            documentTagMapper.deleteById(relation.getId());
            tagMapper.decrementDocumentCount(relation.getTagId());
        }
    }

    private void updateDocumentTagNames(Long documentId, Set<Long> tagIds) {
        String tagNames = tagIds.isEmpty() ? null : tagMapper.selectBatchIds(tagIds).stream()
                .map(Tag::getTagName)
                .collect(java.util.stream.Collectors.joining(","));
        Document update = new Document();
        update.setId(documentId);
        update.setTags(tagNames);
        documentMapper.updateById(update);
    }
}
