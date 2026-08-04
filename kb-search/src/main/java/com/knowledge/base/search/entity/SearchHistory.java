package com.knowledge.base.search.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_search_history")
public class SearchHistory {
    @TableId(type = IdType.ASSIGN_ID) private Long id;
    private Long userId;
    private String keyword;
    private Integer searchCount;
    private LocalDateTime createTime;
}
