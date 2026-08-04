package com.knowledge.base.foundation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.common.utils.UserContextUtil;
import com.knowledge.base.foundation.dto.NotificationQueryDTO;
import com.knowledge.base.foundation.dto.NotificationDTO;
import com.knowledge.base.foundation.vo.NotificationVO;
import jakarta.validation.Valid;
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
    public Result<IPage<NotificationVO>> pageNotifications(@RequestParam(defaultValue = "1") Long current,
                                                          @RequestParam(defaultValue = "10") Long size,
                                                          @RequestParam(required = false) String notificationType,
                                                          @RequestParam(required = false) Integer isRead) {
        NotificationQueryDTO query = new NotificationQueryDTO();
        query.setCurrent(current);
        query.setSize(size);
        query.setUserId(requireUserId());
        query.setNotificationType(notificationType);
        query.setIsRead(isRead);
        return notificationService.getNotifications(query);
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        return Result.success(notificationService.getUnreadCount(requireUserId()));
    }

    @GetMapping("/{id}")
    public Result<Notification> getNotificationById(@PathVariable Long id) {
        return Result.success(notificationService.getNotificationById(id, requireUserId()));
    }

    @PostMapping
    public Result<Long> sendNotification(@Valid @RequestBody NotificationDTO notification) {
        return notificationService.sendNotification(notification);
    }

    @PutMapping("/{id}/read")
    public Result<Boolean> markAsRead(@PathVariable Long id) {
        return Result.success(notificationService.markAsRead(id, requireUserId()));
    }

    @PutMapping("/read-all")
    public Result<Boolean> markAllAsRead() {
        return Result.success(notificationService.markAllAsRead(requireUserId()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteNotification(@PathVariable Long id) {
        if (notificationService.getNotificationById(id, requireUserId()) == null) {
            return Result.success(false);
        }
        return Result.success(notificationService.deleteNotification(id));
    }

    @DeleteMapping("/clear-all")
    public Result<Boolean> clearAll() {
        return Result.success(notificationService.clearAll(requireUserId()));
    }

    private Long requireUserId() {
        Long userId = UserContextUtil.getUserId();
        if (userId == null) {
            throw new IllegalStateException("Current user is not authenticated");
        }
        return userId;
    }
}
