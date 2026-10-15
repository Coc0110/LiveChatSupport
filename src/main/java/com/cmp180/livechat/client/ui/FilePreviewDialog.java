package com.cmp180.livechat.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.ByteArrayInputStream;
import java.util.Optional;

public class FilePreviewDialog {

    public static class FilePreviewResult {
        public final String fileName;
        public final long fileSize;
        public final byte[] fileData;
        public final boolean isImage;
        public final String caption;

        public FilePreviewResult(String fileName, long fileSize, byte[] fileData, boolean isImage, String caption) {
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.fileData = fileData;
            this.isImage = isImage;
            this.caption = caption;
        }
    }

    public static Optional<FilePreviewResult> show(Window owner, String fileName, byte[] fileData, boolean isImage, String initialCaption) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setTitle(isImage ? "Gửi hình ảnh" : "Gửi tệp đính kèm");
        stage.setResizable(false);

        boolean dark = ThemeManager.isDarkMode();
        String brand = ThemeManager.getPrimaryColor();

        VBox root = new VBox(14);
        root.setPadding(new Insets(20, 24, 20, 24));
        root.setPrefWidth(460);
        root.setStyle("-fx-background-color: " + (dark ? "#1e1e1e" : "#ffffff") + ";");

        // Header Title
        Label lblTitle = new Label(isImage ? "Xem trước hình ảnh" : "Xem trước tệp đính kèm");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblTitle.setStyle("-fx-text-fill: " + (dark ? "#e4e6eb" : "#050505") + ";");

        // Preview Area
        VBox previewCard = new VBox(10);
        previewCard.setAlignment(Pos.CENTER);
        previewCard.setPadding(new Insets(14));
        previewCard.setStyle("-fx-background-color: " + (dark ? "#25272a" : "#f0f2f5") 
                + "; -fx-background-radius: 12; -fx-border-color: " + (dark ? "#3a3b3c" : "#d8dadf") + "; -fx-border-radius: 12; -fx-border-width: 1;");

        if (isImage) {
            try {
                Image img = new Image(new ByteArrayInputStream(fileData));
                if (img.isError() || img.getWidth() <= 0) {
                    try {
                        java.awt.image.BufferedImage bimg = javax.imageio.ImageIO.read(new ByteArrayInputStream(fileData));
                        if (bimg != null) {
                            img = javafx.embed.swing.SwingFXUtils.toFXImage(bimg, null);
                        }
                    } catch (Exception ignored) {}
                }

                if (img != null && !img.isError() && img.getWidth() > 0 && img.getHeight() > 0) {
                    double maxW = 380;
                    double maxH = 230;
                    double scale = Math.min(maxW / img.getWidth(), maxH / img.getHeight());
                    if (scale > 1.0) scale = 1.0;
                    double w = Math.max(1, Math.round(img.getWidth() * scale));
                    double h = Math.max(1, Math.round(img.getHeight() * scale));

                    Rectangle imgView = new Rectangle(w, h);
                    imgView.setArcWidth(14);
                    imgView.setArcHeight(14);
                    imgView.setFill(new javafx.scene.paint.ImagePattern(img));

                    previewCard.getChildren().add(imgView);
                } else {
                    Label lblErr = new Label("Không thể tải hình ảnh xem trước");
                    lblErr.setStyle("-fx-text-fill: " + (dark ? "#b0b3b8" : "#65676b") + "; -fx-font-style: italic;");
                    previewCard.getChildren().add(lblErr);
                }
            } catch (Exception e) {
                Label lblErr = new Label("Không thể tải hình ảnh xem trước: " + e.getMessage());
                lblErr.setStyle("-fx-text-fill: " + (dark ? "#b0b3b8" : "#65676b") + "; -fx-font-style: italic;");
                previewCard.getChildren().add(lblErr);
            }
        } else {
            // File Icon
            SVGPath fileIcon = new SVGPath();
            fileIcon.setContent("M14 2H6C4.9 2 4 2.9 4 4V20C4 21.1 4.9 22 6 22H18C19.1 22 20 21.1 20 20V8L14 2ZM18 20H6V4H13V9H18V20ZM12 18L7 13H10V10H14V13H17L12 18Z");
            fileIcon.setFill(Color.web(brand));
            fileIcon.setScaleX(2.0);
            fileIcon.setScaleY(2.0);

            StackPane iconBox = new StackPane(fileIcon);
            iconBox.setPrefSize(70, 70);
            iconBox.setStyle("-fx-background-color: " + ThemeManager.getLightTint(brand) + "; -fx-background-radius: 14;");

            Label lblFileName = new Label(fileName);
            lblFileName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            lblFileName.setStyle("-fx-text-fill: " + (dark ? "#ffffff" : "#1c1e21") + ";");
            lblFileName.setMaxWidth(380);
            lblFileName.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);

            previewCard.getChildren().addAll(iconBox, lblFileName);
        }

        // Info Meta label
        String formattedSize = formatSize(fileData.length);
        Label lblMeta = new Label(fileName + " (" + formattedSize + ")");
        lblMeta.setFont(Font.font("Segoe UI", 12));
        lblMeta.setStyle("-fx-text-fill: " + (dark ? "#b0b3b8" : "#65676b") + ";");
        lblMeta.setMaxWidth(400);
        lblMeta.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        previewCard.getChildren().add(lblMeta);

        // Caption Input Field
        VBox captionBox = new VBox(6);
        Label lblCaptionPrompt = new Label("Chú thích kèm theo:");
        lblCaptionPrompt.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblCaptionPrompt.setStyle("-fx-text-fill: " + (dark ? "#b0b3b8" : "#65676b") + ";");

        TextField txtCaption = new TextField(initialCaption != null ? initialCaption : "");
        txtCaption.setPromptText("Nhập nội dung chú thích (tùy chọn)...");
        txtCaption.setPrefHeight(40);
        txtCaption.setStyle("-fx-background-color: " + (dark ? "#2d3034" : "#ffffff") 
                + "; -fx-text-fill: " + (dark ? "#e4e6eb" : "#050505") 
                + "; -fx-border-color: " + (dark ? "#484b51" : "#c4c9d0") 
                + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 0 12; -fx-font-size: 13.5px;");
        captionBox.getChildren().addAll(lblCaptionPrompt, txtCaption);

        // Action Buttons Row
        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.setPadding(new Insets(6, 0, 0, 0));

        Button btnCancel = new Button("Hủy");
        btnCancel.setPrefHeight(36);
        btnCancel.setCursor(javafx.scene.Cursor.HAND);
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: " + (dark ? "#b0b3b8" : "#65676b") 
                + "; -fx-border-color: " + (dark ? "#484b51" : "#c4c9d0") + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 6 16;");
        btnCancel.setOnAction(e -> stage.close());

        Button btnSend = new Button("Gửi ngay");
        btnSend.setPrefHeight(36);
        btnSend.setDefaultButton(true);
        btnSend.setCursor(javafx.scene.Cursor.HAND);
        btnSend.setStyle("-fx-background-color: " + brand + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 20;");

        final FilePreviewResult[] result = new FilePreviewResult[1];
        Runnable doSend = () -> {
            String caption = txtCaption.getText().trim();
            result[0] = new FilePreviewResult(fileName, fileData.length, fileData, isImage, caption);
            stage.close();
        };

        btnSend.setOnAction(e -> doSend.run());
        txtCaption.setOnAction(e -> doSend.run());

        btnBox.getChildren().addAll(btnCancel, btnSend);

        root.getChildren().addAll(lblTitle, previewCard, captionBox, btnBox);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();

        return Optional.ofNullable(result[0]);
    }

    private static String formatSize(long size) {
        if (size <= 0) return "0 B";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        return String.format("%.1f MB", size / (1024.0 * 1024.0));
    }
}
