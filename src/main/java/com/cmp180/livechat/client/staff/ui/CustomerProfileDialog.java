package com.cmp180.livechat.client.staff.ui;

import com.cmp180.livechat.client.ui.ThemeManager;
import com.cmp180.livechat.server.DatabaseManager;
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

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Hộp thoại Hồ sơ khách hàng (Customer 360 Profile) dành cho nhân viên CSKH.
 * Tối ưu hóa trải nghiệm hỗ trợ khách hàng đa phiên:
 * - Tab 1: Tổng quan & Liên hệ (Thông tin cá nhân, chỉ số CSAT, tóm tắt phiên gần nhất).
 * - Tab 2: Dòng thời gian trò chuyện liên tục (Continuous Unified Timeline):
 *   + Tự động gom nhóm các phiên có cùng chủ đề/vấn đề (kể cả khi khách bấm "Hỏi tiếp").
 *   + Hiển thị tin nhắn xuyên suốt nhiều phiên thành một dòng thời gian duy nhất mà không cần bấm từng phiên.
 *   + Thẻ phân cách phiên (Session Divider Banner) hiển thị rõ thời gian, nhân viên phụ trách và đánh giá của từng phiên.
 */
public class CustomerProfileDialog {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public static void show(Window owner, String customerId, String customerDisplayName) {
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
        String phone = (customer != null && customer.phoneOrEmail != null) ? customer.phoneOrEmail : customerId;
        String fullName = (customer != null && customer.fullName != null) ? customer.fullName : customerDisplayName;
        String email = (customer != null && customer.email != null && !customer.email.trim().isEmpty()) ? customer.email : "Chưa liên kết";
        String tier = (customer != null && customer.tier != null) ? customer.tier : "Thường";
        String regDate = (customer != null && customer.createdAt > 0) ? DATE_FORMAT.format(new Date(customer.createdAt)) : "Hôm nay";

        final List<DatabaseManager.SessionRecord> pastSessions;
        {
            List<DatabaseManager.SessionRecord> tmp;
            try {
                tmp = DatabaseManager.getInstance().getSessionsByCustomer(initialCustDbId);
            } catch (Exception e) {
                tmp = new ArrayList<>();
            }
            pastSessions = (tmp != null) ? tmp : new ArrayList<>();
        }

        int totalSessions = pastSessions.size();
        int ratedCount = 0;
        int ratingSum = 0;
        int resolvedCount = 0;
        for (DatabaseManager.SessionRecord s : pastSessions) {
            if (s.rating > 0) {
                ratingSum += s.rating;
                ratedCount++;
            }
            if ("CLOSED".equalsIgnoreCase(s.status) || "RESOLVED".equalsIgnoreCase(s.status) || "Đã đóng".equalsIgnoreCase(s.status)) {
                resolvedCount++;
            }
        }
        String avgRatingStr = ratedCount > 0 ? String.format("%.1f/5.0 sao (%d lượt)", (double) ratingSum / ratedCount, ratedCount) : "Chưa có đánh giá";

        // Màu sắc giao diện
        String rootBg = dark ? "#18191a" : "#f8fafc";
        String cardBg = dark ? "#242526" : "#ffffff";
        String borderColor = dark ? "#3a3b3c" : "#e2e8f0";
        String titleColor = dark ? "#f1f5f9" : "#0f172a";
        String subColor = dark ? "#94a3b8" : "#64748b";
        String textColor = dark ? "#e4e6eb" : "#1e293b";

        VBox root = new VBox(12);
        root.setPadding(new Insets(16, 20, 16, 20));
        root.setPrefWidth(720);
        root.setPrefHeight(650);
        root.setStyle("-fx-background-color: " + rootBg + ";");

        // Header Title
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Circle avatarCircle = new Circle(18, Color.web(brand));
        Label avatarText = new Label(fullName.isEmpty() ? "K" : fullName.substring(0, 1).toUpperCase());
        avatarText.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        StackPane avatarPane = new StackPane(avatarCircle, avatarText);

        VBox custTitleBox = new VBox(2);
        Label lblName = new Label(fullName);
        lblName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblName.setStyle("-fx-text-fill: " + titleColor + ";");

        Label lblSub = new Label("SĐT: " + phone + "  •  Email: " + email);
        lblSub.setFont(Font.font("Segoe UI", 12));
        lblSub.setStyle("-fx-text-fill: " + subColor + ";");
        custTitleBox.getChildren().addAll(lblName, lblSub);

        Region spacerHeader = new Region();
        HBox.setHgrow(spacerHeader, Priority.ALWAYS);

        Label lblBadgeTier = new Label("Hạng: " + tier.toUpperCase());
        lblBadgeTier.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        applyBadgeStyle(lblBadgeTier, tier, dark);

        headerRow.getChildren().addAll(avatarPane, custTitleBox, spacerHeader, lblBadgeTier);

        // Segmented Tabs Header
        HBox tabHeader = new HBox(6);
        tabHeader.setPadding(new Insets(4, 0, 4, 0));

        Button btnTabOverview = new Button("Tổng quan & Liên hệ");
        btnTabOverview.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        Button btnTabTimeline = new Button("Dòng thời gian trò chuyện (" + totalSessions + " phiên)");
        btnTabTimeline.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        tabHeader.getChildren().addAll(btnTabOverview, btnTabTimeline);

        // Content Area (StackPane chứa 2 Tab)
        StackPane contentPane = new StackPane();
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        // =====================================================================
        // TAB 1: TỔNG QUAN & LIÊN HỆ
        // =====================================================================
        VBox overviewView = new VBox(12);
        overviewView.setPadding(new Insets(4, 0, 0, 0));

        // Thông tin cá nhân
        VBox infoCard = new VBox(8);
        infoCard.setPadding(new Insets(14));
        infoCard.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 8; -fx-border-color: " + borderColor + "; -fx-border-radius: 8;");

        Label lblInfoTitle = new Label("Thông tin liên hệ & Hồ sơ");
        lblInfoTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblInfoTitle.setStyle("-fx-text-fill: " + textColor + ";");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(8);

        addInfoRow(grid, 0, "Họ và tên:", fullName, textColor, subColor);
        addInfoRow(grid, 1, "Số điện thoại:", phone, textColor, subColor);
        addInfoRow(grid, 2, "Email liên hệ:", email, textColor, subColor);
        addInfoRow(grid, 3, "Ngày tham gia:", regDate, textColor, subColor);

        infoCard.getChildren().addAll(lblInfoTitle, grid);

        // Thống kê CSAT & Phiên
        HBox statsBar = new HBox(12);
        statsBar.setAlignment(Pos.CENTER);

        VBox stat1 = createStatBox("Tổng số phiên hỗ trợ", String.valueOf(totalSessions), brand, dark, cardBg, borderColor, subColor);
        VBox stat2 = createStatBox("Đánh giá CSAT trung bình", avgRatingStr, "#f59e0b", dark, cardBg, borderColor, subColor);
        VBox stat3 = createStatBox("Phiên đã giải quyết", resolvedCount + " phiên", "#10b981", dark, cardBg, borderColor, subColor);

        HBox.setHgrow(stat1, Priority.ALWAYS);
        HBox.setHgrow(stat2, Priority.ALWAYS);
        HBox.setHgrow(stat3, Priority.ALWAYS);
        statsBar.getChildren().addAll(stat1, stat2, stat3);

        // Tóm tắt phiên gần nhất
        VBox recentCard = new VBox(8);
        recentCard.setPadding(new Insets(14));
        recentCard.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 8; -fx-border-color: " + borderColor + "; -fx-border-radius: 8;");

        Label lblRecentTitle = new Label("Phiên hỗ trợ gần nhất");
        lblRecentTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblRecentTitle.setStyle("-fx-text-fill: " + textColor + ";");

        if (!pastSessions.isEmpty()) {
            DatabaseManager.SessionRecord lastSession = pastSessions.get(0);
            String lastTime = lastSession.createdAt > 0 ? DATE_FORMAT.format(new Date(lastSession.createdAt)) : "Không rõ";
            String lastTopic = (lastSession.issueTopic != null && !lastSession.issueTopic.isEmpty()) ? lastSession.issueTopic : "Tư vấn chung";
            String lastStaff = (lastSession.staffName != null && !lastSession.staffName.isEmpty()) ? lastSession.staffName : "Chưa rõ";
            String lastRating = lastSession.rating > 0 ? (lastSession.rating + "/5 sao") : "Chưa chấm điểm";

            GridPane recentGrid = new GridPane();
            recentGrid.setHgap(16);
            recentGrid.setVgap(6);

            addInfoRow(recentGrid, 0, "Thời gian:", lastTime, textColor, subColor);
            addInfoRow(recentGrid, 1, "Chủ đề:", lastTopic, textColor, subColor);
            addInfoRow(recentGrid, 2, "Nhân viên:", lastStaff, textColor, subColor);
            addInfoRow(recentGrid, 3, "Đánh giá:", lastRating, textColor, subColor);

            if (lastSession.feedback != null && !lastSession.feedback.trim().isEmpty()) {
                addInfoRow(recentGrid, 4, "Nhận xét:", "\"" + lastSession.feedback.trim() + "\"", (dark ? "#38bdf8" : "#0284c7"), subColor);
            }

            recentCard.getChildren().addAll(lblRecentTitle, recentGrid);
        } else {
            Label lblNoSession = new Label("Khách hàng mới - Chưa có phiên hỗ trợ nào trước đây.");
            lblNoSession.setFont(Font.font("Segoe UI", 12));
            lblNoSession.setStyle("-fx-text-fill: " + subColor + "; -fx-font-style: italic;");
            recentCard.getChildren().addAll(lblRecentTitle, lblNoSession);
        }

        overviewView.getChildren().addAll(infoCard, statsBar, recentCard);

        // =====================================================================
        // TAB 2: DÒNG THỜI GIAN TRÒ CHUYỆN LIÊN TỤC (CONTINUOUS TIMELINE)
        // =====================================================================
        VBox timelineView = new VBox(10);
        timelineView.setPadding(new Insets(4, 0, 0, 0));

        // Thanh công cụ lọc chủ đề & phiên
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(6, 10, 6, 10));
        toolbar.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 8; -fx-border-color: " + borderColor + "; -fx-border-radius: 8;");

        Label lblFilterTopic = new Label("Lọc theo chủ đề:");
        lblFilterTopic.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblFilterTopic.setStyle("-fx-text-fill: " + textColor + ";");

        ComboBox<String> cbTopicFilter = new ComboBox<>();
        cbTopicFilter.setStyle("-fx-font-size: 12px;");

        // Nhóm các phiên theo chủ đề gốc (Topic Normalization: gộp các phiên "Hỏi tiếp:")
        Map<String, List<DatabaseManager.SessionRecord>> topicGroups = groupSessionsByTopic(pastSessions);
        cbTopicFilter.getItems().add("Tất cả chủ đề (" + totalSessions + " phiên)");
        for (Map.Entry<String, List<DatabaseManager.SessionRecord>> entry : topicGroups.entrySet()) {
            cbTopicFilter.getItems().add(entry.getKey() + " (" + entry.getValue().size() + " phiên)");
        }
        cbTopicFilter.getSelectionModel().selectFirst();

        Region spacerToolbar = new Region();
        HBox.setHgrow(spacerToolbar, Priority.ALWAYS);

        Label lblViewMode = new Label("Chế độ xem:");
        lblViewMode.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblViewMode.setStyle("-fx-text-fill: " + subColor + ";");

        ComboBox<String> cbViewMode = new ComboBox<>();
        cbViewMode.setStyle("-fx-font-size: 12px;");
        cbViewMode.getItems().addAll("Dòng thời gian liên tục", "Từng phiên riêng biệt");
        cbViewMode.getSelectionModel().selectFirst();

        String toolBtnStyle = "-fx-background-color: " + (dark ? "#333740" : "#f1f5f9") + "; -fx-text-fill: " + textColor + "; -fx-border-color: " + borderColor + "; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 4 10;";
        Button btnExpandAll = new Button("Mở rộng tất cả");
        btnExpandAll.setStyle(toolBtnStyle);

        Button btnCollapseAll = new Button("Thu gọn tất cả");
        btnCollapseAll.setStyle(toolBtnStyle);

        toolbar.getChildren().addAll(lblFilterTopic, cbTopicFilter, spacerToolbar, btnExpandAll, btnCollapseAll, lblViewMode, cbViewMode);

        // Container cuộn chứa toàn bộ tin nhắn dòng thời gian
        VBox timelineContainer = new VBox(10);
        timelineContainer.setPadding(new Insets(10));
        timelineContainer.setStyle("-fx-background-color: " + cardBg + ";");

        ScrollPane scrollTimeline = new ScrollPane(timelineContainer);
        scrollTimeline.setFitToWidth(true);
        VBox.setVgrow(scrollTimeline, Priority.ALWAYS);
        scrollTimeline.setStyle("-fx-background-color: transparent; -fx-background: " + cardBg + "; -fx-border-color: " + borderColor + "; -fx-border-radius: 8;");

        // Helper render dòng thời gian liên tục
        Runnable refreshTimeline = () -> {
            String selectedTopicItem = cbTopicFilter.getSelectionModel().getSelectedItem();
            boolean isContinuous = "Dòng thời gian liên tục".equals(cbViewMode.getSelectionModel().getSelectedItem());

            List<DatabaseManager.SessionRecord> targetSessions = new ArrayList<>();
            if (selectedTopicItem == null || selectedTopicItem.startsWith("Tất cả chủ đề")) {
                targetSessions.addAll(pastSessions);
            } else {
                for (Map.Entry<String, List<DatabaseManager.SessionRecord>> entry : topicGroups.entrySet()) {
                    if (selectedTopicItem.startsWith(entry.getKey())) {
                        targetSessions.addAll(entry.getValue());
                        break;
                    }
                }
            }

            // Sắp xếp các phiên theo trình tự thời gian (từ cũ đến mới) để đọc cuộc trò chuyện liền mạch từ đầu tới cuối
            targetSessions.sort(Comparator.comparingLong(s -> s.createdAt));

            timelineContainer.getChildren().clear();

            if (targetSessions.isEmpty()) {
                Label empty = new Label("Khách hàng chưa có lịch sử phiên hỗ trợ nào.");
                empty.setFont(Font.font("Segoe UI", 12));
                empty.setStyle("-fx-text-fill: " + subColor + "; -fx-font-style: italic; -fx-padding: 20;");
                timelineContainer.getChildren().add(empty);
                return;
            }

            List<Runnable> expandActions = new ArrayList<>();
            List<Runnable> collapseActions = new ArrayList<>();

            // Render từng phiên nối tiếp nhau kèm Session Divider Banner có thể đóng/mở (Accordion)
            for (int i = 0; i < targetSessions.size(); i++) {
                DatabaseManager.SessionRecord session = targetSessions.get(i);
                int sessionIndex = i + 1;

                List<DatabaseManager.MessageRecord> msgs = DatabaseManager.getInstance().getMessagesBySession(session.sessionId);
                int msgCount = (msgs != null) ? msgs.size() : 0;

                Label lblToggle = new Label("▶ Xem " + msgCount + " tin nhắn");
                lblToggle.setFont(Font.font("Segoe UI", 11));
                lblToggle.setStyle("-fx-text-fill: " + subColor + "; -fx-padding: 2 6; -fx-background-radius: 4;");

                VBox sessionBanner = createSessionDividerBanner(session, sessionIndex, targetSessions.size(), lblToggle, dark, brand, textColor, subColor);

                VBox msgsBox = new VBox(6);
                msgsBox.setPadding(new Insets(4, 0, 8, 16));
                msgsBox.setVisible(false);
                msgsBox.setManaged(false);

                if (msgCount == 0) {
                    Label noMsgLbl = new Label("  (Phiên này không có tin nhắn trao đổi)");
                    noMsgLbl.setFont(Font.font("Segoe UI", 11));
                    noMsgLbl.setStyle("-fx-text-fill: " + subColor + "; -fx-font-style: italic; -fx-padding: 4 0 4 8;");
                    msgsBox.getChildren().add(noMsgLbl);
                } else {
                    for (DatabaseManager.MessageRecord m : msgs) {
                        msgsBox.getChildren().add(createMessageBubble(m, dark, brand, textColor, subColor));
                    }
                }

                Runnable toggleSession = () -> {
                    boolean willShow = !msgsBox.isVisible();
                    msgsBox.setVisible(willShow);
                    msgsBox.setManaged(willShow);
                    if (willShow) {
                        lblToggle.setText("▼ Thu gọn (" + msgCount + " tin nhắn)");
                        lblToggle.setStyle("-fx-text-fill: " + brand + "; -fx-font-weight: bold; -fx-font-size: 11px;");
                    } else {
                        lblToggle.setText("▶ Xem " + msgCount + " tin nhắn");
                        lblToggle.setStyle("-fx-text-fill: " + subColor + "; -fx-font-size: 11px;");
                    }
                };

                sessionBanner.setCursor(javafx.scene.Cursor.HAND);
                sessionBanner.setOnMouseClicked(e -> toggleSession.run());

                expandActions.add(() -> {
                    if (!msgsBox.isVisible()) toggleSession.run();
                });
                collapseActions.add(() -> {
                    if (msgsBox.isVisible()) toggleSession.run();
                });

                VBox sessionCard = new VBox(4);
                sessionCard.getChildren().addAll(sessionBanner, msgsBox);
                timelineContainer.getChildren().add(sessionCard);
            }

            btnExpandAll.setOnAction(e -> expandActions.forEach(Runnable::run));
            btnCollapseAll.setOnAction(e -> collapseActions.forEach(Runnable::run));
        };

        cbTopicFilter.setOnAction(e -> refreshTimeline.run());
        cbViewMode.setOnAction(e -> refreshTimeline.run());

        // Khởi động render dòng thời gian
        refreshTimeline.run();

        timelineView.getChildren().addAll(toolbar, scrollTimeline);

        // =====================================================================
        // TAB SWITCHING LOGIC (SEGMENTED TABS)
        // =====================================================================
        String tabActiveStyle = "-fx-background-color: " + brand + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 16;";
        String tabInactiveStyle = "-fx-background-color: " + (dark ? "#25272a" : "#f1f5f9") + "; -fx-text-fill: " + (dark ? "#cbd5e1" : "#475569") + "; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 16; -fx-border-color: " + borderColor + "; -fx-border-radius: 6;";

        Runnable selectOverviewTab = () -> {
            btnTabOverview.setStyle(tabActiveStyle);
            btnTabTimeline.setStyle(tabInactiveStyle);
            contentPane.getChildren().setAll(overviewView);
        };

        Runnable selectTimelineTab = () -> {
            btnTabOverview.setStyle(tabInactiveStyle);
            btnTabTimeline.setStyle(tabActiveStyle);
            contentPane.getChildren().setAll(timelineView);
        };

        btnTabOverview.setOnAction(e -> selectOverviewTab.run());
        btnTabTimeline.setOnAction(e -> selectTimelineTab.run());

        // Mặc định chọn Tab 1 (Tổng quan)
        selectOverviewTab.run();

        // Bottom Action Row
        HBox actionRow = new HBox();
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnClose = new Button("Đóng");
        btnClose.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btnClose.setStyle("-fx-background-color: " + brand + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 7 24;");
        btnClose.setOnAction(e -> stage.close());

        actionRow.getChildren().add(btnClose);

        root.getChildren().addAll(headerRow, tabHeader, contentPane, actionRow);

        Scene scene = new Scene(root);
        ThemeManager.applyTheme(scene);
        stage.setScene(scene);
        stage.showAndWait();
    }

    // =========================================================================
    // HELPER: GOM NHÓM PHIÊN THEO CHỦ ĐỀ GỐC (TOPIC NORMALIZATION)
    // =========================================================================
    private static Map<String, List<DatabaseManager.SessionRecord>> groupSessionsByTopic(List<DatabaseManager.SessionRecord> sessions) {
        Map<String, List<DatabaseManager.SessionRecord>> map = new LinkedHashMap<>();
        if (sessions == null) return map;

        for (DatabaseManager.SessionRecord s : sessions) {
            String norm = normalizeTopic(s.issueTopic);
            map.computeIfAbsent(norm, k -> new ArrayList<>()).add(s);
        }
        return map;
    }

    /**
     * Chuẩn hóa chủ đề gốc bằng cách loại bỏ các tiền tố "Hỏi tiếp:", "Re:"
     * để gộp các phiên liên quan đến cùng một vấn đề lại với nhau.
     */
    public static String normalizeTopic(String topic) {
        if (topic == null || topic.trim().isEmpty()) return "Tư vấn chung";
        String clean = topic.trim();
        while (clean.toLowerCase().startsWith("hỏi tiếp:") || clean.toLowerCase().startsWith("re:")) {
            if (clean.toLowerCase().startsWith("hỏi tiếp:")) {
                clean = clean.substring("hỏi tiếp:".length()).trim();
            } else if (clean.toLowerCase().startsWith("re:")) {
                clean = clean.substring("re:".length()).trim();
            }
        }
        return clean.isEmpty() ? "Tư vấn chung" : clean;
    }

    /**
     * Tạo Thẻ phân cách phiên (Session Divider Banner) trên dòng thời gian.
     */
    private static VBox createSessionDividerBanner(
            DatabaseManager.SessionRecord s, int sessionNum, int total,
            Label lblToggle,
            boolean dark, String brand, String textColor, String subColor) {

        VBox banner = new VBox(4);
        banner.setPadding(new Insets(8, 12, 8, 12));
        String bannerBg = dark ? "#2a2d34" : "#e2e8f0";
        String bannerBorder = dark ? "#3f4450" : "#cbd5e1";
        banner.setStyle("-fx-background-color: " + bannerBg + "; -fx-background-radius: 8; -fx-border-color: " + bannerBorder + "; -fx-border-radius: 8;");

        // Hover effect for interactive accordion banner
        banner.setOnMouseEntered(e -> banner.setStyle("-fx-background-color: " + (dark ? "#333740" : "#dbeafe") + "; -fx-background-radius: 8; -fx-border-color: " + brand + "; -fx-border-radius: 8;"));
        banner.setOnMouseExited(e -> banner.setStyle("-fx-background-color: " + bannerBg + "; -fx-background-radius: 8; -fx-border-color: " + bannerBorder + "; -fx-border-radius: 8;"));

        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label lblBadge = new Label("Phiên #" + sessionNum + "/" + total);
        lblBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblBadge.setStyle("-fx-background-color: " + brand + "; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 6;");

        String time = s.createdAt > 0 ? DATE_FORMAT.format(new Date(s.createdAt)) : "Không rõ";
        Label lblTime = new Label(time);
        lblTime.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblTime.setStyle("-fx-text-fill: " + textColor + ";");

        Label lblTopic = new Label("•  Chủ đề: " + (s.issueTopic != null ? s.issueTopic : "Chung"));
        lblTopic.setFont(Font.font("Segoe UI", 12));
        lblTopic.setStyle("-fx-text-fill: " + textColor + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String staff = (s.staffName != null && !s.staffName.isEmpty()) ? s.staffName : "CSKH";
        Label lblStaff = new Label("CSKH: " + staff);
        lblStaff.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblStaff.setStyle("-fx-text-fill: " + (dark ? "#38bdf8" : "#0284c7") + ";");

        topRow.getChildren().addAll(lblBadge, lblTime, lblTopic, spacer, lblStaff, lblToggle);

        // Hàng trạng thái & Đánh giá CSAT
        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        Label lblStatus = new Label("Trạng thái: " + (s.status != null ? s.status : "Đã xong"));
        lblStatus.setFont(Font.font("Segoe UI", 11));
        lblStatus.setStyle("-fx-text-fill: " + subColor + ";");

        String ratingStr = s.rating > 0 ? ("Đánh giá: " + s.rating + "/5 ★") : "Chưa chấm điểm";
        Label lblRating = new Label(ratingStr);
        lblRating.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblRating.setStyle("-fx-text-fill: " + (s.rating > 0 ? "#f59e0b" : subColor) + ";");

        bottomRow.getChildren().addAll(lblStatus, lblRating);

        if (s.feedback != null && !s.feedback.trim().isEmpty()) {
            Label lblFb = new Label("Nhận xét của khách: \"" + s.feedback.trim() + "\"");
            lblFb.setFont(Font.font("Segoe UI", 11));
            lblFb.setStyle("-fx-text-fill: " + (dark ? "#4ade80" : "#16a34a") + "; -fx-font-style: italic;");
            bottomRow.getChildren().add(lblFb);
        }

        banner.getChildren().addAll(topRow, bottomRow);
        return banner;
    }

    /**
     * Tạo một bong bóng tin nhắn hiển thị trong dòng thời gian.
     */
    private static VBox createMessageBubble(DatabaseManager.MessageRecord m, boolean dark, String brand, String textColor, String subColor) {
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

        String timeStr = m.timestamp > 0 ? DATE_FORMAT.format(new Date(m.timestamp)) : "";

        // Nếu là bản ghi cuộc gọi thoại
        if (content.startsWith("📞")) {
            boolean isCallEnded = content.contains("kết thúc") || content.contains("Thời lượng");
            String callBg = isCallEnded ? (dark ? "rgba(16, 185, 129, 0.15)" : "#d1fae5") : (dark ? "rgba(239, 68, 68, 0.15)" : "#fee2e2");
            String callBorder = isCallEnded ? (dark ? "#059669" : "#6ee7b7") : (dark ? "#dc2626" : "#fca5a5");
            String callFg = isCallEnded ? (dark ? "#34d399" : "#059669") : (dark ? "#f87171" : "#dc2626");

            VBox callCard = new VBox(2);
            callCard.setAlignment(Pos.CENTER);
            callCard.setPadding(new Insets(6, 14, 6, 14));
            callCard.setStyle("-fx-background-color: " + callBg + "; -fx-background-radius: 14; -fx-border-color: " + callBorder + "; -fx-border-radius: 14; -fx-border-width: 1;");

            Label lblCall = new Label(content);
            lblCall.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            lblCall.setStyle("-fx-text-fill: " + callFg + ";");

            Label lblCallTime = new Label(timeStr);
            lblCallTime.setFont(Font.font("Segoe UI", 10));
            lblCallTime.setStyle("-fx-text-fill: " + subColor + ";");

            callCard.getChildren().addAll(lblCall, lblCallTime);
            return callCard;
        }

        VBox card = new VBox(3);
        card.setPadding(new Insets(6, 10, 6, 10));

        boolean isCustomer = "CUSTOMER".equalsIgnoreCase(m.senderRole);
        String cardBg = isCustomer ? (dark ? "#1c2438" : "#eff6ff") : (dark ? "#1c2e26" : "#f0fdf4");
        String border = isCustomer ? (dark ? "#2563eb" : "#bfdbfe") : (dark ? "#16a34a" : "#bbf7d0");
        card.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 6; -fx-border-color: " + border + "; -fx-border-radius: 6;");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        String roleTag = isCustomer ? "Khách hàng" : ("STAFF".equalsIgnoreCase(m.senderRole) ? "CSKH" : "Hệ thống");
        String roleColor = isCustomer ? "#3b82f6" : ("STAFF".equalsIgnoreCase(m.senderRole) ? "#10b981" : "#f59e0b");

        Label lblSender = new Label((m.senderName != null ? m.senderName : roleTag) + " [" + roleTag + "]");
        lblSender.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblSender.setStyle("-fx-text-fill: " + roleColor + ";");

        Label lblTime = new Label(timeStr);
        lblTime.setFont(Font.font("Segoe UI", 10));
        lblTime.setStyle("-fx-text-fill: " + subColor + ";");

        header.getChildren().addAll(lblSender, lblTime);

        Label lblContent = new Label(content);
        lblContent.setWrapText(true);
        lblContent.setFont(Font.font("Segoe UI", 12));
        lblContent.setStyle("-fx-text-fill: " + textColor + ";");

        card.getChildren().addAll(header, lblContent);
        return card;
    }

    private static void addInfoRow(GridPane grid, int row, String label, String value, String textColor, String subColor) {
        Label lblKey = new Label(label);
        lblKey.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblKey.setStyle("-fx-text-fill: " + subColor + ";");
        lblKey.setMinWidth(110);

        Label lblVal = new Label(value);
        lblVal.setFont(Font.font("Segoe UI", 13));
        lblVal.setStyle("-fx-text-fill: " + textColor + ";");

        grid.add(lblKey, 0, row);
        grid.add(lblVal, 1, row);
    }

    private static VBox createStatBox(String title, String val, String valColor, boolean dark, String cardBg, String borderColor, String subColor) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
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
