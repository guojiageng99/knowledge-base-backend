package com.knowledge.base.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "success"),
    ERROR(500, "系统异常，请联系管理员"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    BUSINESS_ERROR(10000, "业务异常"),
    USERNAME_OR_PASSWORD_ERROR(10001, "用户名或密码错误"),
    USER_NOT_EXIST(10002, "用户不存在"),
    USER_ALREADY_EXIST(10003, "用户已存在"),
    USER_DISABLED(10004, "用户已被禁用");

    private final Integer code;
    private final String message;
}
