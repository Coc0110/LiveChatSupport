package com.cmp180.livechat.client.ui;

import com.cmp180.livechat.client.customer.context.CustomerContext;
import com.cmp180.livechat.server.DatabaseManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Hộp thoại "Hồ sơ của bạn" (Personal Profile) dành cho Nhân viên hoặc Khách hàng.
 * Thiết kế UX chỉnh sửa trực tiếp từng dòng (Inline Editing on Hover):
 * - Khi di chuột vào từng mục có thể sửa (Tên đăng nhập, Mật khẩu, Tên, SĐT, Email),
 *   xuất hiện lớp nền mờ và icon cây bút ở cuối bên phải.
 * - Nhấn vào cây bút sẽ mở khung chỉnh sửa trực tiếp tại mục đó, kèm nút Lưu và nút Hủy nhỏ.
 * - Riêng đổi Mật khẩu: yêu cầu nhập Mật khẩu cũ, Mật khẩu mới và Xác nhận lại mật khẩu mới.
 * - Tự động điều chỉnh kích thước cửa sổ linh hoạt (stage.sizeToScene()) tránh tràn viền.
 */
public class UserProfileDialog {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // Vector icon cây bút SVG theo đúng mẫu yêu cầu của người dùng
    private static final String PENCIL_SVG_PATH =
            "M12 8.00012L4 16.0001V20.0001H20M4 20.0001L8 20.0001L16 12.0001M12 8.00012L14.8686 5.13146L14.8704 5.12976C15.2652 4.73488 15.463 4.53709 15.691 4.46301C15.8919 4.39775 16.1082 4.39775 16.3091 4.46301C16.5369 4.53704 16.7345 4.7346 17.1288 5.12892L18.8686 6.86872C19.2646 7.26474 19.4627 7.46284 19.5369 7.69117C19.6022 7.89201 19.6021 8.10835 19.5369 8.3092C19.4628 8.53736 19.265 8.73516 18.8695 9.13061L18.8686 9.13146L16 12.0001M12 8.00012L16 12.0001";

    /**
     * Tạo nút icon Bút chì nhỏ gọn ở cuối dòng khi hover.
     */
    private static Button createInlinePencilButton(boolean dark) {
        Button btn = new Button();
        btn.setTooltip(new Tooltip("Nhấp để chỉnh sửa"));
        btn.setPrefSize(28, 28);
        btn.setMinSize(28, 28);
        btn.setMaxSize(28, 28);

        SVGPath path = new SVGPath();
        path.setContent(PENCIL_SVG_PATH);
        path.setFill(Color.TRANSPARENT);
        path.setStroke(Color.web(dark ? "#38bdf8" : "#0284c7"));
        path.setStrokeWidth(1.8);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setStrokeLineJoin(StrokeLineJoin.ROUND);
        path.setScaleX(0.65);
        path.setScaleY(0.65);

        btn.setGraphic(path);
        btn.setStyle("-fx-background-color: transparent; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 0;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + (dark ? "rgba(56, 189, 248, 0.22)" : "rgba(2, 132, 199, 0.15)") + "; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 0;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 0;"));
        btn.setVisible(false);
        return btn;
    }

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
        String hoverBg = dark ? "rgba(255, 255, 255, 0.05)" : "rgba(0, 0, 0, 0.04)";
        String inputBg = dark ? "#2a2b2c" : "#ffffff";
        String inputBorder = dark ? "#4b5563" : "#cbd5e1";

        // Tra cứu thông tin chi tiết nhân viên từ CSDL
        DatabaseManager.StaffRecord staffRecord = null;
        try {
            staffRecord = DatabaseManager.getInstance().getStaffById(staffId);
            if (staffRecord == null && staffName != null) {
                List<DatabaseManager.StaffRecord> all = DatabaseManager.getInstance().getAllStaffWithStats();
                for (DatabaseManager.StaffRecord s : all) {
                    if (s.fullName.equalsIgnoreCase(staffName) || s.username.equalsIgnoreCase(staffId)) {
                        staffRecord = s;
                        break;
                    }
                }
            }
        } catch (Exception ignored) {}

        final String finalStaffId = (staffRecord != null && staffRecord.staffId != null) ? staffRecord.staffId : staffId;
        final String fullName = (staffRecord != null && staffRecord.fullName != null) ? staffRecord.fullName : (staffName != null ? staffName : "Nhân viên");
        final AtomicReference<String> currentUsernameRef = new AtomicReference<>(
                (staffRecord != null && staffRecord.username != null) ? staffRecord.username : (staffId != null ? staffId : "staff")
        );
        final String role = (staffRecord != null && staffRecord.role != null) ? ("ADMIN".equalsIgnoreCase(staffRecord.role) ? "Quản trị viên (Admin)" : "Nhân viên CSKH") : "Nhân viên CSKH";
        final String skill = (staffRecord != null && staffRecord.primarySkill != null) ? staffRecord.primarySkill : "Tất cả lĩnh vực";
        final int totalSessions = (staffRecord != null) ? staffRecord.sessionCount : 0;
        final double avgRating = (staffRecord != null) ? staffRecord.avgRating : 5.0;

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setPrefWidth(490);
        root.setStyle("-fx-background-color: " + rootBg + ";");

        // Header Title
        VBox titleBox = new VBox(3);
        Label lblTitle = new Label("Hồ sơ tài khoản Nhân viên");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblTitle.setStyle("-fx-text-fill: " + titleColor + ";");

        Label lblSubTitle = new Label("Di chuột vào mục để chỉnh sửa thông tin trực tiếp");
        lblSubTitle.setFont(Font.font("Segoe UI", 12));
        lblSubTitle.setStyle("-fx-text-fill: " + subColor + ";");
        titleBox.getChildren().addAll(lblTitle, lblSubTitle);

        // Container Card
        VBox card = new VBox(6);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 8; -fx-border-color: " + borderColor + "; -fx-border-radius: 8;");

        // 1. Họ và tên: CỐ ĐỊNH (không cho sửa)
        Pane rowFullName = createStaticRow("Họ và tên:", fullName + " (Cố định)", textColor, subColor);

        // 2. Tên đăng nhập: CHO PHÉP SỬA INLINE
        Pane rowUsername = createInlineEditableTextRow(
                stage, "Tên đăng nhập:", currentUsernameRef.get(),
                textColor, subColor, hoverBg, inputBg, inputBorder, brand, dark,
                (newVal, errorConsumer, successCallback) -> {
                    String clean = newVal.trim();
                    if (clean.isEmpty()) {
                        errorConsumer.accept("Tên đăng nhập không được để trống!");
                        return;
                    }
                    boolean ok = DatabaseManager.getInstance().updateStaffCredentials(finalStaffId, clean, null);
                    if (ok) {
                        currentUsernameRef.set(clean);
                        successCallback.run();
                    } else {
                        errorConsumer.accept("Tên đăng nhập đã tồn tại hoặc không hợp lệ!");
                    }
                }
        );

        // 3. Mật khẩu: CHO PHÉP ĐỔI MẬT KHẨU INLINE (yêu cầu mật khẩu cũ + xác nhận)
        Pane rowPassword = createInlinePasswordChangeRow(
                stage, finalStaffId, textColor, subColor, hoverBg, inputBg, inputBorder, brand, dark
        );

        // 4. Vai trò / Chức vụ: CỐ ĐỊNH
        Pane rowRole = createStaticRow("Vai trò / Chức vụ:", role, textColor, subColor);

        // 5. Chuyên môn chính: CỐ ĐỊNH
        Pane rowSkill = createStaticRow("Chuyên môn chính:", skill, textColor, subColor);

        card.getChildren().addAll(rowFullName, rowUsername, rowPassword, rowRole, rowSkill);

        // Stats Box
        HBox statsBox = new HBox(12);
        statsBox.setAlignment(Pos.CENTER);

        VBox s1 = createMiniStat("Tổng số phiên hỗ trợ", String.valueOf(totalSessions), brand, dark, cardBg, borderColor, subColor);
        String ratingText = totalSessions > 0 ? String.format("%.1f / 5.0 sao", avgRating) : "Chưa có đánh giá";
        VBox s2 = createMiniStat("Đánh giá CSAT trung bình", ratingText, "#f59e0b", dark, cardBg, borderColor, subColor);

        HBox.setHgrow(s1, Priority.ALWAYS);
        HBox.setHgrow(s2, Priority.ALWAYS);
        statsBox.getChildren().addAll(s1, s2);

        // Bottom Action Bar
        HBox actionRow = new HBox();
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnClose = new Button("Đóng");
        btnClose.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btnClose.setStyle("-fx-background-color: " + brand + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 7 22;");
        btnClose.setOnAction(e -> stage.close());

        actionRow.getChildren().add(btnClose);

        root.getChildren().addAll(titleBox, card, statsBox, actionRow);

        Scene scene = new Scene(root);
        ThemeManager.applyTheme(scene);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.showAndWait();
    }

    /**
     * Hiển thị hồ sơ cá nhân của Khách hàng.
     * Cho phép chỉnh sửa trực tiếp từng mục: Tên, Số điện thoại và Liên kết email.
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
        String hoverBg = dark ? "rgba(255, 255, 255, 0.05)" : "rgba(0, 0, 0, 0.04)";
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

        final String dbId = (customer != null && customer.customerId != null) ? customer.customerId : (customerId != null ? customerId : currentPhone);
        final AtomicReference<String> phoneRef = new AtomicReference<>(
                (customer != null && customer.phoneOrEmail != null) ? customer.phoneOrEmail : (currentPhone != null ? currentPhone : "")
        );
        final AtomicReference<String> nameRef = new AtomicReference<>(
                (customer != null && customer.fullName != null) ? customer.fullName : (currentName != null ? currentName : "Khách hàng")
        );
        final AtomicReference<String> emailRef = new AtomicReference<>(
                (customer != null && customer.email != null) ? customer.email : ""
        );
        final String tier = (customer != null && customer.tier != null) ? customer.tier : "Thường";
        final String notes = (customer != null && customer.notes != null) ? customer.notes : "";
        final String regDate = (customer != null && customer.createdAt > 0) ? DATE_FORMAT.format(new Date(customer.createdAt)) : "Hôm nay";

        int totalSessions = 0;
        try {
            totalSessions = DatabaseManager.getInstance().getSessionsByCustomer(dbId).size();
        } catch (Exception ignored) {}

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setPrefWidth(490);
        root.setStyle("-fx-background-color: " + rootBg + ";");

        // Header Title
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(3);
        Label lblTitle = new Label("Hồ sơ của bạn");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblTitle.setStyle("-fx-text-fill: " + titleColor + ";");

        Label lblSubTitle = new Label("Di chuột vào mục để chỉnh sửa thông tin trực tiếp");
        lblSubTitle.setFont(Font.font("Segoe UI", 12));
        lblSubTitle.setStyle("-fx-text-fill: " + subColor + ";");
        titleBox.getChildren().addAll(lblTitle, lblSubTitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblTier = new Label("Hạng: " + tier);
        lblTier.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblTier.setStyle("-fx-background-color: " + (dark ? "#052e16" : "#dcfce7") + "; -fx-text-fill: #16a34a; -fx-padding: 4 12; -fx-background-radius: 12;");

        header.getChildren().addAll(titleBox, spacer, lblTier);

        // Container Card
        VBox card = new VBox(6);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 8; -fx-border-color: " + borderColor + "; -fx-border-radius: 8;");

        // 1. Họ và tên: CHO PHÉP SỬA INLINE
        Pane rowName = createInlineEditableTextRow(
                stage, "Họ và tên:", nameRef.get(),
                textColor, subColor, hoverBg, inputBg, inputBorder, brand, dark,
                (newVal, errorConsumer, successCallback) -> {
                    String clean = newVal.trim();
                    if (clean.isEmpty()) {
                        errorConsumer.accept("Họ và tên không được để trống!");
                        return;
                    }
                    boolean ok = DatabaseManager.getInstance().updateCustomerProfile(
                            dbId, clean, phoneRef.get(), emailRef.get(), tier, notes
                    );
                    if (ok) {
                        nameRef.set(clean);
                        CustomerContext.getInstance().getNetworkService().setCustomerName(clean);
                        if (onNameUpdated != null) onNameUpdated.accept(clean);
                        successCallback.run();
                    } else {
                        errorConsumer.accept("Lỗi lưu thông tin vào cơ sở dữ liệu!");
                    }
                }
        );

        // 2. Số điện thoại: CHO PHÉP SỬA INLINE
        Pane rowPhone = createInlineEditableTextRow(
                stage, "Số điện thoại:", phoneRef.get(),
                textColor, subColor, hoverBg, inputBg, inputBorder, brand, dark,
                (newVal, errorConsumer, successCallback) -> {
                    String clean = newVal.trim();
                    if (clean.isEmpty()) {
                        errorConsumer.accept("Số điện thoại không được để trống!");
                        return;
                    }
                    boolean ok = DatabaseManager.getInstance().updateCustomerProfile(
                            dbId, nameRef.get(), clean, emailRef.get(), tier, notes
                    );
                    if (ok) {
                        phoneRef.set(clean);
                        CustomerContext.getInstance().getNetworkService().setCustomerPhone(clean);
                        successCallback.run();
                    } else {
                        errorConsumer.accept("Lỗi lưu số điện thoại vào CSDL!");
                    }
                }
        );

        // 3. Liên kết email: CHO PHÉP SỬA INLINE
        Pane rowEmail = createInlineEditableTextRow(
                stage, "Liên kết email:", emailRef.get().isEmpty() ? "Chưa liên kết" : emailRef.get(),
                textColor, subColor, hoverBg, inputBg, inputBorder, brand, dark,
                (newVal, errorConsumer, successCallback) -> {
                    String clean = newVal.trim();
                    if (clean.equalsIgnoreCase("Chưa liên kết")) clean = "";
                    boolean ok = DatabaseManager.getInstance().updateCustomerProfile(
                            dbId, nameRef.get(), phoneRef.get(), clean, tier, notes
                    );
                    if (ok) {
                        emailRef.set(clean);
                        successCallback.run();
                    } else {
                        errorConsumer.accept("Lỗi lưu email vào CSDL!");
                    }
                }
        );

        // 4. Ngày tham gia: CỐ ĐỊNH
        Pane rowDate = createStaticRow("Ngày tham gia:", regDate, subColor, subColor);

        card.getChildren().addAll(rowName, rowPhone, rowEmail, rowDate);

        // Stats Box
        VBox statCard = createMiniStat("Tổng số phiên hỗ trợ đã tham gia", totalSessions + " phiên", brand, dark, cardBg, borderColor, subColor);

        // Bottom Action Bar
        HBox actionRow = new HBox();
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnClose = new Button("Đóng");
        btnClose.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btnClose.setStyle("-fx-background-color: " + brand + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 7 22;");
        btnClose.setOnAction(e -> stage.close());

        actionRow.getChildren().add(btnClose);

        root.getChildren().addAll(header, card, statCard, actionRow);

        Scene scene = new Scene(root);
        ThemeManager.applyTheme(scene);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.showAndWait();
    }

    /**
     * Chế độ XEM DÀNH CHO KHÁCH HÀNG (View-Only):
     * Khách hàng xem thông tin hỗ trợ viên đang hỗ trợ phiên chat của mình.
     */
    public static void showStaffProfileViewOnly(Window owner, String staffId, String staffName) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setTitle("Hồ sơ Hỗ trợ viên - " + (staffName != null ? staffName : "Nhân viên"));
        stage.setResizable(false);

        boolean dark = ThemeManager.isDarkMode();
        String brand = ThemeManager.getPrimaryColor();

        String rootBg = dark ? "#18191a" : "#f8fafc";
        String cardBg = dark ? "#242526" : "#ffffff";
        String borderColor = dark ? "#3a3b3c" : "#e2e8f0";
        String titleColor = dark ? "#f1f5f9" : "#0f172a";
        String subColor = dark ? "#94a3b8" : "#64748b";
        String textColor = dark ? "#e4e6eb" : "#1e293b";

        DatabaseManager.StaffRecord staffRecord = null;
        try {
            staffRecord = DatabaseManager.getInstance().getStaffById(staffId);
            if (staffRecord == null && staffName != null) {
                List<DatabaseManager.StaffRecord> all = DatabaseManager.getInstance().getAllStaffWithStats();
                for (DatabaseManager.StaffRecord s : all) {
                    if (s.fullName.equalsIgnoreCase(staffName) || s.username.equalsIgnoreCase(staffId)) {
                        staffRecord = s;
                        break;
                    }
                }
            }
        } catch (Exception ignored) {}

        String fullName = (staffRecord != null && staffRecord.fullName != null) ? staffRecord.fullName : (staffName != null ? staffName : "Chuyên viên tư vấn");
        String role = (staffRecord != null && staffRecord.role != null) ? ("ADMIN".equalsIgnoreCase(staffRecord.role) ? "Quản trị viên (Admin)" : "Nhân viên CSKH") : "Nhân viên CSKH";
        String skill = (staffRecord != null && staffRecord.primarySkill != null) ? staffRecord.primarySkill : "Tất cả lĩnh vực";
        int totalSessions = (staffRecord != null) ? staffRecord.sessionCount : 0;
        double avgRating = (staffRecord != null) ? staffRecord.avgRating : 5.0;

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setPrefWidth(460);
        root.setStyle("-fx-background-color: " + rootBg + ";");

        // Header
        VBox headerBox = new VBox(2);
        Label lblTitle = new Label("Hồ sơ Hỗ trợ viên");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblTitle.setStyle("-fx-text-fill: " + titleColor + ";");

        Label lblSubTitle = new Label("Thông tin chi tiết chuyên viên đang hỗ trợ phiên chat của bạn");
        lblSubTitle.setFont(Font.font("Segoe UI", 11));
        lblSubTitle.setStyle("-fx-text-fill: " + subColor + ";");
        headerBox.getChildren().addAll(lblTitle, lblSubTitle);

        // Profile Card
        VBox card = new VBox(6);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 8; -fx-border-color: " + borderColor + "; -fx-border-radius: 8;");

        card.getChildren().addAll(
                createStaticRow("Họ và tên:", fullName, textColor, subColor),
                createStaticRow("Vị trí chuyên môn:", role, textColor, subColor),
                createStaticRow("Lĩnh vực phụ trách:", skill, textColor, subColor)
        );

        // Stats Box
        HBox statsBox = new HBox(12);
        statsBox.setAlignment(Pos.CENTER);

        VBox s1 = createMiniStat("Tổng phiên đã xử lý", String.valueOf(totalSessions), brand, dark, cardBg, borderColor, subColor);
        String ratingText = totalSessions > 0 ? String.format("%.1f / 5.0 sao", avgRating) : "5.0 / 5.0 sao";
        VBox s2 = createMiniStat("Đánh giá hài lòng (CSAT)", ratingText, "#f59e0b", dark, cardBg, borderColor, subColor);

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

        root.getChildren().addAll(headerBox, card, statsBox, actionRow);

        Scene scene = new Scene(root);
        ThemeManager.applyTheme(scene);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.showAndWait();
    }

    // =========================================================================
    // HELPER METHODS: TẠO DÒNG TĨNH, DÒNG CHỈNH SỬA TEXT VÀ DÒNG ĐỔI MẬT KHẨU
    // =========================================================================

    /**
     * Tạo một dòng hiển thị tĩnh (không thể chỉnh sửa).
     */
    private static Pane createStaticRow(String key, String value, String valColor, String keyColor) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 10, 6, 10));
        row.setStyle("-fx-background-color: transparent; -fx-background-radius: 6;");

        Label lblKey = new Label(key);
        lblKey.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblKey.setStyle("-fx-text-fill: " + keyColor + ";");
        lblKey.setMinWidth(125);
        lblKey.setPrefWidth(125);

        Label lblVal = new Label(value);
        lblVal.setFont(Font.font("Segoe UI", 13));
        lblVal.setStyle("-fx-text-fill: " + valColor + ";");

        row.getChildren().addAll(lblKey, lblVal);
        return row;
    }

    @FunctionalInterface
    public interface InlineSaveHandler {
        void handleSave(String newValue, Consumer<String> onError, Runnable onSuccess);
    }

    /**
     * Tạo một dòng chỉnh sửa Text trực tiếp (Inline Editing on Hover):
     * - Khi di chuột vào dòng: hiện lớp nền mờ và icon cây bút ở cuối bên phải.
     * - Bấm vào cây bút: hiển thị ô nhập liệu trực tiếp tại vị trí đó + nút Lưu + nút Hủy nhỏ.
     */
    private static Pane createInlineEditableTextRow(
            Stage stage, String key, String initialValue,
            String valColor, String keyColor, String hoverBg,
            String inputBg, String inputBorder, String brand, boolean dark,
            InlineSaveHandler saveHandler) {

        VBox container = new VBox(4);
        container.setPadding(new Insets(6, 10, 6, 10));
        container.setStyle("-fx-background-color: transparent; -fx-background-radius: 6;");

        // View Mode Row
        HBox viewRow = new HBox(10);
        viewRow.setAlignment(Pos.CENTER_LEFT);

        Label lblKey = new Label(key);
        lblKey.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblKey.setStyle("-fx-text-fill: " + keyColor + ";");
        lblKey.setMinWidth(125);
        lblKey.setPrefWidth(125);

        Label lblVal = new Label(initialValue);
        lblVal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblVal.setStyle("-fx-text-fill: " + valColor + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnPencil = createInlinePencilButton(dark);
        viewRow.getChildren().addAll(lblKey, lblVal, spacer, btnPencil);

        // Edit Mode Box
        VBox editBox = new VBox(6);
        editBox.setVisible(false);
        editBox.setManaged(false);

        HBox editInputRow = new HBox(8);
        editInputRow.setAlignment(Pos.CENTER_LEFT);

        Label lblEditKey = new Label(key);
        lblEditKey.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblEditKey.setStyle("-fx-text-fill: " + keyColor + ";");
        lblEditKey.setMinWidth(125);
        lblEditKey.setPrefWidth(125);

        TextField txtEdit = new TextField();
        txtEdit.setStyle("-fx-background-color: " + inputBg + "; -fx-text-fill: " + valColor + "; -fx-border-color: " + inputBorder + "; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 5 8; -fx-font-size: 13px;");
        HBox.setHgrow(txtEdit, Priority.ALWAYS);

        Button btnSave = new Button("Lưu");
        btnSave.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btnSave.setStyle("-fx-background-color: " + brand + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 5 14;");

        Button btnCancel = new Button("Hủy");
        btnCancel.setFont(Font.font("Segoe UI", 12));
        btnCancel.setStyle("-fx-background-color: " + (dark ? "#3a3b3c" : "#e2e8f0") + "; -fx-text-fill: " + valColor + "; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 5 10;");

        editInputRow.getChildren().addAll(lblEditKey, txtEdit, btnSave, btnCancel);

        Label lblInlineError = new Label("");
        lblInlineError.setFont(Font.font("Segoe UI", 11));
        lblInlineError.setStyle("-fx-text-fill: #ef4444; -fx-padding: 0 0 0 135;");
        lblInlineError.setVisible(false);
        lblInlineError.setManaged(false);

        editBox.getChildren().addAll(editInputRow, lblInlineError);

        container.getChildren().addAll(viewRow, editBox);

        // State control
        AtomicReference<Boolean> isEditing = new AtomicReference<>(false);

        Runnable showViewMode = () -> {
            isEditing.set(false);
            editBox.setVisible(false);
            editBox.setManaged(false);
            lblInlineError.setVisible(false);
            lblInlineError.setManaged(false);

            viewRow.setVisible(true);
            viewRow.setManaged(true);
            btnPencil.setVisible(false);
            container.setStyle("-fx-background-color: transparent; -fx-background-radius: 6;");
            stage.sizeToScene();
        };

        Runnable showEditMode = () -> {
            isEditing.set(true);
            txtEdit.setText(lblVal.getText().equals("Chưa liên kết") ? "" : lblVal.getText());
            lblInlineError.setText("");
            lblInlineError.setVisible(false);
            lblInlineError.setManaged(false);

            viewRow.setVisible(false);
            viewRow.setManaged(false);

            editBox.setVisible(true);
            editBox.setManaged(true);
            container.setStyle("-fx-background-color: " + hoverBg + "; -fx-background-radius: 6;");
            txtEdit.requestFocus();
            stage.sizeToScene();
        };

        // Hover events
        container.setOnMouseEntered(e -> {
            if (!isEditing.get()) {
                container.setStyle("-fx-background-color: " + hoverBg + "; -fx-background-radius: 6;");
                btnPencil.setVisible(true);
            }
        });
        container.setOnMouseExited(e -> {
            if (!isEditing.get()) {
                container.setStyle("-fx-background-color: transparent; -fx-background-radius: 6;");
                btnPencil.setVisible(false);
            }
        });

        btnPencil.setOnAction(e -> showEditMode.run());
        btnCancel.setOnAction(e -> showViewMode.run());

        btnSave.setOnAction(e -> {
            String newVal = txtEdit.getText();
            saveHandler.handleSave(
                    newVal,
                    errMsg -> {
                        lblInlineError.setText(errMsg);
                        lblInlineError.setVisible(true);
                        lblInlineError.setManaged(true);
                        stage.sizeToScene();
                    },
                    () -> {
                        String display = newVal.trim().isEmpty() ? "Chưa liên kết" : newVal.trim();
                        lblVal.setText(display);
                        showViewMode.run();
                    }
            );
        });

        return container;
    }

    /**
     * Tạo dòng chỉnh sửa Mật khẩu dành cho Nhân viên:
     * - Ở chế độ tĩnh: hiển thị "Mật khẩu: ••••••••".
     * - Khi hover: hiện nền mờ và icon cây bút.
     * - Khi bấm cây bút: mở rộng khung đổi mật khẩu yêu cầu:
     *   1. Mật khẩu hiện tại
     *   2. Mật khẩu mới
     *   3. Xác nhận mật khẩu mới
     *   Kèm nút "Đổi mật khẩu" và nút "Hủy".
     */
    private static Pane createInlinePasswordChangeRow(
            Stage stage, String staffId,
            String valColor, String keyColor, String hoverBg,
            String inputBg, String inputBorder, String brand, boolean dark) {

        VBox container = new VBox(6);
        container.setPadding(new Insets(6, 10, 6, 10));
        container.setStyle("-fx-background-color: transparent; -fx-background-radius: 6;");

        // View Row
        HBox viewRow = new HBox(10);
        viewRow.setAlignment(Pos.CENTER_LEFT);

        Label lblKey = new Label("Mật khẩu:");
        lblKey.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblKey.setStyle("-fx-text-fill: " + keyColor + ";");
        lblKey.setMinWidth(125);
        lblKey.setPrefWidth(125);

        Label lblVal = new Label("••••••••");
        lblVal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblVal.setStyle("-fx-text-fill: " + valColor + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnPencil = createInlinePencilButton(dark);
        viewRow.getChildren().addAll(lblKey, lblVal, spacer, btnPencil);

        // Expanded Password Change Box
        VBox editBox = new VBox(8);
        editBox.setPadding(new Insets(10));
        editBox.setStyle("-fx-background-color: " + (dark ? "#1f2022" : "#f1f5f9") + "; -fx-background-radius: 8; -fx-border-color: " + (dark ? "#374151" : "#cbd5e1") + "; -fx-border-radius: 8;");
        editBox.setVisible(false);
        editBox.setManaged(false);

        Label lblBoxTitle = new Label("Đổi mật khẩu đăng nhập");
        lblBoxTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblBoxTitle.setStyle("-fx-text-fill: " + valColor + ";");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        Label lblOld = new Label("Mật khẩu cũ:");
        lblOld.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblOld.setStyle("-fx-text-fill: " + keyColor + ";");
        lblOld.setMinWidth(120);

        PasswordField txtOldPass = new PasswordField();
        txtOldPass.setPromptText("Nhập mật khẩu hiện tại");
        txtOldPass.setStyle("-fx-background-color: " + inputBg + "; -fx-text-fill: " + valColor + "; -fx-border-color: " + inputBorder + "; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 5 8;");
        GridPane.setHgrow(txtOldPass, Priority.ALWAYS);

        Label lblNew = new Label("Mật khẩu mới:");
        lblNew.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblNew.setStyle("-fx-text-fill: " + keyColor + ";");
        lblNew.setMinWidth(120);

        PasswordField txtNewPass = new PasswordField();
        txtNewPass.setPromptText("Nhập mật khẩu mới");
        txtNewPass.setStyle("-fx-background-color: " + inputBg + "; -fx-text-fill: " + valColor + "; -fx-border-color: " + inputBorder + "; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 5 8;");
        GridPane.setHgrow(txtNewPass, Priority.ALWAYS);

        Label lblConfirm = new Label("Xác nhận mật khẩu:");
        lblConfirm.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblConfirm.setStyle("-fx-text-fill: " + keyColor + ";");
        lblConfirm.setMinWidth(120);

        PasswordField txtConfirmPass = new PasswordField();
        txtConfirmPass.setPromptText("Nhập lại mật khẩu mới");
        txtConfirmPass.setStyle("-fx-background-color: " + inputBg + "; -fx-text-fill: " + valColor + "; -fx-border-color: " + inputBorder + "; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 5 8;");
        GridPane.setHgrow(txtConfirmPass, Priority.ALWAYS);

        grid.add(lblOld, 0, 0);
        grid.add(txtOldPass, 1, 0);
        grid.add(lblNew, 0, 1);
        grid.add(txtNewPass, 1, 1);
        grid.add(lblConfirm, 0, 2);
        grid.add(txtConfirmPass, 1, 2);

        Label lblMsg = new Label("");
        lblMsg.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblMsg.setVisible(false);
        lblMsg.setManaged(false);

        HBox btnRow = new HBox(8);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Region bSpacer = new Region();
        HBox.setHgrow(bSpacer, Priority.ALWAYS);

        Button btnSavePass = new Button("Đổi mật khẩu");
        btnSavePass.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btnSavePass.setStyle("-fx-background-color: " + brand + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 16;");

        Button btnCancelPass = new Button("Hủy");
        btnCancelPass.setFont(Font.font("Segoe UI", 12));
        btnCancelPass.setStyle("-fx-background-color: " + (dark ? "#3a3b3c" : "#e2e8f0") + "; -fx-text-fill: " + valColor + "; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 12;");

        btnRow.getChildren().addAll(lblMsg, bSpacer, btnSavePass, btnCancelPass);

        editBox.getChildren().addAll(lblBoxTitle, grid, btnRow);

        container.getChildren().addAll(viewRow, editBox);

        AtomicReference<Boolean> isEditing = new AtomicReference<>(false);

        Runnable showViewMode = () -> {
            isEditing.set(false);
            txtOldPass.clear();
            txtNewPass.clear();
            txtConfirmPass.clear();
            lblMsg.setText("");
            lblMsg.setVisible(false);
            lblMsg.setManaged(false);

            editBox.setVisible(false);
            editBox.setManaged(false);

            viewRow.setVisible(true);
            viewRow.setManaged(true);
            btnPencil.setVisible(false);
            container.setStyle("-fx-background-color: transparent; -fx-background-radius: 6;");
            stage.sizeToScene();
        };

        Runnable showEditMode = () -> {
            isEditing.set(true);
            txtOldPass.clear();
            txtNewPass.clear();
            txtConfirmPass.clear();
            lblMsg.setText("");
            lblMsg.setVisible(false);
            lblMsg.setManaged(false);

            viewRow.setVisible(false);
            viewRow.setManaged(false);

            editBox.setVisible(true);
            editBox.setManaged(true);
            container.setStyle("-fx-background-color: " + hoverBg + "; -fx-background-radius: 6;");
            txtOldPass.requestFocus();
            stage.sizeToScene();
        };

        container.setOnMouseEntered(e -> {
            if (!isEditing.get()) {
                container.setStyle("-fx-background-color: " + hoverBg + "; -fx-background-radius: 6;");
                btnPencil.setVisible(true);
            }
        });
        container.setOnMouseExited(e -> {
            if (!isEditing.get()) {
                container.setStyle("-fx-background-color: transparent; -fx-background-radius: 6;");
                btnPencil.setVisible(false);
            }
        });

        btnPencil.setOnAction(e -> showEditMode.run());
        btnCancelPass.setOnAction(e -> showViewMode.run());

        btnSavePass.setOnAction(e -> {
            String oldP = txtOldPass.getText();
            String newP = txtNewPass.getText();
            String confirmP = txtConfirmPass.getText();

            if (oldP == null || oldP.isEmpty()) {
                lblMsg.setText("Vui lòng nhập mật khẩu cũ!");
                lblMsg.setStyle("-fx-text-fill: #ef4444;");
                lblMsg.setVisible(true);
                lblMsg.setManaged(true);
                stage.sizeToScene();
                return;
            }

            boolean oldMatch = DatabaseManager.getInstance().verifyStaffPassword(staffId, oldP);
            if (!oldMatch) {
                lblMsg.setText("Mật khẩu cũ không chính xác!");
                lblMsg.setStyle("-fx-text-fill: #ef4444;");
                lblMsg.setVisible(true);
                lblMsg.setManaged(true);
                stage.sizeToScene();
                return;
            }

            if (newP == null || newP.trim().isEmpty()) {
                lblMsg.setText("Mật khẩu mới không được để trống!");
                lblMsg.setStyle("-fx-text-fill: #ef4444;");
                lblMsg.setVisible(true);
                lblMsg.setManaged(true);
                stage.sizeToScene();
                return;
            }

            if (newP.length() < 4) {
                lblMsg.setText("Mật khẩu mới tối thiểu 4 ký tự!");
                lblMsg.setStyle("-fx-text-fill: #ef4444;");
                lblMsg.setVisible(true);
                lblMsg.setManaged(true);
                stage.sizeToScene();
                return;
            }

            if (!newP.equals(confirmP)) {
                lblMsg.setText("Mật khẩu xác nhận không khớp!");
                lblMsg.setStyle("-fx-text-fill: #ef4444;");
                lblMsg.setVisible(true);
                lblMsg.setManaged(true);
                stage.sizeToScene();
                return;
            }

            boolean ok = DatabaseManager.getInstance().updateStaffPassword(staffId, newP.trim());
            if (ok) {
                showViewMode.run();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thông báo");
                alert.setHeaderText(null);
                alert.setContentText("Đã thay đổi mật khẩu thành công!");
                if (stage.isShowing()) alert.initOwner(stage);
                ThemeManager.applyTheme(alert);
                alert.showAndWait();
            } else {
                lblMsg.setText("Lỗi lưu mật khẩu mới vào cơ sở dữ liệu!");
                lblMsg.setStyle("-fx-text-fill: #ef4444;");
                lblMsg.setVisible(true);
                lblMsg.setManaged(true);
                stage.sizeToScene();
            }
        });

        return container;
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
