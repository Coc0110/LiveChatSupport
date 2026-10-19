package com.cmp180.livechat.common;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class SecurityManager {
    // Khóa bí mật 16 byte (128-bit) dùng cho AES
    private static final String SECRET_KEY = "LiveChatKey12345"; 
    private static final String ALGORITHM_AES = "AES";
    private static final String CIPHER_CBC = "AES/CBC/PKCS5Padding";
    private static final String CIPHER_ECB = "AES/ECB/PKCS5Padding";
    private static final int IV_LENGTH = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Mã hóa chuỗi bằng AES/CBC/PKCS5Padding với Vector khởi tạo (IV 16-byte) ngẫu nhiên.
     * Dữ liệu trả về ở định dạng Base64 chứa: [IV (16 bytes)] + [Ciphertext].
     */
    public static String encrypt(String data) {
        if (data == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM_AES);
            Cipher cipher = Cipher.getInstance(CIPHER_CBC);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[IV_LENGTH + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encryptedBytes, 0, combined, IV_LENGTH, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            System.err.println("Lỗi mã hóa CBC: " + e.getMessage());
            return data; // Fallback về chuỗi gốc nếu lỗi
        }
    }

    /**
     * Giải mã chuỗi:
     * 1. Thử giải mã chuẩn mới AES/CBC/PKCS5Padding (tách 16 byte đầu làm IV).
     * 2. Nếu thất bại (tin nhắn cũ lưu trong DB), tự động Fallback về AES/ECB giải mã tương thích ngược.
     * 3. Nếu không phải Base64, trả về nguyên gốc.
     */
    public static String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) return encryptedData;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM_AES);

            // 1. Thử giải mã CBC nếu dữ liệu dài hơn 16 byte
            if (decodedBytes.length > IV_LENGTH) {
                try {
                    byte[] iv = Arrays.copyOfRange(decodedBytes, 0, IV_LENGTH);
                    byte[] cipherBytes = Arrays.copyOfRange(decodedBytes, IV_LENGTH, decodedBytes.length);
                    Cipher cbcCipher = Cipher.getInstance(CIPHER_CBC);
                    cbcCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
                    byte[] decryptedBytes = cbcCipher.doFinal(cipherBytes);
                    return new String(decryptedBytes, StandardCharsets.UTF_8);
                } catch (Exception ignored) {
                    // CBC giải mã không khớp -> Fallback sang ECB cũ
                }
            }

            // 2. Thử giải mã ECB (Legacy Fallback cho các bản tin cũ trong cơ sở dữ liệu)
            try {
                Cipher ecbCipher = Cipher.getInstance(CIPHER_ECB);
                ecbCipher.init(Cipher.DECRYPT_MODE, key);
                byte[] decryptedBytes = ecbCipher.doFinal(decodedBytes);
                return new String(decryptedBytes, StandardCharsets.UTF_8);
            } catch (Exception ignored) {}

            return encryptedData;
        } catch (Exception e) {
            // Không phải Base64 hợp lệ (vd: lệnh hệ thống PAIRED, END...), trả về nguyên bản
            return encryptedData; 
        }
    }

    public static byte[] encryptBytes(byte[] data) {
        if (data == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM_AES);
            Cipher cipher = Cipher.getInstance(CIPHER_CBC);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

            byte[] encryptedBytes = cipher.doFinal(data);
            byte[] combined = new byte[IV_LENGTH + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encryptedBytes, 0, combined, IV_LENGTH, encryptedBytes.length);
            return combined;
        } catch (Exception e) {
            System.err.println("Lỗi mã hóa byte CBC: " + e.getMessage());
            return data;
        }
    }

    public static byte[] decryptBytes(byte[] encryptedData) {
        if (encryptedData == null) return null;
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM_AES);

            if (encryptedData.length > IV_LENGTH) {
                try {
                    byte[] iv = Arrays.copyOfRange(encryptedData, 0, IV_LENGTH);
                    byte[] cipherBytes = Arrays.copyOfRange(encryptedData, IV_LENGTH, encryptedData.length);
                    Cipher cbcCipher = Cipher.getInstance(CIPHER_CBC);
                    cbcCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
                    return cbcCipher.doFinal(cipherBytes);
                } catch (Exception ignored) {}
            }

            // Fallback ECB
            try {
                Cipher ecbCipher = Cipher.getInstance(CIPHER_ECB);
                ecbCipher.init(Cipher.DECRYPT_MODE, key);
                return ecbCipher.doFinal(encryptedData);
            } catch (Exception ignored) {}

            return encryptedData;
        } catch (Exception e) {
            System.err.println("Lỗi giải mã byte: " + e.getMessage());
            return encryptedData;
        }
    }

    /**
     * Băm mật khẩu sử dụng SHA-256 kèm Salt ngẫu nhiên 16 byte.
     * Định dạng kết quả: <salt_hex>:<hash_hex>
     */
    public static String hashPasswordWithSalt(String password) {
        if (password == null) return null;
        byte[] salt = new byte[16];
        SECURE_RANDOM.nextBytes(salt);
        String saltHex = bytesToHex(salt);
        String hashHex = rawHash(saltHex + password);
        return saltHex + ":" + hashHex;
    }

    /**
     * Băm mật khẩu SHA-256 truyền thống (đảm bảo tính tương thích với hàm cũ và unit test).
     */
    public static String hashPassword(String password) {
        if (password == null) return null;
        return rawHash(password);
    }

    /**
     * Kiểm tra mật khẩu khớp với chuỗi băm trong DB (hỗ trợ cả Salted SHA-256, Plain SHA-256 và Plain text).
     */
    public static boolean verifyPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) return false;

        // 1. Dạng băm Salted: <salt_hex>:<hash_hex>
        if (storedHash.contains(":")) {
            String[] parts = storedHash.split(":", 2);
            String saltHex = parts[0];
            String expectedHash = parts[1];
            String computedHash = rawHash(saltHex + rawPassword);
            return computedHash.equalsIgnoreCase(expectedHash);
        }

        // 2. Dạng băm Plain SHA-256 không salt (tương thích dữ liệu cũ)
        String legacyHash = rawHash(rawPassword);
        if (storedHash.equalsIgnoreCase(legacyHash)) {
            return true;
        }

        // 3. Fallback cho plain-text cũ (nếu có tài khoản chưa băm)
        return storedHash.equals(rawPassword);
    }

    private static String rawHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tính SHA-256", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}