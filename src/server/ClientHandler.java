package server;

import java.io.*;
import java.net.*;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Gson gson;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            
            String request;
            while ((request = reader.readLine()) != null) {
                System.out.println("[CLIENT " + socket.getPort() + "] " + request);
            
                String[] parts = request.split(":");
                String action = parts[0];
                String user = parts.length > 1 ? parts[1] : "";
                String pass = parts.length > 2 ? parts[2] : "";
                
                if (action.equals("LOGIN")) {
                    handleLogin(user, pass);
                } else if (action.equals("REGISTER")) {
                    handleRegister(user, pass);
                }
            }
        } catch (IOException e) {
            System.out.println("[SERVER] Client " + socket.getPort() + " da ngat ket noi.");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void handleLogin(String user, String pass) {
        List<User> users = loadUsers();
        boolean success = false;
        
        for (User u : users) {
            // SỬA: Dùng hàm checkPass trong class User để tự băm pass rồi so sánh
            if (u.getUsername().equals(user) && u.checkPass(pass)) {
                success = true;
                break;
            }
        }
        
        if (success) {
            writer.println("LOGIN_SUCCESS");
        } else {
            writer.println("LOGIN_FAILED");
        }
    }

    private synchronized void handleRegister(String user, String pass) {
        List<User> users = loadUsers();
        for (User u : users) {
            if (u.getUsername().equals(user)) {
                writer.println("REGISTER_FAILED_EXISTS");
                return;
            } 
        }
        
        // Constructor này sẽ tự động băm mật khẩu pass sang SHA-256
        users.add(new User(user, pass));
        saveUsers(users);
        writer.println("REGISTER_SUCCESS");
    }

    // SỬA: Đọc file users.dat và Giải mã AES bằng SecurityUtils
    private synchronized List<User> loadUsers() {
        File file = new File("users.dat");
        if (!file.exists()) return new java.util.ArrayList<>();
        try {
            String encryptedString = new String(Files.readAllBytes(Paths.get("users.dat")));
            String jsonString = SecurityUtils.decrypt(encryptedString);
            
            if (jsonString != null) {
                List<User> list = gson.fromJson(jsonString, new TypeToken<List<User>>(){}.getType());
                return list != null ? list : new java.util.ArrayList<>();
            }
            return new java.util.ArrayList<>();
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    // SỬA: Mã hóa AES bằng SecurityUtils rồi mới lưu xuống users.dat
    private synchronized void saveUsers(List<User> users) {
        try {
            String jsonString = gson.toJson(users);
            String encryptedString = SecurityUtils.encrypt(jsonString);
            
            try (FileWriter w = new FileWriter("users.dat")) {
                w.write(encryptedString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}