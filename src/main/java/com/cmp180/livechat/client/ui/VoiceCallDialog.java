package com.cmp180.livechat.client.ui;

import com.cmp180.livechat.common.VoiceManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
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

    private static final String SVG_PHONE = "M7.64551 3C8.46332 3 9.19873 3.4979 9.50246 4.25722L10.5577 6.89309C10.8305 7.57516 10.7066 8.35195 10.2363 8.9163L9.66895 9.59668C9.00673 10.3913 9.06079 11.5596 9.79222 12.2911L11.7082 14.2088C12.4396 14.9402 13.6089 14.9929 14.4035 14.3307L15.0839 13.7632C15.6483 13.2929 16.4249 13.1701 17.1069 13.4429L19.7434 14.4971C20.5027 14.8009 21.0005 15.5361 21.0005 16.354L21 19.1052C21 20.1516 20.1519 21 19.1055 21C10.2108 21 3 13.7892 3 4.89453C3 3.8481 3.8483 3 4.89474 3H7.64551Z";

    private static final String SVG_MIC = "M11.665 7.915v1.31a5.257 5.257 0 0 1-1.514 3.694 5.174 5.174 0 0 1-1.641 1.126 5.04 5.04 0 0 1-1.456.384v1.899h2.312a.554.554 0 0 1 0 1.108H3.634a.554.554 0 0 1 0-1.108h2.312v-1.899a5.045 5.045 0 0 1-1.456-.384 5.174 5.174 0 0 1-1.641-1.126 5.257 5.257 0 0 1-1.514-3.695v-1.31a.554.554 0 1 1 1.109 0v1.31a4.131 4.131 0 0 0 1.195 2.917 3.989 3.989 0 0 0 5.722 0 4.133 4.133 0 0 0 1.195-2.917v-1.31a.554.554 0 1 1 1.109 0zM3.77 10.37a2.875 2.875 0 0 1-.233-1.146V4.738A2.905 2.905 0 0 1 3.77 3.58a3 3 0 0 1 1.59-1.59 2.902 2.902 0 0 1 1.158-.233 2.865 2.865 0 0 1 1.152.233 2.977 2.977 0 0 1 1.793 2.748l-.012 4.487a2.958 2.958 0 0 1-.856 2.09 3.025 3.025 0 0 1-.937.634 2.865 2.865 0 0 1-1.152.233 2.905 2.905 0 0 1-1.158-.233A2.957 2.957 0 0 1 3.77 10.37z";

    private final Stage stage;
    private final Label lblStatus;
    private final Label lblDuration;
    private final Button btnMute;
    private final Button btnEnd;
    private final Timeline timer;
    private int secondsElapsed = 0;
    private boolean isConnected = false;
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

        // Avatar với chữ cái đầu của người đối thoại
        Circle avatar = new Circle(34, Color.web(ThemeManager.getPrimaryColor()));
        String initial = (peerName != null && !peerName.trim().isEmpty()) 
                ? peerName.trim().substring(0, 1).toUpperCase() 
                : "U";
        Label lblAvatar = new Label(initial);
        lblAvatar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        lblAvatar.setStyle("-fx-text-fill: white;");
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

        HBox actionRow = new HBox(24);
        actionRow.setAlignment(Pos.CENTER);

        // Nút Bật / Tắt Mic dạng tròn với icon Mic
        btnMute = new Button();
        btnMute.setPrefSize(48, 48);
        btnMute.setMinSize(48, 48);
        btnMute.setMaxSize(48, 48);
        btnMute.setCursor(javafx.scene.Cursor.HAND);
        btnMute.setOnAction(e -> toggleMute());
        updateMuteButtonState(VoiceManager.getInstance().isMuted());

        // Nút Gác máy dạng tròn màu đỏ với icon điện thoại
        btnEnd = new Button();
        btnEnd.setPrefSize(48, 48);
        btnEnd.setMinSize(48, 48);
        btnEnd.setMaxSize(48, 48);
        btnEnd.setCursor(javafx.scene.Cursor.HAND);
        Tooltip.install(btnEnd, new Tooltip("Gác máy"));
        btnEnd.setGraphic(createPhoneGraphic());
        btnEnd.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 50; -fx-padding: 0;");
        btnEnd.setOnMouseEntered(e -> btnEnd.setStyle("-fx-background-color: #dc2626; -fx-background-radius: 50; -fx-padding: 0;"));
        btnEnd.setOnMouseExited(e -> btnEnd.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 50; -fx-padding: 0;"));
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
        this.isConnected = true;
        Platform.runLater(() -> {
            lblStatus.setText("Đang trong cuộc gọi");
            lblDuration.setVisible(true);
            timer.play();
        });
    }

    private void toggleMute() {
        boolean nowMuted = !VoiceManager.getInstance().isMuted();
        VoiceManager.getInstance().setMuted(nowMuted);
        updateMuteButtonState(nowMuted);
    }

    private void updateMuteButtonState(boolean isMuted) {
        boolean dark = ThemeManager.isDarkMode();
        btnMute.setGraphic(createMicGraphic(isMuted, dark));
        Tooltip.install(btnMute, new Tooltip(isMuted ? "Bật micro" : "Tắt micro"));

        String bg = isMuted 
            ? (dark ? "#3b2020" : "#fee2e2") 
            : (dark ? "#334155" : "#e2e8f0");
        String border = isMuted 
            ? (dark ? "#7f1d1d" : "#fca5a5") 
            : "transparent";
        String hoverBg = isMuted 
            ? (dark ? "#4c2626" : "#fecaca") 
            : (dark ? "#475569" : "#cbd5e1");

        btnMute.setStyle("-fx-background-color: " + bg + "; -fx-border-color: " + border + "; -fx-border-radius: 50; -fx-background-radius: 50; -fx-padding: 0;");
        btnMute.setOnMouseEntered(e -> btnMute.setStyle("-fx-background-color: " + hoverBg + "; -fx-border-color: " + border + "; -fx-border-radius: 50; -fx-background-radius: 50; -fx-padding: 0;"));
        btnMute.setOnMouseExited(e -> btnMute.setStyle("-fx-background-color: " + bg + "; -fx-border-color: " + border + "; -fx-border-radius: 50; -fx-background-radius: 50; -fx-padding: 0;"));
    }

    private Node createMicGraphic(boolean isMuted, boolean dark) {
        SVGPath mic = new SVGPath();
        mic.setContent(SVG_MIC);

        if (!isMuted) {
            mic.setFill(Color.web(dark ? "#f8fafc" : "#1e293b"));
            mic.setScaleX(1.15);
            mic.setScaleY(1.15);
            return mic;
        } else {
            Group group = new Group();
            mic.setFill(Color.web(dark ? "#94a3b8" : "#64748b"));

            // Đường cắt mờ (cutout) cùng màu nền để đường gạch chéo nổi bật rõ ràng qua thân mic
            Line cutout = new Line(0.5, 1.0, 12.5, 17.5);
            cutout.setStroke(Color.web(dark ? "#3b2020" : "#fee2e2"));
            cutout.setStrokeWidth(3.6);
            cutout.setStrokeLineCap(StrokeLineCap.ROUND);

            // Đường gạch chéo màu đỏ với 2 đầu bo tròn
            Line slash = new Line(0.5, 1.0, 12.5, 17.5);
            slash.setStroke(Color.web("#ef4444"));
            slash.setStrokeWidth(2.2);
            slash.setStrokeLineCap(StrokeLineCap.ROUND);

            group.getChildren().addAll(mic, cutout, slash);
            group.setScaleX(1.15);
            group.setScaleY(1.15);
            return group;
        }
    }

    private Node createPhoneGraphic() {
        SVGPath phone = new SVGPath();
        phone.setContent(SVG_PHONE);
        phone.setFill(Color.TRANSPARENT);
        phone.setStroke(Color.WHITE);
        phone.setStrokeWidth(2.0);
        phone.setStrokeLineCap(StrokeLineCap.ROUND);
        phone.setStrokeLineJoin(StrokeLineJoin.ROUND);
        phone.setRotate(135);
        phone.setScaleX(0.95);
        phone.setScaleY(0.95);
        return phone;
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

    public boolean isConnected() {
        return isConnected;
    }
}
