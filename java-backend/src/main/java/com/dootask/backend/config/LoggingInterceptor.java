package com.dootask.backend.config;

import com.dootask.backend.service.SystemLogService;
import com.dootask.backend.service.UserService;
import lombok.RequiredArgsConstructor;
// Temporarily disabled
// import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

// @Component
@RequiredArgsConstructor
public class LoggingInterceptor implements HandlerInterceptor {

    private final SystemLogService systemLogService;
    private final UserService userService;

    private static final ThreadLocal<Long> startTimeThreadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        startTimeThreadLocal.set(System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            // 只记录API请求
            String path = request.getRequestURI();
            if (!path.startsWith("/api/")) {
                return;
            }

            // 排除某些不需要记录的接口
            if (shouldSkipLogging(path)) {
                return;
            }

            String method = request.getMethod();
            String action = determineAction(method, path);
            String module = determineModule(path);

            Long userId = getCurrentUserId(request);
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            String requestParams = getRequestParams(request);
            String responseStatus = String.valueOf(response.getStatus());

            Long executionTime = System.currentTimeMillis() - startTimeThreadLocal.get();

            String description = generateDescription(action, module, path);

            systemLogService.logAction(action, module, description, userId, ipAddress, userAgent,
                    path, method, requestParams, responseStatus, executionTime);

        } catch (Exception e) {
            // 记录日志失败不应该影响正常业务
            e.printStackTrace();
        } finally {
            startTimeThreadLocal.remove();
        }
    }

    private boolean shouldSkipLogging(String path) {
        // 跳过日志查询接口本身，避免无限循环
        if (path.startsWith("/api/logs")) {
            return true;
        }

        // 跳过系统健康检查等接口
        if (path.contains("/health") || path.contains("/actuator")) {
            return true;
        }

        return false;
    }

    private String determineAction(String method, String path) {
        switch (method.toUpperCase()) {
            case "GET":
                return "VIEW";
            case "POST":
                if (path.contains("/login")) return "LOGIN";
                if (path.contains("/logout")) return "LOGOUT";
                if (path.contains("/upload")) return "UPLOAD";
                return "CREATE";
            case "PUT":
            case "PATCH":
                return "UPDATE";
            case "DELETE":
                return "DELETE";
            default:
                return "UNKNOWN";
        }
    }

    private String determineModule(String path) {
        if (path.contains("/users") || path.contains("/auth")) return "USER";
        if (path.contains("/projects")) return "PROJECT";
        if (path.contains("/tasks")) return "TASK";
        if (path.contains("/files")) return "FILE";
        if (path.contains("/messages") || path.contains("/dialogs")) return "MESSAGE";
        if (path.contains("/notifications")) return "NOTIFICATION";
        if (path.contains("/settings")) return "SETTING";
        if (path.contains("/search")) return "SEARCH";
        if (path.contains("/statistics")) return "STATISTICS";
        return "SYSTEM";
    }

    private String generateDescription(String action, String module, String path) {
        Map<String, String> actionMap = new HashMap<>();
        actionMap.put("VIEW", "查看");
        actionMap.put("CREATE", "创建");
        actionMap.put("UPDATE", "更新");
        actionMap.put("DELETE", "删除");
        actionMap.put("LOGIN", "登录");
        actionMap.put("LOGOUT", "退出");
        actionMap.put("UPLOAD", "上传");

        Map<String, String> moduleMap = new HashMap<>();
        moduleMap.put("USER", "用户");
        moduleMap.put("PROJECT", "项目");
        moduleMap.put("TASK", "任务");
        moduleMap.put("FILE", "文件");
        moduleMap.put("MESSAGE", "消息");
        moduleMap.put("NOTIFICATION", "通知");
        moduleMap.put("SETTING", "设置");
        moduleMap.put("SEARCH", "搜索");
        moduleMap.put("STATISTICS", "统计");
        moduleMap.put("SYSTEM", "系统");

        String actionName = actionMap.getOrDefault(action, action);
        String moduleName = moduleMap.getOrDefault(module, module);

        return actionName + moduleName;
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                var user = userService.getUserByToken(token);
                return user != null ? user.getUserid() : null;
            }
        } catch (Exception e) {
            // 获取用户信息失败时返回null
        }
        return null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                // 多级代理的情况，第一个IP为客户端真实IP
                if (ip.contains(",")) {
                    ip = ip.split(",")[0];
                }
                return ip.trim();
            }
        }

        return request.getRemoteAddr();
    }

    private String getRequestParams(HttpServletRequest request) {
        StringBuilder params = new StringBuilder();

        // GET参数
        String queryString = request.getQueryString();
        if (queryString != null) {
            params.append("Query: ").append(queryString);
        }

        // 不记录POST Body内容，避免敏感信息泄露
        return params.toString();
    }
}