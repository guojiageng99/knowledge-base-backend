package com.knowledge.base.document.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.knowledge.base.common.config.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_comment")
@Schema(description = "评论实体")
public class Comment extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "文档ID")
    private Long documentId;

    @Schema(description = "父评论ID")
    private Long parentId;

    @Schema(description = "根评论ID")
    private Long rootId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "评论人ID")
    private Long commenterId;

    @Schema(description = "评论人姓名")
    private String commenterName;

    @Schema(description = "评论人头像")
    private String commenterAvatar;

    @Schema(description = "回复给谁（用户ID）")
    private Long replyToUserId;

    @Schema(description = "回复给谁（用户姓名）")
    private String replyToUserName;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "回复数")
    private Integer replyCount;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @TableLogic
    @Schema(description = "删除标记")
    private Integer deleted;
}
