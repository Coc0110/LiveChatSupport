package com.cmp180.livechat.client.staff.ui;

import com.cmp180.livechat.client.customer.components.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import com.cmp180.livechat.client.staff.context.StaffContext;
import com.cmp180.livechat.client.staff.network.StaffNetworkListener;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class QueuePanel extends JPanel implements StaffNetworkListener {
    private StaffMainFrame parentFrame;

    private JPanel rowsPanel;
    private JPanel th;
    private JLabel lblWaitCount, lblServingCount;

    private Map<String, String> currentQueue = new LinkedHashMap<>();
    private Map<String, Long> arrivalTimes = new HashMap<>();
    private Timer uiTimer;

    public QueuePanel(StaffMainFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(5, 5)); // Khoảng cách giữa các Đảo
        setBackground(new Color(225, 230, 238));
        setBorder(new EmptyBorder(10, 10, 10, 10)); // Cách viền màn hình ngoài cùng

        StaffContext.getInstance().getNetworkService().addListener(this);

        add(createSidebar(), BorderLayout.WEST);
        add(createContent(), BorderLayout.CENTER);

        uiTimer = new Timer(1000, e -> renderRows());
        uiTimer.start();
    }

    private JPanel createSidebar() {
        // Đảo nổi 1: Thanh Sidebar (Bo 25px)
        ShadowPanel sidebar = new ShadowPanel(25, Color.WHITE, 12);
        sidebar.setPreferredSize(new Dimension(90, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        
        sidebar.add(Box.createVerticalStrut(10));
        
        sidebar.add(createSidebarIcon("👥", true, "QUEUE_SCREEN"));
        sidebar.add(Box.createVerticalStrut(15));
        sidebar.add(createSidebarIcon("💬", false, "CHAT_SCREEN"));
        sidebar.add(Box.createVerticalStrut(15));
        sidebar.add(createSidebarIcon("⚙️", false, "SETTINGS_SCREEN"));
        
        sidebar.add(Box.createVerticalGlue());
        
        JLabel avatar = new JLabel("NV");
        avatar.setOpaque(true);
        avatar.setBackground(new Color(220, 240, 255));
        avatar.setForeground(new Color(0, 122, 255));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        avatar.setMaximumSize(new Dimension(40, 40));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Bo tròn avatar (sẽ làm trong class riêng sau, tạm thời dùng viền)
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
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setOpaque(false);

        // Phần Header (Gồm Tiêu đề và Thẻ thống kê nổi)
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Khách hàng đang chờ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        JLabel sub = new JLabel("Cập nhật thời gian thực từ máy chủ");
        sub.setForeground(Color.GRAY);
        titlePanel.add(Box.createVerticalStrut(20));
        titlePanel.add(title);
        titlePanel.add(sub);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setOpaque(false);
        
        lblWaitCount = new JLabel("0 khách");
        lblWaitCount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblWaitCount.setForeground(Color.RED);
        
        lblServingCount = new JLabel("0 khách");
        lblServingCount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblServingCount.setForeground(new Color(0, 122, 255));
        
        statsPanel.add(createStatCard("Đang chờ", lblWaitCount, new Color(255, 235, 238)));
        statsPanel.add(createStatCard("Đang hỗ trợ", lblServingCount, new Color(230, 242, 255)));

        header.add(titlePanel, BorderLayout.WEST);
        header.add(statsPanel, BorderLayout.EAST);

        // Đảo nổi 2: Bảng danh sách khách hàng (Bo 30px)
        ShadowPanel listContainer = new ShadowPanel(30, Color.WHITE, 15);
        listContainer.setLayout(new BorderLayout());

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        filterBar.setOpaque(false);
        PlaceholderTextField searchField = new PlaceholderTextField(10, "🔍 Tìm kiếm khách hàng...");
        searchField.setPreferredSize(new Dimension(300, 35));
        filterBar.add(searchField);
        
        String[] topics = {"Tất cả chủ đề", "Lỗi kỹ thuật", "Tư vấn"};
        RoundedComboBox<String> topicCb = new RoundedComboBox<>(topics, 10);
        topicCb.setPreferredSize(new Dimension(150, 35));
        filterBar.add(topicCb);

        rowsPanel = new JPanel();
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
        rowsPanel.setBackground(Color.WHITE);

        th = new JPanel(new GridLayout(1, 5, 10, 0));
        th.setBackground(Color.WHITE);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        th.add(new JLabel("STT"));
        th.add(new JLabel("Khách hàng"));
        th.add(new JLabel("Chủ đề hỗ trợ"));
        th.add(new JLabel("Thời gian chờ"));
        th.add(new JLabel("Thao tác"));
        th.setMaximumSize(new Dimension(9999, 45));
        rowsPanel.add(th);

        listContainer.add(filterBar, BorderLayout.NORTH);
        
        JScrollPane scroll = new JScrollPane(rowsPanel);
        scroll.setBorder(null); // Tắt viền xấu xí của ScrollPane
        listContainer.add(scroll, BorderLayout.CENTER);

        content.add(header, BorderLayout.NORTH);
        content.add(listContainer, BorderLayout.CENTER);

        return content;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color bg) {
        // Đảo nổi nhỏ: Các Thẻ Thống Kê (Bo 20px)
        ShadowPanel card = new ShadowPanel(20, bg, 8);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(160, 80));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(Color.DARK_GRAY);
        card.add(t);
        card.add(Box.createVerticalStrut(5));
        card.add(valueLabel);
        return card;
    }

    private void renderRows() {
        SwingUtilities.invokeLater(() -> {
            rowsPanel.removeAll();
            rowsPanel.add(th);
            
            lblWaitCount.setText(currentQueue.size() + " khách");

            int stt = 1;
            long now = System.currentTimeMillis();

            for (Map.Entry<String, String> entry : currentQueue.entrySet()) {
                String customerId = entry.getKey();
                String customerName = entry.getValue();
                
                long arrival = arrivalTimes.getOrDefault(customerId, now);
                long waitedSeconds = (now - arrival) / 1000;
                
                long minutes = waitedSeconds / 60;
                long seconds = waitedSeconds % 60;
                String timeStr = String.format("%02d:%02d", minutes, seconds);

                Color timeColor = new Color(40, 167, 69);
                Font timeFont = new Font("Consolas", Font.PLAIN, 14);
                if (minutes >= 5) {
                    timeColor = Color.RED;
                    timeFont = new Font("Consolas", Font.BOLD, 14);
                } else if (minutes >= 2) {
                    timeColor = new Color(255, 140, 0); 
                }

                JPanel row = createQueueRow(String.valueOf(stt++), "KH", customerName, timeStr, timeColor, timeFont, "Tư vấn chung", customerId);
                rowsPanel.add(row);
            }
            
            rowsPanel.revalidate();
            rowsPanel.repaint();
        });
    }

    private JPanel createQueueRow(String stt, String avt, String name, String timeStr, Color timeColor, Font timeFont, String topic, String customerId) {
        JPanel row = new JPanel(new GridLayout(1, 5, 10, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(245, 245, 245)));
        row.setMaximumSize(new Dimension(9999, 65));

        row.add(new JLabel(stt));
        
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        namePanel.setOpaque(false);
        JLabel avatar = new JLabel(avt, SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(220, 240, 255));
        avatar.setForeground(new Color(0, 122, 255));
        avatar.setPreferredSize(new Dimension(35, 35));
        namePanel.add(avatar);
        namePanel.add(new JLabel("<html><b>" + name + "</b></html>"));
        row.add(namePanel);

        row.add(new JLabel(topic));

        JLabel timeLbl = new JLabel(timeStr);
        timeLbl.setForeground(timeColor);
        timeLbl.setFont(timeFont);
        row.add(timeLbl);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 15));
        actionPanel.setOpaque(false);
        RoundedButton acceptBtn = new RoundedButton("Tiếp nhận", 8, new Color(0, 122, 255), Color.WHITE);
        acceptBtn.setPreferredSize(new Dimension(100, 35));
        acceptBtn.addActionListener(e -> StaffContext.getInstance().getNetworkService().acceptCustomer(customerId));
        actionPanel.add(acceptBtn);
        row.add(actionPanel);

        return row;
    }

    @Override public void onLoginSuccess() { }

    @Override
    public void onQueueUpdated(Map<String, String> waitingCustomers) {
        long now = System.currentTimeMillis();
        for (String customerId : waitingCustomers.keySet()) {
            if (!arrivalTimes.containsKey(customerId)) {
                arrivalTimes.put(customerId, now);
            }
        }
        arrivalTimes.keySet().retainAll(waitingCustomers.keySet());
        
        this.currentQueue = waitingCustomers;
        renderRows(); 
    }

    @Override
    public void onPairedWithCustomer(String msg) {
        SwingUtilities.invokeLater(() -> parentFrame.navigateTo("CHAT_SCREEN"));
    }

    @Override public void onMessageReceived(String sender, String msg) { }
    @Override public void onSessionEnded(String reason) { }
    @Override public void onError(String msg) { }
}