package com.knowledge.base.foundation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.foundation.entity.Notification;
import com.knowledge.base.foundation.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Result<IPage<Notification>> pageNotifications(@RequestParam(defaultValue = "1") Long current,
                                                          @RequestParam(defaultValue = "10") Long size,
                                                          @RequestParam(required = false) Long userId,
                                                          @RequestParam(required = false) Integer isRead) {
        return Result.success(notificationService.pageNotifications(current, size, userId, isRead));
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(@RequestParam Long userId) {
        return Result.success(notificationService.getUnreadCount(userId));
    }

    @GetMapping("/{id}")
    public Result<Notification> getNotificationById(@PathVariable Long id) {
        return Result.success(notificationService.getNotificationById(id));
    }

    @PostMapping
    public Result<Boolean> sendNotification(@RequestBody Notification notification) {
        return Result.success(notificationService.sendNotification(notification));
    }

    @PutMapping("/{id}/read")
    public Result<Boolean> markAsRead(@PathVariable Long id) {
        return Result.success(notificationService.markAsRead(id));
    }

    @PutMapping("/read-all")
    public Result<Boolean> markAllAsRead(@RequestParam Long userId) {
        return Result.success(notificationService.markAllAsRead(userId));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteNotification(@PathVariable Long id) {
        return Result.success(notificationService.deleteNotification(id));
    }
}
