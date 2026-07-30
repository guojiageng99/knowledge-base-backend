package com.knowledge.base.foundation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.foundation.dto.NotificationDTO;
import com.knowledge.base.foundation.dto.NotificationQueryDTO;
import com.knowledge.base.foundation.entity.Notification;
import com.knowledge.base.foundation.vo.NotificationVO;

public interface NotificationService {

    Result<Long> sendNotification(NotificationDTO notificationDTO);

    Result<IPage<NotificationVO>> getNotifications(NotificationQueryDTO queryDTO);

    IPage<Notification> pageNotifications(Long current, Long size, Long userId, Integer isRead);

    Notification getNotificationById(Long id);

    boolean sendNotification(Notification notification);

    boolean markAsRead(Long id);

    boolean markAllAsRead(Long userId);

    boolean deleteNotification(Long id);

    long getUnreadCount(Long userId);
}
