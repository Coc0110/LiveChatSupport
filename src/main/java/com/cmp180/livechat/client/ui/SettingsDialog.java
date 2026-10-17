package com.cmp180.livechat.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import com.cmp180.livechat.client.customer.context.CustomerContext;
import com.cmp180.livechat.client.staff.context.StaffContext;

public class SettingsDialog {

    private enum Tab {
        APPEARANCE,
        NETWORK
    }

    public static void show(Window owner) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.setTitle("Cài đặt");
        stage.setResizable(false);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPrefSize(720, 540);

        // --- Sidebar (Left Column) ---
        VBox sidebar = new VBox(6);
        sidebar.setPrefWidth(210);
        sidebar.setPadding(new Insets(20, 12, 16, 12));

        Label lblSidebarTitle = new Label("Cài đặt");
        lblSidebarTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblSidebarTitle.setPadding(new Insets(0, 8, 12, 8));

        Button btnTabAppearance = createSidebarTab("Giao diện", 
            "M12 3C7.02944 3 3 7.02944 3 12C3 16.9706 7.02944 21 12 21C13.1046 21 14 20.1046 14 19C14 18.52 13.82 18.08 13.53 17.74C13.25 17.41 13.08 16.99 13.08 16.5C13.08 15.4 13.98 14.5 15.08 14.5H16.5C18.99 14.5 21 12.49 21 10C21 6.13 16.97 3 12 3ZM6.5 12C5.67 12 5 11.33 5 10.5C5 9.67 5.67 9 6.5 9C7.33 9 8 9.67 8 10.5C8 11.33 7.33 12 6.5 12ZM9.5 8C8.67 8 8 7.33 8 6.5C8 5.67 8.67 5 9.5 5C10.33 5 11 5.67 11 6.5C11 7.33 10.33 8 9.5 8ZM14.5 8C13.67 8 13 7.33 13 6.5C13 5.67 13.67 5 14.5 5C15.33 5 16 5.67 16 6.5C16 7.33 15.33 8 14.5 8ZM17.5 12C16.67 12 16 11.33 16 10.5C16 9.67 16.67 9 17.5 9C18.33 9 19 9.67 19 10.5C19 11.33 18.33 12 17.5 12Z");

        Button btnTabNetwork = createSidebarTab("Kết nối mạng", 
            "M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM11 19.93C7.05 19.44 4 16.08 4 12C4 11.38 4.08 10.79 4.21 10.21L9 15V16C9 17.1 9.9 18 11 18V19.93ZM17.9 17.39C17.64 16.58 16.9 16 16 16H15V13C15 12.45 14.55 12 14 12H8V10H10C10.55 10 11 9.55 11 9V7H13C14.1 7 15 6.1 15 5V4.59C17.93 5.78 20 8.65 20 12C20 14.08 19.2 15.97 17.9 17.39Z");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Separator sepSidebar = new Separator();
        sepSidebar.setPadding(new Insets(4, 0, 4, 0));

        Button btnExitApp = createSidebarTab("Thoát ứng dụng", 
            "M13 3H11V13H13V3ZM17.83 5.17L16.41 6.59C18.05 7.91 19 9.87 19 12C19 15.87 15.87 19 12 19C8.13 19 5 15.87 5 12C5 9.87 5.95 7.91 7.58 6.58L6.17 5.17C4.23 6.82 3 9.26 3 12C3 16.97 7.03 21 12 21C16.97 21 21 16.97 21 12C21 9.26 19.77 6.82 17.83 5.17Z");

        setupTabHover(btnTabAppearance);
        setupTabHover(btnTabNetwork);
        setupExitBtn(btnExitApp);

        sidebar.getChildren().addAll(lblSidebarTitle, btnTabAppearance, btnTabNetwork, spacer, sepSidebar, btnExitApp);
        mainLayout.setLeft(sidebar);

        // --- Content Area (Right Column) ---
        VBox rightPane = new VBox(15);
        rightPane.setPadding(new Insets(20, 24, 20, 24));
        HBox.setHgrow(rightPane, Priority.ALWAYS);

        // Top bar: Active Tab Title & Close Button
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        Label lblContentTitle = new Label("Cài đặt giao diện");
        lblContentTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Button btnClose = new Button("✕");
        btnClose.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnClose.setPrefSize(30, 30);
        btnClose.setMinSize(30, 30);
        btnClose.setCursor(javafx.scene.Cursor.HAND);
        btnClose.setOnAction(e -> stage.close());
        btnClose.setOnMouseEntered(e -> {
            boolean dark = ThemeManager.isDarkMode();
            btnClose.setStyle("-fx-background-color: " + (dark ? "rgba(255,255,255,0.12)" : "rgba(0,0,0,0.06)") 
                + "; -fx-text-fill: " + (dark ? "#ffffff" : "#1c1e21") + "; -fx-background-radius: 15;");
        });
        btnClose.setOnMouseExited(e -> {
            boolean dark = ThemeManager.isDarkMode();
            btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: " 
                + (dark ? "#b0b3b8" : "#65676b") + "; -fx-background-radius: 15;");
        });

        topBar.getChildren().addAll(lblContentTitle, topSpacer, btnClose);

        // Dynamic Tab Body Container
        StackPane tabContentContainer = new StackPane();
        VBox.setVgrow(tabContentContainer, Priority.ALWAYS);

        rightPane.getChildren().addAll(topBar, tabContentContainer);
        mainLayout.setCenter(rightPane);

        // --- Build Content for Tab 1: Appearance ---
        VBox appearanceContent = new VBox(14);

        // Appearance Mode (Light / Dark)
        Label lblAppHeader = new Label("CHẾ ĐỘ HIỂN THỊ");
        lblAppHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));

        HBox modeCardsBox = new HBox(16);
        VBox cardLight = createModeCard("Sáng", false);
        VBox cardDark = createModeCard("Tối", true);
        HBox.setHgrow(cardLight, Priority.ALWAYS);
        HBox.setHgrow(cardDark, Priority.ALWAYS);

        cardLight.setOnMouseClicked(e -> ThemeManager.setDarkMode(false));
        cardDark.setOnMouseClicked(e -> ThemeManager.setDarkMode(true));
        modeCardsBox.getChildren().addAll(cardLight, cardDark);

        // Theme Color Presets & ColorPicker
        Label lblColorHeader = new Label("MÀU SẮC CHỦ ĐẠO");
        lblColorHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));

        FlowPane presetPane = new FlowPane(8, 8);
        presetPane.setPrefWrapLength(450);

        HBox customBox = new HBox(10);
        customBox.setAlignment(Pos.CENTER_LEFT);
        Label lblCustom = new Label("Tùy chọn màu riêng:");
        lblCustom.setFont(Font.font("Segoe UI", 13));

        ColorPicker colorPicker = new ColorPicker(Color.web(ThemeManager.getPrimaryColor()));
        colorPicker.setStyle("-fx-cursor: hand; -fx-background-radius: 8;");
        colorPicker.setOnAction(e -> {
            Color c = colorPicker.getValue();
            String hex = String.format("#%02X%02X%02X", (int)(c.getRed() * 255), (int)(c.getGreen() * 255), (int)(c.getBlue() * 255));
            ThemeManager.setPrimaryColor(hex);
        });
        customBox.getChildren().addAll(lblCustom, colorPicker);

        // Live Preview Box
        Label lblPreviewHeader = new Label("XEM TRƯỚC BONG BÓNG CHAT");
        lblPreviewHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));

        VBox previewBox = new VBox(8);
        previewBox.setPadding(new Insets(10, 14, 10, 14));
        previewBox.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-width: 1;");

        HBox inRow = new HBox();
        inRow.setAlignment(Pos.CENTER_LEFT);
        Label inBubble = new Label("Xin chào! Bạn thấy giao diện này thế nào?");
        inBubble.setPadding(new Insets(6, 12, 6, 12));
        inBubble.setStyle("-fx-background-radius: 12 12 12 0; -fx-font-size: 13px;");
        inRow.getChildren().add(inBubble);

        HBox outRow = new HBox();
        outRow.setAlignment(Pos.CENTER_RIGHT);
        Label outBubble = new Label("Màu sắc trông rất hiện đại & bắt mắt! ✨");
        outBubble.setPadding(new Insets(6, 12, 6, 12));
        outBubble.setStyle("-fx-text-fill: white; -fx-background-radius: 12 12 0 12; -fx-font-size: 13px;");
        outRow.getChildren().add(outBubble);

        previewBox.getChildren().addAll(inRow, outRow);

        Button btnResetTheme = new Button("Khôi phục mặc định");
        btnResetTheme.setCursor(javafx.scene.Cursor.HAND);
        btnResetTheme.setOnAction(e -> ThemeManager.resetToDefault());

        appearanceContent.getChildren().addAll(
            lblAppHeader, modeCardsBox,
            lblColorHeader, presetPane, customBox,
            lblPreviewHeader, previewBox,
            btnResetTheme
        );

        // --- Build Content for Tab 2: Network / IP & Port ---
        VBox networkContent = new VBox(14);

        Label lblNetHeader = new Label("CẤU HÌNH MÁY CHỦ (SERVER)");
        lblNetHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));

        Label lblNetDesc = new Label("Điền thông tin IP và Cổng kết nối đến máy chủ LiveChat:");
        lblNetDesc.setFont(Font.font("Segoe UI", 13));

        VBox formBox = new VBox(10);
        formBox.setPadding(new Insets(14));
        formBox.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-width: 1;");

        VBox hostBox = new VBox(4);
        Label lblHost = new Label("Địa chỉ IP / Domain máy chủ (Host):");
        lblHost.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        TextField txtHost = new TextField(ServerConfig.getHost());
        txtHost.setPrefHeight(38);
        txtHost.setPromptText("Ví dụ: 127.0.0.1 hoặc abc.playit.gg");
        txtHost.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 0 12; -fx-font-size: 13px;");
        hostBox.getChildren().addAll(lblHost, txtHost);

        VBox portBox = new VBox(4);
        Label lblPort = new Label("Cổng máy chủ (Port):");
        lblPort.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        TextField txtPort = new TextField(String.valueOf(ServerConfig.getPort()));
        txtPort.setPrefHeight(38);
        txtPort.setPromptText("Ví dụ: 5000");
        txtPort.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 0 12; -fx-font-size: 13px;");
        portBox.getChildren().addAll(lblPort, txtPort);

        Label lblNetStatus = new Label();
        lblNetStatus.setFont(Font.font("Segoe UI", 12.5));

        HBox btnNetActionBox = new HBox(12);
        btnNetActionBox.setAlignment(Pos.CENTER_LEFT);

        Button btnSaveNet = new Button("Lưu kết nối");
        btnSaveNet.setPrefHeight(36);
        btnSaveNet.setCursor(javafx.scene.Cursor.HAND);
        btnSaveNet.setOnAction(e -> {
            String h = txtHost.getText().trim();
            String pStr = txtPort.getText().trim();
            if (h.isEmpty()) {
                lblNetStatus.setText("⚠ Địa chỉ IP không được để trống!");
                lblNetStatus.setStyle("-fx-text-fill: #E53935; -fx-font-weight: bold;");
                return;
            }
            try {
                int p = Integer.parseInt(pStr);
                if (p <= 0 || p > 65535) throw new NumberFormatException();
                ServerConfig.setHost(h);
                ServerConfig.setPort(p);
                try {
                    if (CustomerContext.getInstance().getNetworkService() != null) {
                        CustomerContext.getInstance().getNetworkService().setServerAddress(h, p);
                    }
                } catch (Exception ignored) {}
                try {
                    if (StaffContext.getInstance().getNetworkService() != null) {
                        StaffContext.getInstance().getNetworkService().setServerAddress(h, p);
                    }
                } catch (Exception ignored) {}
                lblNetStatus.setText("✓ Đã lưu cấu hình kết nối (" + h + ":" + p + ") thành công!");
                lblNetStatus.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
            } catch (NumberFormatException ex) {
                lblNetStatus.setText("⚠ Cổng (Port) phải là số từ 1 đến 65535!");
                lblNetStatus.setStyle("-fx-text-fill: #E53935; -fx-font-weight: bold;");
            }
        });

        Button btnResetNet = new Button("Mặc định (127.0.0.1 : 5000)");
        btnResetNet.setPrefHeight(36);
        btnResetNet.setCursor(javafx.scene.Cursor.HAND);
        btnResetNet.setOnAction(e -> {
            ServerConfig.resetToDefault();
            try {
                if (CustomerContext.getInstance().getNetworkService() != null) {
                    CustomerContext.getInstance().getNetworkService().setServerAddress(ServerConfig.DEFAULT_HOST, ServerConfig.DEFAULT_PORT);
                }
            } catch (Exception ignored) {}
            try {
                if (StaffContext.getInstance().getNetworkService() != null) {
                    StaffContext.getInstance().getNetworkService().setServerAddress(ServerConfig.DEFAULT_HOST, ServerConfig.DEFAULT_PORT);
                }
            } catch (Exception ignored) {}
            txtHost.setText(ServerConfig.DEFAULT_HOST);
            txtPort.setText(String.valueOf(ServerConfig.DEFAULT_PORT));
            lblNetStatus.setText("✓ Đã khôi phục về mặc định 127.0.0.1 : 5000");
            lblNetStatus.setStyle("-fx-text-fill: #2E7D32;");
        });

        btnNetActionBox.getChildren().addAll(btnSaveNet, btnResetNet);

        formBox.getChildren().addAll(hostBox, portBox, btnNetActionBox, lblNetStatus);

        VBox tipBox = new VBox(4);
        tipBox.setPadding(new Insets(10, 14, 10, 14));
        tipBox.setStyle("-fx-background-radius: 8;");
        Label lblTipTitle = new Label("💡 Gợi ý kết nối:");
        lblTipTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        Label lblTipContent = new Label("• Mạng nội bộ (LAN): Nhập địa chỉ IPv4 máy chủ của bạn (ví dụ: 192.168.1.5).\n• Máy chủ Cloud: Nhập địa chỉ tên miền và Port tương ứng được cấp (ví dụ từ Playit.gg hoặc VPS).");
        lblTipContent.setFont(Font.font("Segoe UI", 12));
        lblTipContent.setWrapText(true);
        tipBox.getChildren().addAll(lblTipTitle, lblTipContent);

        networkContent.getChildren().addAll(lblNetHeader, lblNetDesc, formBox, tipBox);

        // --- Tab Switching Logic ---
        final Tab[] currentTab = { Tab.APPEARANCE };

        Runnable updateActiveTab = () -> {
            tabContentContainer.getChildren().clear();
            boolean dark = ThemeManager.isDarkMode();
            String brand = ThemeManager.getPrimaryColor();
            String brandLight = ThemeManager.getLightTint(brand);

            if (currentTab[0] == Tab.APPEARANCE) {
                tabContentContainer.getChildren().add(appearanceContent);
                lblContentTitle.setText("Cài đặt giao diện");
                styleSidebarTabBtn(btnTabAppearance, true, dark, brand, brandLight);
                styleSidebarTabBtn(btnTabNetwork, false, dark, brand, brandLight);
            } else {
                tabContentContainer.getChildren().add(networkContent);
                lblContentTitle.setText("Cài đặt kết nối mạng");
                txtHost.setText(ServerConfig.getHost());
                txtPort.setText(String.valueOf(ServerConfig.getPort()));
                styleSidebarTabBtn(btnTabAppearance, false, dark, brand, brandLight);
                styleSidebarTabBtn(btnTabNetwork, true, dark, brand, brandLight);
            }
        };

        btnTabAppearance.setOnAction(e -> {
            currentTab[0] = Tab.APPEARANCE;
            updateActiveTab.run();
        });

        btnTabNetwork.setOnAction(e -> {
            currentTab[0] = Tab.NETWORK;
            updateActiveTab.run();
        });

        // Exit Application Action
        btnExitApp.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.initOwner(stage);
            confirm.setTitle("Thoát ứng dụng");
            confirm.setHeaderText(null);
            confirm.setContentText("Bạn có chắc chắn muốn thoát ứng dụng hoàn toàn?");
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK) {
                    try {
                        if (CustomerContext.getInstance().getNetworkService() != null) {
                            CustomerContext.getInstance().getNetworkService().disconnect();
                        }
                    } catch (Exception ignored) {}
                    try {
                        if (StaffContext.getInstance().getNetworkService() != null) {
                            StaffContext.getInstance().getNetworkService().disconnect();
                        }
                    } catch (Exception ignored) {}
                    stage.close();
                    javafx.application.Platform.exit();
                    System.exit(0);
                }
            });
        });

        // --- Reactive Theme Refresh Listener ---
        Runnable refreshTheme = () -> {
            boolean dark = ThemeManager.isDarkMode();
            String brand = ThemeManager.getPrimaryColor();
            String brandLight = ThemeManager.getLightTint(brand);

            // Container Styles
            mainLayout.setStyle("-fx-background-color: " + (dark ? "#1e1e1e" : "#f4f6f8") + ";");
            sidebar.setStyle("-fx-background-color: " + (dark ? "#242526" : "#ffffff") + "; -fx-border-color: " + (dark ? "#2f3031" : "#e4e6eb") + "; -fx-border-width: 0 1 0 0;");
            rightPane.setStyle("-fx-background-color: " + (dark ? "#1e1e1e" : "#ffffff") + ";");

            lblSidebarTitle.setStyle("-fx-text-fill: " + (dark ? "#e4e6eb" : "#050505") + ";");
            lblContentTitle.setStyle("-fx-text-fill: " + (dark ? "#e4e6eb" : "#050505") + ";");
            btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: " + (dark ? "#b0b3b8" : "#65676b") + "; -fx-background-radius: 15;");

            lblAppHeader.setStyle("-fx-text-fill: " + (dark ? "#8e9297" : "#65676b") + ";");
            lblColorHeader.setStyle("-fx-text-fill: " + (dark ? "#8e9297" : "#65676b") + ";");
            lblCustom.setStyle("-fx-text-fill: " + (dark ? "#e4e6eb" : "#1c1e21") + ";");
            lblPreviewHeader.setStyle("-fx-text-fill: " + (dark ? "#8e9297" : "#65676b") + ";");

            // Appearance Mode Cards styling
            updateModeCardStyle(cardLight, false, dark, brand, brandLight);
            updateModeCardStyle(cardDark, true, dark, brand, brandLight);

            // Presets
            presetPane.getChildren().clear();
            for (int i = 0; i < ThemeManager.PRESET_COLORS.length; i++) {
                String hex = ThemeManager.PRESET_COLORS[i];
                String name = ThemeManager.PRESET_NAMES[i];
                boolean isSelected = hex.equalsIgnoreCase(brand);

                Button circle = new Button(isSelected ? "✓" : "");
                circle.setPrefSize(36, 36);
                circle.setMinSize(36, 36);
                circle.setMaxSize(36, 36);
                circle.setTooltip(new Tooltip(name));
                circle.setCursor(javafx.scene.Cursor.HAND);

                String borderStyle = isSelected
                        ? "-fx-border-color: " + (dark ? "#ffffff" : "#1c1e21") + "; -fx-border-width: 2.5; -fx-border-radius: 50;"
                        : "-fx-border-color: transparent; -fx-border-width: 0;";

                circle.setStyle("-fx-background-color: " + hex + "; -fx-background-radius: 50; "
                        + borderStyle + " -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 0;");

                circle.setOnAction(e -> {
                    ThemeManager.setPrimaryColor(hex);
                    colorPicker.setValue(Color.web(hex));
                });
                presetPane.getChildren().add(circle);
            }

            // Preview Box styling
            previewBox.setStyle("-fx-background-color: " + (dark ? "#121212" : "#f4f7f8") + "; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: " + (dark ? "#2f3031" : "#e0e4eb") + "; -fx-border-width: 1;");
            inBubble.setStyle("-fx-background-color: " + (dark ? "#242526" : "#ffffff") + "; -fx-text-fill: " + (dark ? "#e4e6eb" : "#050505") + "; -fx-background-radius: 12 12 12 0; -fx-font-size: 13px;");
            outBubble.setStyle("-fx-background-color: " + brand + "; -fx-text-fill: white; -fx-background-radius: 12 12 0 12; -fx-font-size: 13px;");

            btnResetTheme.setStyle("-fx-background-color: transparent; -fx-text-fill: " + (dark ? "#b0b3b8" : "#65676b") + "; -fx-font-size: 12px;");

            // Network Form styling
            lblNetHeader.setStyle("-fx-text-fill: " + (dark ? "#8e9297" : "#65676b") + ";");
            lblNetDesc.setStyle("-fx-text-fill: " + (dark ? "#b0b3b8" : "#65676b") + ";");
            formBox.setStyle("-fx-background-color: " + (dark ? "#242526" : "#f8f9fa") + "; -fx-border-color: " + (dark ? "#2f3031" : "#e4e6eb") + "; -fx-border-radius: 10; -fx-background-radius: 10; -fx-border-width: 1;");
            lblHost.setStyle("-fx-text-fill: " + (dark ? "#e4e6eb" : "#1c1e21") + ";");
            lblPort.setStyle("-fx-text-fill: " + (dark ? "#e4e6eb" : "#1c1e21") + ";");
            txtHost.setStyle("-fx-background-color: " + (dark ? "#323438" : "#ffffff") + "; -fx-text-fill: " + (dark ? "#e4e6eb" : "#050505") + "; -fx-border-color: " + (dark ? "#484b51" : "#c4c9d0") + "; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 0 12; -fx-font-size: 13px;");
            txtPort.setStyle("-fx-background-color: " + (dark ? "#323438" : "#ffffff") + "; -fx-text-fill: " + (dark ? "#e4e6eb" : "#050505") + "; -fx-border-color: " + (dark ? "#484b51" : "#c4c9d0") + "; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 0 12; -fx-font-size: 13px;");

            btnSaveNet.setStyle("-fx-background-color: " + brand + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 7 16;");
            btnResetNet.setStyle("-fx-background-color: transparent; -fx-text-fill: " + (dark ? "#b0b3b8" : "#65676b") + "; -fx-border-color: " + (dark ? "#484b51" : "#c4c9d0") + "; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6 14;");

            tipBox.setStyle("-fx-background-color: " + (dark ? "rgba(255,255,255,0.05)" : "#eef3f8") + "; -fx-background-radius: 8;");
            lblTipTitle.setStyle("-fx-text-fill: " + (dark ? "#e4e6eb" : "#1c1e21") + ";");
            lblTipContent.setStyle("-fx-text-fill: " + (dark ? "#b0b3b8" : "#555555") + ";");

            // Exit Button styling
            btnExitApp.setStyle("-fx-background-color: transparent; -fx-text-fill: #e53935; " + TAB_BASE_STYLE);
            if (btnExitApp.getGraphic() instanceof SVGPath icon) {
                icon.setFill(Color.web("#e53935"));
            }

            updateActiveTab.run();
        };

        refreshTheme.run();
        ThemeManager.addListener(refreshTheme);

        Scene scene = new Scene(mainLayout);
        stage.setScene(scene);
        stage.setOnHidden(e -> ThemeManager.removeListener(refreshTheme));
        stage.show();
    }

    private static final String TAB_BASE_STYLE = 
        "-fx-font-family: 'Segoe UI'; -fx-font-size: 13.5px; -fx-font-weight: normal; "
        + "-fx-alignment: CENTER_LEFT; -fx-padding: 9 12 9 12; -fx-background-radius: 8; "
        + "-fx-background-insets: 0; -fx-border-width: 0; -fx-cursor: hand;";

    private static Button createSidebarTab(String text, String svgPath) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(9, 12, 9, 12));
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13.5));
        btn.setStyle(TAB_BASE_STYLE);

        if (svgPath != null && !svgPath.isEmpty()) {
            SVGPath icon = new SVGPath();
            icon.setContent(svgPath);
            icon.setFill(Color.web("#65676b"));
            icon.setScaleX(0.75);
            icon.setScaleY(0.75);
            btn.setGraphic(icon);
            btn.setGraphicTextGap(10);
        }
        return btn;
    }

    private static void styleSidebarTabBtn(Button btn, boolean active, boolean dark, String brand, String brandLight) {
        btn.getProperties().put("active", active);
        if (active) {
            btn.setStyle("-fx-background-color: " + (dark ? "rgba(255, 255, 255, 0.12)" : brandLight) 
                    + "; -fx-text-fill: " + (dark ? "#ffffff" : brand) + "; " + TAB_BASE_STYLE);
            if (btn.getGraphic() instanceof SVGPath icon) {
                icon.setFill(Color.web(dark ? "#ffffff" : brand));
            }
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + (dark ? "#b0b3b8" : "#444444") 
                    + "; " + TAB_BASE_STYLE);
            if (btn.getGraphic() instanceof SVGPath icon) {
                icon.setFill(Color.web(dark ? "#8e9297" : "#72808e"));
            }
        }
    }

    private static void setupTabHover(Button btn) {
        btn.setOnMouseEntered(e -> {
            boolean active = Boolean.TRUE.equals(btn.getProperties().get("active"));
            if (!active) {
                boolean dark = ThemeManager.isDarkMode();
                btn.setStyle("-fx-background-color: " + (dark ? "rgba(255, 255, 255, 0.08)" : "rgba(0, 0, 0, 0.05)") 
                        + "; -fx-text-fill: " + (dark ? "#ffffff" : "#1c1e21") + "; " + TAB_BASE_STYLE);
                if (btn.getGraphic() instanceof SVGPath icon) {
                    icon.setFill(Color.web(dark ? "#ffffff" : "#1c1e21"));
                }
            }
        });
        btn.setOnMouseExited(e -> {
            boolean active = Boolean.TRUE.equals(btn.getProperties().get("active"));
            if (!active) {
                boolean dark = ThemeManager.isDarkMode();
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + (dark ? "#b0b3b8" : "#444444") 
                        + "; " + TAB_BASE_STYLE);
                if (btn.getGraphic() instanceof SVGPath icon) {
                    icon.setFill(Color.web(dark ? "#8e9297" : "#72808e"));
                }
            }
        });
    }

    private static void setupExitBtn(Button btnExitApp) {
        btnExitApp.setOnMouseEntered(e -> {
            boolean dark = ThemeManager.isDarkMode();
            btnExitApp.setStyle("-fx-background-color: " + (dark ? "rgba(229, 57, 53, 0.15)" : "rgba(229, 57, 53, 0.08)") 
                    + "; -fx-text-fill: #e53935; " + TAB_BASE_STYLE);
            if (btnExitApp.getGraphic() instanceof SVGPath icon) {
                icon.setFill(Color.web("#e53935"));
            }
        });
        btnExitApp.setOnMouseExited(e -> {
            btnExitApp.setStyle("-fx-background-color: transparent; -fx-text-fill: #e53935; " + TAB_BASE_STYLE);
            if (btnExitApp.getGraphic() instanceof SVGPath icon) {
                icon.setFill(Color.web("#e53935"));
            }
        });
    }

    private static VBox createModeCard(String labelText, boolean isDarkCard) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setPrefHeight(90);

        // Preview box: clean white frame for light, clean black frame for dark
        StackPane mockWindow = new StackPane();
        mockWindow.setPrefSize(160, 48);
        mockWindow.setMaxSize(160, 48);
        mockWindow.setStyle(isDarkCard 
            ? "-fx-background-color: #000000; -fx-background-radius: 6; -fx-border-color: #3a3b3c; -fx-border-radius: 6; -fx-border-width: 1.5;"
            : "-fx-background-color: #ffffff; -fx-background-radius: 6; -fx-border-color: #c4c9d0; -fx-border-radius: 6; -fx-border-width: 1.5;");

        Label lbl = new Label(isDarkCard ? "🌙 " + labelText : "☀️ " + labelText);
        lbl.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));

        card.getChildren().addAll(mockWindow, lbl);
        return card;
    }

    private static void updateModeCardStyle(VBox card, boolean isDarkCard, boolean currentIsDark, String brand, String brandLight) {
        boolean selected = (isDarkCard == currentIsDark);
        String border = selected 
            ? "-fx-border-color: " + brand + "; -fx-border-width: 2.5; -fx-border-radius: 8;"
            : "-fx-border-color: " + (currentIsDark ? "#393a3b" : "#d0d4dc") + "; -fx-border-width: 1; -fx-border-radius: 8;";
        String bg = selected 
            ? (currentIsDark ? "rgba(255, 255, 255, 0.08)" : brandLight)
            : (currentIsDark ? "#242526" : "#ffffff");

        card.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 8; " + border);

        if (card.getChildren().size() > 1 && card.getChildren().get(1) instanceof Label lbl) {
            lbl.setStyle("-fx-text-fill: " + (selected ? (currentIsDark ? "#ffffff" : brand) : (currentIsDark ? "#b0b3b8" : "#555555")) 
                    + "; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-font-weight: normal;");
        }
    }
}
