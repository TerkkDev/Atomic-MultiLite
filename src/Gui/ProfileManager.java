package Gui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Pos;

public class ProfileManager {
    private static ProfileManager currentSession = null;
    
    private String username;
    private String avatarUrl;
    private GridPane gp;
    
    //====================//
    //===CÁC HÀM XỬ LÍ====//
    //====================//
    private ProfileManager(String username, String avatarUrl) {
        this.username = username;
        this.avatarUrl = avatarUrl;
    }
    
    public static ProfileManager getInstance() {
        if (currentSession == null) {
            throw new IllegalStateException("LỖI: CHƯA ĐĂNG NHẬP");
        }
        return currentSession;
    }
    
    public static void login(String username, String avatarUrl) {
        currentSession = new ProfileManager(username, avatarUrl);
    }
    
    public static void logout() {
        currentSession = null;
    }
    
    //====================//
    //=== CÁC HÀM VẼ =====//
    //====================//
    public Scene createProfileScene(Stage primaryStage, Scene mainScene) {
        TextField avatarfield = new TextField();
        
        // Fix: Sửa setPrompText -> setPromptText
        avatarfield.setPromptText("GÕ LINK FILE AVATAR VÀO ĐÂY");
        
        Button btnBack = new Button("QUAY LẠI");
        Button btnSave = new Button("LƯU");
        
        
        btnBack.setOnAction(e -> primaryStage.setScene(mainScene));
        btnSave.setOnAction(e -> {
            String newUrl = avatarfield.getText().trim();
            if (!newUrl.isEmpty()) {
                this.avatarUrl = newUrl;
            }
            primaryStage.setScene(mainScene);
        });
        
        gp = new GridPane();
        gp.setAlignment(Pos.CENTER);
        gp.setHgap(15);
        gp.setVgap(30);
        
        gp.add(avatarfield, 1, 1);
        gp.add(btnSave, 2, 1);
        gp.add(btnBack, 1, 3);
       
        return new Scene(gp, 500, 400);
    }
}