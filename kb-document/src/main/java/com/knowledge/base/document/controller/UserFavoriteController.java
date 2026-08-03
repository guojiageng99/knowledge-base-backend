package com.knowledge.base.document.controller;

import com.knowledge.base.common.result.Result;
import com.knowledge.base.document.service.UserFavoriteService;
import com.knowledge.base.document.utils.UserContext;
import com.knowledge.base.document.vo.UserFavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class UserFavoriteController {
    private final UserFavoriteService userFavoriteService;

    @PostMapping("/toggle/{documentId}")
    public Result<Boolean> toggle(@PathVariable Long documentId) {
        return Result.success(userFavoriteService.toggleFavorite(UserContext.getCurrentUserId(), documentId));
    }

    @PostMapping("/add/{documentId}")
    public Result<Boolean> add(@PathVariable Long documentId) {
        return Result.success(userFavoriteService.addFavorite(UserContext.getCurrentUserId(), documentId));
    }

    @DeleteMapping("/remove/{documentId}")
    public Result<Boolean> remove(@PathVariable Long documentId) {
        return Result.success(userFavoriteService.removeFavorite(UserContext.getCurrentUserId(), documentId));
    }

    @GetMapping("/check/{documentId}")
    public Result<Boolean> check(@PathVariable Long documentId) {
        return Result.success(userFavoriteService.isFavorited(UserContext.getCurrentUserId(), documentId));
    }

    @GetMapping("/list")
    public Result<List<UserFavoriteVO>> list() {
        return Result.success(userFavoriteService.getUserFavorites(UserContext.getCurrentUserId()));
    }

    @GetMapping("/count/{documentId}")
    public Result<Long> count(@PathVariable Long documentId) {
        return Result.success(userFavoriteService.getFavoriteCount(documentId));
    }
}
