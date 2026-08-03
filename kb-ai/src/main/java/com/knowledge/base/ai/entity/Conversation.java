package com.knowledge.base.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@TableName("conversation")
public class Conversation {
    @TableId(value="id", type=IdType.AUTO) private Long id;
    private String title;
    @TableField("user_id") private Long userId;
    private String model;
    @TableField("system_prompt") private String systemPrompt;
    @TableField("tokens_used") private Integer tokensUsed;
    @TableField("message_count") private Integer messageCount;
    private Integer status;
    @TableField(value="create_time", fill=FieldFill.INSERT) private LocalDateTime createTime;
    @TableField(value="update_time", fill=FieldFill.INSERT_UPDATE) private LocalDateTime updateTime;
    @TableLogic private Integer deleted;
}
