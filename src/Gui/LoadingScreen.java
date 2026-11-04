package Gui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;

public class LoadingScreen {

    private Stage splashStage;
    private DoubleProperty progressProperty = new SimpleDoubleProperty(0);

    public void showAndLaunch(Runnable onFinished) {
        splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED); // Bỏ khung viền cửa sổ
        
        try {
            File iconFile = new File("res/image/Icon.PNG");
            if (!iconFile.exists()) {
                iconFile = new File("res/image/Icon.png"); // Backup chữ thường
            }
            if (iconFile.exists()) {
                splashStage.getIcons().add(new Image(iconFile.toURI().toString()));
            }
        } catch (Exception e) {
            System.out.println("Lỗi nạp Icon cho Splash: " + e.getMessage());
        }
        // ----------------------------------------

        Pane root = new Pane();
        root.setStyle("-fx-background-color: #1e2022;"); //[cite: 1, 2]

        // 1. Tải Logo[cite: 1, 2]
        Image logoImage = null;
        try {
            File imgFile = new File("res/image/loadingscreenpng.PNG");
            if (imgFile.exists()) {
                logoImage = new Image(imgFile.toURI().toString());
            }
        } catch (Exception e) {
            System.out.println("Lỗi nạp logo Splash: " + e.getMessage());
        }

        Canvas canvas = new Canvas(1920, 1080);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        final Image finalLogo = logoImage;

        // 2. Lắng nghe tiến trình % để vẽ lại đồ họa[cite: 1, 2]
        progressProperty.addListener((obs, oldVal, newVal) -> {
            double currentPercent = newVal.doubleValue();
            double screenWidth = canvas.getWidth();
            double screenHeight = canvas.getHeight();

            // Nền trắng[cite: 1, 2]
            gc.setFill(Color.web("#22252A"));
            gc.fillRect(0, 0, screenWidth, screenHeight);

            // Logo 600x400 căn giữa dịch lên 40px[cite: 1, 2]
            if (finalLogo != null) {
                double logoWidth = 900;  // 600 + 50% = 900
                double logoHeight = 600; // 400 + 50% = 600
                double logoX = (screenWidth - logoWidth) / 2;
                double logoY = ((screenHeight - logoHeight) / 2) - 40; 
    
                gc.drawImage(finalLogo, logoX, logoY, logoWidth, logoHeight);
            }

            // Thanh Loading đặt vị trí 88% chiều cao[cite: 1, 2]
            double barWidth = screenWidth * 0.6; //[cite: 1, 2]
            double barHeight = 12; //[cite: 1, 2]
            double barX = (screenWidth - barWidth) / 2;
            double barY = screenHeight * 0.88; //[cite: 1, 2]

            // Khung xám tối (#282828)[cite: 1, 2]
            gc.setFill(Color.rgb(40, 40, 40));
            gc.fillRect(barX, barY, barWidth, barHeight);

            // Thanh chạy Crimson (#DC143C)[cite: 1, 2]
            double currentWidth = barWidth * (currentPercent / 100.0);
            gc.setFill(Color.rgb(220, 20, 60));
            gc.fillRect(barX, barY, currentWidth, barHeight);

            // Chữ LOADING... %[cite: 1, 2]
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            
            String displayStr = "LOADING... " + (int) currentPercent + "%";
            Text tempText = new Text(displayStr);
            tempText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            double textX = (screenWidth - tempText.getLayoutBounds().getWidth()) / 2;
            
            gc.fillText(displayStr, textX, barY - 12);
        });

        // 3. Timeline chạy từ 0% -> 100% trong 3 giây
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(progressProperty, 0)),
            new KeyFrame(Duration.seconds(3.0), new KeyValue(progressProperty, 100))
        );

        timeline.setOnFinished(e -> {
            splashStage.close();
            if (onFinished != null) {
                onFinished.run(); // Mở main_gui khi nạp xong
            }
        });

        root.getChildren().add(canvas);
        Scene scene = new Scene(root, 1920, 1080);

        scene.widthProperty().addListener((obs, o, n) -> canvas.setWidth(n.doubleValue()));
        scene.heightProperty().addListener((obs, o, n) -> canvas.setHeight(n.doubleValue()));

        splashStage.setScene(scene);
        splashStage.setFullScreen(true);
        splashStage.show();

        timeline.playFromStart();
    }
}