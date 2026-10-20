package com.cmp180.livechat.client.ui;

import com.cmp180.livechat.common.VoiceManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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
 * Hộp thoại hiển thị khi có cuộc gọi đến (Voice Call hoặc Video Call).
 */
public class IncomingCallDialog {

    public interface CallResponseHandler {
        void onResponse(boolean accepted);
    }

    private static Stage activeStage;

    public static void show(Window owner, String callerName, CallResponseHandler handler) {
        show(owner, callerName, false, handler);
    }

    public static void show(Window owner, String callerName, boolean isVideoCall, CallResponseHandler handler) {
        close();

        // Phát chuông gọi đến
        VoiceManager.getInstance().startRingtone();

        Stage stage = new Stage();
        activeStage = stage;
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setTitle(isVideoCall ? "Cuộc gọi video đến" : "Cuộc gọi thoại đến");
        stage.setResizable(false);

        boolean dark = ThemeManager.isDarkMode();

        VBox root = new VBox(18);
        root.setPadding(new Insets(24, 28, 24, 28));
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(360);
        root.setStyle("-fx-background-color: " + (dark ? "#1e1e1e" : "#ffffff") + "; -fx-background-radius: 12;");

        // Icon Avatar
        Circle avatarCircle = new Circle(32, Color.web(isVideoCall ? "#2563eb" : ThemeManager.getPrimaryColor()));
        SVGPath iconAvatar = new SVGPath();
        if (isVideoCall) {
            iconAvatar.setContent("M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z");
            iconAvatar.setScaleX(1.1);
            iconAvatar.setScaleY(1.1);
        } else {
            iconAvatar.setContent("M7.64551 3C8.46332 3 9.19873 3.4979 9.50246 4.25722L10.5577 6.89309C10.8305 7.57516 10.7066 8.35195 10.2363 8.9163L9.66895 9.59668C9.00673 10.3913 9.06079 11.5596 9.79222 12.2911L11.7082 14.2088C12.4396 14.9402 13.6089 14.9929 14.4035 14.3307L15.0839 13.7632C15.6483 13.2929 16.4249 13.1701 17.1069 13.4429L19.7434 14.4971C20.5027 14.8009 21.0005 15.5361 21.0005 16.354L21 19.1052C21 20.1516 20.1519 21 19.1055 21C10.2108 21 3 13.7892 3 4.89453C3 3.8481 3.8483 3 4.89474 3H7.64551Z");
            iconAvatar.setScaleX(1.1);
            iconAvatar.setScaleY(1.1);
        }
        iconAvatar.setFill(Color.TRANSPARENT);
        iconAvatar.setStroke(Color.WHITE);
        iconAvatar.setStrokeWidth(2.0);
        iconAvatar.setStrokeLineCap(StrokeLineCap.ROUND);
        iconAvatar.setStrokeLineJoin(StrokeLineJoin.ROUND);

        StackPane avatarPane = new StackPane(avatarCircle, iconAvatar);

        Label lblTitle = new Label(isVideoCall ? "Cuộc gọi video đến..." : "Cuộc gọi thoại đến...");
        lblTitle.setFont(Font.font("Segoe UI", 13));
        lblTitle.setStyle("-fx-text-fill: " + (dark ? "#94a3b8" : "#64748b") + ";");

        Label lblCaller = new Label(callerName != null ? callerName : "Người dùng");
        lblCaller.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblCaller.setStyle("-fx-text-fill: " + (dark ? "#f8fafc" : "#0f172a") + ";");

        HBox buttonBar = new HBox(44);
        buttonBar.setAlignment(Pos.CENTER);

        // 1. Nút Từ chối (Icon tròn màu đỏ)
        Button btnReject = new Button();
        btnReject.setPrefSize(52, 52);
        btnReject.setMinSize(52, 52);
        btnReject.setMaxSize(52, 52);
        SVGPath iconReject = new SVGPath();
        iconReject.setContent("M7.64551 3C8.46332 3 9.19873 3.4979 9.50246 4.25722L10.5577 6.89309C10.8305 7.57516 10.7066 8.35195 10.2363 8.9163L9.66895 9.59668C9.00673 10.3913 9.06079 11.5596 9.79222 12.2911L11.7082 14.2088C12.4396 14.9402 13.6089 14.9929 14.4035 14.3307L15.0839 13.7632C15.6483 13.2929 16.4249 13.1701 17.1069 13.4429L19.7434 14.4971C20.5027 14.8009 21.0005 15.5361 21.0005 16.354L21 19.1052C21 20.1516 20.1519 21 19.1055 21C10.2108 21 3 13.7892 3 4.89453C3 3.8481 3.8483 3 4.89474 3H7.64551Z");
        iconReject.setFill(Color.TRANSPARENT);
        iconReject.setStroke(Color.WHITE);
        iconReject.setStrokeWidth(2.0);
        iconReject.setStrokeLineCap(StrokeLineCap.ROUND);
        iconReject.setStrokeLineJoin(StrokeLineJoin.ROUND);
        iconReject.setRotate(135);
        iconReject.setScaleX(1.0);
        iconReject.setScaleY(1.0);
        btnReject.setGraphic(iconReject);
        btnReject.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 50; -fx-cursor: hand; -fx-padding: 0; -fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.45), 10, 0, 0, 3);");
        btnReject.setOnMouseEntered(e -> btnReject.setStyle("-fx-background-color: #dc2626; -fx-background-radius: 50; -fx-cursor: hand; -fx-padding: 0; -fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.55), 12, 0, 0, 4);"));
        btnReject.setOnMouseExited(e -> btnReject.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 50; -fx-cursor: hand; -fx-padding: 0; -fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.45), 10, 0, 0, 3);"));
        Tooltip.install(btnReject, new Tooltip("Từ chối cuộc gọi"));

        Label lblReject = new Label("Từ chối");
        lblReject.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblReject.setStyle("-fx-text-fill: #ef4444;");
        VBox boxReject = new VBox(6, btnReject, lblReject);
        boxReject.setAlignment(Pos.CENTER);

        // 2. Nút Đồng ý / Trả lời (Icon tròn màu xanh lá)
        Button btnAccept = new Button();
        btnAccept.setPrefSize(52, 52);
        btnAccept.setMinSize(52, 52);
        btnAccept.setMaxSize(52, 52);
        SVGPath iconAccept = new SVGPath();
        if (isVideoCall) {
            iconAccept.setContent("M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z");
            iconAccept.setScaleX(1.05);
            iconAccept.setScaleY(1.05);
        } else {
            iconAccept.setContent("M7.64551 3C8.46332 3 9.19873 3.4979 9.50246 4.25722L10.5577 6.89309C10.8305 7.57516 10.7066 8.35195 10.2363 8.9163L9.66895 9.59668C9.00673 10.3913 9.06079 11.5596 9.79222 12.2911L11.7082 14.2088C12.4396 14.9402 13.6089 14.9929 14.4035 14.3307L15.0839 13.7632C15.6483 13.2929 16.4249 13.1701 17.1069 13.4429L19.7434 14.4971C20.5027 14.8009 21.0005 15.5361 21.0005 16.354L21 19.1052C21 20.1516 20.1519 21 19.1055 21C10.2108 21 3 13.7892 3 4.89453C3 3.8481 3.8483 3 4.89474 3H7.64551Z");
            iconAccept.setScaleX(1.0);
            iconAccept.setScaleY(1.0);
        }
        iconAccept.setFill(Color.TRANSPARENT);
        iconAccept.setStroke(Color.WHITE);
        iconAccept.setStrokeWidth(2.0);
        iconAccept.setStrokeLineCap(StrokeLineCap.ROUND);
        iconAccept.setStrokeLineJoin(StrokeLineJoin.ROUND);
        btnAccept.setGraphic(iconAccept);
        btnAccept.setStyle("-fx-background-color: #10b981; -fx-background-radius: 50; -fx-cursor: hand; -fx-padding: 0; -fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.45), 10, 0, 0, 3);");
        btnAccept.setOnMouseEntered(e -> btnAccept.setStyle("-fx-background-color: #059669; -fx-background-radius: 50; -fx-cursor: hand; -fx-padding: 0; -fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.55), 12, 0, 0, 4);"));
        btnAccept.setOnMouseExited(e -> btnAccept.setStyle("-fx-background-color: #10b981; -fx-background-radius: 50; -fx-cursor: hand; -fx-padding: 0; -fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.45), 10, 0, 0, 3);"));
        Tooltip.install(btnAccept, new Tooltip(isVideoCall ? "Trả lời cuộc gọi video" : "Trả lời cuộc gọi thoại"));

        Label lblAccept = new Label("Trả lời");
        lblAccept.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblAccept.setStyle("-fx-text-fill: #10b981;");
        VBox boxAccept = new VBox(6, btnAccept, lblAccept);
        boxAccept.setAlignment(Pos.CENTER);

        buttonBar.getChildren().addAll(boxReject, boxAccept);

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
