package com.knowledge.base.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.common.utils.SnowflakeIdGenerator;
import com.knowledge.base.document.entity.Document;
import com.knowledge.base.document.entity.UserFavorite;
import com.knowledge.base.document.mapper.DocumentMapper;
import com.knowledge.base.document.mapper.UserFavoriteMapper;
import com.knowledge.base.document.service.UserFavoriteService;
import com.knowledge.base.document.vo.UserFavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite>
        implements UserFavoriteService {

    private final UserFavoriteMapper userFavoriteMapper;
    private final DocumentMapper documentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addFavorite(Long userId, Long documentId) {
        requireUser(userId);
        Document document = requireDocument(documentId);
        if (userFavoriteMapper.findByUserAndDocument(userId, documentId) != null) return true;

        UserFavorite favorite = new UserFavorite();
        favorite.setId(SnowflakeIdGenerator.nextId());
        favorite.setUserId(userId);
        favorite.setDocumentId(documentId);
        favorite.setDocumentTitle(document.getTitle());
        favorite.setDocumentCategoryId(document.getCategoryId());
        if (userFavoriteMapper.insert(favorite) <= 0) return false;
        documentMapper.incrementFavoriteCount(documentId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeFavorite(Long userId, Long documentId) {
        requireUser(userId);
        int deleted = userFavoriteMapper.physicalDelete(userId, documentId);
        if (deleted > 0) documentMapper.decrementFavoriteCount(documentId);
        return deleted > 0;
    }

    @Override
    public Boolean isFavorited(Long userId, Long documentId) {
        return userId != null && userFavoriteMapper.findByUserAndDocument(userId, documentId) != null;
    }

    @Override
    public List<UserFavoriteVO> getUserFavorites(Long userId) {
        requireUser(userId);
        List<UserFavoriteVO> result = new ArrayList<>();
        for (UserFavorite favorite : userFavoriteMapper.getUserFavorites(userId)) {
            UserFavoriteVO vo = new UserFavoriteVO();
            vo.setId(favorite.getId());
            vo.setUserId(favorite.getUserId());
            vo.setDocumentId(favorite.getDocumentId());
            vo.setDocumentTitle(favorite.getDocumentTitle());
            vo.setDocumentSummary(favorite.getDocumentSummary());
            vo.setDocumentCategoryId(favorite.getDocumentCategoryId());
            vo.setDocumentCategoryName(favorite.getDocumentCategoryName());
            vo.setDocumentAuthorId(favorite.getDocumentAuthorId());
            vo.setDocumentAuthorName(favorite.getDocumentAuthorName());
            vo.setDocumentStatus(favorite.getDocumentStatus());
            vo.setDocumentViewCount(favorite.getDocumentViewCount());
            vo.setFavoriteTime(favorite.getFavoriteTime());
            vo.setIsFavorited(true);
            result.add(vo);
        }
        return result;
    }

    @Override
    public Long getFavoriteCount(Long documentId) {
        Integer count = userFavoriteMapper.countByDocumentId(documentId);
        return count == null ? 0L : count.longValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean toggleFavorite(Long userId, Long documentId) {
        if (isFavorited(userId, documentId)) {
            removeFavorite(userId, documentId);
            return false;
        }
        return addFavorite(userId, documentId);
    }

    private Document requireDocument(Long documentId) {
        if (documentId == null) throw new BusinessException("Document ID is required");
        Document document = documentMapper.selectById(documentId);
        if (document == null) throw new BusinessException("Document does not exist");
        return document;
    }

    private void requireUser(Long userId) {
        if (userId == null) throw new BusinessException("User is not authenticated");
    }
}
