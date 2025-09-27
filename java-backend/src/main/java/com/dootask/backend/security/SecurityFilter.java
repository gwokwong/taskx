package com.dootask.backend.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 安全过滤器
 */
@Slf4j
@Component
public class SecurityFilter implements Filter {

    // 敏感文件扩展名
    private static final List<String> SENSITIVE_EXTENSIONS = Arrays.asList(
        ".jsp", ".jspx", ".properties", ".class", ".jar", ".war",
        ".xml", ".sql", ".log", ".backup", ".bak", ".tmp"
    );

    // 危险路径
    private static final List<String> DANGEROUS_PATHS = Arrays.asList(
        "../", "..\\", "..", "/etc/", "/proc/", "/sys/",
        "WEB-INF", "META-INF", "classes/"
    );

    // SQL注入关键词
    private static final List<String> SQL_INJECTION_KEYWORDS = Arrays.asList(
        "union", "select", "insert", "update", "delete", "drop",
        "create", "alter", "exec", "execute", "script", "declare"
    );

    // XSS关键词
    private static final List<String> XSS_KEYWORDS = Arrays.asList(
        "<script", "</script>", "javascript:", "vbscript:", "onload=",
        "onerror=", "onclick=", "onmouseover=", "alert(", "confirm(",
        "prompt(", "document.cookie", "document.write"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // 设置安全响应头
            setSecurityHeaders(httpResponse);

            // 验证请求
            if (!isValidRequest(httpRequest)) {
                log.warn("检测到可疑请求: {} from {}", httpRequest.getRequestURI(), getClientIpAddress(httpRequest));
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpResponse.getWriter().write("{\"error\":\"Invalid request\",\"code\":400}");
                return;
            }

            // 检查文件访问
            if (!isAllowedFileAccess(httpRequest)) {
                log.warn("检测到敏感文件访问尝试: {} from {}", httpRequest.getRequestURI(), getClientIpAddress(httpRequest));
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.getWriter().write("{\"error\":\"Forbidden\",\"code\":403}");
                return;
            }

            // 检查SQL注入
            if (containsSQLInjection(httpRequest)) {
                log.warn("检测到SQL注入尝试: {} from {}", httpRequest.getRequestURI(), getClientIpAddress(httpRequest));
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpResponse.getWriter().write("{\"error\":\"Invalid parameters\",\"code\":400}");
                return;
            }

            // 检查XSS攻击
            if (containsXSS(httpRequest)) {
                log.warn("检测到XSS攻击尝试: {} from {}", httpRequest.getRequestURI(), getClientIpAddress(httpRequest));
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpResponse.getWriter().write("{\"error\":\"Invalid parameters\",\"code\":400}");
                return;
            }

            chain.doFilter(request, response);

        } catch (Exception e) {
            log.error("安全过滤器处理异常", e);
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpResponse.getWriter().write("{\"error\":\"Internal server error\",\"code\":500}");
        }
    }

    /**
     * 设置安全响应头
     */
    private void setSecurityHeaders(HttpServletResponse response) {
        // 防止点击劫持
        response.setHeader("X-Frame-Options", "DENY");

        // 防止MIME类型嗅探
        response.setHeader("X-Content-Type-Options", "nosniff");

        // XSS保护
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // 强制HTTPS
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // 内容安全策略
        response.setHeader("Content-Security-Policy",
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
            "style-src 'self' 'unsafe-inline'; " +
            "img-src 'self' data: https:; " +
            "font-src 'self' https:; " +
            "connect-src 'self' https: wss:; " +
            "object-src 'none'; " +
            "frame-ancestors 'none'"
        );

        // 推荐下载而不是执行
        response.setHeader("X-Download-Options", "noopen");

        // 防止信息泄露
        response.setHeader("Server", "DooTask");
        response.setHeader("X-Powered-By", "");

        // 缓存控制
        if (response.getHeader("Cache-Control") == null) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }
    }

    /**
     * 验证请求是否有效
     */
    private boolean isValidRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 检查URI长度
        if (uri.length() > 2048) {
            return false;
        }

        // 检查请求方法
        List<String> allowedMethods = Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD");
        if (!allowedMethods.contains(method)) {
            return false;
        }

        // 检查User-Agent
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && (userAgent.toLowerCase().contains("sqlmap") ||
                                 userAgent.toLowerCase().contains("nikto") ||
                                 userAgent.toLowerCase().contains("nessus") ||
                                 userAgent.toLowerCase().contains("burp"))) {
            return false;
        }

        return true;
    }

    /**
     * 检查是否允许文件访问
     */
    private boolean isAllowedFileAccess(HttpServletRequest request) {
        String uri = request.getRequestURI().toLowerCase();

        // 检查路径遍历
        for (String dangerousPath : DANGEROUS_PATHS) {
            if (uri.contains(dangerousPath.toLowerCase())) {
                return false;
            }
        }

        // 检查敏感文件扩展名
        for (String extension : SENSITIVE_EXTENSIONS) {
            if (uri.endsWith(extension)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查SQL注入
     */
    private boolean containsSQLInjection(HttpServletRequest request) {
        // 检查所有参数
        if (request.getParameterMap() != null) {
            for (String[] values : request.getParameterMap().values()) {
                for (String value : values) {
                    if (value != null && containsSQLInjectionKeywords(value.toLowerCase())) {
                        return true;
                    }
                }
            }
        }

        // 检查URI
        String uri = request.getRequestURI();
        if (containsSQLInjectionKeywords(uri.toLowerCase())) {
            return true;
        }

        return false;
    }

    /**
     * 检查是否包含SQL注入关键词
     */
    private boolean containsSQLInjectionKeywords(String input) {
        for (String keyword : SQL_INJECTION_KEYWORDS) {
            if (input.contains(" " + keyword + " ") ||
                input.contains("'" + keyword + " ") ||
                input.contains(" " + keyword + "'") ||
                input.startsWith(keyword + " ") ||
                input.endsWith(" " + keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查XSS攻击
     */
    private boolean containsXSS(HttpServletRequest request) {
        // 检查所有参数
        if (request.getParameterMap() != null) {
            for (String[] values : request.getParameterMap().values()) {
                for (String value : values) {
                    if (value != null && containsXSSKeywords(value.toLowerCase())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 检查是否包含XSS关键词
     */
    private boolean containsXSSKeywords(String input) {
        for (String keyword : XSS_KEYWORDS) {
            if (input.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty() && !"unknown".equalsIgnoreCase(xRealIP)) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }
}