package com.knowledge.base.document.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.common.utils.SnowflakeIdGenerator;
import com.knowledge.base.document.dto.DocumentDTO;
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
import com.knowledge.base.document.utils.UserContext;
import com.knowledge.base.document.vo.AuthorVO;
import com.knowledge.base.document.vo.DocumentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Locale;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {

    private final DocumentMapper documentMapper;
    private final DocumentTagMapper documentTagMapper;
    private final TagMapper tagMapper;
    private final DocumentVersionService documentVersionService;
    private final DocumentContentService documentContentService;

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
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteDocument(Long documentId) {
        if (documentId == null) throw new BusinessException("Document ID is required");
        Document document = requireDocument(documentId);
        deleteContent(document);
        clearDocumentTags(documentId);
        return documentMapper.deleteById(documentId) > 0;
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
        document.setViewCount(document.getViewCount() + 1);
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
        String datePath = LocalDateTime.now().toLocalDate().toString();
        File directory = new File(uploadPath, datePath).getAbsoluteFile();
        if (!directory.exists() && !directory.mkdirs()) throw new BusinessException("Failed to create upload directory");
        String fileName = IdUtil.simpleUUID() + "." + extension;
        try {
            file.transferTo(new File(directory, fileName));
        } catch (IOException e) {
            log.error("Failed to upload document file to {}", directory, e);
            throw new BusinessException("File upload failed");
        }
        return datePath + File.separator + fileName;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean likeDocument(Long documentId) { requireDocument(documentId); return documentMapper.incrementLikeCount(documentId) > 0; }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean favoriteDocument(Long documentId) { requireDocument(documentId); return documentMapper.incrementFavoriteCount(documentId) > 0; }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publishDocument(Long documentId) { return updateStatus(documentId, 1, true); }

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

    private DocumentVO toVO(Document document, boolean includeContent) {
        DocumentVO result = BeanUtil.copyProperties(document, DocumentVO.class);
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
