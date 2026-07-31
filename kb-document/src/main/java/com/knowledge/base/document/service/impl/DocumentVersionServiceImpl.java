package com.knowledge.base.document.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.common.utils.SnowflakeIdGenerator;
import com.knowledge.base.document.dto.DocumentVersionRestoreDTO;
import com.knowledge.base.document.entity.Document;
import com.knowledge.base.document.entity.DocumentVersion;
import com.knowledge.base.document.mapper.DocumentMapper;
import com.knowledge.base.document.mapper.DocumentVersionMapper;
import com.knowledge.base.document.service.DocumentVersionService;
import com.knowledge.base.document.vo.DocumentVersionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentVersionServiceImpl implements DocumentVersionService {

    private final DocumentVersionMapper documentVersionMapper;
    private final DocumentMapper documentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createVersion(Long documentId, String changeDescription, Long userId) {
        Document document = requireDocument(documentId);
        DocumentVersion previousVersion = documentVersionMapper.selectLatestByDocumentId(documentId);

        DocumentVersion version = new DocumentVersion();
        version.setId(SnowflakeIdGenerator.nextId());
        version.setDocumentId(documentId);
        version.setVersion(documentVersionMapper.getNextVersionNumber(documentId));
        version.setTitle(document.getTitle());
        version.setContent(document.getContent());
        version.setSummary(document.getSummary());
        version.setChangeDescription(changeDescription);
        version.setChangeSize(previousVersion == null ? 0L
                : contentLength(document.getContent()) - contentLength(previousVersion.getContent()));
        version.setOperatorId(userId == null ? 1L : userId);
        version.setOperatorName("系统用户");
        version.setCreatedAt(LocalDateTime.now());
        return documentVersionMapper.insert(version) > 0;
    }

    @Override
    public IPage<DocumentVersionVO> getVersionList(Long documentId, Long current, Long size) {
        requireDocument(documentId);
        long pageCurrent = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        return documentVersionMapper.selectPage(new Page<DocumentVersion>(pageCurrent, pageSize),
                        new LambdaQueryWrapper<DocumentVersion>()
                                .eq(DocumentVersion::getDocumentId, documentId)
                                .orderByDesc(DocumentVersion::getVersion))
                .convert(this::toVO);
    }

    @Override
    public DocumentVersionVO getVersionDetail(Long versionId) {
        return toVO(requireVersion(versionId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreVersion(Long documentId, DocumentVersionRestoreDTO restoreDTO, Long userId) {
        if (restoreDTO == null || restoreDTO.getVersionId() == null) {
            throw new BusinessException("Version ID is required");
        }
        Document document = requireDocument(documentId);
        DocumentVersion version = requireVersion(restoreDTO.getVersionId());
        if (!Objects.equals(version.getDocumentId(), documentId)) {
            throw new BusinessException("Version does not belong to this document");
        }

        String description = "恢复前自动备份";
        if (restoreDTO.getReason() != null && !restoreDTO.getReason().isBlank()) {
            description += "：" + restoreDTO.getReason();
        }
        createVersion(documentId, description, userId);

        document.setTitle(version.getTitle());
        document.setContent(version.getContent());
        document.setSummary(version.getSummary());
        return documentMapper.updateById(document) > 0;
    }

    @Override
    public String compareVersions(Long versionId1, Long versionId2) {
        if (versionId1 == null || versionId2 == null) {
            throw new BusinessException("Version ID is required");
        }
        if (Objects.equals(versionId1, versionId2)) {
            throw new BusinessException("Cannot compare the same version");
        }
        DocumentVersion version1 = requireVersion(versionId1);
        DocumentVersion version2 = requireVersion(versionId2);
        if (!Objects.equals(version1.getDocumentId(), version2.getDocumentId())) {
            throw new BusinessException("Versions do not belong to the same document");
        }

        String content1 = Objects.toString(version1.getContent(), "");
        String content2 = Objects.toString(version2.getContent(), "");
        StringBuilder diff = new StringBuilder("=== 版本对比 ===\n");
        diff.append(String.format("版本 %d vs 版本 %d\n\n", version1.getVersion(), version2.getVersion()));
        if (!Objects.equals(version1.getTitle(), version2.getTitle())) {
            diff.append("【标题差异】\n")
                    .append(String.format("- 版本%d: %s\n", version1.getVersion(), version1.getTitle()))
                    .append(String.format("+ 版本%d: %s\n\n", version2.getVersion(), version2.getTitle()));
        }
        if (!Objects.equals(content1, content2)) {
            diff.append("【内容差异】\n")
                    .append(String.format("版本%d 内容长度: %d 字符\n", version1.getVersion(), content1.length()))
                    .append(String.format("版本%d 内容长度: %d 字符\n", version2.getVersion(), content2.length()))
                    .append(String.format("差异大小: %d 字符\n", content2.length() - content1.length()));
        }
        return diff.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteVersion(Long versionId, Long userId) {
        DocumentVersion version = requireVersion(versionId);
        DocumentVersion latestVersion = documentVersionMapper.selectLatestByDocumentId(version.getDocumentId());
        if (latestVersion == null || !Objects.equals(latestVersion.getId(), versionId)) {
            throw new BusinessException("Only the latest version can be deleted");
        }
        log.info("Delete document version: versionId={}, userId={}", versionId, userId);
        return documentVersionMapper.deleteById(versionId) > 0;
    }

    private Document requireDocument(Long documentId) {
        if (documentId == null) {
            throw new BusinessException("Document ID is required");
        }
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException("Document does not exist");
        }
        return document;
    }

    private DocumentVersion requireVersion(Long versionId) {
        if (versionId == null) {
            throw new BusinessException("Version ID is required");
        }
        DocumentVersion version = documentVersionMapper.selectById(versionId);
        if (version == null) {
            throw new BusinessException("Version does not exist");
        }
        return version;
    }

    private DocumentVersionVO toVO(DocumentVersion version) {
        return BeanUtil.copyProperties(version, DocumentVersionVO.class);
    }

    private long contentLength(String content) {
        return content == null ? 0 : content.length();
    }
}
