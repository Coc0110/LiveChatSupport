import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class ApplyCssLogin {
    public static void main(String[] args) throws IOException {
        String content = "package com.cmp180.livechat.client.staff.ui;\n\n" +
"import com.cmp180.livechat.client.staff.context.StaffContext;\n" +
"import javax.swing.*;\n" +
"import javax.swing.border.EmptyBorder;\n" +
"import java.awt.*;\n" +
"import java.awt.event.*;\n" +
"import java.awt.geom.*;\n\n" +
"public class LoginPanel extends JPanel {\n" +
"    private StaffMainFrame parentFrame;\n" +
"    private CardLayout authLayout;\n" +
"    private JPanel authContainer;\n\n" +
"    public LoginPanel(StaffMainFrame parentFrame) {\n" +
"        this.parentFrame = parentFrame;\n" +
"        setLayout(new GridBagLayout());\n" +
"        setBackground(new Color(245, 247, 250));\n\n" +
"        authLayout = new CardLayout();\n" +
"        authContainer = new JPanel(authLayout);\n" +
"        authContainer.setOpaque(false);\n\n" +
"        authContainer.add(buildLoginForm(), \"LOGIN\");\n" +
"        add(authContainer);\n" +
"    }\n\n" +
"    private JPanel buildLoginForm() {\n" +
"        // Container: gradient background, 40px radius, 5px white border\n" +
"        JPanel form = new JPanel() {\n" +
"            @Override\n" +
"            protected void paintComponent(Graphics g) {\n" +
"                Graphics2D g2 = (Graphics2D) g.create();\n" +
"                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);\n" +
"                // Fake drop shadow\n" +
"                g2.setColor(new Color(133, 189, 215, 60));\n" +
"                g2.fillRoundRect(0, 15, getWidth(), getHeight()-15, 40, 40);\n" +
"                // Gradient background\n" +
"                GradientPaint gp = new GradientPaint(0, getHeight(), new Color(244, 247, 251), 0, 0, new Color(255, 255, 255));\n" +
"                g2.setPaint(gp);\n" +
"                g2.fillRoundRect(0, 0, getWidth(), getHeight()-10, 40, 40);\n" +
"                // 5px White Border\n" +
"                g2.setStroke(new BasicStroke(5));\n" +
"                g2.setColor(Color.WHITE);\n" +
"                g2.drawRoundRect(2, 2, getWidth()-5, getHeight()-15, 40, 40);\n" +
"                g2.dispose();\n" +
"            }\n" +
"        };\n" +
"        form.setOpaque(false);\n" +
"        form.setPreferredSize(new Dimension(350, 450));\n" +
"        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));\n" +
"        form.setBorder(new EmptyBorder(25, 35, 35, 35));\n\n" +
"        // Heading\n" +
"        JLabel title = new JLabel(\"Sign in\");\n" +
"        title.setFont(new Font(\"Segoe UI\", Font.BOLD, 30));\n" +
"        title.setForeground(new Color(16, 137, 211));\n" +
"        title.setAlignmentX(Component.CENTER_ALIGNMENT);\n\n" +
"        // Inputs\n" +
"        JTextField userField = createCustomTextField(\"Mã NV hoặc Email\");\n" +
"        JPasswordField pwdField = createCustomPasswordField();\n\n" +
"        // Forgot password\n" +
"        JPanel forgotPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));\n" +
"        forgotPanel.setOpaque(false);\n" +
"        forgotPanel.setMaximumSize(new Dimension(300, 20));\n" +
"        JLabel forgotLbl = new JLabel(\"Quên mật khẩu?\");\n" +
"        forgotLbl.setFont(new Font(\"Segoe UI\", Font.PLAIN, 11));\n" +
"        forgotLbl.setForeground(new Color(0, 153, 255));\n" +
"        forgotLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));\n" +
"        forgotPanel.add(forgotLbl);\n\n" +
"        // Button\n" +
"        JButton loginBtn = new JButton(\"Đăng nhập\") {\n" +
"            @Override\n" +
"            protected void paintComponent(Graphics g) {\n" +
"                Graphics2D g2 = (Graphics2D) g.create();\n" +
"                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);\n" +
"                GradientPaint gp = new GradientPaint(0, 0, new Color(16, 137, 211), getWidth(), getHeight(), new Color(18, 177, 209));\n" +
"                g2.setPaint(gp);\n" +
"                g2.fillRoundRect(0, 0, getWidth(), getHeight()-5, 20, 20);\n" +
"                \n" +
"                FontMetrics fm = g2.getFontMetrics();\n" +
"                Rectangle2D r = fm.getStringBounds(getText(), g2);\n" +
"                int x = (getWidth() - (int) r.getWidth()) / 2;\n" +
"                int y = (getHeight() - 5 - (int) r.getHeight()) / 2 + fm.getAscent();\n" +
"                g2.setColor(Color.WHITE);\n" +
"                g2.drawString(getText(), x, y);\n" +
"                g2.dispose();\n" +
"            }\n" +
"        };\n" +
"        loginBtn.setContentAreaFilled(false);\n" +
"        loginBtn.setBorderPainted(false);\n" +
"        loginBtn.setFocusPainted(false);\n" +
"        loginBtn.setFont(new Font(\"Segoe UI\", Font.BOLD, 15));\n" +
"        loginBtn.setMaximumSize(new Dimension(300, 50));\n" +
"        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);\n" +
"        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));\n" +
"        loginBtn.addActionListener(e -> {\n" +
"            String username = userField.getText().trim();\n" +
"            if (username.isEmpty() || username.equals(\"Mã NV hoặc Email\")) {\n" +
"                JOptionPane.showMessageDialog(this, \"Vui lòng nhập tên/mã NV\");\n" +
"                return;\n" +
"            }\n" +
"            StaffContext.getInstance().setStaffName(username);\n" +
"            StaffContext.getInstance().getNetworkService().connectAndLogin(username);\n" +
"            parentFrame.navigateTo(\"QUEUE_SCREEN\");\n" +
"        });\n\n" +
"        // Agreement\n" +
"        JLabel agreeLbl = new JLabel(\"Bằng việc đăng nhập, bạn đồng ý với Chính sách\");\n" +
"        agreeLbl.setFont(new Font(\"Segoe UI\", Font.PLAIN, 10));\n" +
"        agreeLbl.setForeground(new Color(170, 170, 170));\n" +
"        agreeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);\n\n" +
"        form.add(Box.createVerticalStrut(10));\n" +
"        form.add(title);\n" +
"        form.add(Box.createVerticalStrut(30));\n" +
"        form.add(userField);\n" +
"        form.add(Box.createVerticalStrut(15));\n" +
"        form.add(pwdField);\n" +
"        form.add(Box.createVerticalStrut(10));\n" +
"        form.add(forgotPanel);\n" +
"        form.add(Box.createVerticalStrut(25));\n" +
"        form.add(loginBtn);\n" +
"        form.add(Box.createVerticalGlue());\n" +
"        form.add(agreeLbl);\n\n" +
"        return form;\n" +
"    }\n\n" +
"    private JTextField createCustomTextField(String placeholder) {\n" +
"        JTextField field = new JTextField() {\n" +
"            @Override\n" +
"            protected void paintComponent(Graphics g) {\n" +
"                Graphics2D g2 = (Graphics2D) g.create();\n" +
"                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);\n" +
"                g2.setColor(new Color(207, 240, 255, 100)); // fake shadow\n" +
"                g2.fillRoundRect(0, 5, getWidth(), getHeight()-5, 20, 20);\n" +
"                g2.setColor(Color.WHITE);\n" +
"                g2.fillRoundRect(0, 0, getWidth(), getHeight()-5, 20, 20);\n" +
"                if (isFocusOwner()) {\n" +
"                    g2.setColor(new Color(18, 177, 209));\n" +
"                    g2.setStroke(new BasicStroke(2));\n" +
"                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-7, 20, 20);\n" +
"                }\n" +
"                super.paintComponent(g);\n" +
"                if (getText().isEmpty() && !isFocusOwner()) {\n" +
"                    g2.setColor(new Color(170, 170, 170));\n" +
"                    g2.drawString(placeholder, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);\n" +
"                }\n" +
"                g2.dispose();\n" +
"            }\n" +
"        };\n" +
"        field.setOpaque(false);\n" +
"        field.setBorder(new EmptyBorder(15, 20, 20, 20));\n" +
"        field.setMaximumSize(new Dimension(300, 55));\n" +
"        field.setAlignmentX(Component.CENTER_ALIGNMENT);\n" +
"        return field;\n" +
"    }\n\n" +
"    private JPasswordField createCustomPasswordField() {\n" +
"        JPasswordField field = new JPasswordField() {\n" +
"            @Override\n" +
"            protected void paintComponent(Graphics g) {\n" +
"                Graphics2D g2 = (Graphics2D) g.create();\n" +
"                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);\n" +
"                g2.setColor(new Color(207, 240, 255, 100)); // fake shadow\n" +
"                g2.fillRoundRect(0, 5, getWidth(), getHeight()-5, 20, 20);\n" +
"                g2.setColor(Color.WHITE);\n" +
"                g2.fillRoundRect(0, 0, getWidth(), getHeight()-5, 20, 20);\n" +
"                if (isFocusOwner()) {\n" +
"                    g2.setColor(new Color(18, 177, 209));\n" +
"                    g2.setStroke(new BasicStroke(2));\n" +
"                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-7, 20, 20);\n" +
"                }\n" +
"                super.paintComponent(g);\n" +
"                g2.dispose();\n" +
"            }\n" +
"        };\n" +
"        field.setOpaque(false);\n" +
"        field.setBorder(new EmptyBorder(15, 20, 20, 20));\n" +
"        field.setMaximumSize(new Dimension(300, 55));\n" +
"        field.setAlignmentX(Component.CENTER_ALIGNMENT);\n" +
"        return field;\n" +
"    }\n" +
"}\n";

        Path path = Paths.get("src/com/cmp180/livechat/client/staff/ui/LoginPanel.java");
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }
}
