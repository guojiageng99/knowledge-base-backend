package com.knowledge.base.document.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.common.utils.UserContextUtil;
import com.knowledge.base.document.dto.ShareDTO;
import com.knowledge.base.document.entity.Document;
import com.knowledge.base.document.entity.DocumentShare;
import com.knowledge.base.document.mapper.DocumentShareMapper;
import com.knowledge.base.document.service.DocumentService;
import com.knowledge.base.document.service.DocumentShareService;
import com.knowledge.base.document.vo.DocumentVO;
import com.knowledge.base.document.vo.ShareVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentShareServiceImpl implements DocumentShareService {
    private static final DateTimeFormatter SHARE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DocumentShareMapper shareMapper;
    private final DocumentService documentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareVO createShare(ShareDTO dto) {
        Long userId = requireUser();
        Document document = requireDocument(dto.getDocumentId());
        int expireType = dto.getExpireType() == null ? 1 : dto.getExpireType();
        int requirePassword = dto.getRequirePassword() == null ? 0 : dto.getRequirePassword();
        LocalDateTime expireTime = parseExpireTime(dto.getExpireTime(), expireType);
        if (requirePassword == 1 && (dto.getPassword() == null || dto.getPassword().isBlank())) {
            throw new BusinessException("Password is required");
        }
        DocumentShare share = new DocumentShare();
        share.setId(com.knowledge.base.common.utils.SnowflakeIdGenerator.nextId());
        share.setShareId(IdUtil.fastSimpleUUID().substring(0, 12));
        share.setDocumentId(document.getId());
        share.setTitle(document.getTitle());
        share.setShareType(dto.getShareType() == null ? 1 : dto.getShareType());
        share.setExpireType(expireType);
        share.setExpireTime(expireTime);
        share.setAccessLimit(dto.getAccessLimit() == null ? 0 : Math.max(0, dto.getAccessLimit()));
        share.setAccessCount(0);
        share.setRequirePassword(requirePassword);
        share.setPassword(requirePassword == 1 ? DigestUtil.md5Hex(dto.getPassword()) : null);
        share.setSharerId(userId);
        share.setSharerName(UserContextUtil.getCurrentUsername() == null ? "Unknown user" : UserContextUtil.getCurrentUsername());
        share.setDescription(dto.getDescription());
        share.setStatus(0);
        share.setShareTime(LocalDateTime.now());
        shareMapper.insert(share);
        return toVO(share);
    }

    @Override
    public ShareVO getShareById(String shareId) {
        return toVO(requireValidShare(shareId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long accessShare(String shareId, String password) {
        DocumentShare share = requireValidShare(shareId);
        verifyPassword(share, password);
        if (shareMapper.incrementAccessCount(shareId) <= 0) {
            throw new BusinessException("Share access limit reached");
        }
        return share.getDocumentId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentVO getSharedDocument(String shareId, String password) {
        return documentService.getDocumentById(accessShare(shareId, password));
    }

    @Override
    public List<ShareVO> getSharesByDocumentId(Long documentId) {
        requireUser();
        return shareMapper.selectValidSharesByDocumentId(documentId).stream().map(this::toVO).toList();
    }

    @Override
    public List<ShareVO> getMyShares() {
        return shareMapper.selectBySharerId(requireUser()).stream().map(this::toVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteShare(String shareId) {
        DocumentShare share = requireOwnedShare(shareId);
        share.setStatus(2);
        return shareMapper.updateById(share) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateShare(String shareId, ShareDTO dto) {
        DocumentShare share = requireOwnedShare(shareId);
        if (dto.getExpireType() != null) share.setExpireType(dto.getExpireType());
        if (dto.getExpireTime() != null) share.setExpireTime(parseExpireTime(dto.getExpireTime(), 2));
        if (dto.getAccessLimit() != null) share.setAccessLimit(Math.max(0, dto.getAccessLimit()));
        if (dto.getRequirePassword() != null) {
            share.setRequirePassword(dto.getRequirePassword());
            share.setPassword(dto.getRequirePassword() == 1 && dto.getPassword() != null ? DigestUtil.md5Hex(dto.getPassword()) : null);
        }
        if (dto.getDescription() != null) share.setDescription(dto.getDescription());
        return shareMapper.updateById(share) > 0;
    }

    private DocumentShare requireValidShare(String shareId) {
        DocumentShare share = shareMapper.selectByShareId(shareId);
        if (share == null || !Integer.valueOf(0).equals(share.getStatus())) throw new BusinessException("Share does not exist or is disabled");
        if (share.getExpireType() != null && share.getExpireType() == 2 && share.getExpireTime() != null && share.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Share has expired");
        }
        if (share.getAccessLimit() != null && share.getAccessLimit() > 0 && share.getAccessCount() >= share.getAccessLimit()) {
            throw new BusinessException("Share access limit reached");
        }
        return share;
    }

    private DocumentShare requireOwnedShare(String shareId) {
        DocumentShare share = requireValidShare(shareId);
        if (!requireUser().equals(share.getSharerId())) throw new BusinessException("You cannot modify this share");
        return share;
    }

    private void verifyPassword(DocumentShare share, String password) {
        if (Integer.valueOf(1).equals(share.getRequirePassword()) && (password == null || !DigestUtil.md5Hex(password).equals(share.getPassword()))) {
            throw new BusinessException("Incorrect share password");
        }
    }

    private LocalDateTime parseExpireTime(String value, int expireType) {
        if (expireType != 2) return null;
        if (value == null || value.isBlank()) throw new BusinessException("Expire time is required");
        try { return LocalDateTime.parse(value, SHARE_TIME_FORMAT); }
        catch (RuntimeException e) { throw new BusinessException("Invalid expire time"); }
    }

    private Long requireUser() {
        Long userId = UserContextUtil.getCurrentUserId();
        if (userId == null) throw new BusinessException("User is not authenticated");
        return userId;
    }

    private Document requireDocument(Long id) {
        Document document = documentService.getById(id);
        if (document == null) throw new BusinessException("Document does not exist");
        return document;
    }

    private ShareVO toVO(DocumentShare share) {
        ShareVO vo = new ShareVO();
        vo.setShareId(share.getShareId());
        vo.setShareUrl("/share/" + share.getShareId());
        vo.setDocumentId(share.getDocumentId());
        vo.setTitle(share.getTitle());
        vo.setShareType(share.getShareType());
        vo.setShareTypeDesc(Integer.valueOf(1).equals(share.getShareType()) ? "Public link" : "Private share");
        vo.setExpireType(share.getExpireType());
        vo.setExpireTime(share.getExpireTime());
        vo.setExpired(share.getStatus() != 0 || (share.getExpireTime() != null && share.getExpireTime().isBefore(LocalDateTime.now())));
        vo.setRequirePassword(Integer.valueOf(1).equals(share.getRequirePassword()));
        vo.setSharerName(share.getSharerName());
        vo.setShareTime(share.getShareTime());
        vo.setAccessCount(share.getAccessCount());
        vo.setAccessLimit(share.getAccessLimit());
        vo.setDescription(share.getDescription());
        return vo;
    }
}
