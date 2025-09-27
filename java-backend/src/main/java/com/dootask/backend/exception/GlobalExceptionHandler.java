package com.dootask.backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义API异常
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException e, HttpServletRequest request) {
        log.error("API异常: {} - URI: {}", e.getMessage(), request.getRequestURI(), e);

        Map<String, Object> response = new HashMap<>();
        response.put("error", e.getMessage());
        response.put("code", e.getCode());
        response.put("timestamp", System.currentTimeMillis());
        response.put("path", request.getRequestURI());

        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {

        log.warn("参数验证失败: URI: {}", request.getRequestURI());

        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        response.put("error", "参数验证失败");
        response.put("code", 400);
        response.put("details", errors);
        response.put("timestamp", System.currentTimeMillis());
        response.put("path", request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBindException(
            BindException e, HttpServletRequest request) {

        log.warn("参数绑定失败: URI: {}", request.getRequestURI());

        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        response.put("error", "参数绑定失败");
        response.put("code", 400);
        response.put("details", errors);
        response.put("timestamp", System.currentTimeMillis());
        response.put("path", request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request) {

        log.warn("约束验证失败: URI: {}", request.getRequestURI());

        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(propertyPath, message);
        }

        response.put("error", "约束验证失败");
        response.put("code", 400);
        response.put("details", errors);
        response.put("timestamp", System.currentTimeMillis());
        response.put("path", request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {

        log.warn("参数类型不匹配: {} - URI: {}", e.getMessage(), request.getRequestURI());

        Map<String, Object> response = new HashMap<>();
        response.put("error", String.format("参数 '%s' 类型不正确，期望类型: %s",
            e.getName(), e.getRequiredType().getSimpleName()));
        response.put("code", 400);
        response.put("timestamp", System.currentTimeMillis());
        response.put("path", request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, Object>> handleNullPointerException(
            NullPointerException e, HttpServletRequest request) {

        log.error("空指针异常: URI: {}", request.getRequestURI(), e);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "系统内部错误");
        response.put("code", 500);
        response.put("timestamp", System.currentTimeMillis());
        response.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException e, HttpServletRequest request) {

        log.warn("非法参数异常: {} - URI: {}", e.getMessage(), request.getRequestURI());

        Map<String, Object> response = new HashMap<>();
        response.put("error", "参数不合法: " + e.getMessage());
        response.put("code", 400);
        response.put("timestamp", System.currentTimeMillis());
        response.put("path", request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理非法状态异常
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(
            IllegalStateException e, HttpServletRequest request) {

        log.error("非法状态异常: {} - URI: {}", e.getMessage(), request.getRequestURI(), e);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "系统状态异常: " + e.getMessage());
        response.put("code", 500);
        response.put("timestamp", System.currentTimeMillis());
        response.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException e, HttpServletRequest request) {

        log.error("运行时异常: {} - URI: {}", e.getMessage(), request.getRequestURI(), e);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "系统运行异常");
        response.put("code", 500);
        response.put("timestamp", System.currentTimeMillis());
        response.put("path", request.getRequestURI());

        // 开发环境下返回详细错误信息
        if (isDevelopmentMode()) {
            response.put("details", e.getMessage());
            response.put("cause", e.getCause() != null ? e.getCause().getMessage() : null);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception e, HttpServletRequest request) {

        log.error("未处理异常: {} - URI: {}", e.getMessage(), request.getRequestURI(), e);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "系统内部错误");
        response.put("code", 500);
        response.put("timestamp", System.currentTimeMillis());
        response.put("path", request.getRequestURI());

        // 开发环境下返回详细错误信息
        if (isDevelopmentMode()) {
            response.put("details", e.getMessage());
            response.put("exceptionType", e.getClass().getSimpleName());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 判断是否为开发模式
     */
    private boolean isDevelopmentMode() {
        String profiles = System.getProperty("spring.profiles.active", "");
        return profiles.contains("dev") || profiles.contains("local");
    }
}