package com.cmp180.livechat.client.staff.ui;

import com.cmp180.livechat.client.ui.ThemeManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;

/**
 * Hộp thoại Quản lý tin nhắn nhanh (Canned Replies) cá nhân của Nhân viên.
 * Cho phép nhân viên CSKH Thêm, Sửa, Xoá các tin nhắn nhanh cá nhân.
 */
public class QuickRepliesDialog {

    public static void show(Window owner, String staffKey, Runnable onUpdatedCallback) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setTitle("Quản lý tin nhắn nhanh cá nhân");
        stage.setResizable(true);

        boolean dark = ThemeManager.isDarkMode();
        String brand = ThemeManager.getPrimaryColor();

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setPrefWidth(560);
        root.setPrefHeight(500);

        String rootBg = dark ? "#18191a" : "#f8fafc";
        String cardBg = dark ? "#242526" : "#ffffff";
        String borderColor = dark ? "#3a3b3c" : "#e4e6eb";
        String titleColor = dark ? "#f1f5f9" : "#0f172a";
        String subColor = dark ? "#94a3b8" : "#64748b";
        String textColor = dark ? "#e4e6eb" : "#1c1e21";
        String mutedColor = dark ? "#8a8d91" : "#65676b";

        root.setStyle("-fx-background-color: " + rootBg + ";");

        // Header
        Label lblTitle = new Label("Quản lý tin nhắn nhanh cá nhân");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblTitle.setStyle("-fx-text-fill: " + titleColor + ";");

        Label lblSub = new Label("Thêm, chỉnh sửa hoặc xoá các mẫu câu trả lời nhanh để gửi cho khách hàng.");
        lblSub.setFont(Font.font("Segoe UI", 12.5));
        lblSub.setStyle("-fx-text-fill: " + subColor + ";");

        // Danh sách tin nhắn nhanh
        List<String> rawReplies = StaffQuickRepliesManager.getQuickReplies(staffKey);
        ObservableList<String> items = FXCollections.observableArrayList(rawReplies);

        ListView<String> listView = new ListView<>(items);
        listView.setPrefHeight(190);
        VBox.setVgrow(listView, Priority.ALWAYS);
        listView.setStyle("-fx-control-inner-background: " + cardBg + "; "
                + "-fx-background-color: " + cardBg + "; "
                + "-fx-background-radius: 8; -fx-border-radius: 8; "
                + "-fx-border-color: " + borderColor + "; -fx-padding: 4;");

        Label placeholder = new Label("Chưa có tin nhắn nhanh nào. Nhập câu trả lời mẫu bên dưới và bấm 'Thêm mới'!");
        placeholder.setStyle("-fx-text-fill: " + mutedColor + "; -fx-font-style: italic; -fx-font-size: 13px;");
        listView.setPlaceholder(placeholder);

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText((getIndex() + 1) + ". " + item);
                    setWrapText(true);
                    setFont(Font.font("Segoe UI", 13));
                    applyVisual();
                }
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                if (!isEmpty() && getItem() != null) {
                    applyVisual();
                }
            }

            private void applyVisual() {
                if (isSelected()) {
                    String selBg = dark ? "#3a3b3c" : "#e0f2fe";
                    String selText = dark ? "#ffffff" : "#0284c7";
                    setStyle("-fx-background-color: " + selBg + "; -fx-text-fill: " + selText + "; -fx-padding: 8 12; -fx-background-radius: 6; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-background-color: " + cardBg + "; -fx-text-fill: " + textColor + "; -fx-padding: 8 12; -fx-background-radius: 6;");
                }
            }
        });

        // Khu vực soạn thảo / chỉnh sửa
        Label lblInputHeader = new Label("Nội dung tin nhắn nhanh:");
        lblInputHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12.5));
        lblInputHeader.setStyle("-fx-text-fill: " + textColor + ";");

        TextArea txtInput = new TextArea();
        txtInput.setPromptText("Nhập câu trả lời mẫu tại đây (ví dụ: Chào bạn, mình có thể giúp gì cho bạn?)...");
        txtInput.setPrefRowCount(3);
        txtInput.setWrapText(true);
        txtInput.getStyleClass().add("clean-text-area");
        txtInput.setStyle("-fx-control-inner-background: " + cardBg + "; "
                + "-fx-background-color: " + cardBg + "; "
                + "-fx-text-fill: " + (dark ? "#f8fafc" : "#0f172a") + "; "
                + "-fx-prompt-text-fill: " + (dark ? "#94a3b8" : "#94a3b8") + "; "
                + "-fx-border-color: " + borderColor + "; -fx-border-radius: 8; -fx-background-radius: 8; "
                + "-fx-padding: 8 10; -fx-font-size: 13px;");

        // Khi click vào item trong list -> tự điền vào txtInput
        listView.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            int idx = newVal != null ? newVal.intValue() : -1;
            if (idx >= 0 && idx < items.size()) {
                txtInput.setText(items.get(idx));
            }
        });

        // Hàng nút thao tác CRUD (Loại bỏ toàn bộ emoji, thiết kế giao diện thanh lịch)
        HBox btnRow = new HBox(8);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        Button btnAdd = new Button("Thêm mới");
        btnAdd.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12.5));
        btnAdd.setStyle("-fx-background-color: " + brand + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 7 16;");

        Button btnEdit = new Button("Sửa");
        btnEdit.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12.5));
        btnEdit.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 7 16;");

        Button btnDelete = new Button("Xoá");
        btnDelete.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12.5));
        btnDelete.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 7 16;");

        Button btnClear = new Button("Làm mới ô nhập");
        btnClear.setFont(Font.font("Segoe UI", 12.5));
        btnClear.setStyle("-fx-background-color: transparent; -fx-text-fill: " + subColor + "; -fx-border-color: " + borderColor + "; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 12;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnClose = new Button("Đóng");
        btnClose.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12.5));
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: " + textColor + "; -fx-border-color: " + borderColor + "; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 18;");
        btnClose.setOnAction(e -> stage.close());

        btnRow.getChildren().addAll(btnAdd, btnEdit, btnDelete, btnClear, spacer, btnClose);

        // Sự kiện Thêm mới
        btnAdd.setOnAction(e -> {
            String text = txtInput.getText() != null ? txtInput.getText().trim() : "";
            if (text.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Chưa nhập nội dung", "Vui lòng nhập nội dung tin nhắn nhanh trước khi bấm Thêm!");
                return;
            }
            StaffQuickRepliesManager.addQuickReply(staffKey, text);
            items.setAll(StaffQuickRepliesManager.getQuickReplies(staffKey));
            txtInput.clear();
            listView.getSelectionModel().select(items.size() - 1);
            if (onUpdatedCallback != null) onUpdatedCallback.run();
        });

        // Sự kiện Sửa
        btnEdit.setOnAction(e -> {
            int idx = listView.getSelectionModel().getSelectedIndex();
            if (idx < 0) {
                showAlert(Alert.AlertType.WARNING, "Chưa chọn tin nhắn", "Vui lòng chọn một tin nhắn trong danh sách ở trên để sửa!");
                return;
            }
            String text = txtInput.getText() != null ? txtInput.getText().trim() : "";
            if (text.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Nội dung rỗng", "Vui lòng không để trống nội dung tin nhắn!");
                return;
            }
            StaffQuickRepliesManager.updateQuickReply(staffKey, idx, text);
            items.setAll(StaffQuickRepliesManager.getQuickReplies(staffKey));
            listView.getSelectionModel().select(idx);
            if (onUpdatedCallback != null) onUpdatedCallback.run();
        });

        // Sự kiện Xóa
        btnDelete.setOnAction(e -> {
            int idx = listView.getSelectionModel().getSelectedIndex();
            if (idx < 0) {
                showAlert(Alert.AlertType.WARNING, "Chưa chọn tin nhắn", "Vui lòng chọn một tin nhắn trong danh sách ở trên để xoá!");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.initOwner(stage);
            confirm.setTitle("Xác nhận xoá");
            confirm.setHeaderText("Xoá tin nhắn nhanh");
            confirm.setContentText("Bạn có chắc chắn muốn xoá tin nhắn nhanh này không?");
            ThemeManager.applyTheme(confirm);
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    StaffQuickRepliesManager.deleteQuickReply(staffKey, idx);
                    items.setAll(StaffQuickRepliesManager.getQuickReplies(staffKey));
                    txtInput.clear();
                    if (onUpdatedCallback != null) onUpdatedCallback.run();
                }
            });
        });

        btnClear.setOnAction(e -> {
            txtInput.clear();
            listView.getSelectionModel().clearSelection();
        });

        root.getChildren().addAll(lblTitle, lblSub, listView, lblInputHeader, txtInput, btnRow);

        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(QuickRepliesDialog.class.getResource("menu_style.css").toExternalForm());
        } catch (Exception ignored) {}
        ThemeManager.applyTheme(scene);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private static void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        ThemeManager.applyTheme(alert);
        alert.showAndWait();
    }
}
