package com.cmp180.livechat.common;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class SecurityManager {
    // Khóa bí mật 16 byte (128-bit) dùng cho AES. 
    // Trong thực tế, khóa này nên được cấp phát động, nhưng ta fix cứng để dễ test nội bộ.
    private static final String SECRET_KEY = "LiveChatKey12345"; 
    private static final String ALGORITHM = "AES";

    public static String encrypt(String data) {
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            System.err.println("Lỗi mã hóa: " + e.getMessage());
            return data; // Fallback về chuỗi gốc nếu lỗi
        }
    }

    public static String decrypt(String encryptedData) {
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            // Nếu không phải chuỗi Base64 hợp lệ (vd: các lệnh hệ thống như PAIRED), trả về nguyên gốc
            return encryptedData; 
        }
    }
}