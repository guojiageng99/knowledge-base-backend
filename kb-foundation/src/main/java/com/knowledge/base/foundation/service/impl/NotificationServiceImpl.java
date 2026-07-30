package com.knowledge.base.foundation.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.common.utils.SnowflakeIdGenerator;
import com.knowledge.base.foundation.dto.NotificationDTO;
import com.knowledge.base.foundation.dto.NotificationQueryDTO;
import com.knowledge.base.foundation.entity.Notification;
import com.knowledge.base.foundation.mapper.NotificationMapper;
import com.knowledge.base.foundation.service.NotificationService;
import com.knowledge.base.foundation.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> sendNotification(NotificationDTO notificationDTO) {
        Notification notification = BeanUtil.copyProperties(notificationDTO, Notification.class);
        notification.setIsRead(0);
        if (!sendNotification(notification)) {
            throw new BusinessException("Failed to send notification");
        }
        return Result.success(notification.getId());
    }

    @Override
    public Result<IPage<NotificationVO>> getNotifications(NotificationQueryDTO queryDTO) {
        if (queryDTO.getUserId() == null) {
            throw new BusinessException("User ID is required");
        }
        LambdaQueryWrapper<Notification> query = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, queryDTO.getUserId())
                .eq(queryDTO.getNotificationType() != null, Notification::getNotificationType,
                        queryDTO.getNotificationType())
                .eq(queryDTO.getIsRead() != null, Notification::getIsRead, queryDTO.getIsRead())
                .ge(queryDTO.getStartTime() != null, Notification::getCreateTime, queryDTO.getStartTime())
                .le(queryDTO.getEndTime() != null, Notification::getCreateTime, queryDTO.getEndTime())
                .orderByDesc(Notification::getCreateTime);
        IPage<NotificationVO> page = notificationMapper.selectPage(
                new Page<Notification>(queryDTO.getCurrent(), queryDTO.getSize()), query)
                .convert(notification -> BeanUtil.copyProperties(notification, NotificationVO.class));
        return Result.success(page);
    }

    @Override
    public IPage<Notification> pageNotifications(Long current, Long size, Long userId, Integer isRead) {
        LambdaQueryWrapper<Notification> query = new LambdaQueryWrapper<Notification>()
                .eq(userId != null, Notification::getUserId, userId)
                .eq(isRead != null, Notification::getIsRead, isRead)
                .orderByDesc(Notification::getCreateTime);
        return notificationMapper.selectPage(new Page<>(current, size), query);
    }

    @Override
    public Notification getNotificationById(Long id) {
        return notificationMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendNotification(Notification notification) {
        if (notification.getUserId() == null || notification.getNotificationType() == null
                || notification.getTitle() == null) {
            throw new BusinessException("Notification user, type, and title are required");
        }
        notification.setId(SnowflakeIdGenerator.nextId());
        if (notification.getIsRead() == null) {
            notification.setIsRead(0);
        }
        notification.setCreateTime(LocalDateTime.now());
        return notificationMapper.insert(notification) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long id) {
        if (notificationMapper.selectById(id) == null) {
            throw new BusinessException("Notification does not exist");
        }
        return notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getId, id)
                .set(Notification::getIsRead, 1)
                .set(Notification::getReadTime, LocalDateTime.now())) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAllAsRead(Long userId) {
        if (userId == null) {
            throw new BusinessException("User ID is required");
        }
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1)
                .set(Notification::getReadTime, LocalDateTime.now()));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteNotification(Long id) {
        return notificationMapper.deleteById(id) > 0;
    }

    @Override
    public long getUnreadCount(Long userId) {
        if (userId == null) {
            throw new BusinessException("User ID is required");
        }
        Long count = notificationMapper.countUnreadByUserId(userId);
        return count == null ? 0 : count;
    }
}
