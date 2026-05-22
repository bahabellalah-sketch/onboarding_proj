package com.onboarding.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class CryptoUtils {
    
    /**
     * Generate SHA-256 hash of document content
     */
    public static String generateSHA256Hash(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(content);
            
            // Convert byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Generate SHA-256 hash of string content
     */
    public static String generateSHA256Hash(String content) {
        return generateSHA256Hash(content.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Verify content against hash
     */
    public static boolean verifyHash(byte[] content, String expectedHash) {
        String actualHash = generateSHA256Hash(content);
        return actualHash.equals(expectedHash);
    }
    
    /**
     * Verify string content against hash
     */
    public static boolean verifyHash(String content, String expectedHash) {
        String actualHash = generateSHA256Hash(content);
        return actualHash.equals(expectedHash);
    }
}
