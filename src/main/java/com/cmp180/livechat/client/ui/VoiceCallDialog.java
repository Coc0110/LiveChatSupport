package com.cmp180.livechat.client.ui;

import com.cmp180.livechat.common.VoiceManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
 * Cửa sổ quản lý Cuộc gọi thoại trực tiếp (Voice Call Window).
 * Hiển thị thời gian cuộc gọi, nút bật/tắt Micro và nút gác máy.
 */
public class VoiceCallDialog {

    private final Stage stage;
    private final Label lblStatus;
    private final Label lblDuration;
    private final Button btnMute;
    private final Button btnEnd;
    private final Timeline timer;
    private int secondsElapsed = 0;
    private Runnable onHangUp;

    public VoiceCallDialog(Window owner, String peerName, boolean isCaller, Runnable onHangUp) {
        this.onHangUp = onHangUp;
        this.stage = new Stage();
        this.stage.initModality(Modality.NONE);
        if (owner != null) this.stage.initOwner(owner);
        this.stage.setTitle("Cuộc gọi thoại - " + peerName);
        this.stage.setResizable(false);

        boolean dark = ThemeManager.isDarkMode();

        VBox root = new VBox(16);
        root.setPadding(new Insets(24, 30, 24, 30));
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(360);
        root.setStyle("-fx-background-color: " + (dark ? "#1e1e1e" : "#ffffff") + "; -fx-background-radius: 12;");

        // Avatar
        Circle avatar = new Circle(34, Color.web(ThemeManager.getPrimaryColor()));
        Label lblAvatar = new Label("🎧");
        lblAvatar.setFont(Font.font(28));
        StackPane avatarBox = new StackPane(avatar, lblAvatar);

        Label lblPeer = new Label(peerName);
        lblPeer.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblPeer.setStyle("-fx-text-fill: " + (dark ? "#f8fafc" : "#0f172a") + ";");

        lblStatus = new Label(isCaller ? "Đang đổ chuông..." : "Đang kết nối...");
        lblStatus.setFont(Font.font("Segoe UI", 13));
        lblStatus.setStyle("-fx-text-fill: " + (dark ? "#94a3b8" : "#64748b") + ";");

        lblDuration = new Label("00:00");
        lblDuration.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblDuration.setStyle("-fx-text-fill: " + (dark ? "#38bdf8" : "#0284c7") + ";");
        lblDuration.setVisible(false);

        HBox actionRow = new HBox(20);
        actionRow.setAlignment(Pos.CENTER);

        btnMute = new Button("🎙️ Tắt Mic");
        btnMute.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btnMute.setStyle("-fx-background-color: " + (dark ? "#334155" : "#e2e8f0") + "; -fx-text-fill: " + (dark ? "#f8fafc" : "#1e293b") + "; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 8 16;");
        btnMute.setOnAction(e -> toggleMute());

        btnEnd = new Button("🔴 Gác máy");
        btnEnd.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btnEnd.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 8 20;");
        btnEnd.setOnAction(e -> hangUp());

        actionRow.getChildren().addAll(btnMute, btnEnd);

        root.getChildren().addAll(avatarBox, lblPeer, lblStatus, lblDuration, actionRow);

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsElapsed++;
            int m = secondsElapsed / 60;
            int s = secondsElapsed % 60;
            lblDuration.setText(String.format("%02d:%02d", m, s));
        }));
        timer.setCycleCount(Timeline.INDEFINITE);

        stage.setOnCloseRequest(e -> hangUp());

        Scene scene = new Scene(root);
        ThemeManager.applyTheme(scene);
        stage.setScene(scene);
    }

    public void show() {
        stage.show();
    }

    public void setConnected() {
        Platform.runLater(() -> {
            lblStatus.setText("Đang trong cuộc gọi");
            lblDuration.setVisible(true);
            timer.play();
        });
    }

    private void toggleMute() {
        boolean nowMuted = !VoiceManager.getInstance().isMuted();
        VoiceManager.getInstance().setMuted(nowMuted);
        btnMute.setText(nowMuted ? "🔇 Bật Mic" : "🎙️ Tắt Mic");
        btnMute.setStyle(nowMuted 
                ? "-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 8 16;" 
                : "-fx-background-color: " + (ThemeManager.isDarkMode() ? "#334155" : "#e2e8f0") + "; -fx-text-fill: " + (ThemeManager.isDarkMode() ? "#f8fafc" : "#1e293b") + "; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 8 16;");
    }

    public void hangUp() {
        if (timer != null) timer.stop();
        VoiceManager.getInstance().stopCallAudio();
        try {
            stage.close();
        } catch (Exception ignored) {}
        if (onHangUp != null) {
            Runnable cb = onHangUp;
            onHangUp = null;
            cb.run();
        }
    }

    public void closeDirectly() {
        if (timer != null) timer.stop();
        VoiceManager.getInstance().stopCallAudio();
        try {
            stage.close();
        } catch (Exception ignored) {}
    }

    public int getSecondsElapsed() {
        return secondsElapsed;
    }
}
