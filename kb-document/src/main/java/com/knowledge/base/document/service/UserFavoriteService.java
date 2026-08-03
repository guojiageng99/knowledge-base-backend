package com.knowledge.base.document.service;

import com.knowledge.base.document.vo.UserFavoriteVO;

import java.util.List;

public interface UserFavoriteService {
    Boolean addFavorite(Long userId, Long documentId);
    Boolean removeFavorite(Long userId, Long documentId);
    Boolean isFavorited(Long userId, Long documentId);
    List<UserFavoriteVO> getUserFavorites(Long userId);
    Long getFavoriteCount(Long documentId);
    Boolean toggleFavorite(Long userId, Long documentId);
}
