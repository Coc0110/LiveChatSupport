package com.cmp180.livechat.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;

public class ImageViewerDialog {

    public static void show(Window owner, String fileName, byte[] imageData) {
        if (imageData == null || imageData.length == 0) return;

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setTitle(fileName != null ? fileName : "Xem hình ảnh");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #121212;");

        // Top Bar: Filename and Close
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(12, 16, 12, 16));
        topBar.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");

        Label lblName = new Label(fileName != null ? fileName : "Hình ảnh");
        lblName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblName.setStyle("-fx-text-fill: #ffffff;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnSave = new Button("💾 Lưu về máy");
        btnSave.setCursor(javafx.scene.Cursor.HAND);
        btnSave.setStyle("-fx-background-color: rgba(255, 255, 255, 0.15); -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 12;");
        btnSave.setOnAction(e -> saveImage(stage, fileName, imageData));

        Button btnClose = new Button("✕");
        btnClose.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnClose.setPrefSize(32, 32);
        btnClose.setCursor(javafx.scene.Cursor.HAND);
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #b0b3b8; -fx-background-radius: 16;");
        btnClose.setOnAction(e -> stage.close());

        topBar.getChildren().addAll(lblName, spacer, btnSave, btnClose);
        root.setTop(topBar);

        // Center Image
        Image img = new Image(new ByteArrayInputStream(imageData));
        ImageView iv = new ImageView(img);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);

        double screenW = 800;
        double screenH = 600;
        iv.setFitWidth(screenW);
        iv.setFitHeight(screenH);

        StackPane centerPane = new StackPane(iv);
        centerPane.setPadding(new Insets(10));
        root.setCenter(centerPane);

        Scene scene = new Scene(root, Math.min(Math.max(img.getWidth() + 40, 500), 900), Math.min(Math.max(img.getHeight() + 90, 400), 720));
        stage.setScene(scene);
        stage.show();
    }

    private static void saveImage(Window owner, String defaultName, byte[] data) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Lưu hình ảnh về máy");
        chooser.setInitialFileName(defaultName != null ? defaultName : "image.png");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Hình ảnh (*.png, *.jpg)", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Tất cả tệp (*.*)", "*.*")
        );
        File dest = chooser.showSaveDialog(owner);
        if (dest != null) {
            try {
                Files.write(dest.toPath(), data);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.initOwner(owner);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Đã lưu hình ảnh về: " + dest.getAbsolutePath());
                alert.showAndWait();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(owner);
                alert.setTitle("Lỗi");
                alert.setHeaderText("Không thể lưu hình ảnh");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        }
    }
}
