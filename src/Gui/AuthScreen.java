package Gui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.File;

public class AuthScreen {

    // Hàm trả về Scene để main_gui nhúng vào Stage
    public Scene createAuthScene(Stage stage, Scene mainScene) {
        
        // --- 1. KHỞI TẠO CÁC CONTROL GIAO DIỆN ---
        Label lblTitle = new Label("XÁC THỰC TÀI KHOẢN");
        lblTitle.getStyleClass().add("game-title");

        Label lblUser = new Label("Tài khoản:");
        lblUser.setStyle("-fx-text-fill: #00fff5; -fx-font-weight: bold;");
        TextField txtUser = new TextField();
        txtUser.setPromptText("Nhập username...");

        Label lblPass = new Label("Mật khẩu:");
        lblPass.setStyle("-fx-text-fill: #00fff5; -fx-font-weight: bold;");
        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Nhập password...");

        Button btnLogin = new Button("ĐĂNG NHẬP");
        Button btnRegister = new Button("ĐĂNG KÝ");
        btnLogin.getStyleClass().add("menu-button");
        btnRegister.getStyleClass().add("menu-button");
        btnLogin.setPrefSize(120, 35);
        btnRegister.setPrefSize(120, 35);

        Label lblMessage = new Label("");

        // --- 2. XẾP VÀO LƯỚI GRIDPANE ---
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(12);
        grid.setVgap(12);

        grid.add(lblUser, 0, 0);
        grid.add(txtUser, 1, 0);
        grid.add(lblPass, 0, 1);
        grid.add(txtPass, 1, 1);

        HBox buttonBox = new HBox(12, btnLogin, btnRegister);
        buttonBox.setAlignment(Pos.CENTER);
        grid.add(buttonBox, 0, 2, 2, 1); 

        grid.add(lblMessage, 0, 3, 2, 1); 

        // --- 3. NÚT QUAY LẠI MAIN MENU ---
        Button btnBack = new Button("QUAY LẠI MENU");
        btnBack.getStyleClass().add("menu-button");
        btnBack.setPrefSize(160, 38);
        
        // Bấm Quay lại -> Đổi Scene về mainScene
        btnBack.setOnAction(e -> {
           stage.setScene(mainScene);
           stage.setFullScreen(true);
        });

        VBox root = new VBox(20, lblTitle, grid, btnBack);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root-panel");

        // --- 4. LOGIC XỬ LÝ SỰ KIỆN ---
        btnLogin.setOnAction(e -> {
           String user = txtUser.getText().trim();
           String pass = txtPass.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            lblMessage.setStyle("-fx-text-fill: #ff4d4d; -fx-font-weight: bold;");
            lblMessage.setText("LỖI: VUI LÒNG NHẬP THÔNG TIN ĐẦY ĐỦ");
        } else {
           // Gửi lệnh LOGIN lên Server
            String response = sendAuthRequest("LOGIN", user, pass);

            if ("LOGIN_SUCCESS".equals(response)) {
                lblMessage.setStyle("-fx-text-fill: #00ff88; -fx-font-weight: bold;");
                lblMessage.setText("ĐĂNG NHẬP THÀNH CÔNG! CHÀO " + user.toUpperCase());
            // Sau này: Chuyển sang màn hình chọn nhân vật / game
            } else if ("SERVER_ERROR".equals(response)) {
                lblMessage.setStyle("-fx-text-fill: #ff4d4d; -fx-font-weight: bold;");
                lblMessage.setText("LỖI: KHÔNG THỂ KẾT NỐI TỚI SERVER!");
            } else {
                lblMessage.setStyle("-fx-text-fill: #ff4d4d; -fx-font-weight: bold;");
                lblMessage.setText("ĐĂNG NHẬP THẤT BẠI: SAI TÀI KHOẢN/MẬT KHẨU");
            }
          }
      });

        btnRegister.setOnAction(e -> {
           String user = txtUser.getText().trim();
           String pass = txtPass.getText().trim();

           if (user.isEmpty() || pass.isEmpty()) {
               lblMessage.setStyle("-fx-text-fill: #ff4d4d; -fx-font-weight: bold;");
               lblMessage.setText("LỖI: VUI LÒNG NHẬP THÔNG TIN ĐẦY ĐỦ");
           } else if (pass.length() < 6) {
               lblMessage.setStyle("-fx-text-fill: #ff4d4d; -fx-font-weight: bold;");
               lblMessage.setText("LỖI: MẬT KHẨU PHẢI CÓ ÍT NHẤT 6 KÝ TỰ");
           } else {
             // Gửi lệnh REGISTER lên Server
                String response = sendAuthRequest("REGISTER", user, pass);

                if ("REGISTER_SUCCESS".equals(response)) {
                    lblMessage.setStyle("-fx-text-fill: #00ff88; -fx-font-weight: bold;");
                    lblMessage.setText("ĐĂNG KÝ THÀNH CÔNG TÀI KHOẢN " + user.toUpperCase());
                } else if ("SERVER_ERROR".equals(response)) {
                    lblMessage.setStyle("-fx-text-fill: #ff4d4d; -fx-font-weight: bold;");
                    lblMessage.setText("LỖI: KHÔNG THỂ KẾT NỐI TỚI SERVER!");
                } else {
                    lblMessage.setStyle("-fx-text-fill: #ff4d4d; -fx-font-weight: bold;");
                    lblMessage.setText("ĐĂNG KÝ THẤT BẠI: TÀI KHOẢN ĐÃ TỒN TẠI");
                }
          }
      });

        Scene scene = new Scene(root, 500, 600);
        applyCSS(scene);
        return scene;
    }

    private void applyCSS(Scene scene) {
        try {
            File cssFile = new File("res/CSS/style.css");
            if (cssFile.exists()) {
                scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
            }
        } catch (Exception e) {
            System.out.println("Lỗi CSS: " + e.getMessage());
        }
    }
    private String sendAuthRequest(String action, String user, String pass) {
    String host = "127.0.0.1";
    int port = 9999;

    try (Socket socket = new Socket(host, port);
         PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
         BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

        // 1. Gửi gói tin lên Server
        String packageData = action + ":" + user + ":" + pass;
        writer.println(packageData);

        // 2. Nhận câu trả lời từ Server
        return reader.readLine();

      } catch (Exception e) {
        System.out.println("Lỗi kết nối Socket: " + e.getMessage());
        return "SERVER_ERROR";
      } 
   }
}