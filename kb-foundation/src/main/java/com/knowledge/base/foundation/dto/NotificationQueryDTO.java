package com.knowledge.base.foundation.dto;

import com.knowledge.base.common.result.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Notification query parameters")
public class NotificationQueryDTO extends PageParam {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String notificationType;
    private Integer isRead;
    private String startTime;
    private String endTime;
}
