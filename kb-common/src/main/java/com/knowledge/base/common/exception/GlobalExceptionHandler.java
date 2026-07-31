package com.knowledge.base.common.exception;

import com.knowledge.base.common.result.Result;
import com.knowledge.base.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Throwable.class)
    public Object handleException(Throwable e) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes
                && StringUtils.isNotBlank(servletAttributes.getRequest().getHeader("INNER-REQUEST"))) {
            return handleInternalException(e);
        }
        return handleExternalException(e);
    }

    private Object handleInternalException(Throwable e) {
        if (e instanceof BusinessException businessException) {
            log.error("Internal business error: code={}, message={}", businessException.getCode(), businessException.getMessage(), e);
            return ResponseEntity.status(resolveHttpStatus(businessException.getCode())).body(businessException.getMessage());
        }
        log.error("Internal service error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    private Result<Void> handleExternalException(Throwable e) {
        if (e instanceof BusinessException businessException) {
            log.error("Business error: code={}, message={}", businessException.getCode(), businessException.getMessage(), e);
            return Result.error(businessException.getCode(), businessException.getMessage());
        }
        if (e instanceof AccessDeniedException) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        if (e instanceof MethodArgumentNotValidException validationException) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), firstFieldError(validationException.getBindingResult().getFieldError()));
        }
        if (e instanceof BindException bindException) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), firstFieldError(bindException.getBindingResult().getFieldError()));
        }
        if (e instanceof ConstraintViolationException || e instanceof IllegalArgumentException) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), e.getMessage());
        }
        log.error("System error: {}", requestInfo(), e);
        return Result.error(ResultCode.ERROR);
    }

    private static String firstFieldError(FieldError fieldError) {
        return fieldError == null ? "参数校验失败" : fieldError.getDefaultMessage();
    }

    private static HttpStatus resolveHttpStatus(Integer code) {
        HttpStatus status = HttpStatus.resolve(code);
        return status == null ? HttpStatus.BAD_REQUEST : status;
    }

    private static String requestInfo() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            return request.getMethod() + " " + request.getRequestURI();
        }
        return "unknown request";
    }
}
