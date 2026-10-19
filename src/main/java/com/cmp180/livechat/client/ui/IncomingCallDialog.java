package com.cmp180.livechat.client.ui;

import com.cmp180.livechat.common.VoiceManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Hộp thoại hiển thị khi có cuộc gọi thoại đến (Incoming Voice Call).
 */
public class IncomingCallDialog {

    public interface CallResponseHandler {
        void onResponse(boolean accepted);
    }

    private static Stage activeStage;

    public static void show(Window owner, String callerName, CallResponseHandler handler) {
        close();

        // Phát chuông gọi đến
        VoiceManager.getInstance().startRingtone();

        Stage stage = new Stage();
        activeStage = stage;
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setTitle("Cuộc gọi đến");
        stage.setResizable(false);

        boolean dark = ThemeManager.isDarkMode();

        VBox root = new VBox(18);
        root.setPadding(new Insets(24, 28, 24, 28));
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(360);
        root.setStyle("-fx-background-color: " + (dark ? "#1e1e1e" : "#ffffff") + "; -fx-background-radius: 12;");

        // Icon Avatar
        Circle avatarCircle = new Circle(32, Color.web(ThemeManager.getPrimaryColor()));
        Label lblAvatarText = new Label("📞");
        lblAvatarText.setFont(Font.font(26));
        StackPane avatarPane = new StackPane(avatarCircle, lblAvatarText);

        Label lblTitle = new Label("Cuộc gọi thoại đến...");
        lblTitle.setFont(Font.font("Segoe UI", 13));
        lblTitle.setStyle("-fx-text-fill: " + (dark ? "#94a3b8" : "#64748b") + ";");

        Label lblCaller = new Label(callerName != null ? callerName : "Người dùng");
        lblCaller.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblCaller.setStyle("-fx-text-fill: " + (dark ? "#f8fafc" : "#0f172a") + ";");

        HBox buttonBar = new HBox(20);
        buttonBar.setAlignment(Pos.CENTER);

        Button btnReject = new Button("Từ chối");
        btnReject.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnReject.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 8 20;");

        Button btnAccept = new Button("Trả lời");
        btnAccept.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnAccept.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 8 24;");

        buttonBar.getChildren().addAll(btnReject, btnAccept);

        root.getChildren().addAll(avatarPane, lblTitle, lblCaller, buttonBar);

        // Timeout 30 giây tự động từ chối
        Timeline timeout = new Timeline(new KeyFrame(Duration.seconds(30), ev -> {
            close();
            if (handler != null) handler.onResponse(false);
        }));
        timeout.play();

        btnReject.setOnAction(e -> {
            timeout.stop();
            close();
            if (handler != null) handler.onResponse(false);
        });

        btnAccept.setOnAction(e -> {
            timeout.stop();
            close();
            if (handler != null) handler.onResponse(true);
        });

        stage.setOnCloseRequest(e -> {
            timeout.stop();
            VoiceManager.getInstance().stopRingtone();
            if (handler != null) handler.onResponse(false);
        });

        Scene scene = new Scene(root);
        ThemeManager.applyTheme(scene);
        stage.setScene(scene);
        stage.show();
    }

    public static void close() {
        VoiceManager.getInstance().stopRingtone();
        if (activeStage != null) {
            try {
                activeStage.close();
            } catch (Exception ignored) {}
            activeStage = null;
        }
    }
}
