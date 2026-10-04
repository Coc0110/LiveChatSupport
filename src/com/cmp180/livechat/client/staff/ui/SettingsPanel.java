package com.cmp180.livechat.client.staff.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import com.cmp180.livechat.client.staff.context.StaffContext;

public class SettingsPanel extends JPanel {
    private StaffMainFrame parentFrame;

    public SettingsPanel(StaffMainFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(225, 230, 238));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(createSidebar(), BorderLayout.WEST);
        add(createContent(), BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        ShadowPanel sidebar = new ShadowPanel(25, Color.WHITE, 12);
        sidebar.setPreferredSize(new Dimension(90, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        
        sidebar.add(Box.createVerticalStrut(10));
        
        sidebar.add(createSidebarIcon("👥", false, "QUEUE_SCREEN"));
        sidebar.add(Box.createVerticalStrut(15));
        sidebar.add(createSidebarIcon("💬", false, "CHAT_SCREEN"));
        sidebar.add(Box.createVerticalStrut(15));
        sidebar.add(createSidebarIcon("⚙️", true, "SETTINGS_SCREEN"));
        
        sidebar.add(Box.createVerticalGlue());
        
        JLabel avatar = new JLabel("NV");
        avatar.setOpaque(true);
        avatar.setBackground(new Color(220, 240, 255));
        avatar.setForeground(new Color(0, 122, 255));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        avatar.setMaximumSize(new Dimension(40, 40));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(avatar);
        sidebar.add(Box.createVerticalStrut(20));
        return sidebar;
    }

    private JLabel createSidebarIcon(String icon, boolean isActive, String targetScreen) {
        JLabel lbl = new JLabel(icon, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        lbl.setMaximumSize(new Dimension(50, 50));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (isActive) {
            lbl.setOpaque(true);
            lbl.setBackground(new Color(230, 242, 255));
        }

        lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (targetScreen != null) {
                    parentFrame.navigateTo(targetScreen);
                }
            }
        });
        return lbl;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setOpaque(false);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Cài đặt hệ thống");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        
        header.add(Box.createVerticalStrut(20), BorderLayout.NORTH);
        header.add(title, BorderLayout.WEST);

        // Body (Đảo nổi)
        ShadowPanel body = new ShadowPanel(30, Color.WHITE, 15);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel infoTitle = new JLabel("Thông tin tài khoản");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        infoTitle.setForeground(new Color(0, 122, 255));
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        infoPanel.setOpaque(false);
        infoPanel.setMaximumSize(new Dimension(600, 80));
        
        JLabel lb1 = new JLabel("Tên hiển thị:");
        lb1.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        infoPanel.add(lb1);
        
        JLabel nameLbl = new JLabel();
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        infoPanel.add(nameLbl);
        
        JLabel lb2 = new JLabel("Trạng thái:");
        lb2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        infoPanel.add(lb2);
        
        JLabel statusLbl = new JLabel("<html><span style='color:green;'>● Đang hoạt động</span></html>");
        statusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        infoPanel.add(statusLbl);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                nameLbl.setText(StaffContext.getInstance().getStaffName());
            }
        });

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(9999, 1));
        sep.setForeground(new Color(240, 240, 240));

        JLabel prefTitle = new JLabel("Tùy chọn");
        prefTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        prefTitle.setForeground(new Color(0, 122, 255));

        JCheckBox soundCheck = new JCheckBox("Bật âm thanh thông báo khi có tin nhắn hoặc khách mới");
        soundCheck.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        soundCheck.setOpaque(false);
        soundCheck.setSelected(true);

        body.add(Box.createVerticalStrut(20));
        body.add(infoTitle);
        body.add(Box.createVerticalStrut(20));
        body.add(infoPanel);
        body.add(Box.createVerticalStrut(30));
        body.add(sep);
        body.add(Box.createVerticalStrut(30));
        body.add(prefTitle);
        body.add(Box.createVerticalStrut(20));
        body.add(soundCheck);
        
        body.add(Box.createVerticalGlue());

        content.add(header, BorderLayout.NORTH);
        content.add(body, BorderLayout.CENTER);

        return content;
    }
}
