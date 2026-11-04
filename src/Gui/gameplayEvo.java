package Gui;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator.LeapableGenerator;

public class gameplayEvo {

    private List<Entity2D> foodList = new ArrayList<>();
    private Entity2D player;
    
    private final int TOTAL_FOOD = 200;
    private double playerX = 1500;
    private double playerY = 1500;
    private double baseRadius = 20;
    private double playerRadius = baseRadius;
    private double speed = 300;

    private List<Entity2D> subCells = new ArrayList<>();
    private final double subCellsRadius = 15;
    private double subCellSpeed = 250;

    private int score = 0;
    private final double worldWidth = 3000;
    private final double worldHeight = 3000;
    private Random random = new Random();

    private AnimationTimer gameLoop;
    private boolean isUpgradeMenuOpen = false;

    private boolean goUp, goDown, goLeft, goRight;

    public Scene createGamePlayScene(Stage stage, Scene mainScene) {
        try {
            stage.getIcons().clear();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/game_icon.png")));
        } catch (Exception e) {
        }
        stage.setTitle("Evo 3D - Evolution Game");

        BorderPane root = new BorderPane();
        Canvas canvas = new Canvas(1280, 720);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        StackPane canvasHolder = new StackPane(canvas);
        canvas.widthProperty().bind(canvasHolder.widthProperty());
        canvas.heightProperty().bind(canvasHolder.heightProperty());

        VBox upgradeUI = createEvolutionUpgradePanel(stage);
        upgradeUI.setVisible(false);

        StackPane gameStack = new StackPane(canvasHolder, upgradeUI);
        root.setCenter(gameStack);

        Scene gameScene = new Scene(root, 1280, 720);

        initGameData();

        gameScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP) goUp = true;
            if (e.getCode() == KeyCode.S || e.getCode() == KeyCode.DOWN) goDown = true;
            if (e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT) goLeft = true;
            if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) goRight = true;

            if (e.getCode() == KeyCode.E || e.getCode() == KeyCode.TAB) {
                isUpgradeMenuOpen = !isUpgradeMenuOpen;
                upgradeUI.setVisible(isUpgradeMenuOpen);
            }

            if (e.getCode() == KeyCode.ESCAPE) {
                if (gameLoop != null) gameLoop.stop();
                stage.setScene(mainScene);
                stage.setFullScreen(true);
            }
        });

        gameScene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP) goUp = false;
            if (e.getCode() == KeyCode.S || e.getCode() == KeyCode.DOWN) goDown = false;
            if (e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT) goLeft = false;
            if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) goRight = false;
        });

        gameLoop = new AnimationTimer() {
            private long lastTime = System.nanoTime();

            @Override
            public void handle(long now) {
                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                if (deltaTime > 0.1) deltaTime = 0.1;

                if (!isUpgradeMenuOpen) {
                    updateGameLogic(deltaTime);
                }
                renderGame(gc, canvas.getWidth(), canvas.getHeight());
            }
        };
        gameLoop.start();

        return gameScene;
    }

    private VBox createEvolutionUpgradePanel(Stage stage) {
        VBox panel = new VBox(15);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxSize(450, 480);
        panel.setStyle("-fx-background-color: rgba(15, 23, 42, 0.95); -fx-border-color: #38bdf8; -fx-border-width: 3px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 20px;");

        Label title = new Label("🧬 BẢNG TIẾN HOÁ SINH HỌC 🧬");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#38bdf8"));

        Button btnSize = createEvoButton("⚡ Tăng Kích Thước Cơ Thể (+10 Radius)", "Tiêu tốn: 5 Điểm tiến hoá", () -> {
            if (score >= 5) {
                score -= 5;
                playerRadius += 10;
                player.radius = playerRadius;
            }
        });

        Button btnSpeed = createEvoButton("🚀 Tăng Tốc Độ Di Chuyển (+50 Speed)", "Tiêu tốn: 8 Điểm tiến hoá", () -> {
            if (score >= 8) {
                score -= 8;
                speed += 50;
            }
        });

        Button btnSplit = createEvoButton("🧫 Tách Bào Tử Săn Mồi (Phân Bào)", "Tiêu tốn: 10 Điểm tiến hoá", () -> {
            if (score >= 10) {
                score -= 10;
                splitPlayerAuto();
            }
        });
        Button btn3Dscene = createEvoButton("CHUYỂN SANG 3D SCENE", "KHÔNG TỐN ĐIỂM", () -> {
    
            if (gameLoop != null) {
                gameLoop.stop();
            }
            try {
                stage.close(); 
        
        
                loading3Dscene nextSceneLauncher = new loading3Dscene();
        
                Stage newStage = new Stage();
                nextSceneLauncher.start(newStage); 
        
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        TextField txtThuHoi = new TextField();
        txtThuHoi.setPromptText("Số lượng...");
        txtThuHoi.setMaxWidth(130);
        txtThuHoi.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white; -fx-border-color: #0284c7; -fx-border-radius: 6px; -fx-padding: 6px;");

        Button btnThuhoi = new Button("Thu hồi bào tử");
        btnThuhoi.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 6px 12px;");
        btnThuhoi.setOnAction(e -> {
            try {
                int amount = Integer.parseInt(txtThuHoi.getText().trim());
                if (amount > 0 && !subCells.isEmpty()) {
                   int countToRemove = Math.min(amount, subCells.size());
                   for (int i = 0; i < countToRemove; i++) {
                       subCells.remove(subCells.size() - 1);
                   }
                   txtThuHoi.clear();
                }
            } catch (NumberFormatException ex) {
            }
        });

        HBox thuHoiGroup = new HBox(10, txtThuHoi, btnThuhoi);
        thuHoiGroup.setAlignment(Pos.CENTER);

        Button btnClose = new Button("Đóng Menu [Phím E]");
        btnClose.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 8px 16px;");
        btnClose.setOnAction(e -> {
            isUpgradeMenuOpen = false;
            panel.setVisible(false);
        });

        panel.getChildren().addAll(title, btnSize, btnSpeed, btnSplit, thuHoiGroup, btnClose, btn3Dscene);
        return panel;
    }

    private Button createEvoButton(String text, String subtext, Runnable action) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        Label lblMain = new Label(text);
        lblMain.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblMain.setTextFill(Color.WHITE);

        Label lblSub = new Label(subtext);
        lblSub.setFont(Font.font("Segoe UI", 11));
        lblSub.setTextFill(Color.web("#fbbf24"));

        box.getChildren().addAll(lblMain, lblSub);

        Button btn = new Button();
        btn.setGraphic(box);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: #1e293b; -fx-border-color: #0284c7; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8px;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #0369a1; -fx-border-color: #38bdf8; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8px;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #1e293b; -fx-border-color: #0284c7; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8px;"));
        
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private void initGameData() {
        score = 0;
        playerX = worldWidth / 2;
        playerY = worldHeight / 2;
        playerRadius = baseRadius;
        player = new Entity2D(playerX, playerY, playerRadius);

        foodList.clear();
        for (int i = 0; i < TOTAL_FOOD; i++) {
            foodList.add(new Entity2D(random.nextDouble() * worldWidth, random.nextDouble() * worldHeight, 8));
        }
        subCells.clear();
    }

    private void updateGameLogic(double deltaTime) {
        if (goUp) playerY -= speed * deltaTime;
        if (goDown) playerY += speed * deltaTime;
        if (goLeft) playerX -= speed * deltaTime;
        if (goRight) playerX += speed * deltaTime;

        playerX = Math.max(playerRadius, Math.min(worldWidth - playerRadius, playerX));
        playerY = Math.max(playerRadius, Math.min(worldHeight - playerRadius, playerY));

        player.x = playerX;
        player.y = playerY;

        for (Entity2D food : foodList) {
            double dx = playerX - food.x;
            double dy = playerY - food.y;
            if (Math.sqrt(dx * dx + dy * dy) < playerRadius + food.radius) {
                score++;
                playerRadius += 0.3;
                player.radius = playerRadius;
                food.x = random.nextDouble() * worldWidth;
                food.y = random.nextDouble() * worldHeight;
                break;
            }
        }

        for (Entity2D sub : subCells) {
            Entity2D nearestFood = null;
            double minDistance = Double.MAX_VALUE;

            for (Entity2D food : foodList) {
                double dist = Math.hypot(food.x - sub.x, food.y - sub.y);
                if (dist < minDistance) {
                    minDistance = dist;
                    nearestFood = food;
                }
            }

            if (nearestFood != null) {
                double dx = nearestFood.x - sub.x;
                double dy = nearestFood.y - sub.y;
                double angle = Math.atan2(dy, dx);

                sub.x += Math.cos(angle) * subCellSpeed * deltaTime;
                sub.y += Math.sin(angle) * subCellSpeed * deltaTime;

                sub.x = Math.max(sub.radius, Math.min(worldWidth - sub.radius, sub.x));
                sub.y = Math.max(sub.radius, Math.min(worldHeight - sub.radius, sub.y));

                if (minDistance < sub.radius + nearestFood.radius) {
                    score++;
                    nearestFood.x = random.nextDouble() * worldWidth;
                    nearestFood.y = random.nextDouble() * worldHeight;
                }
            }
        }
    }

    private void renderGame(GraphicsContext gc, double screenW, double screenH) {
        gc.setFill(Color.web("#0f172a"));
        gc.fillRect(0, 0, screenW, screenH);

        gc.save();
        gc.translate(screenW / 2 - playerX, screenH / 2 - playerY);

        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(4);
        gc.strokeRect(0, 0, worldWidth, worldHeight);

        gc.setFill(Color.web("#f43f5e"));
        for (Entity2D food : foodList) {
            gc.fillOval(food.x - food.radius, food.y - food.radius, food.radius * 2, food.radius * 2);
        }

        gc.setFill(Color.web("#22c55e"));
        for (Entity2D sub : subCells) {
            gc.fillOval(sub.x - sub.radius, sub.y - sub.radius, sub.radius * 2, sub.radius * 2);
        }

        gc.setFill(Color.web("#eab308"));
        gc.fillOval(player.x - player.radius, player.y - player.radius, player.radius * 2, player.radius * 2);

        gc.restore();

        gc.setFill(Color.web("#1e293b"));
        gc.fillRect(20, 20, 220, 60);
        gc.setStroke(Color.web("#38bdf8"));
        gc.strokeRect(20, 20, 220, 60);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        gc.fillText("Điểm Tiến hoá: " + score, 35, 45);
        gc.setFont(Font.font("Segoe UI", 12));
        gc.fillText("Ấn [E] mở Bảng Tiến Hoá", 35, 65);
    }

    private void splitPlayerAuto() {
        if (playerRadius > 20) {
            playerRadius -= 5;
            player.radius = playerRadius;
            subCells.add(new Entity2D(playerX + 30, playerY + 30, subCellsRadius));
        }
    }

    private static class Entity2D {
        double x, y, radius;
        Entity2D(double x, double y, double radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }
    }
}