package com.cmp180.livechat.client.staff.ui;

import com.cmp180.livechat.client.staff.model.OnlineStaffItem;
import com.cmp180.livechat.client.staff.network.StaffNetworkListener;
import com.cmp180.livechat.client.staff.network.StaffNetworkService;
import com.cmp180.livechat.client.ui.ThemeManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;

public class TransferSessionDialog {

    public static void show(Window owner, String customerId, String customerDisplayName, StaffNetworkService networkService) {
        if (customerId == null || networkService == null || !networkService.isConnected()) {
            return;
        }

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setTitle("Chuyển tiếp phiên tư vấn");
        stage.setResizable(false);

        boolean dark = ThemeManager.isDarkMode();
        String brand = ThemeManager.getPrimaryColor();

        VBox root = new VBox(14);
        root.setPadding(new Insets(20, 24, 20, 24));
        root.setPrefWidth(500);
        root.setStyle("-fx-background-color: " + (dark ? "#1e1e1e" : "#ffffff") + ";");

        // Header Title
        Label lblTitle = new Label("Chuyển tiếp phiên tư vấn");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblTitle.setStyle("-fx-text-fill: " + (dark ? "#e4e6eb" : "#050505") + ";");

        Label lblSubtitle = new Label("Khách hàng: " + customerDisplayName);
        lblSubtitle.setFont(Font.font("Segoe UI", 13));
        lblSubtitle.setStyle("-fx-text-fill: " + (dark ? "#b0b3b8" : "#65676b") + ";");

        VBox headerBox = new VBox(3, lblTitle, lblSubtitle);

        // Staff List Section
        Label lblSelectStaff = new Label("Chọn nhân viên tiếp nhận:");
        lblSelectStaff.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblSelectStaff.setStyle("-fx-text-fill: " + (dark ? "#e4e6eb" : "#1c1e21") + ";");

        ListView<OnlineStaffItem> listView = new ListView<>();
        listView.setPrefHeight(180);
        listView.setPlaceholder(new Label("Đang tải danh sách nhân viên trực tuyến..."));
        listView.setStyle("-fx-background-color: " + (dark ? "#25272a" : "#f0f2f5") 
                + "; -fx-border-color: " + (dark ? "#3a3b3c" : "#d8dadf") + "; -fx-border-radius: 8; -fx-background-radius: 8;");

        listView.setCellFactory(lv -> new ListCell<OnlineStaffItem>() {
            @Override
            protected void updateItem(OnlineStaffItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(6, 8, 6, 8));

                    Circle avatar = new Circle(14, Color.web(brand));
                    Label avatarText = new Label(item.getFullName().substring(0, 1).toUpperCase());
                    avatarText.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
                    StackPane avatarPane = new StackPane(avatar, avatarText);

                    VBox infoBox = new VBox(2);
                    Label nameLbl = new Label(item.getFullName());
                    nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + (dark ? "#e4e6eb" : "#1c1e21") + ";");

                    Label skillBadge = new Label(" " + item.getPrimarySkill() + " ");
                    skillBadge.setStyle("-fx-background-color: " + (dark ? "#333e4f" : "#e0e7ff") 
                            + "; -fx-text-fill: " + (dark ? "#93c5fd" : "#3b82f6") 
                            + "; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");

                    infoBox.getChildren().addAll(nameLbl, skillBadge);
                    HBox.setHgrow(infoBox, Priority.ALWAYS);

                    Label statusLbl = new Label();
                    if (item.isBusy()) {
                        statusLbl.setText("3/3 phiên (Đã đầy)");
                        statusLbl.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-font-weight: bold;");
                    } else {
                        statusLbl.setText(item.getActiveSessions() + "/3 phiên (" + (item.getActiveSessions() == 0 ? "Rảnh" : "Đang chat") + ")");
                        statusLbl.setStyle("-fx-text-fill: #10b981; -fx-font-size: 12px; -fx-font-weight: bold;");
                    }

                    row.getChildren().addAll(avatarPane, infoBox, statusLbl);
                    setGraphic(row);
                    setText(null);

                    if (item.isBusy()) {
                        setDisable(true);
                        setStyle("-fx-opacity: 0.5; -fx-background-color: transparent;");
                    } else {
                        setDisable(false);
                        setStyle("-fx-background-color: transparent;");
                    }
                }
            }
        });

        // Note Section
        Label lblNote = new Label("Ghi chú bàn giao cho nhân viên mới (không bắt buộc):");
        lblNote.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblNote.setStyle("-fx-text-fill: " + (dark ? "#e4e6eb" : "#1c1e21") + ";");

        TextArea txtNote = new TextArea();
        txtNote.setPromptText("Ví dụ: Khách cần hỗ trợ kỹ thuật cài driver, đã tiếp nhận thông tin đơn hàng...");
        txtNote.setPrefRowCount(3);
        txtNote.setWrapText(true);
        txtNote.setStyle("-fx-control-inner-background: " + (dark ? "#25272a" : "#f0f2f5") 
                + "; -fx-text-fill: " + (dark ? "#e4e6eb" : "#1c1e21") 
                + "; -fx-border-color: " + (dark ? "#3a3b3c" : "#d8dadf") + "; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Action Buttons
        Button btnCancel = new Button("Hủy");
        btnCancel.setStyle("-fx-background-color: " + (dark ? "#3a3b3c" : "#e4e6eb") 
                + "; -fx-text-fill: " + (dark ? "#e4e6eb" : "#1c1e21") 
                + "; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 18; -fx-background-radius: 8; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> stage.close());

        Button btnConfirm = new Button("Xác nhận chuyển");
        btnConfirm.setDisable(true);
        btnConfirm.setStyle("-fx-background-color: " + brand 
                + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 18; -fx-background-radius: 8; -fx-cursor: hand;");

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            boolean valid = newV != null && !newV.isBusy();
            btnConfirm.setDisable(!valid);
        });

        btnConfirm.setOnAction(e -> {
            OnlineStaffItem selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null || selected.isBusy()) return;

            btnConfirm.setDisable(true);
            btnCancel.setDisable(true);

            String note = txtNote.getText() != null ? txtNote.getText().trim() : "";
            networkService.transferSession(customerId, selected.getStaffId(), note);
            stage.close();
        });

        HBox btnBox = new HBox(12, btnCancel, btnConfirm);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.setPadding(new Insets(10, 0, 0, 0));

        root.getChildren().addAll(headerBox, lblSelectStaff, listView, lblNote, txtNote, btnBox);

        Scene scene = new Scene(root);
        ThemeManager.applyTheme(scene);
        stage.setScene(scene);

        // Network Listener for online staff list
        StaffNetworkListener listener = new StaffNetworkListener() {
            @Override
            public void onLoginSuccess() {}
            @Override
            public void onQueueUpdated(List<com.cmp180.livechat.client.staff.model.CustomerItem> waitingCustomers) {}
            @Override
            public void onPairedWithCustomer(String customerName, String customerId) {}
            @Override
            public void onMessageReceived(String senderName, String senderId, String message) {}
            @Override
            public void onSessionEnded(String reason) {}
            @Override
            public void onError(String errorMessage) {}

            @Override
            public void onOnlineStaffListReceived(List<OnlineStaffItem> staffList) {
                Platform.runLater(() -> {
                    listView.getItems().setAll(staffList);
                    if (staffList.isEmpty()) {
                        listView.setPlaceholder(new Label("Không có nhân viên CSKH nào khác đang trực tuyến."));
                    }
                });
            }
        };

        networkService.addListener(listener);
        stage.setOnHidden(ev -> networkService.removeListener(listener));

        // Request initial list from server
        networkService.requestOnlineStaffList();

        stage.show();
    }
}
