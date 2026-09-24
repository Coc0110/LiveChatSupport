package com.cmp180.livechat.client.customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class CustomerClient extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;

    public CustomerClient() {
        setTitle("LiveChat Support Desk");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(new Color(160, 160, 160)); 

        mainContainer.add(createStartScreen(), "START_SCREEN");
        mainContainer.add(createMainChatScreen(), "MAIN_CHAT_SCREEN");

        add(mainContainer);
        cardLayout.show(mainContainer, "START_SCREEN");
        
        mainContainer.requestFocusInWindow();
    }

    // ================= 1. MÀN HÌNH BẮT ĐẦU =================
    private JPanel createStartScreen() {
        JPanel bgPanel = new JPanel(new GridBagLayout());
        bgPanel.setBackground(new Color(160, 160, 160)); 
        
        bgPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { bgPanel.requestFocus(); }
        });

        RoundedPanel formCard = new RoundedPanel(20, Color.WHITE);
        formCard.setPreferredSize(new Dimension(500, 620));
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(new EmptyBorder(35, 20, 35, 20));

        JComponent headphoneIcon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 122, 255));
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(10, 5, 30, 30, 0, 180);
                g2.fillRoundRect(8, 20, 8, 14, 5, 5);
                g2.fillRoundRect(34, 20, 8, 14, 5, 5);
                g2.dispose();
            }
        };
        headphoneIcon.setPreferredSize(new Dimension(50, 40));
        headphoneIcon.setMaximumSize(new Dimension(50, 40));
        headphoneIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Bắt đầu cuộc trò chuyện");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("<html><div style='text-align: center; width: 380px; color: #777777;'>Vui lòng cung cấp thông tin bên dưới để được kết nối trực<br>tiếp với tư vấn viên hỗ trợ.</div></html>");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        formCard.add(headphoneIcon);
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(titleLabel);
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(subtitleLabel);
        formCard.add(Box.createVerticalStrut(25));

        JPanel formWrapper = new JPanel();
        formWrapper.setLayout(new BoxLayout(formWrapper, BoxLayout.Y_AXIS));
        formWrapper.setOpaque(false);
        formWrapper.setMaximumSize(new Dimension(380, 270)); 
        formWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        addPlaceholderField(formWrapper, "Họ và tên *", "Nhập họ và tên của bạn");
        addPlaceholderField(formWrapper, "Địa chỉ Email *", "vidu@email.com");

        JLabel topicLabel = new JLabel("Chủ đề cần hỗ trợ *");
        topicLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        topicLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formWrapper.add(topicLabel);
        formWrapper.add(Box.createVerticalStrut(8));
        
        String[] topics = {"Hỏi về chính sách bảo hành", "Hỗ trợ kỹ thuật", "Khác"};
        RoundedComboBox<String> topicCombo = new RoundedComboBox<>(topics, 15);
        topicCombo.setMaximumSize(new Dimension(380, 42)); 
        topicCombo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        topicCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        formWrapper.add(topicCombo);

        formCard.add(formWrapper);
        formCard.add(Box.createVerticalStrut(40));

        RoundedButton startBtn = new RoundedButton("Bắt đầu trò chuyện", 15, new Color(0, 122, 255), Color.WHITE);
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        startBtn.setMaximumSize(new Dimension(380, 45)); 
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.addActionListener(e -> cardLayout.show(mainContainer, "MAIN_CHAT_SCREEN"));
        
        formCard.add(startBtn);
        
        JLabel footerLabel = new JLabel("Thời gian kết nối trung bình dưới 1 phút");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(new Color(170, 170, 170));
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(Box.createVerticalStrut(15));
        formCard.add(footerLabel);

        bgPanel.add(formCard);
        return bgPanel;
    }

    private void addPlaceholderField(JPanel panel, String title, String placeholder) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        PlaceholderTextField field = new PlaceholderTextField(15, placeholder); 
        field.setMaximumSize(new Dimension(380, 42)); 
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(label);
        panel.add(Box.createVerticalStrut(8));
        panel.add(field);
        panel.add(Box.createVerticalStrut(15));
    }

    // ================= 2. MÀN HÌNH CHAT CHÍNH (ĐÃ CẬP NHẬT CHUẨN 100%) =================
    private JPanel createMainChatScreen() {
        JPanel mainChatPanel = new JPanel(new BorderLayout());
        
        mainChatPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { mainChatPanel.requestFocus(); }
        });

        // --- Sidebar (Bên trái) ---
        JPanel sidebar = new JPanel(null); 
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));
        
        JComponent logoIcon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 122, 255));
                g2.fillRoundRect(0, 0, 32, 32, 12, 12); 
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(8, 8, 16, 12, 6, 6); 
                g2.drawLine(12, 20, 10, 24); 
                g2.drawLine(10, 24, 16, 20);
                g2.dispose();
            }
        };
        logoIcon.setBounds(20, 20, 32, 32);
        
        JLabel logoText = new JLabel("<html><b>LiveChat</b><br><span style='font-size:10px; color:#007AFF'>SUPPORT DESK</span></html>");
        logoText.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        logoText.setBounds(65, 16, 150, 40);
        
        sidebar.add(logoIcon);
        sidebar.add(logoText);

        // --- Khung chat (Bên phải) ---
        JPanel chatArea = new JPanel(new BorderLayout());
        chatArea.setBackground(Color.WHITE); 

        // Header chuẩn Figma (Avatar tư vấn viên + Thông tin + Nút Kết thúc trò chuyện)[cite: 21]
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
            new EmptyBorder(12, 25, 12, 25)
        ));
        
        // Thông tin tư vấn viên gồm Avatar tròn & trạng thái[cite: 21]
        JPanel agentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        agentPanel.setOpaque(false);
        
        JComponent avatarComp = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Vẽ vòng tròn xanh biểu thị online ở góc avatar
                g2.setColor(new Color(40, 167, 69));
                g2.fillOval(28, 28, 10, 10);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(28, 28, 10, 10);
                g2.dispose();
            }
        };
        avatarComp.setPreferredSize(new Dimension(40, 40));
        
        JLabel agentDetails = new JLabel("<html><b>Nguyễn Văn A</b><br><span style='font-size:11px; color:#28a745;'>Tư vấn viên hỗ trợ • Đang hoạt động</span></html>");
        agentDetails.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        agentPanel.add(avatarComp);
        agentPanel.add(agentDetails);

        // Nút Kết thúc trò chuyện: Icon và chữ cùng 1 hàng ngang, bo tròn mượt mà[cite: 21]
        JButton endChatBtn = new JButton("Kết thúc trò chuyện") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Nền hồng nhạt
                g2.setColor(new Color(255, 235, 238));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                
                // Vẽ icon cấm/hủy nhỏ xinh nằm bên trái chữ
                g2.setColor(new Color(220, 53, 69));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(12, 11, 12, 12);
                g2.drawLine(15, 14, 21, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        endChatBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        endChatBtn.setForeground(new Color(220, 53, 69));
        endChatBtn.setContentAreaFilled(false);
        endChatBtn.setBorderPainted(false);
        endChatBtn.setFocusPainted(false);
        endChatBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        endChatBtn.setBorder(new EmptyBorder(0, 28, 0, 12)); // Chừa chỗ bên trái cho icon
        endChatBtn.setPreferredSize(new Dimension(175, 36));
        endChatBtn.addActionListener(e -> cardLayout.show(mainContainer, "START_SCREEN"));
        
        header.add(agentPanel, BorderLayout.WEST);
        header.add(endChatBtn, BorderLayout.EAST);

        // Vùng hiển thị tin nhắn (Chat Message Panel)[cite: 21]
        JPanel messageListPanel = new JPanel();
        messageListPanel.setLayout(new BoxLayout(messageListPanel, BoxLayout.Y_AXIS));
        messageListPanel.setBackground(new Color(248, 249, 250));
        messageListPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Thanh mốc thời gian phiên chat[cite: 21]
        JPanel timeBadgePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        timeBadgePanel.setOpaque(false);
        JLabel timeBadge = new JLabel(" Phiên chat được bắt đầu lúc 09:30 AM ");
        timeBadge.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeBadge.setForeground(new Color(110, 110, 110));
        timeBadge.setBackground(new Color(230, 230, 230));
        timeBadge.setOpaque(true);
        timeBadge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        timeBadgePanel.add(timeBadge);
        messageListPanel.add(timeBadgePanel);
        messageListPanel.add(Box.createVerticalStrut(15));

        // Tin nhắn mẫu từ tư vấn viên (Bên trái)[cite: 21]
        addChatMessage(messageListPanel, "Xin chào anh/chị! Em là Nguyễn Văn A, tư vấn viên của LiveChat Support. Em có thể hỗ trợ gì cho mình hôm nay ạ?", "09:30 AM", false);
        // Tin nhắn mẫu từ khách hàng (Bên phải)[cite: 21]
        addChatMessage(messageListPanel, "Chào bạn, mình muốn hỏi về chính sách bảo hành của dòng sản phẩm bên mình.", "09:32 AM", true);
        addChatMessage(messageListPanel, "Dạ, dòng sản phẩm bên em được bảo hành chính hãng 12 tháng kể từ ngày kích hoạt ạ. Không biết dòng sản phẩm của mình mua từ khi nào vậy ạ?", "09:33 AM", false);
        addChatMessage(messageListPanel, "Mình mua bản Pro từ đầu tháng trước rồi, giờ cần hỗ trợ kích hoạt trực tuyến.", "09:35 AM", true);

        JScrollPane scrollPane = new JScrollPane(messageListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Thanh nhập tin nhắn dưới cùng (Input Area)[cite: 21]
        JPanel inputArea = new JPanel(new BorderLayout(15, 0));
        inputArea.setBackground(Color.WHITE);
        inputArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)),
            new EmptyBorder(18, 25, 18, 25)
        ));
        
        // Ô nhập liệu bo tròn lớn[cite: 21]
        PlaceholderTextField inputField = new PlaceholderTextField(25, "Nhập tin nhắn hỗ trợ tại đây...");
        inputField.setPreferredSize(new Dimension(0, 48));

        // Nút gửi hình máy bay giấy tròn xanh[cite: 21]
        JButton sendBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 122, 255));
                g2.fillOval(0, 0, getWidth(), getHeight());
                
                g2.setColor(Color.WHITE);
                Path2D.Double plane = new Path2D.Double();
                plane.moveTo(14, 24);
                plane.lineTo(34, 14);
                plane.lineTo(24, 34);
                plane.lineTo(21, 25);
                plane.closePath();
                g2.fill(plane);
                g2.dispose();
            }
        };
        sendBtn.setPreferredSize(new Dimension(48, 48));
        sendBtn.setContentAreaFilled(false);
        sendBtn.setBorderPainted(false);
        sendBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        inputArea.add(inputField, BorderLayout.CENTER);
        inputArea.add(sendBtn, BorderLayout.EAST);

        chatArea.add(header, BorderLayout.NORTH);
        chatArea.add(scrollPane, BorderLayout.CENTER);
        chatArea.add(inputArea, BorderLayout.SOUTH);

        mainChatPanel.add(sidebar, BorderLayout.WEST);
        mainChatPanel.add(chatArea, BorderLayout.CENTER);

        return mainChatPanel;
    }

    // Hàm tiện ích tạo bong bóng chat[cite: 21]
    private void addChatMessage(JPanel container, String message, String time, boolean isUser) {
        JPanel msgWrapper = new JPanel(new FlowLayout(isUser ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 2));
        msgWrapper.setOpaque(false);
        
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setOpaque(true);
        bubble.setBackground(isUser ? new Color(0, 122, 255) : new Color(230, 232, 235));
        bubble.setBorder(new EmptyBorder(10, 14, 10, 14));
        
        JLabel textLabel = new JLabel("<html><p style='width: 350px;'>" + message + "</p></html>");
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textLabel.setForeground(isUser ? Color.WHITE : Color.DARK_GRAY);
        
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(isUser ? new Color(220, 235, 255) : new Color(130, 130, 130));
        timeLabel.setAlignmentX(isUser ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        
        bubble.add(textLabel);
        bubble.add(Box.createVerticalStrut(4));
        bubble.add(timeLabel);
        
        msgWrapper.add(bubble);
        container.add(msgWrapper);
        container.add(Box.createVerticalStrut(10));
    }

    // ================= CÁC LỚP ĐỒ HỌA TÙY CHỈNH =================

    class RoundedComboBox<E> extends JComboBox<E> {
        private int cornerRadius;

        public RoundedComboBox(E[] items, int radius) {
            super(items);
            this.cornerRadius = radius;
            setOpaque(false);
            setBackground(new Color(0, 0, 0, 0));
            setBorder(new EmptyBorder(5, 10, 5, 10));
            
            setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton button = new JButton("▼");
                    button.setContentAreaFilled(false);
                    button.setBorderPainted(false);
                    button.setFocusPainted(false);
                    button.setForeground(new Color(120, 120, 120));
                    button.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    return button;
                }
                
                @Override
                public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {}
            });

            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    label.setBorder(new EmptyBorder(8, 12, 8, 12));
                    label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    if (isSelected) {
                        label.setBackground(new Color(230, 240, 255));
                        label.setForeground(new Color(0, 100, 255));
                    } else {
                        label.setBackground(Color.WHITE);
                        label.setForeground(Color.BLACK);
                    }
                    return label;
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isFocusOwner()) g2.setColor(new Color(0, 122, 255));
            else g2.setColor(new Color(220, 220, 220));
            g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));
            g2.dispose();
        }
    }

    class PlaceholderTextField extends JTextField implements FocusListener {
        private String placeholder;
        private int cornerRadius;

        public PlaceholderTextField(int radius, String placeholder) {
            super();
            this.cornerRadius = radius;
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(new EmptyBorder(5, 15, 5, 15));
            
            setText(placeholder);
            setForeground(new Color(150, 150, 150)); 
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            
            addFocusListener(this);
        }

        @Override
        public void focusGained(FocusEvent e) {
            if (getText().equals(placeholder)) {
                setText(""); 
                setForeground(Color.BLACK); 
                setFont(new Font("Segoe UI", Font.BOLD, 14));
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (getText().isEmpty()) {
                setText(placeholder); 
                setForeground(new Color(150, 150, 150));
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isFocusOwner()) g2.setColor(new Color(0, 122, 255)); 
            else g2.setColor(new Color(220, 220, 220));
            g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));
            g2.dispose();
        }
    }

    class RoundedPanel extends JPanel {
        private int cornerRadius;
        private Color bgColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.cornerRadius = radius;
            this.bgColor = bgColor;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
            g2.dispose();
        }
    }

    class RoundedButton extends JButton {
        private int cornerRadius;
        private Color bgColor;

        public RoundedButton(String text, int radius, Color bg, Color fg) {
            super(text);
            this.cornerRadius = radius;
            this.bgColor = bg;
            setForeground(fg);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? bgColor.darker() : bgColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CustomerClient().setVisible(true);
        });
    }
}