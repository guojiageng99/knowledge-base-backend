package com.knowledge.base.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@TableName("ai_feedback")
public class AiFeedback {
    @TableId(value="id", type=IdType.AUTO) private Long id;
    @TableField("conversation_id") private Long conversationId;
    @TableField("message_id") private Long messageId;
    @TableField("user_id") private Long userId;
    @TableField("feedback_type") private String feedbackType;
    @TableField("feedback_content") private String feedbackContent;
    private Integer rating;
    @TableField(value="create_time", fill=FieldFill.INSERT) private LocalDateTime createTime;
    @TableLogic private Integer deleted;
}
