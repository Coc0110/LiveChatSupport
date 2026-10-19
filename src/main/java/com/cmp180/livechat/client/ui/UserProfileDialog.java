package com.cmp180.livechat.client.ui;

import com.cmp180.livechat.client.customer.context.CustomerContext;
import com.cmp180.livechat.server.DatabaseManager;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * Hộp thoại "Hồ sơ của bạn" (Personal Profile) dành cho Nhân viên hoặc Khách hàng
 * khi nhấn vào mục Hồ sơ cá nhân trong menu Avatar.
 */
public class UserProfileDialog {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    /**
     * Hiển thị hồ sơ cá nhân của Nhân viên CSKH.
     */
    public static void showStaffProfile(Window owner, String staffId, String staffName) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setTitle("Hồ sơ cá nhân - " + (staffName != null ? staffName : "Nhân viên"));
        stage.setResizable(false);

        boolean dark = ThemeManager.isDarkMode();
        String brand = ThemeManager.getPrimaryColor();

        String rootBg = dark ? "#18191a" : "#f8fafc";
        String cardBg = dark ? "#242526" : "#ffffff";
        String borderColor = dark ? "#3a3b3c" : "#e2e8f0";
        String titleColor = dark ? "#f1f5f9" : "#0f172a";
        String subColor = dark ? "#94a3b8" : "#64748b";
        String textColor = dark ? "#e4e6eb" : "#1e293b";

        // Tra cứu thông tin chi tiết nhân viên từ CSDL
        DatabaseManager.StaffRecord staffRecord = null;
        try {
            List<DatabaseManager.StaffRecord> allStaff = DatabaseManager.getInstance().getAllStaffWithStats();
            for (DatabaseManager.StaffRecord s : allStaff) {
                if (s.staffId.equalsIgnoreCase(staffId) || s.fullName.equalsIgnoreCase(staffName)) {
                    staffRecord = s;
                    break;
                }
            }
        } catch (Exception ignored) {}

        String fullName = (staffRecord != null && staffRecord.fullName != null) ? staffRecord.fullName : staffName;
        String username = (staffRecord != null && staffRecord.username != null) ? staffRecord.username : staffId;
        String role = (staffRecord != null && staffRecord.role != null) ? ("ADMIN".equalsIgnoreCase(staffRecord.role) ? "Quản trị viên (Admin)" : "Nhân viên CSKH") : "Nhân viên CSKH";
        String skill = (staffRecord != null && staffRecord.primarySkill != null) ? staffRecord.primarySkill : "Tất cả lĩnh vực";
        int totalSessions = (staffRecord != null) ? staffRecord.sessionCount : 0;
        double avgRating = (staffRecord != null) ? staffRecord.avgRating : 5.0;

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setPrefWidth(460);
        root.setStyle("-fx-background-color: " + rootBg + ";");

        // Header
        Label lblTitle = new Label("Hồ sơ tài khoản Nhân viên");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblTitle.setStyle("-fx-text-fill: " + titleColor + ";");

        // Profile Card
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 8; -fx-border-color: " + borderColor + "; -fx-border-radius: 8;");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(8);

        addProfileRow(grid, 0, "Họ và tên:", fullName, textColor, subColor);
        addProfileRow(grid, 1, "Tên đăng nhập:", username, textColor, subColor);
        addProfileRow(grid, 2, "Vai trò / Chức vụ:", role, textColor, subColor);
        addProfileRow(grid, 3, "Chuyên môn chính:", skill, textColor, subColor);

        card.getChildren().add(grid);

        // Stats Box
        HBox statsBox = new HBox(12);
        statsBox.setAlignment(Pos.CENTER);

        VBox s1 = createMiniStat("Tổng số phiên hỗ trợ", String.valueOf(totalSessions), brand, dark, cardBg, borderColor, subColor);
        String ratingText = totalSessions > 0 ? String.format("%.1f/5 sao", avgRating) : "Chưa có đánh giá";
        VBox s2 = createMiniStat("Đánh giá CSAT trung bình", ratingText, "#f59e0b", dark, cardBg, borderColor, subColor);

        HBox.setHgrow(s1, Priority.ALWAYS);
        HBox.setHgrow(s2, Priority.ALWAYS);
        statsBox.getChildren().addAll(s1, s2);

        // Bottom Button
        HBox actionRow = new HBox();
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnClose = new Button("Đóng");
        btnClose.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btnClose.setStyle("-fx-background-color: " + brand + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 7 22;");
        btnClose.setOnAction(e -> stage.close());

        actionRow.getChildren().add(btnClose);

        root.getChildren().addAll(lblTitle, card, statsBox, actionRow);

        Scene scene = new Scene(root);
        ThemeManager.applyTheme(scene);
        stage.setScene(scene);
        stage.showAndWait();
    }

    /**
     * Hiển thị hồ sơ cá nhân của Khách hàng, cho phép cập nhật họ tên và email.
     */
    public static void showCustomerProfile(Window owner, String customerId, String currentName, String currentPhone, Consumer<String> onNameUpdated) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setTitle("Hồ sơ của bạn");
        stage.setResizable(false);

        boolean dark = ThemeManager.isDarkMode();
        String brand = ThemeManager.getPrimaryColor();

        String rootBg = dark ? "#18191a" : "#f8fafc";
        String cardBg = dark ? "#242526" : "#ffffff";
        String borderColor = dark ? "#3a3b3c" : "#e2e8f0";
        String titleColor = dark ? "#f1f5f9" : "#0f172a";
        String subColor = dark ? "#94a3b8" : "#64748b";
        String textColor = dark ? "#e4e6eb" : "#1e293b";
        String inputBg = dark ? "#2a2b2c" : "#ffffff";
        String inputBorder = dark ? "#4b5563" : "#cbd5e1";

        // Tra cứu CSDL
        DatabaseManager.CustomerRecord customer = null;
        try {
            customer = DatabaseManager.getInstance().findCustomerByPhoneOrName(currentPhone);
            if (customer == null && customerId != null) {
                customer = DatabaseManager.getInstance().findCustomerByPhoneOrName(customerId);
            }
        } catch (Exception ignored) {}

        String dbId = (customer != null) ? customer.customerId : customerId;
        String phone = (customer != null && customer.phoneOrEmail != null) ? customer.phoneOrEmail : currentPhone;
        String name = (customer != null && customer.fullName != null) ? customer.fullName : currentName;
        String email = (customer != null && customer.email != null) ? customer.email : "";
        String tier = (customer != null && customer.tier != null) ? customer.tier : "Thường";
        String notes = (customer != null && customer.notes != null) ? customer.notes : "";
        String regDate = (customer != null && customer.createdAt > 0) ? DATE_FORMAT.format(new Date(customer.createdAt)) : "Hôm nay";

        int totalSessions = 0;
        try {
            totalSessions = DatabaseManager.getInstance().getSessionsByCustomer(dbId).size();
        } catch (Exception ignored) {}

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setPrefWidth(480);
        root.setStyle("-fx-background-color: " + rootBg + ";");

        // Header Title
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("Hồ sơ của bạn");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblTitle.setStyle("-fx-text-fill: " + titleColor + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblTier = new Label("Hạng: " + tier);
        lblTier.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblTier.setStyle("-fx-background-color: " + (dark ? "#052e16" : "#dcfce7") + "; -fx-text-fill: #16a34a; -fx-padding: 3 10; -fx-background-radius: 10;");

        header.getChildren().addAll(lblTitle, spacer, lblTier);

        // Edit Card
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 8; -fx-border-color: " + borderColor + "; -fx-border-radius: 8;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        // Họ tên
        Label lblName = new Label("Họ và tên:");
        lblName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblName.setStyle("-fx-text-fill: " + subColor + ";");
        TextField txtName = new TextField(name);
        txtName.setStyle("-fx-background-color: " + inputBg + "; -fx-text-fill: " + textColor + "; -fx-border-color: " + inputBorder + "; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 5 8;");
        grid.add(lblName, 0, 0);
        grid.add(txtName, 1, 0);

        // Số điện thoại
        Label lblPhoneTitle = new Label("Số điện thoại:");
        lblPhoneTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblPhoneTitle.setStyle("-fx-text-fill: " + subColor + ";");
        Label lblPhoneVal = new Label(phone);
        lblPhoneVal.setFont(Font.font("Segoe UI", 13));
        lblPhoneVal.setStyle("-fx-text-fill: " + textColor + ";");
        grid.add(lblPhoneTitle, 0, 1);
        grid.add(lblPhoneVal, 1, 1);

        // Email
        Label lblEmail = new Label("Email liên hệ:");
        lblEmail.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblEmail.setStyle("-fx-text-fill: " + subColor + ";");
        TextField txtEmail = new TextField(email);
        txtEmail.setPromptText("ví dụ: email@domain.com");
        txtEmail.setStyle("-fx-background-color: " + inputBg + "; -fx-text-fill: " + textColor + "; -fx-border-color: " + inputBorder + "; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 5 8;");
        grid.add(lblEmail, 0, 2);
        grid.add(txtEmail, 1, 2);

        // Ngày tham gia
        Label lblDateTitle = new Label("Ngày tham gia:");
        lblDateTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblDateTitle.setStyle("-fx-text-fill: " + subColor + ";");
        Label lblDateVal = new Label(regDate);
        lblDateVal.setFont(Font.font("Segoe UI", 12));
        lblDateVal.setStyle("-fx-text-fill: " + subColor + ";");
        grid.add(lblDateTitle, 0, 3);
        grid.add(lblDateVal, 1, 3);

        card.getChildren().add(grid);

        // Stats box
        VBox statCard = createMiniStat("Tổng số phiên hỗ trợ đã tham gia", totalSessions + " phiên", brand, dark, cardBg, borderColor, subColor);

        // Save & Feedback
        Label lblStatus = new Label("");
        lblStatus.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnSave = new Button("Lưu thông tin");
        btnSave.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btnSave.setStyle("-fx-background-color: " + brand + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 7 18;");
        btnSave.setOnAction(e -> {
            String newName = txtName.getText().trim();
            String newEmail = txtEmail.getText().trim();

            if (newName.isEmpty()) {
                lblStatus.setText("Tên không được để trống!");
                lblStatus.setStyle("-fx-text-fill: #ef4444;");
                return;
            }

            boolean ok = DatabaseManager.getInstance().updateCustomerProfile(
                    dbId, newName, phone, newEmail, tier, notes
            );

            if (ok) {
                lblStatus.setText("Đã cập nhật hồ sơ thành công!");
                lblStatus.setStyle("-fx-text-fill: #10b981;");
                CustomerContext.getInstance().getNetworkService().setCustomerName(newName);
                if (onNameUpdated != null) {
                    onNameUpdated.accept(newName);
                }
            } else {
                lblStatus.setText("Lỗi lưu hồ sơ vào cơ sở dữ liệu!");
                lblStatus.setStyle("-fx-text-fill: #ef4444;");
            }
        });

        Button btnClose = new Button("Đóng");
        btnClose.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btnClose.setStyle("-fx-background-color: " + (dark ? "#3a3b3c" : "#e2e8f0") + "; -fx-text-fill: " + textColor + "; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 7 18;");
        btnClose.setOnAction(e -> stage.close());

        Region actionSpacer = new Region();
        HBox.setHgrow(actionSpacer, Priority.ALWAYS);

        actionRow.getChildren().addAll(lblStatus, actionSpacer, btnSave, btnClose);

        root.getChildren().addAll(header, card, statCard, actionRow);

        Scene scene = new Scene(root);
        ThemeManager.applyTheme(scene);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private static void addProfileRow(GridPane grid, int row, String label, String value, String textColor, String subColor) {
        Label lblKey = new Label(label);
        lblKey.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblKey.setStyle("-fx-text-fill: " + subColor + ";");

        Label lblVal = new Label(value);
        lblVal.setFont(Font.font("Segoe UI", 13));
        lblVal.setStyle("-fx-text-fill: " + textColor + ";");

        grid.add(lblKey, 0, row);
        grid.add(lblVal, 1, row);
    }

    private static VBox createMiniStat(String title, String val, String valColor, boolean dark, String cardBg, String borderColor, String subColor) {
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
}
