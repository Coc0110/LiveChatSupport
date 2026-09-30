import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class FixLoginPanel {
    public static void main(String[] args) throws IOException {
        String content = "package com.cmp180.livechat.client.staff.ui;\n\n" +
            "import com.cmp180.livechat.client.customer.components.*;\n" +
            "import com.cmp180.livechat.client.staff.context.StaffContext;\n\n" +
            "import javax.swing.*;\n" +
            "import javax.swing.border.EmptyBorder;\n" +
            "import java.awt.*;\n" +
            "import java.awt.event.*;\n\n" +
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
            "        authContainer.add(buildForgotPwdForm(), \"FORGOT\");\n\n" +
            "        add(authContainer);\n" +
            "        authLayout.show(authContainer, \"LOGIN\");\n" +
            "    }\n\n" +
            "    private JPanel buildLoginForm() {\n" +
            "        RoundedPanel form = new RoundedPanel(20, Color.WHITE);\n" +
            "        form.setPreferredSize(new Dimension(400, 480));\n" +
            "        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));\n" +
            "        form.setBorder(new EmptyBorder(40, 30, 40, 30));\n\n" +
            "        JLabel title = new JLabel(\"Đăng nhập Workspace\");\n" +
            "        title.setFont(new Font(\"Segoe UI\", Font.BOLD, 22));\n" +
            "        title.setAlignmentX(Component.CENTER_ALIGNMENT);\n\n" +
            "        PlaceholderTextField userField = new PlaceholderTextField(10, \"Họ và tên hoặc Mã NV\");\n" +
            "        userField.setMaximumSize(new Dimension(340, 40));\n" +
            "        userField.setAlignmentX(Component.CENTER_ALIGNMENT);\n\n" +
            "        JPasswordField pwdField = new JPasswordField();\n" +
            "        pwdField.setMaximumSize(new Dimension(340, 40));\n" +
            "        pwdField.setAlignmentX(Component.CENTER_ALIGNMENT);\n" +
            "        pwdField.setBorder(BorderFactory.createCompoundBorder(\n" +
            "            BorderFactory.createLineBorder(new Color(220, 220, 220)),\n" +
            "            new EmptyBorder(5, 10, 5, 10)\n" +
            "        ));\n\n" +
            "        JPanel optionsPanel = new JPanel(new BorderLayout());\n" +
            "        optionsPanel.setOpaque(false);\n" +
            "        optionsPanel.setMaximumSize(new Dimension(340, 30));\n" +
            "        optionsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);\n\n" +
            "        JCheckBox rememberChk = new JCheckBox(\"Ghi nhớ\");\n" +
            "        rememberChk.setOpaque(false);\n\n" +
            "        JLabel forgotLbl = new JLabel(\"<html><a href=''>Quên mật khẩu?</a></html>\");\n" +
            "        forgotLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));\n" +
            "        forgotLbl.addMouseListener(new MouseAdapter() {\n" +
            "            public void mouseClicked(MouseEvent e) { authLayout.show(authContainer, \"FORGOT\"); }\n" +
            "        });\n\n" +
            "        optionsPanel.add(rememberChk, BorderLayout.WEST);\n" +
            "        optionsPanel.add(forgotLbl, BorderLayout.EAST);\n\n" +
            "        RoundedButton loginBtn = new RoundedButton(\"Đăng nhập\", 10, new Color(0, 122, 255), Color.WHITE);\n" +
            "        loginBtn.setMaximumSize(new Dimension(340, 45));\n" +
            "        loginBtn.setFont(new Font(\"Segoe UI\", Font.BOLD, 14));\n" +
            "        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);\n" +
            "        loginBtn.addActionListener(e -> {\n" +
            "            String username = userField.getText().trim();\n" +
            "            if (username.isEmpty()) {\n" +
            "                JOptionPane.showMessageDialog(this, \"Vui lòng nhập tên/mã NV\");\n" +
            "                return;\n" +
            "            }\n" +
            "            StaffContext.getInstance().setStaffName(username);\n" +
            "            StaffContext.getInstance().getNetworkService().connectAndLogin(username);\n" +
            "            parentFrame.navigateTo(\"QUEUE_SCREEN\");\n" +
            "        });\n\n" +
            "        form.add(Box.createVerticalGlue());\n" +
            "        form.add(title);\n" +
            "        form.add(Box.createVerticalStrut(30));\n" +
            "        form.add(userField);\n" +
            "        form.add(Box.createVerticalStrut(15));\n" +
            "        form.add(pwdField);\n" +
            "        form.add(Box.createVerticalStrut(15));\n" +
            "        form.add(optionsPanel);\n" +
            "        form.add(Box.createVerticalStrut(30));\n" +
            "        form.add(loginBtn);\n" +
            "        form.add(Box.createVerticalGlue());\n\n" +
            "        return form;\n" +
            "    }\n\n" +
            "    private JPanel buildForgotPwdForm() {\n" +
            "        RoundedPanel form = new RoundedPanel(20, Color.WHITE);\n" +
            "        form.setPreferredSize(new Dimension(400, 350));\n" +
            "        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));\n" +
            "        form.setBorder(new EmptyBorder(40, 30, 40, 30));\n\n" +
            "        JLabel title = new JLabel(\"Khôi phục mật khẩu\");\n" +
            "        title.setFont(new Font(\"Segoe UI\", Font.BOLD, 22));\n" +
            "        title.setAlignmentX(Component.CENTER_ALIGNMENT);\n\n" +
            "        JLabel sub = new JLabel(\"<html><center>Nhập Email hoặc Số điện thoại để<br>nhận mã OTP xác thực.</center></html>\");\n" +
            "        sub.setForeground(Color.GRAY);\n" +
            "        sub.setAlignmentX(Component.CENTER_ALIGNMENT);\n\n" +
            "        PlaceholderTextField contactField = new PlaceholderTextField(10, \"Email / Số điện thoại\");\n" +
            "        contactField.setMaximumSize(new Dimension(340, 40));\n" +
            "        contactField.setAlignmentX(Component.CENTER_ALIGNMENT);\n\n" +
            "        RoundedButton otpBtn = new RoundedButton(\"Gửi mã OTP\", 10, new Color(0, 122, 255), Color.WHITE);\n" +
            "        otpBtn.setMaximumSize(new Dimension(340, 45));\n" +
            "        otpBtn.setAlignmentX(Component.CENTER_ALIGNMENT);\n\n" +
            "        JLabel backLbl = new JLabel(\"<html><a href=''>Quay lại đăng nhập</a></html>\");\n" +
            "        backLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));\n" +
            "        backLbl.setAlignmentX(Component.CENTER_ALIGNMENT);\n" +
            "        backLbl.addMouseListener(new MouseAdapter() {\n" +
            "            public void mouseClicked(MouseEvent e) { authLayout.show(authContainer, \"LOGIN\"); }\n" +
            "        });\n\n" +
            "        form.add(Box.createVerticalGlue());\n" +
            "        form.add(title);\n" +
            "        form.add(Box.createVerticalStrut(10));\n" +
            "        form.add(sub);\n" +
            "        form.add(Box.createVerticalStrut(30));\n" +
            "        form.add(contactField);\n" +
            "        form.add(Box.createVerticalStrut(20));\n" +
            "        form.add(otpBtn);\n" +
            "        form.add(Box.createVerticalStrut(20));\n" +
            "        form.add(backLbl);\n" +
            "        form.add(Box.createVerticalGlue());\n\n" +
            "        return form;\n" +
            "    }\n" +
            "}\n";
            
        Path path = Paths.get("src/com/cmp180/livechat/client/staff/ui/LoginPanel.java");
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }
}
