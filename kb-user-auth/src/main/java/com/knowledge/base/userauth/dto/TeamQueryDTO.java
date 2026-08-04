package com.knowledge.base.userauth.dto;

import com.knowledge.base.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeamQueryDTO extends PageParam {
    private String teamName;
    private String teamCode;
    private Long parentId;
    private Integer status;
}
