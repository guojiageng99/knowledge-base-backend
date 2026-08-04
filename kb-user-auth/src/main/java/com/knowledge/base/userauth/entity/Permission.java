package com.knowledge.base.userauth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.knowledge.base.common.config.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class Permission extends BaseEntity {

    private Long parentId;
    private String permissionName;
    private String permissionCode;

    @TableField("permission_type")
    private Integer permissionType;

    @TableField("path")
    private String menuUrl;

    @TableField("api_url")
    private String apiUrl;

    @TableField("method")
    private String method;

    private String icon;

    @TableField("sort_order")
    private Integer sort;

    private Integer status;

    @TableField("remark")
    private String remark;
}
