package com.cmp180.livechat.client.staff.ui;

import javax.swing.*;
import java.awt.*;

public class StaffMainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;

    public StaffMainFrame() {
        setTitle("LiveChat Workspace - Staff");
        setSize(1280, 800); // Kích thước rộng chuẩn Desktop
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(new Color(225, 230, 238));

        // Nạp các màn hình độc lập vào hệ thống quản lý, truyền 'this' để chúng gọi được hàm navigateTo
        mainContainer.add(new LoginPanel(this), "LOGIN_SCREEN");
        mainContainer.add(new QueuePanel(this), "QUEUE_SCREEN");
        mainContainer.add(new ChatPanel(this), "CHAT_SCREEN");
        mainContainer.add(new SettingsPanel(this), "SETTINGS_SCREEN");

        add(mainContainer);
        
        // Hiển thị màn hình mặc định
        cardLayout.show(mainContainer, "LOGIN_SCREEN");
    }

    // Hàm public để các Panel con gọi khi muốn chuyển trang
    public void navigateTo(String screenName) {
        cardLayout.show(mainContainer, screenName);
    }
}