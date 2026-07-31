package com.knowledge.base.document.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TagTypeEnum {
    SYSTEM(0, "System tag"),
    USER(1, "User tag");

    private final Integer code;
    private final String description;

    public static TagTypeEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (TagTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static TagTypeEnum ofOrDefault(Integer code) {
        TagTypeEnum type = of(code);
        return type == null ? USER : type;
    }
}
