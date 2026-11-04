package Gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.File;
import javafx.animation.*;
import javafx.util.Duration;

public class maingui extends Application {

    // 1. Khai báo các Scene & Manager ở phạm vi Class
    private Scene mainScene;
    private Scene settingsScene;
    private Scene authScene;
    private Scene gameModeScene;
    private Scene loadingScene;
    private Scene gameplayEvoScene;

    // Đối tượng gameplay và bộ đếm thời gian
    private gameplayEvo gpEvo;
    private Timeline loadtimer;
    private ProgressBar progressB;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // ==========================================
        // 📌 KHU VỰC 1: CẤU HÌNH CỬA SỔ & NẠP ICON
        // ==========================================
        primaryStage.setTitle("Atomic MultiLite - MAIN MENU");

        try {
            File iconFile = new File("res/image/Icon.PNG");
            if (iconFile.exists()) {
                Image img = new Image(iconFile.toURI().toString());
        
        // Clear sạch icon cũ (nếu có) rồi thêm lại
                primaryStage.getIcons().clear();
                primaryStage.getIcons().add(img);
        
        // Gán Icon này làm mặc định cho tất cả Cửa sổ/Dialog sau này
        // (Cách này giúp ép toàn bộ app dùng chung 1 icon)
            }
         } catch (Exception e) {
             e.printStackTrace();
         }

        BorderPane mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("root-panel");
        mainLayout.setPadding(new Insets(30, 20, 30, 20));


        // ==========================================
        // 📌 KHU VỰC 2: PHẦN TOP (TIÊU ĐỀ & TIỀN)
        // ==========================================
        Label titleLabel = new Label("ATOMIC MULTILITE");
        titleLabel.getStyleClass().add("game-title");

        Label coinLabel = new Label("Xu Tiến Hóa: 0");
        coinLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-weight: bold;");

        VBox topBox = new VBox(8, titleLabel, coinLabel);
        topBox.setAlignment(Pos.CENTER);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        
        Button btnprofile = new Button("👤 HỒ SƠ");
        btnprofile.setStyle("-fx-font-fill: #ffd700; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        btnprofile.setPrefSize(130, 35);
        
        grid.add(btnprofile, 2, 0);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setAlignment(Pos.TOP_RIGHT);
        VBox headcontainer = new VBox(12, topBox, grid);
        headcontainer.setAlignment(Pos.CENTER);
        
        mainLayout.setTop(headcontainer);


        // ==========================================
        // 📌 KHU VỰC 3: PHẦN CENTER (MENU CHẾ ĐỘ CHƠI)
        // ==========================================
        Button btnplay = createMenuButton("CHƠI");
        btnplay.getStyleClass().add("-menu-button");
        
        VBox centerBox = new VBox(12, btnplay);
        centerBox.setAlignment(Pos.CENTER);
        mainLayout.setCenter(centerBox);


        // ==========================================
        // 📌 KHU VỰC 4: PHẦN BOTTOM (CỬA HÀNG, CÀI ĐẶT, THOÁT)
        // ==========================================
        Button btnShop = new Button("CỬA HÀNG");
        Button btnSettings = new Button("CÀI ĐẶT");
        Button btnExit = new Button("THOÁT");

        btnShop.getStyleClass().add("menu-button");
        btnSettings.getStyleClass().add("menu-button");
        btnExit.getStyleClass().add("menu-button");

        btnShop.setPrefSize(110, 38);
        btnSettings.setPrefSize(110, 38);
        btnExit.setPrefSize(110, 38);

        btnExit.setOnAction(e -> Platform.exit());

        HBox bottomBox = new HBox(15, btnShop, btnSettings, btnExit);
        bottomBox.setAlignment(Pos.CENTER);
        mainLayout.setBottom(bottomBox);


        // ==========================================
        // 📌 KHU VỰC 5: KHỞI TẠO CÁC SCENE & BẮT SỰ KIỆN
        // ==========================================
        mainScene = new Scene(mainLayout, 500, 650);
        
        // Khởi tạo Gameplay & Auth
        gpEvo = new gameplayEvo();
        gameplayEvoScene = gpEvo.createGamePlayScene(primaryStage, mainScene);

        AuthScreen auth = new AuthScreen();
        authScene = auth.createAuthScene(primaryStage, mainScene);

        // Khởi tạo các Scene phụ
        settingsScene = createsettingscene(primaryStage);
        gameModeScene = createGameModeScene(primaryStage);
        loadingScene = createLoadingScreen(primaryStage);

        // Nạp CSS
        applyCSS(mainScene);
        applyCSS(settingsScene);
        applyCSS(gameModeScene);
        applyCSS(loadingScene);

        // --- BẮT SỰ KIỆN CHUYỂN TRANG ---
        btnSettings.setOnAction(e -> {
            primaryStage.setScene(settingsScene);
            primaryStage.setFullScreen(true);
        });
        
        btnplay.setOnAction(e -> {
            primaryStage.setScene(gameModeScene);
            primaryStage.setFullScreen(true);
        });
        
        btnShop.setOnAction(e -> {
            primaryStage.setScene(mainScene);
            primaryStage.setFullScreen(true);
        });
        
        btnprofile.setOnAction(e -> {
            primaryStage.setScene(authScene);
            primaryStage.setFullScreen(true);
        });
        try {
            File iconFile = new File("res/image/Icon.PNG");
            if (iconFile.exists()) {
                primaryStage.getIcons().add(new Image(iconFile.toURI().toString()));
            }
        } catch (Exception e) {}

        // 2. Chạy LoadingScreen
        LoadingScreen splash = new LoadingScreen();
        splash.showAndLaunch(() -> {
            primaryStage.setScene(mainScene);
            primaryStage.setResizable(true);
            primaryStage.setFullScreen(true);
            primaryStage.centerOnScreen();
            primaryStage.show(); // Lúc này Main Menu hiện lên với Icon chuẩn!
        });
     

        
    }


    // ==========================================
    // 📌 KHU VỰC 6: CÁC HÀM PHỤ (HELPER METHODS)
    // ==========================================
    
    private Scene createsettingscene(Stage stage) {
        BorderPane settinglayout = new BorderPane();
        settinglayout.getStyleClass().add("root-panel");

        Button btnback = new Button("QUAY LẠI");
        btnback.getStyleClass().add("menu-button");
        btnback.setPrefSize(150, 40);
        btnback.setOnAction(e -> {
            stage.setScene(mainScene);
            stage.setFullScreen(true);
        });

        settinglayout.setCenter(btnback);
        return new Scene(settinglayout, 500, 650);
    }

    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setPrefSize(280, 45);
        button.getStyleClass().add("menu-button");
        return button;
    }

    private Scene createGameModeScene(Stage stage) {
        BorderPane modeLayout = new BorderPane();
        modeLayout.setStyle("-fx-background-color: #191923");
    
        Button btnEvo = createMenuButton("EVOLUTION & SURVIVAL");
        Button btnBuild = createMenuButton("BUILD & CREATE 🔒");
        Button btnVersus = createMenuButton("VERSUS MODE 🔒");
        Button btnChaos = createMenuButton("CHAOS & MADNESS 🔒");

        btnBuild.setDisable(true);
        btnVersus.setDisable(true);
        btnChaos.setDisable(true);
        
        Button btnback = new Button("QUAY LẠI");
        btnback.getStyleClass().add("menu-button");
        btnback.setOnAction(e -> {
            stage.setScene(mainScene);
            stage.setFullScreen(true);
        });

        // Click EVOLUTION -> Kích hoạt Loading & Chạy Timeline
        btnEvo.setOnAction(e -> {
            stage.setScene(loadingScene);
            stage.setFullScreen(true);
            
            // Chạy Timeline thanh Loading từ 0%
            progressB.setProgress(0);
            loadtimer.playFromStart();
        });

        VBox centerBox = new VBox(12, btnEvo, btnBuild, btnVersus, btnChaos);
        HBox bottomBox = new HBox(12, btnback);
        centerBox.setAlignment(Pos.CENTER);
        bottomBox.setAlignment(Pos.BOTTOM_LEFT);
        bottomBox.setPadding(new Insets(0, 0, 20, 20));
        
        modeLayout.setCenter(centerBox);
        modeLayout.setBottom(bottomBox);
        
        Scene scene = new Scene(modeLayout, 500, 650);
        applyCSS(scene);
        return scene;
    }

    private Scene createLoadingScreen(Stage stage) {
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #121212;");

        Label loadingLabel = new Label("ĐANG TẢI GAME...");
        loadingLabel.setStyle("-fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-text-fill: #ffd700;");
        
        progressB = new ProgressBar(0);
        progressB.setPrefWidth(350);
        
        VBox bottomBox = new VBox(15, loadingLabel, progressB);
        bottomBox.setAlignment(Pos.BOTTOM_CENTER);
        bottomBox.setPadding(new Insets(0, 0, 40, 0));
        
        layout.setBottom(bottomBox);
        
        Scene lScene = new Scene(layout, 500, 650);
        
        loadtimer = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(progressB.progressProperty(), 0)),
            new KeyFrame(Duration.seconds(3.0), new KeyValue(progressB.progressProperty(), 1.0))
        );

        // Nạp xong 100% -> Chuyển sang Gameplay & Bật Game Loop trong CÙNG 1 CỬA SỔ
        loadtimer.setOnFinished(e -> {
            // Khởi tạo GameScene trực tiếp và gắn lên Stage hiện tại
            gameplayEvoScene = gpEvo.createGamePlayScene(stage, mainScene);
            stage.setScene(gameplayEvoScene);
            stage.setFullScreen(true);
        });
        
        return lScene;
    }

    private void applyCSS(Scene scene) {
        try {
            File cssFile = new File("res/CSS/style.css");
            if (cssFile.exists()) {
                scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
            }
        } catch (Exception e) {
            System.out.println("Lỗi nạp file CSS: " + e.getMessage());
        }
    }
}