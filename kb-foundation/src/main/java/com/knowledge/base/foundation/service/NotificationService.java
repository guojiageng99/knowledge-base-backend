package com.knowledge.base.foundation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledge.base.foundation.entity.Notification;

public interface NotificationService {

    IPage<Notification> pageNotifications(Long current, Long size, Long userId, Integer isRead);

    Notification getNotificationById(Long id);

    boolean sendNotification(Notification notification);

    boolean markAsRead(Long id);

    boolean markAllAsRead(Long userId);

    boolean deleteNotification(Long id);

    long getUnreadCount(Long userId);
}
