package com.knowledge.base.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@TableName("message")
public class Message {
    @TableId(value="id", type=IdType.AUTO) private Long id;
    @TableField("conversation_id") private Long conversationId;
    private String role;
    private String content;
    private Integer tokens;
    @TableField(value="create_time", fill=FieldFill.INSERT) private LocalDateTime createTime;
    @TableLogic private Integer deleted;
}
