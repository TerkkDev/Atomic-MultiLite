package server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User {
    private String username;
    private String password;

    // 1. Constructor rỗng (Bắt buộc để Gson tự đọc/ghi JSON)
    public User() {
    }

    // 2. Constructor khởi tạo (Tự động mã hóa mật khẩu thô khi tạo mới)
    public User(String username, String rawPassWord) {
        this.username = username;
        this.password = hashPassWord(rawPassWord);
    }

    // 3. Hàm riêng mã hóa mật khẩu thô sang SHA-256 (Private Helper)
    private String hashPassWord(String rawPassWord) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedByte = md.digest(rawPassWord.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedByte) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Không tìm thấy thuật toán mã hóa", e);
        }
    }

    // 4. Hàm kiểm tra mật khẩu khi đăng nhập
    public boolean checkPass(String inputRawPassword) {
        String hashedInput = hashPassWord(inputRawPassword);
        return this.password.equals(hashedInput);
    }

    // 5. Các Getter & Setter cần thiết
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Khi đổi mật khẩu mới cũng đi qua mã hóa luôn
    public void setPassword(String newRawPassword) {
        this.password = hashPassWord(newRawPassword);
    }
}