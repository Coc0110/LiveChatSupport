package com.cmp180.livechat.client.customer.desktop;

import com.cmp180.livechat.client.customer.components.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;

public class DesktopApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;

    public DesktopApp() {
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

    private JPanel createMainChatScreen() {
        JPanel mainChatPanel = new JPanel(new BorderLayout());
        
        mainChatPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { mainChatPanel.requestFocus(); }
        });

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

        JPanel chatArea = new JPanel(new BorderLayout());
        chatArea.setBackground(Color.WHITE); 

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
            new EmptyBorder(12, 25, 12, 25)
        ));
        
        JPanel agentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        agentPanel.setOpaque(false);
        
        JComponent avatarComp = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

        Icon cancelIcon = new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 53, 69));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, 14, 14);
                g2.drawLine(x + 3, y + 3, x + 11, y + 11);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 14; }
            @Override public int getIconHeight() { return 14; }
        };

        RoundedButton endChatBtn = new RoundedButton("Kết thúc trò chuyện", 16, new Color(255, 235, 238), new Color(220, 53, 69));
        endChatBtn.setIcon(cancelIcon);
        endChatBtn.setIconTextGap(8); 
        endChatBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        endChatBtn.setPreferredSize(new Dimension(175, 36));
        endChatBtn.addActionListener(e -> cardLayout.show(mainContainer, "START_SCREEN"));
        
        header.add(agentPanel, BorderLayout.WEST);
        header.add(endChatBtn, BorderLayout.EAST);

        JPanel messageListPanel = new JPanel();
        messageListPanel.setLayout(new BoxLayout(messageListPanel, BoxLayout.Y_AXIS));
        messageListPanel.setBackground(new Color(248, 249, 250));
        messageListPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

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

        addChatMessage(messageListPanel, "Xin chào anh/chị! Em là Nguyễn Văn A, tư vấn viên của LiveChat Support. Em có thể hỗ trợ gì cho mình hôm nay ạ?", "09:30 AM", false);
        addChatMessage(messageListPanel, "Chào bạn, mình muốn hỏi về chính sách bảo hành của dòng sản phẩm bên mình.", "09:32 AM", true);
        addChatMessage(messageListPanel, "Dạ, dòng sản phẩm bên em được bảo hành chính hãng 12 tháng kể từ ngày kích hoạt ạ.", "09:33 AM", false);
        addChatMessage(messageListPanel, "Mình mua bản Pro từ đầu tháng trước rồi, giờ cần hỗ trợ kích hoạt trực tuyến.", "09:35 AM", true);

        JScrollPane scrollPane = new JScrollPane(messageListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel inputArea = new JPanel(new BorderLayout(15, 0));
        inputArea.setBackground(Color.WHITE);
        inputArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)),
            new EmptyBorder(18, 25, 18, 25)
        ));
        
        PlaceholderTextField inputField = new PlaceholderTextField(25, "Nhập tin nhắn hỗ trợ tại đây...");
        inputField.setPreferredSize(new Dimension(0, 48));

        JButton sendBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 122, 255));
                g2.fillOval(0, 0, getWidth(), getHeight());
                
                g2.setColor(Color.WHITE);
                Path2D.Double plane = new Path2D.Double();
                plane.moveTo(14, 24); plane.lineTo(34, 14);
                plane.lineTo(24, 34); plane.lineTo(21, 25);
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
}