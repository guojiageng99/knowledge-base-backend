package com.knowledge.base.foundation.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_notification")
public class Notification {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String userName;
    private String notificationType;
    private String title;
    private String content;
    private String link;
    private String relatedType;
    private Long relatedId;
    private Integer isRead;
    private LocalDateTime readTime;

    @TableField("create_time")
    private LocalDateTime createTime;
}
