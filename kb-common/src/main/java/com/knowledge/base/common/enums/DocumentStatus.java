package com.knowledge.base.common.enums;

import lombok.Getter;

@Getter
public enum DocumentStatus {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    ARCHIVED(2, "已归档"),
    PENDING_REVIEW(3, "待审核");

    private final Integer code;
    private final String name;

    DocumentStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static DocumentStatus getByCode(Integer code) {
        for (DocumentStatus status : values()) {
            if (status.code.equals(code)) return status;
        }
        return null;
    }
}
