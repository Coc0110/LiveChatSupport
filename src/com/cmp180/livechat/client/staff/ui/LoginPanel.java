package com.cmp180.livechat.client.staff.ui;

import com.cmp180.livechat.client.staff.context.StaffContext;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import com.cmp180.livechat.client.staff.network.StaffNetworkListener;

public class LoginPanel extends JPanel implements StaffNetworkListener {
    private StaffMainFrame parentFrame;
    private CardLayout authLayout;
    private JPanel authContainer;
    private JButton loginBtn;

    public LoginPanel(StaffMainFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new GridBagLayout());
        setBackground(new Color(225, 230, 238));

        authLayout = new CardLayout();
        authContainer = new JPanel(authLayout);
        authContainer.setOpaque(false);

        authContainer.add(buildLoginForm(), "LOGIN");
        add(authContainer);
        
        StaffContext.getInstance().getNetworkService().addListener(this);
    }

    private JPanel buildLoginForm() {
        JPanel form = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                for (int i = 0; i < 15; i++) {
                    g2.setColor(new Color(133, 189, 215, 5 + (15 - i)));
                    g2.fillRoundRect(i/2, 10 + i, getWidth() - i, getHeight() - 20 - i, 40, 40);
                }

                GradientPaint gp = new GradientPaint(0, getHeight(), new Color(248, 250, 255), 0, 0, new Color(255, 255, 255));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() - 20, 40, 40);

                g2.setStroke(new BasicStroke(4));
                g2.setColor(Color.WHITE);
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 24, 40, 40);
                g2.dispose();
            }
        };
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(380, 520));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(40, 40, 50, 40));

        JLabel title = new JLabel("Sign in");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(16, 137, 211));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField userField = createCustomTextField("Mã NV hoặc Email");
        JPasswordField pwdField = createCustomPasswordField("Mật khẩu");

        JPanel forgotPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        forgotPanel.setOpaque(false);
        forgotPanel.setPreferredSize(new Dimension(300, 30));
        forgotPanel.setMaximumSize(new Dimension(300, 30));
        JLabel forgotLbl = new JLabel("Quên mật khẩu?");
        forgotLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        forgotLbl.setForeground(new Color(0, 153, 255));
        forgotLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPanel.add(forgotLbl);

        loginBtn = new JButton("Đăng nhập") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(133, 189, 215, 150));
                g2.fillRoundRect(0, 5, getWidth(), getHeight() - 5, 20, 20);

                GradientPaint gp = new GradientPaint(0, 0, new Color(16, 137, 211), getWidth(), getHeight(), new Color(18, 177, 209));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() - 5, 20, 20);
                
                FontMetrics fm = g2.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - 5 - (int) r.getHeight()) / 2 + fm.getAscent();
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        loginBtn.setContentAreaFilled(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginBtn.setPreferredSize(new Dimension(300, 55));
        loginBtn.setMaximumSize(new Dimension(300, 55));
        loginBtn.setMinimumSize(new Dimension(300, 55));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên/mã NV");
                return;
            }
            loginBtn.setEnabled(false);
            loginBtn.setText("Đang kết nối...");
            StaffContext.getInstance().setStaffName(username);
            StaffContext.getInstance().getNetworkService().connectAndLogin(username);
        });

        JLabel agreeLbl = new JLabel("Bằng việc đăng nhập, bạn đồng ý với Chính sách");
        agreeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        agreeLbl.setForeground(new Color(150, 150, 150));
        agreeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        form.add(Box.createVerticalStrut(10));
        form.add(title);
        form.add(Box.createVerticalStrut(40));
        form.add(userField);
        form.add(Box.createVerticalStrut(20));
        form.add(pwdField);
        form.add(Box.createVerticalStrut(10));
        form.add(forgotPanel);
        form.add(Box.createVerticalStrut(30));
        form.add(loginBtn);
        form.add(Box.createVerticalGlue());
        form.add(agreeLbl);

        return form;
    }

    private JTextField createCustomTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(207, 240, 255, 120));
                g2.fillRoundRect(0, 5, getWidth(), getHeight() - 5, 20, 20);
                
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() - 5, 20, 20);
                
                if (isFocusOwner()) {
                    g2.setColor(new Color(18, 177, 209));
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 7, 20, 20);
                } else {
                    g2.setColor(new Color(230, 230, 230));
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 6, 20, 20);
                }

                if (getDocument().getLength() == 0) {
                    g2.setColor(new Color(170, 170, 170));
                    g2.drawString(placeholder, getInsets().left + 2, g.getFontMetrics().getMaxAscent() + getInsets().top);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(12, 20, 17, 20));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(300, 55));
        field.setMaximumSize(new Dimension(300, 55));
        field.setMinimumSize(new Dimension(300, 55));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { field.repaint(); }
            public void focusLost(FocusEvent e) { field.repaint(); }
        });
        return field;
    }

    private JPasswordField createCustomPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(207, 240, 255, 120));
                g2.fillRoundRect(0, 5, getWidth(), getHeight() - 5, 20, 20);
                
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() - 5, 20, 20);
                
                if (isFocusOwner()) {
                    g2.setColor(new Color(18, 177, 209));
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 7, 20, 20);
                } else {
                    g2.setColor(new Color(230, 230, 230));
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 6, 20, 20);
                }

                if (getDocument().getLength() == 0) {
                    g2.setColor(new Color(170, 170, 170));
                    g2.drawString(placeholder, getInsets().left + 2, g.getFontMetrics().getMaxAscent() + getInsets().top);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(12, 20, 17, 20));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(300, 55));
        field.setMaximumSize(new Dimension(300, 55));
        field.setMinimumSize(new Dimension(300, 55));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { field.repaint(); }
            public void focusLost(FocusEvent e) { field.repaint(); }
        });
        return field;
    }

    @Override
    public void onLoginSuccess() {
        SwingUtilities.invokeLater(() -> {
            if (loginBtn != null) {
                loginBtn.setEnabled(true);
                loginBtn.setText("Đăng nhập");
            }
            parentFrame.navigateTo("QUEUE_SCREEN");
        });
    }

    @Override public void onQueueUpdated(java.util.Map<String, String> waitingCustomers) {}
    @Override public void onPairedWithCustomer(String customerName) {}
    @Override public void onMessageReceived(String sender, String msg) {}
    @Override public void onSessionEnded(String reason) {}

    @Override
    public void onError(String msg) {
        SwingUtilities.invokeLater(() -> {
            if (loginBtn != null) {
                loginBtn.setEnabled(true);
                loginBtn.setText("Đăng nhập");
            }
            JOptionPane.showMessageDialog(this, msg, "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
        });
    }
}
