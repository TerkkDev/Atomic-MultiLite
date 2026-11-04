package Gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.joml.Matrix4f;
import java.nio.FloatBuffer;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class loading3Dscene extends Application {
    private float posX = 0.0f;
    private float posY = 0.0f;
    private float speed = 0.002f;

    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/res/icon.png")));
        } catch (Exception e) {
        }
        primaryStage.setFullScreen(true);
        ProgressBar pb = new ProgressBar(0);
        pb.setPrefWidth(250);

        Label status = new Label("ĐANG TẢI ENGINE ATOMIC MULTILITE...");
        VBox root = new VBox(15, pb, status);
        root.setAlignment(Pos.CENTER);

        Scene loadSce = new Scene(root, 350, 150);
        primaryStage.setTitle("Atomic MultiLite 3D");
        primaryStage.setScene(loadSce);
        primaryStage.show();

        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for (int i = 1; i <= 100; i++) {
                    Thread.sleep(15);
                    updateProgress(i, 100);
                }
                return null;
            }
        };

        pb.progressProperty().bind(loadTask.progressProperty());

        loadTask.setOnSucceeded(e -> {
            primaryStage.close();
            Platform.runLater(() -> {
                startLWJGL();
            });
        });

        new Thread(loadTask).start();
    }

    private void startLWJGL() {
        if (!glfwInit()) {
            throw new IllegalStateException("Không thể khởi tạo GLFW!");
        }

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        long window = glfwCreateWindow(vidmode.width(), vidmode.height(), "Atomic MultiLite", glfwGetPrimaryMonitor(), NULL);
        if (window == NULL) {
            throw new RuntimeException("Tạo cửa sổ GLFW thất bại!");
        }

        try (org.lwjgl.system.MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush()) {
            java.nio.IntBuffer w = stack.mallocInt(1);
            java.nio.IntBuffer h = stack.mallocInt(1);
            java.nio.IntBuffer comp = stack.mallocInt(1);

            var is = getClass().getResourceAsStream("/resources/game_icon.png");
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                java.nio.ByteBuffer bb = stack.malloc(bytes.length);
                bb.put(bytes);
                bb.flip();

                java.nio.ByteBuffer imgBuf = org.lwjgl.stb.STBImage.stbi_load_from_memory(bb, w, h, comp, 4);
                if (imgBuf != null) {
                    org.lwjgl.glfw.GLFWImage image = org.lwjgl.glfw.GLFWImage.malloc();
                    org.lwjgl.glfw.GLFWImage.Buffer imageBufs = org.lwjgl.glfw.GLFWImage.malloc(1);
                    image.set(w.get(0), h.get(0), imgBuf);
                    imageBufs.put(0, image);

                    glfwSetWindowIcon(window, imageBufs);

                    org.lwjgl.stb.STBImage.stbi_image_free(imgBuf);
                    image.free();
                    imageBufs.free();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        glfwMakeContextCurrent(window);
        glfwShowWindow(window);
        GL.createCapabilities();

        GL11.glEnable(GL11.GL_DEPTH_TEST);

        // 1. Biên dịch Shader nhận Ma trận 3D (uTransForm)
        String vertexShaderSource = "#version 330 core\n" +
        "layout (location = 0) in vec3 aPos;\n" +
        "uniform mat4 uProjection;\n" +
        "uniform mat4 uView;\n" +
        "uniform mat4 uModel;\n" +
        "void main()\n" +
        "{\n" +
        "   gl_Position = uProjection * uView * uModel *vec4(aPos, 1.0);\n" +
        "}";
        
        String fragmentShaderSource = "#version 330 core\n" +
        "out vec4 FragColor;\n" +
        "void main()\n" +
        "{\n" +
        "   FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);\n" +
        "}";

        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexShaderSource);
        GL20.glCompileShader(vertexShader);
        
        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, fragmentShaderSource);
        GL20.glCompileShader(fragmentShader);

        int shaderProgramme = GL20.glCreateProgram();
        GL20.glAttachShader(shaderProgramme, vertexShader);
        GL20.glAttachShader(shaderProgramme, fragmentShader);
        GL20.glLinkProgram(shaderProgramme);

        int projLoc = GL20.glGetUniformLocation(shaderProgramme, "uProjection");
        int viewLoc = GL20.glGetUniformLocation(shaderProgramme, "uView");
        int modeLoc = GL20.glGetUniformLocation(shaderProgramme, "uModel");

        // 2. Khởi tạo dữ liệu đỉnh tam giác cố định ban đầu
        float[] vertices = {
            //mặt sau
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            // mặt trước
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f , 0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,
            //mặt trái
            -0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f , 0.5f, 0.5f,
            //mặt phải
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            //mặt dưới
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f,
            //mặt trên
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f

        };

        int vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);
        
        int vboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);
        
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        glClearColor(0.1f, 0.2f, 0.3f, 1.0f);

        Matrix4f projectionMatrix = new Matrix4f();
        Matrix4f viewMatrix = new Matrix4f();
        Matrix4f modelMatrix = new Matrix4f();
        FloatBuffer matrixBuffer = org.lwjgl.BufferUtils.createFloatBuffer(16);

        projectionMatrix.perspective((float) Math.toRadians(60.0f), (float) vidmode.width() / vidmode.height(), 0.1f, 100.0f);

        // 3. Vòng lặp render chính
        while (!glfwWindowShouldClose(window)) {
            // Xử lý phím di chuyển W A SD
            if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) posX -= speed;
            if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) posX += speed;
            if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) posY += speed;
            if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) posY -= speed;

            // Xóa màn hình
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            // Kích hoạt shader
            GL20.glUseProgram(shaderProgramme);

            projectionMatrix.get(matrixBuffer);
            GL20.glUniformMatrix4fv(projLoc, false, matrixBuffer);

            viewMatrix.identity().setLookAt(
                0.0f, 0.0f, 3.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f
            );
            viewMatrix.get(matrixBuffer);
            GL20.glUniformMatrix4fv(viewLoc, false, matrixBuffer);

            modelMatrix.identity()
                .translate(posX, posY, 0.0f)
                .rotate((float)glfwGetTime(), 1.0f, 1.0f, 0.0f);
            modelMatrix.get(matrixBuffer);
            GL20.glUniformMatrix4fv(modeLoc, false, matrixBuffer);

            // Vẽ tam giác thông qua VAO
            GL30.glBindVertexArray(vaoId);
            GL20.glEnableVertexAttribArray(0);

            glDrawArrays(GL_TRIANGLES, 0, 36);

            GL20.glDisableVertexAttribArray(0);
            GL30.glBindVertexArray(0);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        glfwDestroyWindow(window);
        glfwTerminate();
    }

    public static void main(String[] args) {
        launch(args);
    }
}