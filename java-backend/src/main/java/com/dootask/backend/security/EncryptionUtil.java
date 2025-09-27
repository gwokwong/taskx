package com.dootask.backend.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 加密工具类
 */
@Slf4j
@Component
public class EncryptionUtil {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String SHA_ALGORITHM = "SHA-256";

    private final SecretKey secretKey;

    public EncryptionUtil() {
        // 在实际应用中，应该从配置文件或环境变量中获取密钥
        this.secretKey = generateSecretKey();
    }

    /**
     * 生成AES密钥
     */
    private SecretKey generateSecretKey() {
        try {
            // 这里应该使用固定的密钥，可以从配置文件读取
            String keyString = "DooTaskSecretKey2024!@#$%^&*()";
            byte[] key = keyString.getBytes(StandardCharsets.UTF_8);

            // 确保密钥长度为16字节
            MessageDigest sha = MessageDigest.getInstance(SHA_ALGORITHM);
            key = sha.digest(key);
            key = java.util.Arrays.copyOf(key, 16); // 使用前16字节

            return new SecretKeySpec(key, AES_ALGORITHM);
        } catch (Exception e) {
            log.error("生成AES密钥失败", e);
            throw new RuntimeException("密钥生成失败", e);
        }
    }

    /**
     * AES加密
     */
    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("AES加密失败", e);
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * AES解密
     */
    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密失败", e);
            throw new RuntimeException("解密失败", e);
        }
    }

    /**
     * SHA-256哈希
     */
    public String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_ALGORITHM);
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            log.error("SHA-256哈希失败", e);
            throw new RuntimeException("哈希失败", e);
        }
    }

    /**
     * 生成随机盐值
     */
    public String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 带盐值的密码哈希
     */
    public String hashPassword(String password, String salt) {
        return hash(password + salt);
    }

    /**
     * 验证密码
     */
    public boolean verifyPassword(String password, String salt, String hashedPassword) {
        String computedHash = hashPassword(password, salt);
        return computedHash.equals(hashedPassword);
    }

    /**
     * 生成安全的随机字符串
     */
    public String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }

        return sb.toString();
    }

    /**
     * 敏感信息脱敏
     */
    public String maskSensitiveInfo(String input, int keepStart, int keepEnd) {
        if (input == null || input.length() <= keepStart + keepEnd) {
            return input;
        }

        StringBuilder masked = new StringBuilder();
        masked.append(input.substring(0, keepStart));

        int maskLength = input.length() - keepStart - keepEnd;
        for (int i = 0; i < maskLength; i++) {
            masked.append("*");
        }

        masked.append(input.substring(input.length() - keepEnd));
        return masked.toString();
    }

    /**
     * 邮箱脱敏
     */
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 2) {
            return username + "@" + domain;
        }

        return username.charAt(0) + "***" + username.charAt(username.length() - 1) + "@" + domain;
    }

    /**
     * 手机号脱敏
     */
    public String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 7) {
            return phoneNumber;
        }

        return maskSensitiveInfo(phoneNumber, 3, 4);
    }
}