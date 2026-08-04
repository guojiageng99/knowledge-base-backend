package com.knowledge.base.userauth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Permission response")
public class PermissionVO implements Serializable {
    private Long id;
    private String name;
    private String code;
    private String type;
    private Long parentId;
    private String menuUrl;
    private String apiUrl;
    private String method;
    private String icon;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private List<PermissionVO> children;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
