package com.knowledge.base.common.result;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SYSTEM_ERROR(1000, "系统错误"),
    PARAM_ERROR(1001, "参数错误"),
    DATA_NOT_FOUND(1002, "数据不存在"),
    DATA_ALREADY_EXISTS(1003, "数据已存在"),
    OPERATION_FAILED(1004, "操作失败"),

    USER_NOT_EXIST(2000, "用户不存在"),
    USER_ALREADY_EXISTS(2001, "用户已存在"),
    USER_PASSWORD_ERROR(2002, "密码错误"),
    USER_ACCOUNT_DISABLED(2003, "账号已被禁用"),
    USER_TOKEN_EXPIRED(2004, "Token已过期"),
    USER_TOKEN_INVALID(2005, "Token无效"),

    DOC_NOT_FOUND(3000, "文档不存在"),
    DOC_ALREADY_EXISTS(3001, "文档已存在"),
    DOC_TITLE_EMPTY(3002, "文档标题不能为空"),
    DOC_CONTENT_EMPTY(3003, "文档内容不能为空"),

    PERMISSION_DENIED(4000, "权限不足"),
    ROLE_NOT_EXIST(4001, "角色不存在"),
    ROLE_ALREADY_EXISTS(4002, "角色已存在");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
