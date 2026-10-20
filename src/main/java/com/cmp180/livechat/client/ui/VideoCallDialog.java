package com.cmp180.livechat.client.ui;

import com.cmp180.livechat.common.VideoManager;
import com.cmp180.livechat.common.VoiceManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;

/**
 * Cửa sổ quản lý Cuộc gọi Video (Video Call Window).
 * Giao diện hiện đại với khung hình đối phương toàn màn hình,
 * khung PiP (Picture-in-Picture) xem trước camera cá nhân và thanh công cụ nổi.
 */
public class VideoCallDialog {

    private static final String SVG_PHONE = "M7.64551 3C8.46332 3 9.19873 3.4979 9.50246 4.25722L10.5577 6.89309C10.8305 7.57516 10.7066 8.35195 10.2363 8.9163L9.66895 9.59668C9.00673 10.3913 9.06079 11.5596 9.79222 12.2911L11.7082 14.2088C12.4396 14.9402 13.6089 14.9929 14.4035 14.3307L15.0839 13.7632C15.6483 13.2929 16.4249 13.1701 17.1069 13.4429L19.7434 14.4971C20.5027 14.8009 21.0005 15.5361 21.0005 16.354L21 19.1052C21 20.1516 20.1519 21 19.1055 21C10.2108 21 3 13.7892 3 4.89453C3 3.8481 3.8483 3 4.89474 3H7.64551Z";
    private static final String SVG_MIC = "M11.665 7.915v1.31a5.257 5.257 0 0 1-1.514 3.694 5.174 5.174 0 0 1-1.641 1.126 5.04 5.04 0 0 1-1.456.384v1.899h2.312a.554.554 0 0 1 0 1.108H3.634a.554.554 0 0 1 0-1.108h2.312v-1.899a5.045 5.045 0 0 1-1.456-.384 5.174 5.174 0 0 1-1.641-1.126 5.257 5.257 0 0 1-1.514-3.695v-1.31a.554.554 0 1 1 1.109 0v1.31a4.131 4.131 0 0 0 1.195 2.917 3.989 3.989 0 0 0 5.722 0 4.133 4.133 0 0 0 1.195-2.917v-1.31a.554.554 0 1 1 1.109 0zM3.77 10.37a2.875 2.875 0 0 1-.233-1.146V4.738A2.905 2.905 0 0 1 3.77 3.58a3 3 0 0 1 1.59-1.59 2.902 2.902 0 0 1 1.158-.233 2.865 2.865 0 0 1 1.152.233 2.977 2.977 0 0 1 1.793 2.748l-.012 4.487a2.958 2.958 0 0 1-.856 2.09 3.025 3.025 0 0 1-.937.634 2.865 2.865 0 0 1-1.152.233 2.905 2.905 0 0 1-1.158-.233A2.957 2.957 0 0 1 3.77 10.37z";
    private static final String SVG_VIDEO = "M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z";
    private static final String SVG_SWITCH = "M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15";

    private final Stage stage;
    private final String peerName;
    private final boolean isCaller;

    private final ImageView remoteVideoView;
    private final ImageView localVideoView;
    private final VBox remotePlaceholder;
    private final Label lblStatus;
    private final Label lblDuration;
    private final Button btnMute;
    private final Button btnToggleCam;
    private final Button btnSwitchSource;
    private final Button btnEnd;

    private final Timeline timer;
    private int secondsElapsed = 0;
    private boolean isConnected = false;
    private Runnable onHangUp;

    public VideoCallDialog(Window owner, String peerName, boolean isCaller, Runnable onHangUp) {
        this.peerName = peerName != null ? peerName : "Người dùng";
        this.isCaller = isCaller;
        this.onHangUp = onHangUp;

        this.stage = new Stage();
        this.stage.initModality(Modality.NONE);
        if (owner != null) this.stage.initOwner(owner);
        this.stage.setTitle("Cuộc gọi video - " + this.peerName);
        this.stage.setMinWidth(520);
        this.stage.setMinHeight(420);
        this.stage.setWidth(680);
        this.stage.setHeight(520);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #0f172a;");

        // 1. Khung hiển thị Video của người đối thoại (Remote Video)
        remoteVideoView = new ImageView();
        remoteVideoView.setPreserveRatio(true);
        remoteVideoView.setSmooth(true);
        remoteVideoView.fitWidthProperty().bind(root.widthProperty());
        remoteVideoView.fitHeightProperty().bind(root.heightProperty());

        // Placeholder hiển thị khi chưa nhận được khung hình hoặc camera đối phương tắt
        remotePlaceholder = new VBox(14);
        remotePlaceholder.setAlignment(Pos.CENTER);
        remotePlaceholder.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        remotePlaceholder.setStyle("-fx-background-color: transparent;");
        StackPane.setAlignment(remotePlaceholder, Pos.CENTER);

        Circle avatar = new Circle(44, Color.web(ThemeManager.getPrimaryColor()));
        String initial = (this.peerName != null && !this.peerName.trim().isEmpty())
                ? this.peerName.trim().substring(0, 1).toUpperCase()
                : "U";
        Label lblAvatar = new Label(initial);
        lblAvatar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        lblAvatar.setStyle("-fx-text-fill: white;");
        StackPane avatarBox = new StackPane(avatar, lblAvatar);
        avatarBox.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0, 0, 4);");

        Label lblPeerBig = new Label(this.peerName);
        lblPeerBig.setFont(Font.font("Segoe UI", FontWeight.BOLD, 19));
        lblPeerBig.setStyle("-fx-text-fill: #f8fafc;");

        lblStatus = new Label(isCaller ? "Đang đổ chuông cuộc gọi video..." : "Đang kết nối video...");
        lblStatus.setFont(Font.font("Segoe UI", 13.5));
        lblStatus.setStyle("-fx-text-fill: #94a3b8;");

        remotePlaceholder.getChildren().addAll(avatarBox, lblPeerBig, lblStatus);

        // 2. Header Overlay (Góc trên trái)
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        topBar.setStyle("-fx-background-color: rgba(15, 23, 42, 0.75); -fx-background-radius: 20; -fx-padding: 6 14 6 12; -fx-border-color: rgba(255,255,255,0.12); -fx-border-radius: 20; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 8, 0, 0, 2);");

        Circle liveDot = new Circle(4.5, Color.web("#22c55e"));
        Label lblPeerName = new Label(this.peerName);
        lblPeerName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblPeerName.setStyle("-fx-text-fill: white;");

        lblDuration = new Label("00:00");
        lblDuration.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblDuration.setStyle("-fx-text-fill: #38bdf8; -fx-background-color: rgba(0,0,0,0.35); -fx-background-radius: 8; -fx-padding: 2 6;");
        lblDuration.setVisible(false);

        topBar.getChildren().addAll(liveDot, lblPeerName, lblDuration);
        StackPane.setAlignment(topBar, Pos.TOP_LEFT);
        StackPane.setMargin(topBar, new Insets(16, 0, 0, 20));

        // 3. Picture-in-Picture (PiP) - Khung xem trước Camera của chính mình (Góc trên phải)
        StackPane pipBox = new StackPane();
        pipBox.setPrefSize(160, 120);
        pipBox.setMaxSize(160, 120);
        pipBox.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 14; -fx-border-color: rgba(255,255,255,0.35); -fx-border-radius: 14; -fx-border-width: 1.5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 12, 0, 0, 4);");

        VBox pipPlaceholder = new VBox(6);
        pipPlaceholder.setAlignment(Pos.CENTER);
        SVGPath camPlaceholderIcon = new SVGPath();
        camPlaceholderIcon.setContent(SVG_VIDEO);
        camPlaceholderIcon.setFill(Color.TRANSPARENT);
        camPlaceholderIcon.setStroke(Color.web("#94a3b8"));
        camPlaceholderIcon.setStrokeWidth(1.8);
        camPlaceholderIcon.setScaleX(0.85);
        camPlaceholderIcon.setScaleY(0.85);
        Label lblCamWait = new Label("Camera của bạn");
        lblCamWait.setFont(Font.font("Segoe UI", 11));
        lblCamWait.setStyle("-fx-text-fill: #94a3b8;");
        pipPlaceholder.getChildren().addAll(camPlaceholderIcon, lblCamWait);

        localVideoView = new ImageView();
        localVideoView.setFitWidth(160);
        localVideoView.setFitHeight(120);
        localVideoView.setPreserveRatio(false);

        Rectangle pipClip = new Rectangle(160, 120);
        pipClip.setArcWidth(26);
        pipClip.setArcHeight(26);
        localVideoView.setClip(pipClip);

        Label lblYou = new Label("Bạn");
        lblYou.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10.5));
        lblYou.setStyle("-fx-background-color: rgba(0,0,0,0.65); -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 2 6;");
        StackPane.setAlignment(lblYou, Pos.BOTTOM_LEFT);
        StackPane.setMargin(lblYou, new Insets(0, 0, 6, 6));

        pipBox.getChildren().addAll(pipPlaceholder, localVideoView, lblYou);
        StackPane.setAlignment(pipBox, Pos.TOP_RIGHT);
        StackPane.setMargin(pipBox, new Insets(16, 20, 0, 0));

        // 4. Thanh công cụ điều khiển nổi ở dưới đáy (Bottom Floating Controls)
        HBox controlBar = new HBox(16);
        controlBar.setAlignment(Pos.CENTER);
        controlBar.setPadding(new Insets(10, 22, 10, 22));
        controlBar.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        controlBar.setStyle("-fx-background-color: rgba(15, 23, 42, 0.88); -fx-background-radius: 35; -fx-border-color: rgba(255,255,255,0.18); -fx-border-radius: 35; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 16, 0, 0, 6);");

        // Nút Micro
        btnMute = createRoundButton();
        btnMute.setOnAction(e -> toggleMute());
        updateMuteButtonState(VoiceManager.getInstance().isMuted());

        // Nút Camera
        btnToggleCam = createRoundButton();
        btnToggleCam.setOnAction(e -> toggleCamera());
        updateCamButtonState(VideoManager.getInstance().isCameraEnabled());

        // Nút Đổi nguồn camera (Webcam / Virtual Avatar / Screen Share)
        btnSwitchSource = createRoundButton();
        btnSwitchSource.setGraphic(createSwitchGraphic());
        Tooltip.install(btnSwitchSource, new Tooltip("Đổi nguồn Camera / Chia sẻ màn hình"));
        btnSwitchSource.setOnAction(e -> showSourceMenu());

        // Nút Gác máy
        btnEnd = createRoundButton();
        btnEnd.setGraphic(createPhoneGraphic());
        btnEnd.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 50; -fx-padding: 0;");
        btnEnd.setOnMouseEntered(e -> btnEnd.setStyle("-fx-background-color: #dc2626; -fx-background-radius: 50; -fx-padding: 0;"));
        btnEnd.setOnMouseExited(e -> btnEnd.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 50; -fx-padding: 0;"));
        btnEnd.setOnAction(e -> hangUp());
        Tooltip.install(btnEnd, new Tooltip("Kết thúc cuộc gọi"));

        controlBar.getChildren().addAll(btnMute, btnToggleCam, btnSwitchSource, btnEnd);
        StackPane.setAlignment(controlBar, Pos.BOTTOM_CENTER);
        StackPane.setMargin(controlBar, new Insets(0, 0, 24, 0));

        root.getChildren().addAll(remoteVideoView, remotePlaceholder, topBar, pipBox, controlBar);

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsElapsed++;
            int m = secondsElapsed / 60;
            int s = secondsElapsed % 60;
            lblDuration.setText(String.format("%02d:%02d", m, s));
        }));
        timer.setCycleCount(Timeline.INDEFINITE);

        stage.setOnCloseRequest(e -> hangUp());

        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    public void show() {
        stage.show();
    }

    public void setConnected() {
        this.isConnected = true;
        Platform.runLater(() -> {
            lblStatus.setText("Đang truyền phát video...");
            lblDuration.setVisible(true);
            timer.play();
        });
    }

    /**
     * Cập nhật khung hình nhận được từ đối phương.
     */
    public void onRemoteFrameReceived(byte[] jpegBytes) {
        if (jpegBytes == null || jpegBytes.length == 0) return;
        Platform.runLater(() -> {
            try {
                Image img = new Image(new ByteArrayInputStream(jpegBytes));
                if (!img.isError() && img.getWidth() > 0) {
                    remoteVideoView.setImage(img);
                    remotePlaceholder.setVisible(false);
                }
            } catch (Exception ignored) {}
        });
    }

    /**
     * Cập nhật khung hình xem trước cục bộ (Self-view).
     */
    public void onLocalFrameReceived(byte[] jpegBytes) {
        if (jpegBytes == null || jpegBytes.length == 0) return;
        Platform.runLater(() -> {
            try {
                Image img = new Image(new ByteArrayInputStream(jpegBytes));
                if (!img.isError() && img.getWidth() > 0) {
                    localVideoView.setImage(img);
                }
            } catch (Exception ignored) {}
        });
    }

    private Button createRoundButton() {
        Button b = new Button();
        b.setPrefSize(46, 46);
        b.setMinSize(46, 46);
        b.setMaxSize(46, 46);
        b.setCursor(javafx.scene.Cursor.HAND);
        b.setStyle("-fx-background-color: #334155; -fx-background-radius: 50; -fx-padding: 0;");
        return b;
    }

    private void toggleMute() {
        boolean nowMuted = !VoiceManager.getInstance().isMuted();
        VoiceManager.getInstance().setMuted(nowMuted);
        updateMuteButtonState(nowMuted);
    }

    private void updateMuteButtonState(boolean isMuted) {
        btnMute.setGraphic(createMicGraphic(isMuted));
        Tooltip.install(btnMute, new Tooltip(isMuted ? "Bật micro" : "Tắt micro"));
        String bg = isMuted ? "#7f1d1d" : "#334155";
        btnMute.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 50; -fx-padding: 0;");
    }

    private void toggleCamera() {
        boolean nowEnabled = !VideoManager.getInstance().isCameraEnabled();
        VideoManager.getInstance().setCameraEnabled(nowEnabled);
        updateCamButtonState(nowEnabled);
    }

    private void updateCamButtonState(boolean isEnabled) {
        btnToggleCam.setGraphic(createVideoGraphic(!isEnabled));
        Tooltip.install(btnToggleCam, new Tooltip(isEnabled ? "Tắt camera" : "Bật camera"));
        String bg = isEnabled ? "#334155" : "#7f1d1d";
        btnToggleCam.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 50; -fx-padding: 0;");
    }

    private void showSourceMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem itemWebcam = new MenuItem("📷 Webcam vật lý");
        itemWebcam.setOnAction(e -> VideoManager.getInstance().setVideoSource(VideoManager.VideoSource.WEBCAM));

        MenuItem itemAvatar = new MenuItem("✨ Camera ảo / Avatar mô phỏng");
        itemAvatar.setOnAction(e -> VideoManager.getInstance().setVideoSource(VideoManager.VideoSource.VIRTUAL_AVATAR));

        MenuItem itemScreen = new MenuItem("🖥 Chia sẻ màn hình trực tiếp");
        itemScreen.setOnAction(e -> VideoManager.getInstance().setVideoSource(VideoManager.VideoSource.SCREEN_SHARE));

        menu.getItems().addAll(itemWebcam, itemAvatar, itemScreen);
        menu.show(btnSwitchSource, javafx.geometry.Side.TOP, 0, -10);
    }

    private Node createMicGraphic(boolean isMuted) {
        SVGPath mic = new SVGPath();
        mic.setContent(SVG_MIC);
        mic.setFill(Color.WHITE);
        mic.setScaleX(1.1);
        mic.setScaleY(1.1);
        if (!isMuted) return mic;

        Group g = new Group();
        Line slash = new Line(0, 0, 13, 18);
        slash.setStroke(Color.web("#ef4444"));
        slash.setStrokeWidth(2.2);
        slash.setStrokeLineCap(StrokeLineCap.ROUND);
        g.getChildren().addAll(mic, slash);
        return g;
    }

    private Node createVideoGraphic(boolean isOff) {
        SVGPath cam = new SVGPath();
        cam.setContent(SVG_VIDEO);
        cam.setFill(Color.TRANSPARENT);
        cam.setStroke(Color.WHITE);
        cam.setStrokeWidth(2.0);
        cam.setStrokeLineCap(StrokeLineCap.ROUND);
        cam.setStrokeLineJoin(StrokeLineJoin.ROUND);
        cam.setScaleX(0.9);
        cam.setScaleY(0.9);
        if (!isOff) return cam;

        Group g = new Group();
        Line slash = new Line(2, 2, 22, 20);
        slash.setStroke(Color.web("#ef4444"));
        slash.setStrokeWidth(2.2);
        slash.setStrokeLineCap(StrokeLineCap.ROUND);
        g.getChildren().addAll(cam, slash);
        return g;
    }

    private Node createSwitchGraphic() {
        SVGPath sw = new SVGPath();
        sw.setContent(SVG_SWITCH);
        sw.setFill(Color.TRANSPARENT);
        sw.setStroke(Color.WHITE);
        sw.setStrokeWidth(2.0);
        sw.setStrokeLineCap(StrokeLineCap.ROUND);
        sw.setStrokeLineJoin(StrokeLineJoin.ROUND);
        sw.setScaleX(0.85);
        sw.setScaleY(0.85);
        return sw;
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
        phone.setScaleX(0.9);
        phone.setScaleY(0.9);
        return phone;
    }

    public void hangUp() {
        if (timer != null) timer.stop();
        VoiceManager.getInstance().stopCallAudio();
        VideoManager.getInstance().stopVideoCapture();
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
        VideoManager.getInstance().stopVideoCapture();
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
