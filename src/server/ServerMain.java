package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ServerMain {
    // Port 9999 dùng chung toàn hệ thống
    private static final int PORT = 9999;

    // Bộ quản lý dữ liệu User và bộ chuyển Gson
    private static Map<String, User> userMap = new HashMap<>();
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        System.out.println("=== ATOMIC MULTILITE MULTITHREAD SERVER ===");

        // 1. [BẮT BỤC] Tải dữ liệu User đã mã hóa lên bộ nhớ ngay khi Server vừa bật
        loadUsers();

        // 2. Mở ServerSocket listening ở cổng 9999
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] Dang lang nghe tai cong " + PORT + "...");

            // Vòng lặp vĩnh cửu đón kết nối
            while (true) {
                // Dừng lại chờ Client mới bấm vào game
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] Co Client moi ket noi tu cong: " + clientSocket.getPort());

                // Tạo công việc ClientHandler và giao Socket cho nó
                ClientHandler clientHandler = new ClientHandler(clientSocket);

                // Tạo Thread mới và khởi chạy độc lập
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("[SERVER LỖI] " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =======================================================
    // BỘ HÀM XỬ LÝ LƯU & ĐỌC FILE MÃ HÓA (AES)
    // =======================================================

    // Hàm LƯU file users.dat (Gọi khi có người đăng ký mới hoặc đổi MK)
    public static synchronized void saveUsers() {
        try {
            // Bước 1: Ép Map người dùng sang chuỗi JSON
            String jsonString = gson.toJson(userMap);

            // Bước 2: Mã hóa chuỗi JSON đó bằng AES (SecurityUtils) thành chuỗi rác
            String encryptedString = SecurityUtils.encrypt(jsonString);

            // Bước 3: Ghi chuỗi mã hóa xuống đĩa
            FileWriter writer = new FileWriter("users.dat");
            writer.write(encryptedString);
            writer.close();

            System.out.println("[SERVER] Da luu du lieu nguoi dung an toan thanh cong!");
        } catch (Exception e) {
            System.err.println("[SERVER LỖI LƯU FILE] " + e.getMessage());
        }
    }

    // Hàm ĐỌC file users.dat (Gọi 1 lần duy nhất khi bật Server)
    public static synchronized void loadUsers() {
        File file = new File("users.dat");
        if (!file.exists()) {
            System.out.println("[SERVER] Chưa có file users.dat, tạo danh sách tài khoản mới trống.");
            return;
        }

        try {
            // Bước 1: Đọc chuỗi ký tự rác từ file users.dat
            String encryptedString = new String(Files.readAllBytes(Paths.get("users.dat")));

            // Bước 2: Giải mã AES bằng chìa khóa bí mật
            String jsonString = SecurityUtils.decrypt(encryptedString);

            // Bước 3: Kiểm tra tính hợp lệ và ép ngược JSON về lại Map<String, User>
            if (jsonString != null) {
                Type type = new TypeToken<Map<String, User>>(){}.getType();
                userMap = gson.fromJson(jsonString, type);
                System.out.println("[SERVER] Tải thành công " + userMap.size() + " tài khoản từ users.dat!");
            } else {
                System.err.println("[CẢNH BÁO] File users.dat bị ai đó cố tình chỉnh sửa hoặc bị lỗi mã hóa!");
            }
        } catch (Exception e) {
            System.err.println("[SERVER LỖI ĐỌC FILE] " + e.getMessage());
        }
    }

    // Getter & Setter để ClientHandler truy cập danh sách User an toàn
    public static Map<String, User> getUserMap() {
        return userMap;
    }
}