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
import com.knowledge.base.document.mapper.DocumentMapper;
import com.knowledge.base.document.service.DocumentService;
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
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {

    private final DocumentMapper documentMapper;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.upload.max-size:104857600}")
    private long maxFileSize;

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
        document.setAuthorId(1L);
        document.setAuthorName("System Administrator");
        if (Objects.equals(document.getStatus(), 1)) {
            document.setPublishTime(LocalDateTime.now());
        }
        if (documentMapper.insert(document) <= 0) {
            throw new BusinessException("Failed to create document");
        }
        return document.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDocument(DocumentDTO dto) {
        if (dto.getId() == null) throw new BusinessException("Document ID is required");
        Document existing = requireDocument(dto.getId());
        Document document = BeanUtil.copyProperties(dto, Document.class);
        if (Objects.equals(existing.getStatus(), 0) && Objects.equals(dto.getStatus(), 1)) {
            document.setPublishTime(LocalDateTime.now());
        }
        return documentMapper.updateById(document) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteDocument(Long documentId) {
        if (documentId == null) throw new BusinessException("Document ID is required");
        return documentMapper.deleteById(documentId) > 0;
    }

    @Override
    public DocumentVO getDocumentById(Long documentId) {
        return BeanUtil.copyProperties(requireDocument(documentId), DocumentVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentVO viewDocument(Long documentId) {
        Document document = requireDocument(documentId);
        documentMapper.incrementViewCount(documentId);
        document.setViewCount(document.getViewCount() + 1);
        return BeanUtil.copyProperties(document, DocumentVO.class);
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
                .convert(document -> BeanUtil.copyProperties(document, DocumentVO.class));
    }

    @Override
    public String uploadDocumentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("File must not be empty");
        if (file.getSize() > maxFileSize) throw new BusinessException("File size exceeds the limit");
        String extension = FileUtil.extName(file.getOriginalFilename());
        if (!StrUtil.isNotBlank(extension)) throw new BusinessException("Unsupported file type");
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
}
