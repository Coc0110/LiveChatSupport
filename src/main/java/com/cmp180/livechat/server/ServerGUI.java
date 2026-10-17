package com.cmp180.livechat.server;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Giao diện Bảng điều khiển Server - Bổ sung các tab Quản trị CSDL (Staff, Sessions, History)
 */
public class ServerGUI extends JFrame {

    // Tab 1: Giám sát thời gian thực
    private JButton btnStart;
    private JLabel lblStatus;
    private JTextArea txtLog;
    private boolean isRunning = false;
    private ServerMain serverEngine;
    private JLabel lblWait, lblStaff, lblActive;

    // Tab 2: Quản lý Nhân viên
    private JTable tblStaff;
    private DefaultTableModel staffTableModel;

    // Tab 3: Lịch sử phiên chat
    private JTable tblSessions;
    private DefaultTableModel sessionsTableModel;
    private JTextArea txtSessionDetail;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public ServerGUI() {
        setTitle("LIVE CHAT SUPPORT - SERVER DASHBOARD & MANAGEMENT");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Tab 1: Giám sát
        JPanel monitorPanel = createMonitorPanel();
        tabbedPane.addTab("🖥️ Giám sát Hệ thống", monitorPanel);

        // Tab 2: Quản lý Nhân viên
        JPanel staffPanel = createStaffPanel();
        tabbedPane.addTab("👥 Quản lý Nhân viên", staffPanel);

        // Tab 3: Lịch sử Phiên & Đánh giá
        JPanel historyPanel = createHistoryPanel();
        tabbedPane.addTab("📋 Lịch sử Hỗ trợ & CSAT", historyPanel);

        // Sự kiện khi chuyển tab thì tự động reload dữ liệu bảng
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 1) {
                loadStaffData();
            } else if (selectedIndex == 2) {
                loadSessionsData();
            }
        });

        setContentPane(tabbedPane);
    }

    // ==========================================
    // TAB 1: GIÁM SÁT HỆ THỐNG
    // ==========================================
    private JPanel createMonitorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel northPanel = new JPanel(new BorderLayout(5, 5));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        btnStart = new JButton("Khởi Động Server");
        btnStart.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatus = new JLabel("Trạng thái: ĐÃ DỪNG");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatus.setForeground(Color.RED);

        JButton btnClearLog = new JButton("Xóa Log");
        btnClearLog.addActionListener(e -> txtLog.setText(""));

        btnStart.addActionListener((ActionEvent e) -> toggleServer());

        topPanel.add(btnStart);
        topPanel.add(lblStatus);
        topPanel.add(btnClearLog);

        JPanel dashPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        dashPanel.setBorder(BorderFactory.createTitledBorder("Thống kê trực tuyến (Live Stats)"));
        lblWait = new JLabel("Khách đợi: 0");
        lblStaff = new JLabel("CSKH sẵn sàng: 0");
        lblActive = new JLabel("Phiên đang mở: 0");
        lblWait.setForeground(Color.BLUE); lblWait.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStaff.setForeground(new Color(0, 150, 0)); lblStaff.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblActive.setForeground(Color.RED); lblActive.setFont(new Font("Segoe UI", Font.BOLD, 14));

        dashPanel.add(lblWait); dashPanel.add(new JLabel("   |   "));
        dashPanel.add(lblStaff); dashPanel.add(new JLabel("   |   "));
        dashPanel.add(lblActive);

        northPanel.add(topPanel, BorderLayout.NORTH);
        northPanel.add(dashPanel, BorderLayout.SOUTH);
        panel.add(northPanel, BorderLayout.NORTH);

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Nhật ký hệ thống (System Log)"));

        panel.add(scrollLog, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // TAB 2: QUẢN LÝ NHÂN VIÊN
    // ==========================================
    private JPanel createStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tiêu đề & Công cụ
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnAdd = new JButton("➕ Thêm Nhân viên");
        JButton btnChangePass = new JButton("🔑 Đổi Mật khẩu");
        JButton btnDelete = new JButton("❌ Khóa / Xóa");
        JButton btnRefresh = new JButton("🔄 Làm mới");

        btnAdd.addActionListener(e -> showAddStaffDialog());
        btnChangePass.addActionListener(e -> showChangePasswordDialog());
        btnDelete.addActionListener(e -> handleDeleteStaff());
        btnRefresh.addActionListener(e -> loadStaffData());

        toolBar.add(btnAdd);
        toolBar.add(btnChangePass);
        toolBar.add(btnDelete);
        toolBar.add(btnRefresh);

        panel.add(toolBar, BorderLayout.NORTH);

        // Bảng dữ liệu
        String[] columns = {"Mã NV", "Tên đăng nhập", "Họ và Tên", "Vai trò", "Chuyên môn", "Số phiên đã hỗ trợ", "Đánh giá TB (⭐)"};
        staffTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblStaff = new JTable(staffTableModel);
        tblStaff.setRowHeight(26);
        tblStaff.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblStaff.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblStaff.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(tblStaff);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách Nhân viên CSKH (Lưu trong SQLite)"));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadStaffData() {
        staffTableModel.setRowCount(0);
        List<DatabaseManager.StaffRecord> list = DatabaseManager.getInstance().getAllStaffWithStats();
        for (DatabaseManager.StaffRecord s : list) {
            String ratingStr = s.avgRating > 0 ? String.format("%.1f ⭐", s.avgRating) : "Chưa có";
            staffTableModel.addRow(new Object[]{
                    s.staffId,
                    s.username,
                    s.fullName,
                    s.role,
                    s.primarySkill,
                    s.sessionCount,
                    ratingStr
            });
        }
    }

    private void showAddStaffDialog() {
        JTextField txtUser = new JTextField(15);
        JPasswordField txtPass = new JPasswordField(15);
        JTextField txtName = new JTextField(15);
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"STAFF", "ADMIN"});
        JComboBox<String> cbSkill = new JComboBox<>(new String[]{
                "Tất cả",
                "Tư vấn Mua Laptop & Build PC",
                "Hỗ trợ Kỹ thuật & Cài đặt",
                "Bảo hành & Đổi trả linh kiện",
                "Tra cứu Đơn hàng & Giao vận",
                "Thắc mắc & Dịch vụ khác"
        });

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Tên đăng nhập:"));
        form.add(txtUser);
        form.add(new JLabel("Mật khẩu:"));
        form.add(txtPass);
        form.add(new JLabel("Họ và Tên:"));
        form.add(txtName);
        form.add(new JLabel("Vai trò:"));
        form.add(cbRole);
        form.add(new JLabel("Chuyên môn chính:"));
        form.add(cbSkill);

        int result = JOptionPane.showConfirmDialog(this, form, "Thêm Nhân viên CSKH", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword()).trim();
            String n = txtName.getText().trim();
            String r = (String) cbRole.getSelectedItem();
            String s = (String) cbSkill.getSelectedItem();

            if (u.isEmpty() || p.isEmpty() || n.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok = DatabaseManager.getInstance().addStaff(u, p, n, r, s);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");
                loadStaffData();
            } else {
                JOptionPane.showMessageDialog(this, "Thất bại (Tên đăng nhập có thể đã tồn tại)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showChangePasswordDialog() {
        int selected = tblStaff.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên từ danh sách!");
            return;
        }
        String staffId = (String) staffTableModel.getValueAt(selected, 0);
        String name = (String) staffTableModel.getValueAt(selected, 2);

        JPasswordField txtNewPass = new JPasswordField(15);
        int result = JOptionPane.showConfirmDialog(this, txtNewPass, "Nhập mật khẩu mới cho: " + name, JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String pass = new String(txtNewPass.getPassword()).trim();
            if (pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mật khẩu không được để trống!");
                return;
            }
            if (DatabaseManager.getInstance().updateStaffPassword(staffId, pass)) {
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteStaff() {
        int selected = tblStaff.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần xóa!");
            return;
        }
        String staffId = (String) staffTableModel.getValueAt(selected, 0);
        String name = (String) staffTableModel.getValueAt(selected, 2);

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn khóa/xóa nhân viên: " + name + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (DatabaseManager.getInstance().deleteStaff(staffId)) {
                JOptionPane.showMessageDialog(this, "Đã xóa nhân viên!");
                loadStaffData();
            }
        }
    }

    // ==========================================
    // TAB 3: LỊCH SỬ PHIÊN & CSAT
    // ==========================================
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnRefresh = new JButton("🔄 Làm mới");
        JButton btnSeed = new JButton("📦 Nạp Dữ Liệu Mẫu (Demo)");

        btnRefresh.addActionListener(e -> loadSessionsData());
        btnSeed.addActionListener(e -> {
            int cf = JOptionPane.showConfirmDialog(this, "Thao tác này sẽ đặt lại dữ liệu về trạng thái mẫu. Tiếp tục?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (cf == JOptionPane.YES_OPTION) {
                DatabaseManager.getInstance().resetAndSeedSampleData();
                loadSessionsData();
                loadStaffData();
                JOptionPane.showMessageDialog(this, "Đã nạp dữ liệu mẫu thành công!");
            }
        });

        toolBar.add(btnRefresh);
        toolBar.add(btnSeed);
        panel.add(toolBar, BorderLayout.NORTH);

        // Chia 2 phần: Bảng danh sách phiên ở trên / Chi tiết tin nhắn ở dưới
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        String[] cols = {"Mã phiên", "Khách hàng", "SĐT / Email", "CSKH hỗ trợ", "Chủ đề", "Trạng thái", "Đánh giá", "Thời gian tạo"};
        sessionsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblSessions = new JTable(sessionsTableModel);
        tblSessions.setRowHeight(24);
        tblSessions.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblSessions.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblSessions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblSessions.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblSessions.getSelectedRow();
                if (row >= 0) {
                    String sessId = (String) sessionsTableModel.getValueAt(row, 0);
                    showSessionMessages(sessId);
                }
            }
        });

        JScrollPane scrollSessions = new JScrollPane(tblSessions);
        scrollSessions.setBorder(BorderFactory.createTitledBorder("Danh sách các phiên hỗ trợ (Click vào phiên để xem tin nhắn)"));
        splitPane.setTopComponent(scrollSessions);

        txtSessionDetail = new JTextArea();
        txtSessionDetail.setEditable(false);
        txtSessionDetail.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane scrollDetail = new JScrollPane(txtSessionDetail);
        scrollDetail.setBorder(BorderFactory.createTitledBorder("Chi tiết hội thoại & Đánh giá của khách hàng"));
        splitPane.setBottomComponent(scrollDetail);

        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadSessionsData() {
        sessionsTableModel.setRowCount(0);
        txtSessionDetail.setText("");
        List<DatabaseManager.SessionRecord> list = DatabaseManager.getInstance().getAllSessions();
        for (DatabaseManager.SessionRecord s : list) {
            String ratingStr = s.rating > 0 ? (s.rating + " ⭐") : "Chưa đánh giá";
            String timeStr = s.createdAt > 0 ? dateFormat.format(new Date(s.createdAt)) : "";
            sessionsTableModel.addRow(new Object[]{
                    s.sessionId,
                    s.customerName,
                    s.customerPhone,
                    s.staffName,
                    s.issueTopic,
                    s.status,
                    ratingStr,
                    timeStr
            });
        }
    }

    private void showSessionMessages(String sessionId) {
        List<DatabaseManager.MessageRecord> msgs = DatabaseManager.getInstance().getMessagesBySession(sessionId);
        StringBuilder sb = new StringBuilder();
        sb.append("=== CHI TIẾT PHIÊN HỖ TRỢ: ").append(sessionId).append(" ===\n\n");

        if (msgs.isEmpty()) {
            sb.append("(Không có tin nhắn nào trong phiên này)\n");
        } else {
            for (DatabaseManager.MessageRecord m : msgs) {
                String time = m.timestamp > 0 ? dateFormat.format(new Date(m.timestamp)) : "";
                String sender = m.senderName + " (" + m.senderRole + ")";
                sb.append(String.format("[%s] %s:\n", time, sender));

                if (m.isRecalled) {
                    sb.append("   [Tin nhắn này đã bị thu hồi]\n\n");
                    continue;
                }

                if (m.replyToSender != null && !m.replyToSender.isEmpty()) {
                    sb.append("   ↳ Trả lời @").append(m.replyToSender).append(": ").append(m.replyToText).append("\n");
                }

                if (m.fileName != null && !m.fileName.isEmpty()) {
                    sb.append("   📎 [Tệp đính kèm]: ").append(m.fileName)
                      .append(" (").append(m.fileSize / 1024).append(" KB)\n");
                }

                if (m.content != null && !m.content.isEmpty()) {
                    // Thử giải mã nếu là chuỗi đã mã hóa
                    String plainText = com.cmp180.livechat.common.SecurityManager.decrypt(m.content);
                    sb.append("   ").append(plainText).append("\n");
                }
                sb.append("\n");
            }
        }

        txtSessionDetail.setText(sb.toString());
        txtSessionDetail.setCaretPosition(0);
    }

    // ==========================================
    // KHỞI ĐỘNG / DỪNG SERVER
    // ==========================================
    private void toggleServer() {
        if (!isRunning) {
            isRunning = true;
            btnStart.setText("Dừng Server");
            lblStatus.setText("Trạng thái: ĐANG CHẠY (PORT 5000)");
            lblStatus.setForeground(new Color(34, 139, 34));

            log("-> Khởi động Server thành công!");

            new Thread(() -> {
                serverEngine = new ServerMain(this);
                serverEngine.start();
            }).start();

        } else {
            isRunning = false;
            btnStart.setText("Khởi Động Server");
            lblStatus.setText("Trạng thái: ĐÃ DỪNG");
            lblStatus.setForeground(Color.RED);

            if (serverEngine != null) {
                serverEngine.stop();
                serverEngine = null;
            }

            log("-> Server đã dừng hoạt động.");
        }
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            if (txtLog != null) {
                txtLog.append(message + "\n");
                txtLog.setCaretPosition(txtLog.getDocument().getLength());
            }
        });
    }

    public void updateDashboard(int waiting, int staff, int active) {
        SwingUtilities.invokeLater(() -> {
            if (lblWait != null) lblWait.setText("Khách đợi: " + waiting);
            if (lblStaff != null) lblStaff.setText("CSKH sẵn sàng: " + staff);
            if (lblActive != null) lblActive.setText("Phiên đang mở: " + active);
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new ServerGUI().setVisible(true);
        });
    }
}