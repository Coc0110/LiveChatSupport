import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class ApplyCssLogin2 {
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
"        // Làm nền ngoài tối hơn một chút để khối form nổi lên\n" +
"        setBackground(new Color(225, 230, 238));\n\n" +
"        authLayout = new CardLayout();\n" +
"        authContainer = new JPanel(authLayout);\n" +
"        authContainer.setOpaque(false);\n\n" +
"        authContainer.add(buildLoginForm(), \"LOGIN\");\n" +
"        add(authContainer);\n" +
"    }\n\n" +
"    private JPanel buildLoginForm() {\n" +
"        // Container: gradient background, 40px radius\n" +
"        JPanel form = new JPanel() {\n" +
"            @Override\n" +
"            protected void paintComponent(Graphics g) {\n" +
"                Graphics2D g2 = (Graphics2D) g.create();\n" +
"                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);\n\n" +
"                // Đổ bóng (Fake shadow) nhiều lớp để tạo độ nhòe\n" +
"                for (int i = 0; i < 15; i++) {\n" +
"                    g2.setColor(new Color(133, 189, 215, 5 + (15 - i)));\n" +
"                    g2.fillRoundRect(i/2, 10 + i, getWidth() - i, getHeight() - 20 - i, 40, 40);\n" +
"                }\n\n" +
"                // Nền Gradient của form trắng sáng hơn\n" +
"                GradientPaint gp = new GradientPaint(0, getHeight(), new Color(248, 250, 255), 0, 0, new Color(255, 255, 255));\n" +
"                g2.setPaint(gp);\n" +
"                g2.fillRoundRect(0, 0, getWidth(), getHeight() - 20, 40, 40);\n\n" +
"                // Viền form (trắng tinh)\n" +
"                g2.setStroke(new BasicStroke(4));\n" +
"                g2.setColor(Color.WHITE);\n" +
"                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 24, 40, 40);\n" +
"                g2.dispose();\n" +
"            }\n" +
"        };\n" +
"        form.setOpaque(false);\n" +
"        form.setPreferredSize(new Dimension(380, 520));\n" +
"        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));\n" +
"        form.setBorder(new EmptyBorder(40, 40, 50, 40));\n\n" +
"        // Heading\n" +
"        JLabel title = new JLabel(\"Sign in\");\n" +
"        title.setFont(new Font(\"Segoe UI\", Font.BOLD, 32));\n" +
"        title.setForeground(new Color(16, 137, 211));\n" +
"        title.setAlignmentX(Component.CENTER_ALIGNMENT);\n\n" +
"        // Inputs\n" +
"        JTextField userField = createCustomTextField(\"Mã NV hoặc Email\");\n" +
"        JPasswordField pwdField = createCustomPasswordField();\n\n" +
"        // Forgot password\n" +
"        JPanel forgotPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));\n" +
"        forgotPanel.setOpaque(false);\n" +
"        forgotPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));\n" +
"        JLabel forgotLbl = new JLabel(\"Quên mật khẩu?\");\n" +
"        forgotLbl.setFont(new Font(\"Segoe UI\", Font.BOLD, 12));\n" +
"        forgotLbl.setForeground(new Color(0, 153, 255));\n" +
"        forgotLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));\n" +
"        forgotPanel.add(forgotLbl);\n\n" +
"        // Button\n" +
"        JButton loginBtn = new JButton(\"Đăng nhập\") {\n" +
"            @Override\n" +
"            protected void paintComponent(Graphics g) {\n" +
"                Graphics2D g2 = (Graphics2D) g.create();\n" +
"                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);\n\n" +
"                // Bóng của nút\n" +
"                g2.setColor(new Color(133, 189, 215, 150));\n" +
"                g2.fillRoundRect(0, 5, getWidth(), getHeight() - 5, 20, 20);\n\n" +
"                // Nền nút Gradient\n" +
"                GradientPaint gp = new GradientPaint(0, 0, new Color(16, 137, 211), getWidth(), getHeight(), new Color(18, 177, 209));\n" +
"                g2.setPaint(gp);\n" +
"                g2.fillRoundRect(0, 0, getWidth(), getHeight() - 5, 20, 20);\n" +
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
"        loginBtn.setFont(new Font(\"Segoe UI\", Font.BOLD, 16));\n" +
"        // Kéo dãn nút bằng 100% chiều ngang\n" +
"        loginBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 55));\n" +
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
"        agreeLbl.setFont(new Font(\"Segoe UI\", Font.PLAIN, 11));\n" +
"        agreeLbl.setForeground(new Color(150, 150, 150));\n" +
"        agreeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);\n\n" +
"        form.add(Box.createVerticalStrut(10));\n" +
"        form.add(title);\n" +
"        form.add(Box.createVerticalStrut(40));\n" +
"        form.add(userField);\n" +
"        form.add(Box.createVerticalStrut(20));\n" +
"        form.add(pwdField);\n" +
"        form.add(Box.createVerticalStrut(10));\n" +
"        form.add(forgotPanel);\n" +
"        form.add(Box.createVerticalStrut(30));\n" +
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
"                \n" +
"                // Nền bóng nhạt cho input\n" +
"                g2.setColor(new Color(207, 240, 255, 120));\n" +
"                g2.fillRoundRect(0, 5, getWidth(), getHeight() - 5, 20, 20);\n" +
"                \n" +
"                // Nền input trắng\n" +
"                g2.setColor(Color.WHITE);\n" +
"                g2.fillRoundRect(0, 0, getWidth(), getHeight() - 5, 20, 20);\n" +
"                \n" +
"                // Viền\n" +
"                if (isFocusOwner()) {\n" +
"                    g2.setColor(new Color(18, 177, 209));\n" +
"                    g2.setStroke(new BasicStroke(2));\n" +
"                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 7, 20, 20);\n" +
"                } else {\n" +
"                    g2.setColor(new Color(230, 230, 230));\n" +
"                    g2.setStroke(new BasicStroke(1));\n" +
"                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 6, 20, 20);\n" +
"                }\n\n" +
"                super.paintComponent(g);\n\n" +
"                // Placeholder\n" +
"                if (getText().isEmpty() && !isFocusOwner()) {\n" +
"                    g2.setColor(new Color(170, 170, 170));\n" +
"                    g2.drawString(placeholder, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);\n" +
"                }\n" +
"                g2.dispose();\n" +
"            }\n" +
"        };\n" +
"        field.setOpaque(false);\n" +
"        field.setBorder(new EmptyBorder(12, 20, 17, 20));\n" +
"        field.setFont(new Font(\"Segoe UI\", Font.PLAIN, 14));\n" +
"        // Ép width fill 100% ngang\n" +
"        field.setMaximumSize(new Dimension(Short.MAX_VALUE, 55));\n" +
"        field.setAlignmentX(Component.CENTER_ALIGNMENT);\n" +
"        return field;\n" +
"    }\n\n" +
"    private JPasswordField createCustomPasswordField() {\n" +
"        JPasswordField field = new JPasswordField() {\n" +
"            @Override\n" +
"            protected void paintComponent(Graphics g) {\n" +
"                Graphics2D g2 = (Graphics2D) g.create();\n" +
"                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);\n\n" +
"                g2.setColor(new Color(207, 240, 255, 120));\n" +
"                g2.fillRoundRect(0, 5, getWidth(), getHeight() - 5, 20, 20);\n" +
"                \n" +
"                g2.setColor(Color.WHITE);\n" +
"                g2.fillRoundRect(0, 0, getWidth(), getHeight() - 5, 20, 20);\n" +
"                \n" +
"                if (isFocusOwner()) {\n" +
"                    g2.setColor(new Color(18, 177, 209));\n" +
"                    g2.setStroke(new BasicStroke(2));\n" +
"                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 7, 20, 20);\n" +
"                } else {\n" +
"                    g2.setColor(new Color(230, 230, 230));\n" +
"                    g2.setStroke(new BasicStroke(1));\n" +
"                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 6, 20, 20);\n" +
"                }\n\n" +
"                super.paintComponent(g);\n" +
"                g2.dispose();\n" +
"            }\n" +
"        };\n" +
"        field.setOpaque(false);\n" +
"        field.setBorder(new EmptyBorder(12, 20, 17, 20));\n" +
"        field.setFont(new Font(\"Segoe UI\", Font.PLAIN, 14));\n" +
"        field.setMaximumSize(new Dimension(Short.MAX_VALUE, 55));\n" +
"        field.setAlignmentX(Component.CENTER_ALIGNMENT);\n" +
"        return field;\n" +
"    }\n" +
"}\n";

        Path path = Paths.get("src/com/cmp180/livechat/client/staff/ui/LoginPanel.java");
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }
}
