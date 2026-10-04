package com.cmp180.livechat.client.staff.ui;

import com.cmp180.livechat.client.customer.components.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import com.cmp180.livechat.client.staff.context.StaffContext;
import com.cmp180.livechat.client.staff.network.StaffNetworkListener;
import java.util.Map;

public class ChatPanel extends JPanel implements StaffNetworkListener {
    private StaffMainFrame parentFrame;
    private JPanel chatArea;
    private JTextField typeField;
    private JLabel activeUser;

    public ChatPanel(StaffMainFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(5, 5));
        setBackground(new Color(225, 230, 238));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        StaffContext.getInstance().getNetworkService().addListener(this);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                typeField.requestFocusInWindow();
            }
        });

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
        sidebar.add(createSidebarIcon("💬", true, "CHAT_SCREEN"));
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
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        
        // Vùng bên trái: Có thể làm lịch sử chat, tạm để trống
        
        // Vùng trung tâm: Khung Chat Chính (Đảo nổi 30px)
        ShadowPanel mainChat = new ShadowPanel(30, Color.WHITE, 15);
        mainChat.setLayout(new BorderLayout());

        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBackground(Color.WHITE);
        chatHeader.setBorder(new EmptyBorder(15, 25, 15, 25));
        
        activeUser = new JLabel("<html><b>Đang kết nối...</b><br><span style='color:green; font-size:10px;'>● Đang hoạt động</span></html>");
        activeUser.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        chatHeader.add(activeUser, BorderLayout.WEST);

        RoundedButton endBtn = new RoundedButton("Kết thúc hỗ trợ", 8, new Color(255, 59, 48), Color.WHITE);
        endBtn.setPreferredSize(new Dimension(140, 35));
        endBtn.addActionListener(e -> {
            StaffContext.getInstance().getNetworkService().endSession();
            activeUser.setText("<html><b>Đang kết nối...</b><br><span style='color:green; font-size:10px;'>● Đang hoạt động</span></html>");
            chatArea.removeAll();
            chatArea.revalidate();
            chatArea.repaint();
            parentFrame.navigateTo("QUEUE_SCREEN");
        });
        chatHeader.add(endBtn, BorderLayout.EAST);
        
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(240, 240, 240));
        chatHeader.add(sep, BorderLayout.SOUTH);

        mainChat.add(chatHeader, BorderLayout.NORTH);

        chatArea = new JPanel();
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));
        chatArea.setBackground(new Color(250, 251, 253));
        chatArea.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainChat.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(15, 0));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        typeField = new PlaceholderTextField(15, "Nhập tin nhắn hỗ trợ khách hàng...");
        typeField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        inputPanel.add(typeField, BorderLayout.CENTER);

        RoundedButton sendBtn = new RoundedButton("Gửi", 15, new Color(0, 122, 255), Color.WHITE);
        sendBtn.setPreferredSize(new Dimension(80, 45));
        sendBtn.addActionListener(e -> sendMessage());
        typeField.addActionListener(e -> sendMessage());
        
        inputPanel.add(sendBtn, BorderLayout.EAST);
        mainChat.add(inputPanel, BorderLayout.SOUTH);

        content.add(mainChat, BorderLayout.CENTER);
        return content;
    }

    private void sendMessage() {
        String text = typeField.getText().trim();
        if (!text.isEmpty()) {
            addMessageBubble("Bạn", text, true);
            typeField.setText("");
            StaffContext.getInstance().getNetworkService().sendChatMessage(text);
        }
    }

    private void addMessageBubble(String sender, String text, boolean isSelf) {
        JPanel bubble = new JPanel(new FlowLayout(isSelf ? FlowLayout.RIGHT : FlowLayout.LEFT));
        bubble.setOpaque(false);
        JLabel lbl = new JLabel("<html><div style='max-width: 280px; font-family: Segoe UI, sans-serif;'>" + text + "</div></html>");
        lbl.setOpaque(true);
        lbl.setBackground(isSelf ? new Color(0, 122, 255) : new Color(233, 236, 239));
        lbl.setForeground(isSelf ? Color.WHITE : Color.BLACK);
        lbl.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        bubble.add(lbl);
        chatArea.add(bubble);
        chatArea.revalidate();
        chatArea.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollPane scroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, chatArea);
            if (scroll != null) {
                JScrollBar vertical = scroll.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }

    @Override public void onLoginSuccess() { }
    @Override public void onQueueUpdated(Map<String, String> waitingCustomers) { }
    
    @Override
    public void onPairedWithCustomer(String customerName) {
        SwingUtilities.invokeLater(() -> {
            activeUser.setText("<html><b>" + customerName + "</b><br><span style='color:green; font-size:10px;'>● Đang hoạt động</span></html>");
            chatArea.removeAll();
            addMessageBubble("Hệ thống", "Đã kết nối với " + customerName, false);
        });
    }

    @Override
    public void onMessageReceived(String sender, String msg) {
        SwingUtilities.invokeLater(() -> {
            addMessageBubble(sender, msg, false);
        });
    }

    @Override
    public void onSessionEnded(String reason) {
        SwingUtilities.invokeLater(() -> {
            addMessageBubble("Hệ thống", reason, false);
            activeUser.setText("<html><b>Đã kết thúc</b><br><span style='color:red; font-size:10px;'>● Mất kết nối</span></html>");
        });
    }

    @Override public void onError(String msg) { }
}
