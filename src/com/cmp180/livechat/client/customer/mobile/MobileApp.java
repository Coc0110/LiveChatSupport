package com.cmp180.livechat.client.customer.mobile;

import com.cmp180.livechat.client.customer.components.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;

public class MobileApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;

    public MobileApp() {
        setTitle("LiveChat Mobile");
        setSize(375, 812);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(Color.WHITE);

        mainContainer.add(createIntroScreen(), "INTRO_SCREEN");
        mainContainer.add(createFormScreen(), "FORM_SCREEN");
        mainContainer.add(createLoadingScreen(), "LOADING_SCREEN");
        mainContainer.add(createSuccessScreen(), "SUCCESS_SCREEN");
        mainContainer.add(createChatScreen(), "CHAT_SCREEN");

        JPanel phoneFrame = new JPanel(new BorderLayout());
        phoneFrame.setBackground(Color.WHITE);
        phoneFrame.add(createStatusBar(), BorderLayout.NORTH);
        phoneFrame.add(mainContainer, BorderLayout.CENTER);
        phoneFrame.add(createHomeIndicator(), BorderLayout.SOUTH);

        add(phoneFrame);
        cardLayout.show(mainContainer, "INTRO_SCREEN");
        mainContainer.requestFocusInWindow();
    }

    private JPanel createStatusBar() {
        JPanel status = new JPanel(new BorderLayout());
        status.setBackground(Color.WHITE);
        status.setPreferredSize(new Dimension(375, 44));
        status.setBorder(new EmptyBorder(10, 20, 0, 20));
        JLabel timeLabel = new JLabel("9:41");
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel rightIcons = new JLabel("LTE  🔋");
        rightIcons.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        status.add(timeLabel, BorderLayout.WEST);
        status.add(rightIcons, BorderLayout.EAST);
        return status;
    }

    private JPanel createHomeIndicator() {
        JPanel homePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);
                g2.fillRoundRect(getWidth() / 2 - 65, 15, 130, 5, 5, 5);
                g2.dispose();
            }
        };
        homePanel.setBackground(Color.WHITE);
        homePanel.setPreferredSize(new Dimension(375, 34));
        return homePanel;
    }

    private JPanel createIntroScreen() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(40, 25, 20, 25));

        JComponent logoIcon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 122, 255));
                g2.fillOval(0, 0, 80, 80);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(22, 25, 36, 26, 8, 8);
                g2.drawLine(30, 51, 25, 60);
                g2.drawLine(25, 60, 38, 51);
                g2.dispose();
            }
        };
        logoIcon.setPreferredSize(new Dimension(80, 80));
        logoIcon.setMaximumSize(new Dimension(80, 80));
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("LiveChat Support");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(0, 122, 255));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Hỗ trợ khách hàng trực tuyến");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalStrut(20));
        panel.add(logoIcon);
        panel.add(Box.createVerticalStrut(25));
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(40));

        panel.add(createFeatureItem("🛡️", "An toàn & Bảo mật", "Thông tin cuộc gọi và chat được mã hóa đầu cuối."));
        panel.add(Box.createVerticalStrut(20));
        panel.add(createFeatureItem("⚡", "Kết nối nhanh chóng", "Tư vấn viên sẵn sàng hỗ trợ bạn trong 30 giây."));
        panel.add(Box.createVerticalGlue());

        RoundedButton startBtn = new RoundedButton("Bắt đầu trò chuyện", 25, new Color(0, 122, 255), Color.WHITE);
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        startBtn.setMaximumSize(new Dimension(320, 50));
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.addActionListener(e -> cardLayout.show(mainContainer, "FORM_SCREEN"));

        JLabel termLabel = new JLabel("<html><div style='text-align: center; color: #AAAAAA; font-size: 10px;'>Bằng cách tiếp tục, bạn đồng ý với Điều khoản dịch vụ của chúng tôi.</div></html>");
        termLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(startBtn);
        panel.add(Box.createVerticalStrut(15));
        panel.add(termLabel);
        return panel;
    }

    private JPanel createFeatureItem(String emoji, String title, String desc) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(320, 60));
        JLabel icon = new JLabel(emoji, SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        icon.setPreferredSize(new Dimension(40, 40));
        icon.setOpaque(true);
        icon.setBackground(new Color(240, 245, 255));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(Color.WHITE);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel descLbl = new JLabel("<html><span style='color: #888888; font-size: 10px;'>" + desc + "</span></html>");
        textPanel.add(titleLbl);
        textPanel.add(descLbl);
        
        panel.add(icon, BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFormScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        header.setBackground(Color.WHITE);
        JLabel backBtn = new JLabel("<html><span style='color: #007AFF; font-size: 18px;'>←</span> <b>Thông tin cá nhân</b></html>");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { cardLayout.show(mainContainer, "INTRO_SCREEN"); }
        });
        header.add(backBtn);
        panel.add(header, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Hãy cho chúng tôi biết về bạn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subTitle = new JLabel("<html><div style='color: #888888;'>Vui lòng điền thông tin bên dưới để được kết nối với<br>tư vấn viên phù hợp nhất.</div></html>");
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        formPanel.add(title);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(subTitle);
        formPanel.add(Box.createVerticalStrut(30));

        addMobileField(formPanel, "Họ và tên *", "Ví dụ: Nguyễn Văn A");
        addMobileField(formPanel, "Địa chỉ Email *", "username@domain.com");

        JLabel topicLabel = new JLabel("Chủ đề cần hỗ trợ *");
        topicLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        topicLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(topicLabel);
        formPanel.add(Box.createVerticalStrut(8));
        
        String[] topics = {"Tư vấn mua hàng & Khuyến mãi", "Hỗ trợ kỹ thuật", "Khác"};
        RoundedComboBox<String> topicCombo = new RoundedComboBox<>(topics, 12);
        topicCombo.setMaximumSize(new Dimension(320, 45)); 
        topicCombo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        topicCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(topicCombo);

        panel.add(formPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 20, 0));
        RoundedButton connectBtn = new RoundedButton("Kết nối với tư vấn viên", 25, new Color(0, 122, 255), Color.WHITE);
        connectBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        connectBtn.setPreferredSize(new Dimension(320, 50));
        connectBtn.addActionListener(e -> {
            cardLayout.show(mainContainer, "LOADING_SCREEN");
            Timer timer = new Timer(2500, ev -> {
                cardLayout.show(mainContainer, "SUCCESS_SCREEN");
                Timer timer2 = new Timer(1500, ev2 -> cardLayout.show(mainContainer, "CHAT_SCREEN"));
                timer2.setRepeats(false);
                timer2.start();
            });
            timer.setRepeats(false);
            timer.start();
        });
        bottomPanel.add(connectBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addMobileField(JPanel panel, String title, String placeholder) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        PlaceholderTextField field = new PlaceholderTextField(12, placeholder); 
        field.setMaximumSize(new Dimension(320, 45)); 
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(8));
        panel.add(field);
        panel.add(Box.createVerticalStrut(20));
    }

    private JPanel createLoadingScreen() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.add(Box.createVerticalStrut(150));

        JComponent loadingIcon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 242, 255)); g2.fillOval(10, 10, 100, 100);
                g2.setColor(new Color(200, 225, 255)); g2.fillOval(25, 25, 70, 70);
                g2.setColor(new Color(0, 122, 255)); g2.fillOval(35, 35, 50, 50);
                g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2f));
                g2.drawOval(48, 48, 24, 24); g2.drawLine(55, 55, 65, 65); g2.drawLine(65, 55, 55, 65);
                g2.dispose();
            }
        };
        loadingIcon.setPreferredSize(new Dimension(120, 120));
        loadingIcon.setMaximumSize(new Dimension(120, 120));
        loadingIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Đang kết nối với tư vấn viên...");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setHorizontalAlignment(SwingConstants.CENTER); 
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("<html><div style='text-align: center; color: #888888; font-size: 12px; width: 320px;'>Vui lòng chờ trong giây lát. Hệ thống đang<br>tìm tư vấn viên phù hợp nhất.</div></html>");
        subtitle.setHorizontalAlignment(SwingConstants.CENTER); 
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(loadingIcon);
        panel.add(Box.createVerticalStrut(30));
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subtitle);
        panel.add(Box.createVerticalGlue());

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        badgePanel.setOpaque(false);
        JLabel timeBadge = new JLabel("⏱ Thời gian chờ dự kiến: ~1 phút");
        timeBadge.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeBadge.setForeground(Color.GRAY);
        timeBadge.setBackground(new Color(245, 245, 245));
        timeBadge.setOpaque(true);
        timeBadge.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        badgePanel.add(timeBadge);
        
        RoundedButton cancelBtn = new RoundedButton("Hủy kết nối", 25, Color.WHITE, new Color(0, 122, 255));
        cancelBtn.setBorder(BorderFactory.createLineBorder(new Color(0, 122, 255), 1));
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cancelBtn.setMaximumSize(new Dimension(320, 50));
        cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelBtn.addActionListener(e -> cardLayout.show(mainContainer, "FORM_SCREEN"));

        panel.add(badgePanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(cancelBtn);
        panel.add(Box.createVerticalStrut(50));
        return panel;
    }

    private JPanel createSuccessScreen() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.add(Box.createVerticalStrut(200));

        JComponent successIcon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(235, 255, 240)); g2.fillOval(10, 10, 100, 100);
                g2.setColor(new Color(200, 255, 215)); g2.fillOval(25, 25, 70, 70);
                g2.setColor(new Color(0, 210, 50)); g2.fillOval(35, 35, 50, 50);
                g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(48, 48, 24, 24); g2.drawLine(54, 60, 58, 64); g2.drawLine(58, 64, 66, 56);
                g2.dispose();
            }
        };
        successIcon.setPreferredSize(new Dimension(120, 120));
        successIcon.setMaximumSize(new Dimension(120, 120));
        successIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("<html><div style='text-align: center; width: 300px;'><b>Đã kết nối với tư vấn viên<br>Nguyễn Văn A</b></div></html>");
        title.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        title.setHorizontalAlignment(SwingConstants.CENTER); 
        title.setAlignmentX(Component.CENTER_ALIGNMENT); 

        panel.add(successIcon);
        panel.add(Box.createVerticalStrut(30));
        panel.add(title);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createChatScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
            new EmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel agentDetails = new JLabel("<html><b>Nguyễn Văn A</b><br><span style='font-size:10px; color:#A0A0A0;'>● Đang hoạt động</span></html>");
        agentDetails.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        
        Icon cancelIcon = new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 53, 69));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y + 1, 12, 12); 
                g2.drawLine(x + 3, y + 4, x + 9, y + 10);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 12; }
            @Override public int getIconHeight() { return 12; }
        };

        RoundedButton endChatBtn = new RoundedButton("Kết thúc trò chuyện", 14, new Color(255, 235, 238), new Color(220, 53, 69));
        endChatBtn.setIcon(cancelIcon);
        endChatBtn.setIconTextGap(6);
        endChatBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        endChatBtn.setPreferredSize(new Dimension(145, 30));
        endChatBtn.addActionListener(e -> cardLayout.show(mainContainer, "INTRO_SCREEN")); 

        header.add(agentDetails, BorderLayout.WEST);
        header.add(endChatBtn, BorderLayout.EAST);

        JPanel messageListPanel = new JPanel();
        messageListPanel.setLayout(new BoxLayout(messageListPanel, BoxLayout.Y_AXIS));
        messageListPanel.setBackground(Color.WHITE);
        messageListPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        addMobileChatMessage(messageListPanel, "Xin chào quý khách! Tôi có thể giúp gì cho quý khách về chủ đề 'Tư vấn mua hàng' hôm nay ạ?", "09:41", false);
        addMobileChatMessage(messageListPanel, "Chào bạn, mình đang muốn tìm hiểu về chương trình trả góp 0% khi mua dòng sản phẩm Pro Max.", "09:42", true);
        addMobileChatMessage(messageListPanel, "Dạ hiện tại dòng Pro Max bên em đang có ưu đãi trả góp 0% thông qua thẻ tín dụng.", "09:43", false);

        JScrollPane scrollPane = new JScrollPane(messageListPanel);
        scrollPane.setBorder(null);

        JPanel inputArea = new JPanel(new BorderLayout(10, 0));
        inputArea.setBackground(Color.WHITE);
        inputArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)),
            new EmptyBorder(10, 15, 15, 15)
        ));
        
        PlaceholderTextField inputField = new PlaceholderTextField(20, "Nhập tin nhắn...");
        inputField.setPreferredSize(new Dimension(0, 40));
        inputField.setBackground(new Color(245, 245, 245));
        
        JButton sendBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 122, 255));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                Path2D.Double plane = new Path2D.Double();
                plane.moveTo(10, 18); plane.lineTo(26, 10);
                plane.lineTo(18, 26); plane.lineTo(16, 19); plane.closePath();
                g2.fill(plane);
                g2.dispose();
            }
        };
        sendBtn.setPreferredSize(new Dimension(36, 36));
        sendBtn.setContentAreaFilled(false);
        sendBtn.setBorderPainted(false);

        inputArea.add(inputField, BorderLayout.CENTER);
        inputArea.add(sendBtn, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inputArea, BorderLayout.SOUTH);
        return panel;
    }

    private void addMobileChatMessage(JPanel container, String message, String time, boolean isUser) {
        JPanel rowWrapper = new JPanel(new BorderLayout(5, 0));
        rowWrapper.setOpaque(false);
        if (!isUser) {
            JComponent avatar = new JComponent() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0, 122, 255)); g2.fillOval(0, 0, 30, 30);
                    g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2.drawString("NV", 7, 19);
                    g2.dispose();
                }
            };
            avatar.setPreferredSize(new Dimension(30, 30));
            JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            avatarPanel.setOpaque(false);
            avatarPanel.add(avatar);
            rowWrapper.add(avatarPanel, BorderLayout.WEST);
        }
        
        JPanel bubbleWrapper = new JPanel();
        bubbleWrapper.setLayout(new BoxLayout(bubbleWrapper, BoxLayout.Y_AXIS));
        bubbleWrapper.setOpaque(false);

        JPanel bubbleBg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isUser ? new Color(0, 122, 255) : new Color(240, 242, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        bubbleBg.setLayout(new BorderLayout());
        bubbleBg.setBorder(new EmptyBorder(10, 12, 10, 12));
        JLabel textLabel = new JLabel("<html><p style='width: 190px;'>" + message + "</p></html>");
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textLabel.setForeground(isUser ? Color.WHITE : Color.DARK_GRAY);
        bubbleBg.add(textLabel, BorderLayout.CENTER);
        
        JPanel alignPanel = new JPanel(new FlowLayout(isUser ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        alignPanel.setOpaque(false);
        alignPanel.add(bubbleBg);

        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(new Color(170, 170, 170));
        JPanel timePanel = new JPanel(new FlowLayout(isUser ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 2));
        timePanel.setOpaque(false);
        timePanel.add(timeLabel);
        
        bubbleWrapper.add(alignPanel);
        bubbleWrapper.add(timePanel);
        rowWrapper.add(bubbleWrapper, BorderLayout.CENTER);
        container.add(rowWrapper);
        container.add(Box.createVerticalStrut(15));
    }
}