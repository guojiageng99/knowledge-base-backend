package com.knowledge.base.foundation.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.knowledge.base.common.config.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_notification")
@Schema(description = "System notification")
public class Notification extends BaseEntity {

    @TableField("user_id")
    private Long userId;
    @TableField("user_name")
    private String userName;
    @TableField("notification_type")
    private String notificationType;
    @TableField("title")
    private String title;
    @TableField("content")
    private String content;
    @TableField("link")
    private String link;
    @TableField("related_type")
    private String relatedType;
    @TableField("related_id")
    private Long relatedId;
    @TableField("is_read")
    private Integer isRead;
    @TableField("read_time")
    private LocalDateTime readTime;
}
