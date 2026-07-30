package com.knowledge.base.foundation.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_dict_data")
public class DictData {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long dictId;
    private String dictCode;
    private String dictLabel;
    private String dictValue;
    private Integer dictSort;
    private String cssClass;
    private String listClass;
    private Integer isDefault;
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;
}
