package com.dootask.backend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 安全审计注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityAudit {

    /**
     * 操作描述
     */
    String value() default "";

    /**
     * 是否记录参数
     */
    boolean logArgs() default true;

    /**
     * 是否记录返回值
     */
    boolean logResult() default false;

    /**
     * 敏感级别
     */
    String level() default "INFO";
}