package com.cmp180.livechat.client.staff.ui;

import com.cmp180.livechat.client.customer.components.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Map;
import com.cmp180.livechat.client.staff.context.StaffContext;
import com.cmp180.livechat.client.staff.network.StaffNetworkListener;

public class ChatPanel extends JPanel implements StaffNetworkListener {
    private StaffMainFrame parentFrame;
    private JPanel chatArea;
    private PlaceholderTextField typeField;
    private JLabel activeUser;

    public ChatPanel(StaffMainFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                StaffContext.getInstance().getNetworkService().setListener(ChatPanel.this);
            }
        });
        setBackground(Color.WHITE);

        add(createSidebar(), BorderLayout.WEST);
        add(createContent(), BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(70, 0));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        
        String[] icons = {"💬", "👥", "📊", "⚙️"};
        sidebar.add(Box.createVerticalStrut(20));
        for (String icon : icons) {
            JLabel lbl = new JLabel(icon, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
            lbl.setMaximumSize(new Dimension(50, 50));
            sidebar.add(lbl);
            sidebar.add(Box.createVerticalStrut(20));
        }
        sidebar.add(Box.createVerticalGlue());
        
        JLabel avatar = new JLabel("NM");
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

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout());
        
        // CỘT 1: Danh sách hội thoại đang active
        JPanel chatListPanel = new JPanel(new BorderLayout());
        chatListPanel.setPreferredSize(new Dimension(300, 0));
        chatListPanel.setBackground(Color.WHITE);
        chatListPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));
        
        JPanel chatListHeader = new JPanel(new BorderLayout(0, 10));
        chatListHeader.setBackground(Color.WHITE);
        chatListHeader.setBorder(new EmptyBorder(20, 20, 10, 20));
        JLabel lblHoiThoai = new JLabel("Hội thoại");
        lblHoiThoai.setFont(new Font("Segoe UI", Font.BOLD, 20));
        chatListHeader.add(lblHoiThoai, BorderLayout.NORTH);
        PlaceholderTextField searchChat = new PlaceholderTextField(10, "🔍 Tìm kiếm khách hàng...");
        searchChat.setPreferredSize(new Dimension(260, 35));
        chatListHeader.add(searchChat, BorderLayout.CENTER);
        
        chatListPanel.add(chatListHeader, BorderLayout.NORTH);
        
        JPanel activeChats = new JPanel();
        activeChats.setLayout(new BoxLayout(activeChats, BoxLayout.Y_AXIS));
        activeChats.setBackground(Color.WHITE);
        activeChats.add(createChatListItem("NV", "Nguyễn Văn Nam", "Tôi muốn hỏi về chính sá...", "10:24", 2, true));
        activeChats.add(createChatListItem("TT", "Trần Thị Lan", "Đã chuyển khoản thành côn...", "10:15", 0, false));
        chatListPanel.add(new JScrollPane(activeChats), BorderLayout.CENTER);

        // CỘT 2: Khu vực nhắn tin chính
        JPanel mainChat = new JPanel(new BorderLayout());
        mainChat.setBackground(new Color(248, 249, 250));
        
        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBackground(Color.WHITE);
        chatHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        activeUser = new JLabel("<html><b>Nguyễn Văn Nam</b><br><span style='color:green; font-size:10px;'>● Đang hoạt động</span></html>");
        
        RoundedButton endBtn = new RoundedButton("Kết thúc phiên", 8, new Color(220, 53, 69), Color.WHITE);
        endBtn.setPreferredSize(new Dimension(120, 35));
        
        // SỰ KIỆN QUAY LẠI TRANG CHỜ KHI KẾT THÚC CHAT
        endBtn.addActionListener(e -> {
            StaffContext.getInstance().getNetworkService().endSession();
            chatArea.removeAll();
            parentFrame.navigateTo("QUEUE_SCREEN");
        }); 
        
        chatHeader.add(activeUser, BorderLayout.WEST);
        chatHeader.add(endBtn, BorderLayout.EAST);

        chatArea = new JPanel();
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS)); 
        chatArea.setBackground(new Color(248, 249, 250)); // Khung tin nhắn trống tạm thời

        JPanel chatInput = new JPanel(new BorderLayout(10, 0));
        chatInput.setBackground(Color.WHITE);
        chatInput.setBorder(new EmptyBorder(15, 20, 15, 20));
        typeField = new PlaceholderTextField(15, "Nhập tin nhắn...");
        typeField.setPreferredSize(new Dimension(0, 45));
        RoundedButton sendBtn = new RoundedButton("Gửi", 10, new Color(0, 122, 255), Color.WHITE);
        sendBtn.setPreferredSize(new Dimension(80, 45));
        chatInput.add(typeField, BorderLayout.CENTER);
        sendBtn.addActionListener(e -> {
            String text = typeField.getText().trim();
            if (!text.isEmpty()) {
                StaffContext.getInstance().getNetworkService().sendChatMessage(text);
                addMessageBubble("Bạn", text, true);
                typeField.setText("");
            }
        });
        chatInput.add(sendBtn, BorderLayout.EAST);

        mainChat.add(chatHeader, BorderLayout.NORTH);
        mainChat.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        mainChat.add(chatInput, BorderLayout.SOUTH);

        // CỘT 3: Hồ sơ chi tiết của Khách hàng
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setPreferredSize(new Dimension(280, 0));
        profilePanel.setBackground(Color.WHITE);
        profilePanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(230, 230, 230)));
        
        JLabel proAvt = new JLabel("NV", SwingConstants.CENTER);
        proAvt.setOpaque(true); proAvt.setBackground(new Color(220, 240, 255));
        proAvt.setFont(new Font("Segoe UI", Font.BOLD, 24));
        proAvt.setPreferredSize(new Dimension(80, 80));
        proAvt.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel proName = new JLabel("Nguyễn Văn Nam");
        proName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        proName.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        profilePanel.add(Box.createVerticalStrut(40));
        profilePanel.add(proAvt);
        profilePanel.add(Box.createVerticalStrut(15));
        profilePanel.add(proName);
        
        content.add(chatListPanel, BorderLayout.WEST);
        content.add(mainChat, BorderLayout.CENTER);
        content.add(profilePanel, BorderLayout.EAST);

        return content;
    }

    private JPanel createChatListItem(String avt, String name, String snippet, String time, int unread, boolean isActive) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(isActive ? new Color(240, 247, 255) : Color.WHITE);
        item.setBorder(new EmptyBorder(15, 20, 15, 20));
        item.setMaximumSize(new Dimension(999, 70));
        
        JLabel avatar = new JLabel(avt, SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(220, 240, 255));
        avatar.setPreferredSize(new Dimension(40, 40));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(new JLabel("<html><b>" + name + "</b></html>"));
        JLabel snipLbl = new JLabel(snippet);
        snipLbl.setForeground(Color.GRAY);
        textPanel.add(snipLbl);
        
        JPanel rightPanel = new JPanel(new GridLayout(2, 1));
        rightPanel.setOpaque(false);
        JLabel timeLbl = new JLabel(time, SwingConstants.RIGHT);
        timeLbl.setForeground(Color.GRAY);
        timeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rightPanel.add(timeLbl);
        
        if (unread > 0) {
            JLabel badge = new JLabel(String.valueOf(unread), SwingConstants.CENTER);
            badge.setOpaque(true);
            badge.setBackground(Color.RED);
            badge.setForeground(Color.WHITE);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            rightPanel.add(badge);
        }
        
        item.add(avatar, BorderLayout.WEST);
        item.add(textPanel, BorderLayout.CENTER);
        item.add(rightPanel, BorderLayout.EAST);
        return item;
    }

    private void addMessageBubble(String sender, String text, boolean isSelf) {
        JPanel bubble = new JPanel(new FlowLayout(isSelf ? FlowLayout.RIGHT : FlowLayout.LEFT));
        bubble.setOpaque(false);
        JLabel lbl = new JLabel("<html><b>" + sender + ":</b> " + text + "</html>");
        lbl.setOpaque(true);
        lbl.setBackground(isSelf ? new Color(0, 122, 255) : Color.WHITE);
        lbl.setForeground(isSelf ? Color.WHITE : Color.BLACK);
        lbl.setBorder(new EmptyBorder(10, 15, 10, 15));
        bubble.add(lbl);
        chatArea.add(bubble);
        chatArea.revalidate();
        chatArea.repaint();
    }

    @Override public void onLoginSuccess() { }
    @Override public void onQueueUpdated(java.util.Map<String, String> waitingCustomers) { }
    @Override public void onPairedWithCustomer(String customerName) {
        SwingUtilities.invokeLater(() -> {
            activeUser.setText("<html><b>" + customerName + "</b><br><span style='color:green; font-size:10px;'>&#128994; Đang hoạt động</span></html>");
            chatArea.removeAll();
            addMessageBubble("Hệ thống", "Đã kết nối với " + customerName, false);
        });
    }
    @Override public void onMessageReceived(String sender, String msg) {
        SwingUtilities.invokeLater(() -> { addMessageBubble(sender, msg, false); });
    }
    @Override public void onSessionEnded(String reason) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Phiên chat đã kết thúc: " + reason);
            parentFrame.navigateTo("QUEUE_SCREEN");
        });
    }
    @Override public void onError(String msg) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE));
    }
}
