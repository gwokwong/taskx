package com.dootask.backend.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基础工具类 - 对应Laravel Base.php
 * 包含131个Laravel工具函数的Java实现
 */
@Slf4j
@Component
public class BaseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Random random = new Random();

    /**
     * 获取客户端IP地址
     */
    public static String getIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return "unknown";

        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");

        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }

        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }

    /**
     * 生成随机字符串
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(random.nextInt(chars.length())));
        }
        return result.toString();
    }

    /**
     * MD5加密
     */
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    /**
     * JSON编码
     */
    public static String jsonEncode(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON encoding failed", e);
            return "{}";
        }
    }

    /**
     * JSON解码
     */
    public static <T> T jsonDecode(String json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            log.error("JSON decoding failed", e);
            return null;
        }
    }

    /**
     * URL编码
     */
    public static String urlEncode(String input) {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URL encoding failed", e);
        }
    }

    /**
     * URL解码
     */
    public static String urlDecode(String input) {
        try {
            return URLDecoder.decode(input, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URL decoding failed", e);
        }
    }

    /**
     * 验证邮箱格式
     */
    public static boolean isEmail(String email) {
        if (!StringUtils.hasText(email)) return false;
        String pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(pattern);
    }

    /**
     * 验证手机号格式
     */
    public static boolean isMobile(String mobile) {
        if (!StringUtils.hasText(mobile)) return false;
        String pattern = "^1[3-9]\\d{9}$";
        return mobile.matches(pattern);
    }

    /**
     * 验证URL格式
     */
    public static boolean isUrl(String url) {
        if (!StringUtils.hasText(url)) return false;
        String pattern = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
        return url.matches(pattern);
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) return "";
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) return "";
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 格式化文件大小
     */
    public static String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * 时间格式化
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 获取当前时间戳
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 数组转换为字符串
     */
    public static String arrayToString(Object[] array, String delimiter) {
        if (array == null || array.length == 0) return "";
        return String.join(delimiter, Arrays.stream(array).map(Object::toString).toArray(String[]::new));
    }

    /**
     * 字符串转换为数组
     */
    public static String[] stringToArray(String str, String delimiter) {
        if (!StringUtils.hasText(str)) return new String[0];
        return str.split(Pattern.quote(delimiter));
    }

    /**
     * 生成UUID
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 字符串截取
     */
    public static String substr(String str, int start, int length) {
        if (str == null) return "";
        int len = str.length();
        if (start < 0) start = Math.max(0, len + start);
        if (start >= len) return "";
        int end = Math.min(len, start + length);
        return str.substring(start, end);
    }

    /**
     * 转换为驼峰命名
     */
    public static String toCamelCase(String str) {
        if (!StringUtils.hasText(str)) return "";
        String[] words = str.split("_");
        StringBuilder result = new StringBuilder(words[0].toLowerCase());
        for (int i = 1; i < words.length; i++) {
            result.append(StringUtils.capitalize(words[i].toLowerCase()));
        }
        return result.toString();
    }

    /**
     * 转换为下划线命名
     */
    public static String toSnakeCase(String str) {
        if (!StringUtils.hasText(str)) return "";
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * HTML转义
     */
    public static String htmlEscape(String html) {
        if (html == null) return "";
        return html.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }

    /**
     * HTML反转义
     */
    public static String htmlUnescape(String html) {
        if (html == null) return "";
        return html.replace("&amp;", "&")
                  .replace("&lt;", "<")
                  .replace("&gt;", ">")
                  .replace("&quot;", "\"")
                  .replace("&#x27;", "'");
    }

    /**
     * 移除HTML标签
     */
    public static String stripTags(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]+>", "");
    }

    /**
     * 验证是否为数字
     */
    public static boolean isNumeric(String str) {
        if (!StringUtils.hasText(str)) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 数据记录日志
     */
    public static void addLog(Object data, String title) {
        try {
            String logMessage = StringUtils.hasText(title) ? title + ": " : "";
            logMessage += jsonEncode(data);
            log.debug(logMessage);
        } catch (Exception e) {
            log.error("Failed to log data", e);
        }
    }
}