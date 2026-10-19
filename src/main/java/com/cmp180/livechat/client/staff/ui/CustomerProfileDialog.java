package com.cmp180.livechat.client.staff.ui;

import com.cmp180.livechat.client.ui.ThemeManager;
import com.cmp180.livechat.server.DatabaseManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * Hộp thoại Hồ sơ khách hàng (Customer 360 Profile) dành cho nhân viên CSKH.
 * Cho phép xem và chỉnh sửa thông tin khách hàng (Họ tên, SĐT, Email, Hạng thành viên VIP, Ghi chú CSKH),
 * xem thống kê CSAT, danh sách toàn bộ các phiên hỗ trợ và xem lại chi tiết tin nhắn của từng phiên.
 */
public class CustomerProfileDialog {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public static void show(Window owner, String customerId, String customerDisplayName) {
        show(owner, customerId, customerDisplayName, null);
    }

    public static void show(Window owner, String customerId, String customerDisplayName, Consumer<String> onNameUpdatedCallback) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return;
        }

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setTitle("Hồ sơ Khách hàng - " + customerDisplayName);
        stage.setResizable(true);

        boolean dark = ThemeManager.isDarkMode();
        String brand = ThemeManager.getPrimaryColor();

        // 1. Tra cứu thông tin từ CSDL SQLite
        DatabaseManager.CustomerRecord customer = null;
        try {
            customer = DatabaseManager.getInstance().findCustomerByPhoneOrName(customerId);
            if (customer == null && customerDisplayName != null) {
                customer = DatabaseManager.getInstance().findCustomerByPhoneOrName(customerDisplayName);
            }
        } catch (Exception ignored) {}

        final String initialCustDbId = (customer != null) ? customer.customerId : customerId;
        String initialPhone = (customer != null && customer.phoneOrEmail != null) ? customer.phoneOrEmail : customerId;
        String initialFullName = (customer != null && customer.fullName != null) ? customer.fullName : customerDisplayName;
        String initialEmail = (customer != null && customer.email != null) ? customer.email : "";
        String initialTier = (customer != null && customer.tier != null) ? customer.tier : "Thường";
        String initialNotes = (customer != null && customer.notes != null) ? customer.notes : "";
        String regDate = (customer != null && customer.createdAt > 0) ? DATE_FORMAT.format(new Date(customer.createdAt)) : "Hôm nay";

        List<DatabaseManager.SessionRecord> pastSessions;
        try {
            pastSessions = DatabaseManager.getInstance().getSessionsByCustomer(initialCustDbId);
        } catch (Exception e) {
            pastSessions = List.of();
        }

        int totalSessions = pastSessions.size();
        int ratedCount = 0;
        int ratingSum = 0;
        for (DatabaseManager.SessionRecord s : pastSessions) {
            if (s.rating > 0) {
                ratingSum += s.rating;
                ratedCount++;
            }
        }
        String avgRatingStr = ratedCount > 0 ? String.format("%.1f/5 sao (%d lượt)", (double) ratingSum / ratedCount, ratedCount) : "Chưa có đánh giá";

        // Màu sắc giao diện phù hợp với chế độ Sáng / Tối
        String rootBg = dark ? "#18191a" : "#f8fafc";
        String cardBg = dark ? "#242526" : "#ffffff";
        String borderColor = dark ? "#3a3b3c" : "#e2e8f0";
        String titleColor = dark ? "#f1f5f9" : "#0f172a";
        String subColor = dark ? "#94a3b8" : "#64748b";
        String textColor = dark ? "#e4e6eb" : "#1e293b";
        String inputBg = dark ? "#2a2b2c" : "#ffffff";
        String inputBorder = dark ? "#4b5563" : "#cbd5e1";

        VBox root = new VBox(12);
        root.setPadding(new Insets(16, 20, 16, 20));
        root.setPrefWidth(720);
        root.setPrefHeight(670);
        root.setStyle("-fx-background-color: " + rootBg + ";");

        // Header Title
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("Hồ sơ Khách hàng 360");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblTitle.setStyle("-fx-text-fill: " + titleColor + ";");

        Region spacerHeader = new Region();
        HBox.setHgrow(spacerHeader, Priority.ALWAYS);

        Label lblBadgeTier = new Label(initialTier.toUpperCase());
        lblBadgeTier.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        applyBadgeStyle(lblBadgeTier, initialTier, dark);

        headerRow.getChildren().addAll(lblTitle, spacerHeader, lblBadgeTier);

        // Card Thông tin khách hàng & Chỉnh sửa
        VBox editCard = new VBox(10);
        editCard.setPadding(new Insets(12));
        editCard.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 8; -fx-border-color: " + borderColor + "; -fx-border-radius: 8;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(14);
        formGrid.setVgap(8);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        formGrid.getColumnConstraints().addAll(col1, col2);

        // Hàng 1: Họ tên & Số điện thoại
        VBox boxName = createFormField("Họ và tên:", dark);
        TextField txtFullName = new TextField(initialFullName);
        styleInput(txtFullName, inputBg, textColor, inputBorder);
        boxName.getChildren().add(txtFullName);

        VBox boxPhone = createFormField("Số điện thoại / Định danh:", dark);
        TextField txtPhone = new TextField(initialPhone);
        styleInput(txtPhone, inputBg, textColor, inputBorder);
        boxPhone.getChildren().add(txtPhone);

        formGrid.add(boxName, 0, 0);
        formGrid.add(boxPhone, 1, 0);

        // Hàng 2: Email & Hạng thành viên
        VBox boxEmail = createFormField("Email liên hệ:", dark);
        TextField txtEmail = new TextField(initialEmail);
        txtEmail.setPromptText("ví dụ: khachhang@email.com");
        styleInput(txtEmail, inputBg, textColor, inputBorder);
        boxEmail.getChildren().add(txtEmail);

        VBox boxTier = createFormField("Hạng thành viên:", dark);
        ComboBox<String> cmbTier = new ComboBox<>();
        cmbTier.getItems().addAll("Thường", "Thân thiết", "Tiềm năng", "VIP", "Doanh nghiệp");
        cmbTier.setValue(initialTier);
        cmbTier.setMaxWidth(Double.MAX_VALUE);
        cmbTier.setStyle("-fx-background-color: " + inputBg + "; -fx-text-fill: " + textColor + "; -fx-border-color: " + inputBorder + "; -fx-border-radius: 6; -fx-background-radius: 6;");
        boxTier.getChildren().add(cmbTier);

        formGrid.add(boxEmail, 0, 1);
        formGrid.add(boxTier, 1, 1);

        // Hàng 3: Ghi chú nội bộ CSKH
        VBox boxNotes = createFormField("Ghi chú nội bộ CSKH:", dark);
        TextArea txtNotes = new TextArea(initialNotes);
        txtNotes.setPromptText("Nhập ghi chú quan trọng, thói quen hoặc yêu cầu đặc biệt của khách hàng...");
        txtNotes.setPrefRowCount(2);
        txtNotes.setWrapText(true);
        txtNotes.setStyle("-fx-control-inner-background: " + inputBg + "; -fx-text-fill: " + textColor + "; -fx-border-color: " + inputBorder + "; -fx-border-radius: 6; -fx-background-radius: 6;");
        boxNotes.getChildren().add(txtNotes);

        formGrid.add(boxNotes, 0, 2, 2, 1);

        // Hàng nút Lưu & Thông báo
        HBox saveRow = new HBox(12);
        saveRow.setAlignment(Pos.CENTER_LEFT);

        Label lblRegDate = new Label("Ngày tạo: " + regDate);
        lblRegDate.setFont(Font.font("Segoe UI", 11));
        lblRegDate.setStyle("-fx-text-fill: " + subColor + ";");

        Region spacerSave = new Region();
        HBox.setHgrow(spacerSave, Priority.ALWAYS);

        Label lblSaveStatus = new Label("");
        lblSaveStatus.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        Button btnSave = new Button("Lưu hồ sơ");
        btnSave.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btnSave.setStyle("-fx-background-color: " + brand + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 16;");
        btnSave.setOnAction(e -> {
            String newName = txtFullName.getText().trim();
            String newPhone = txtPhone.getText().trim();
            String newEmail = txtEmail.getText().trim();
            String newTier = cmbTier.getValue() != null ? cmbTier.getValue() : "Thường";
            String newNotes = txtNotes.getText().trim();

            if (newName.isEmpty()) {
                lblSaveStatus.setText("Họ tên không được để trống!");
                lblSaveStatus.setStyle("-fx-text-fill: #ef4444;");
                return;
            }

            boolean ok = DatabaseManager.getInstance().updateCustomerProfile(
                    initialCustDbId, newName, newPhone, newEmail, newTier, newNotes
            );

            if (ok) {
                lblSaveStatus.setText("Đã lưu hồ sơ thành công!");
                lblSaveStatus.setStyle("-fx-text-fill: #10b981;");
                lblBadgeTier.setText(newTier.toUpperCase());
                applyBadgeStyle(lblBadgeTier, newTier, dark);

                if (onNameUpdatedCallback != null) {
                    onNameUpdatedCallback.accept(newName);
                }
            } else {
                lblSaveStatus.setText("Lỗi khi lưu vào cơ sở dữ liệu!");
                lblSaveStatus.setStyle("-fx-text-fill: #ef4444;");
            }
        });

        saveRow.getChildren().addAll(lblRegDate, spacerSave, lblSaveStatus, btnSave);
        editCard.getChildren().addAll(formGrid, saveRow);

        // Card Thống kê chỉ số (Stats Bar)
        HBox statsBar = new HBox(12);
        statsBar.setAlignment(Pos.CENTER);

        VBox stat1 = createStatBox("Tổng số phiên hỗ trợ", String.valueOf(totalSessions), brand, dark, cardBg, borderColor, subColor);
        VBox stat2 = createStatBox("Đánh giá CSAT trung bình", avgRatingStr, "#f59e0b", dark, cardBg, borderColor, subColor);

        HBox.setHgrow(stat1, Priority.ALWAYS);
        HBox.setHgrow(stat2, Priority.ALWAYS);
        statsBar.getChildren().addAll(stat1, stat2);

        // Lịch sử phiên trước đây
        Label lblHistTitle = new Label("Danh sách các phiên hỗ trợ trước đây:");
        lblHistTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblHistTitle.setStyle("-fx-text-fill: " + textColor + ";");

        ListView<DatabaseManager.SessionRecord> listView = new ListView<>();
        listView.setPrefHeight(120);
        listView.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + borderColor + ";");

        if (pastSessions.isEmpty()) {
            listView.setPlaceholder(new Label("Khách hàng mới - Chưa có lịch sử phiên nào"));
        } else {
            listView.getItems().addAll(pastSessions);
        }

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(DatabaseManager.SessionRecord item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox box = new VBox(2);
                    box.setPadding(new Insets(3, 6, 3, 6));

                    String time = item.createdAt > 0 ? DATE_FORMAT.format(new Date(item.createdAt)) : "Không rõ";
                    String rating = item.rating > 0 ? (item.rating + "/5 sao") : "Chưa chấm điểm";

                    Label lblLine1 = new Label(time + "  •  Chủ đề: " + (item.issueTopic != null ? item.issueTopic : "Chung"));
                    lblLine1.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                    lblLine1.setStyle("-fx-text-fill: " + textColor + ";");

                    Label lblLine2 = new Label("CSKH: " + (item.staffName != null ? item.staffName : "Chưa rõ") + "  •  Trạng thái: " + item.status + "  •  " + rating);
                    lblLine2.setFont(Font.font("Segoe UI", 11));
                    lblLine2.setStyle("-fx-text-fill: " + subColor + ";");

                    box.getChildren().addAll(lblLine1, lblLine2);

                    if (item.feedback != null && !item.feedback.trim().isEmpty()) {
                        Label lblFeedback = new Label("Nhận xét: \"" + item.feedback.trim() + "\"");
                        lblFeedback.setFont(Font.font("Segoe UI", 11));
                        lblFeedback.setStyle("-fx-text-fill: " + (dark ? "#38bdf8" : "#0284c7") + "; -fx-font-style: italic;");
                        box.getChildren().add(lblFeedback);
                    }

                    setGraphic(box);
                }
            }
        });

        // 3. Khung xem chi tiết tin nhắn
        HBox msgHeaderRow = new HBox(10);
        msgHeaderRow.setAlignment(Pos.CENTER_LEFT);

        Label lblMsgTitle = new Label("Chi tiết tin nhắn:");
        lblMsgTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblMsgTitle.setStyle("-fx-text-fill: " + textColor + ";");

        Region spacerMsg = new Region();
        HBox.setHgrow(spacerMsg, Priority.ALWAYS);

        Button btnViewAllMsgs = new Button("Xem toàn bộ lịch sử trò chuyện");
        btnViewAllMsgs.setFont(Font.font("Segoe UI", 11));
        btnViewAllMsgs.setStyle("-fx-background-color: transparent; -fx-text-fill: " + brand + "; -fx-border-color: " + brand + "; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 3 10;");

        msgHeaderRow.getChildren().addAll(lblMsgTitle, spacerMsg, btnViewAllMsgs);

        VBox msgContainer = new VBox(6);
        msgContainer.setPadding(new Insets(10));
        msgContainer.setStyle("-fx-background-color: " + cardBg + ";");

        ScrollPane scrollMessages = new ScrollPane(msgContainer);
        scrollMessages.setFitToWidth(true);
        scrollMessages.setPrefHeight(150);
        VBox.setVgrow(scrollMessages, Priority.ALWAYS);
        scrollMessages.setStyle("-fx-background-color: transparent; -fx-background: " + cardBg + "; -fx-border-color: " + borderColor + "; -fx-border-radius: 8;");

        Label placeholder = new Label("Chọn một phiên ở trên để xem tin nhắn, hoặc bấm 'Xem toàn bộ lịch sử trò chuyện'.");
        placeholder.setStyle("-fx-text-fill: " + subColor + "; -fx-font-style: italic;");
        msgContainer.getChildren().add(placeholder);

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lblMsgTitle.setText("Tin nhắn phiên #" + (newVal.sessionId.length() > 8 ? newVal.sessionId.substring(0, 8) : newVal.sessionId) + " (" + (newVal.issueTopic != null ? newVal.issueTopic : "Tư vấn") + "):");
                loadMessagesToContainer(msgContainer, DatabaseManager.getInstance().getMessagesBySession(newVal.sessionId), dark, brand, textColor, subColor);
            }
        });

        btnViewAllMsgs.setOnAction(e -> {
            listView.getSelectionModel().clearSelection();
            lblMsgTitle.setText("Toàn bộ lịch sử trò chuyện của khách hàng (" + totalSessions + " phiên):");
            loadMessagesToContainer(msgContainer, DatabaseManager.getInstance().getAllMessagesByCustomer(initialCustDbId), dark, brand, textColor, subColor);
        });

        // Bottom Button
        HBox actionRow = new HBox();
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnClose = new Button("Đóng");
        btnClose.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btnClose.setStyle("-fx-background-color: " + (dark ? "#3a3b3c" : "#e2e8f0") + "; -fx-text-fill: " + (dark ? "#e4e6eb" : "#1e293b") + "; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 7 20;");
        btnClose.setOnAction(e -> stage.close());

        actionRow.getChildren().add(btnClose);

        root.getChildren().addAll(headerRow, editCard, statsBar, lblHistTitle, listView, msgHeaderRow, scrollMessages, actionRow);

        Scene scene = new Scene(root);
        ThemeManager.applyTheme(scene);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private static void loadMessagesToContainer(VBox container, List<DatabaseManager.MessageRecord> msgs, boolean dark, String brand, String textColor, String subColor) {
        container.getChildren().clear();
        if (msgs == null || msgs.isEmpty()) {
            Label empty = new Label("Phiên này chưa có tin nhắn nào.");
            empty.setStyle("-fx-text-fill: " + subColor + "; -fx-font-style: italic;");
            container.getChildren().add(empty);
            return;
        }

        for (DatabaseManager.MessageRecord m : msgs) {
            VBox msgCard = new VBox(2);
            msgCard.setPadding(new Insets(5, 8, 5, 8));
            msgCard.setStyle("-fx-background-color: " + (dark ? "#262626" : "#f1f5f9") + "; -fx-background-radius: 6;");

            HBox header = new HBox(8);
            header.setAlignment(Pos.CENTER_LEFT);

            String roleTag = "CUSTOMER".equalsIgnoreCase(m.senderRole) ? "Khách hàng" : ("STAFF".equalsIgnoreCase(m.senderRole) ? "CSKH" : "Hệ thống");
            String roleColor = "CUSTOMER".equalsIgnoreCase(m.senderRole) ? "#3b82f6" : ("STAFF".equalsIgnoreCase(m.senderRole) ? "#10b981" : "#f59e0b");

            Label lblSender = new Label(m.senderName + " [" + roleTag + "]");
            lblSender.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            lblSender.setStyle("-fx-text-fill: " + roleColor + ";");

            String timeStr = m.timestamp > 0 ? DATE_FORMAT.format(new Date(m.timestamp)) : "";
            Label lblTime = new Label(timeStr);
            lblTime.setFont(Font.font("Segoe UI", 10));
            lblTime.setStyle("-fx-text-fill: " + subColor + ";");

            header.getChildren().addAll(lblSender, lblTime);

            String content = m.content;
            if (content != null) {
                try {
                    String decrypted = com.cmp180.livechat.common.SecurityManager.decrypt(content);
                    if (decrypted != null && !decrypted.isEmpty()) content = decrypted;
                } catch (Exception ignored) {}
            }

            if (content == null || content.isEmpty()) {
                if (m.fileName != null) {
                    boolean isVoice = m.fileName.startsWith("voice_") && m.fileName.endsWith(".wav");
                    content = "[" + (isVoice ? "Tin nhắn thoại" : (m.isImage ? "Hình ảnh" : "Tệp đính kèm")) + "] " + m.fileName;
                } else {
                    content = "(Tin nhắn rỗng)";
                }
            }

            Label lblContent = new Label(content);
            lblContent.setWrapText(true);
            lblContent.setFont(Font.font("Segoe UI", 12));
            lblContent.setStyle("-fx-text-fill: " + textColor + ";");

            msgCard.getChildren().addAll(header, lblContent);
            container.getChildren().add(msgCard);
        }
    }

    private static VBox createFormField(String label, boolean dark) {
        VBox box = new VBox(3);
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11.5));
        lbl.setStyle("-fx-text-fill: " + (dark ? "#94a3b8" : "#64748b") + ";");
        box.getChildren().add(lbl);
        return box;
    }

    private static void styleInput(TextField tf, String bg, String textColor, String borderColor) {
        tf.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + textColor + "; -fx-border-color: " + borderColor + "; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 5 8;");
    }

    private static VBox createStatBox(String title, String val, String valColor, boolean dark, String cardBg, String borderColor, String subColor) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(8));
        box.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 8; -fx-border-color: " + borderColor + "; -fx-border-radius: 8;");

        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Segoe UI", 11));
        lblTitle.setStyle("-fx-text-fill: " + subColor + ";");

        Label lblVal = new Label(val);
        lblVal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblVal.setStyle("-fx-text-fill: " + valColor + ";");

        box.getChildren().addAll(lblTitle, lblVal);
        return box;
    }

    private static void applyBadgeStyle(Label badge, String tier, boolean dark) {
        String bg;
        String fg;
        String border;
        String t = tier != null ? tier.toLowerCase() : "";
        if (t.contains("vip") || t.contains("doanh nghiệp")) {
            bg = dark ? "#3b1e08" : "#fef3c7";
            fg = "#d97706";
            border = "#f59e0b";
        } else if (t.contains("tiềm năng") || t.contains("thân thiết")) {
            bg = dark ? "#052e16" : "#dcfce7";
            fg = "#16a34a";
            border = "#22c55e";
        } else {
            bg = dark ? "#1e293b" : "#f1f5f9";
            fg = dark ? "#94a3b8" : "#64748b";
            border = dark ? "#475569" : "#cbd5e1";
        }
        badge.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-border-color: " + border + "; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 3 10;");
    }
}
