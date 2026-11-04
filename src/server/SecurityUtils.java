package server;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class SecurityUtils {
    // Khóa bí mật đúng 16 ký tự để mã hóa AES
    private static final String SECRET_KEY = "AtomicLiteKey123"; 

    // Mã hóa chuỗi JSON thành chuỗi ký tự rác
    public static String encrypt(String data) {
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa dữ liệu!", e);
        }
    }

    // Giải mã chuỗi rác về lại chuỗi JSON ban đầu
    public static String decrypt(String encryptedData) {
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            return new String(cipher.doFinal(decodedBytes));
        } catch (Exception e) {
            System.err.println("File bị can thiệp hoặc sai khóa giải mã!");
            return null; // Trả về null nếu file bị sửa bậy
        }
    }
}