package com.cmp180.livechat.server;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Giao diện Bảng điều khiển Server - Quản trị CSDL, Giám sát phiên trực tiếp & Xuất báo cáo KPI
 */
public class ServerGUI extends JFrame {

    // Thông tin Quản trị viên
    private String currentAdminName = "Quản trị viên";
    private JLabel lblAdminStatus;

    // Tab 1: Giám sát thời gian thực & Giám sát phiên trực tiếp (Live Supervisor)
    private JButton btnStart;
    private JLabel lblStatus;
    private JTextArea txtLog;
    private boolean isRunning = false;
    private ServerMain serverEngine;
    private JLabel lblWait, lblStaff, lblActive;
    private JTable tblLiveSessions;
    private DefaultTableModel liveSessionsTableModel;

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
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        AIBotService.getInstance().setLogger(this::log);
        AIBotService.getInstance().checkAndLogStatus();
    }

    private void initUI() {
        JPanel mainContainer = new JPanel(new BorderLayout());

        // Header Quản trị viên
        JPanel adminHeader = new JPanel(new BorderLayout(10, 5));
        adminHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        adminHeader.setBackground(new Color(248, 250, 252));

        JLabel lblHeaderTitle = new JLabel("LIVE CHAT SUPPORT - BẢNG ĐIỀU KHIỂN QUẢN TRỊ SERVER");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeaderTitle.setForeground(new Color(15, 23, 42));

        JPanel adminRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        adminRight.setOpaque(false);
        lblAdminStatus = new JLabel("Quản trị viên: " + currentAdminName);
        lblAdminStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblAdminStatus.setForeground(new Color(51, 65, 85));

        JButton btnLock = new JButton("Khóa");
        btnLock.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLock.setFocusPainted(false);
        btnLock.addActionListener(e -> lockDashboard());

        adminRight.add(lblAdminStatus);
        adminRight.add(btnLock);

        adminHeader.add(lblHeaderTitle, BorderLayout.WEST);
        adminHeader.add(adminRight, BorderLayout.EAST);

        // TabbedPane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Tab 1: Giám sát
        JPanel monitorPanel = createMonitorPanel();
        tabbedPane.addTab("Giám sát Hệ thống", monitorPanel);

        // Tab 2: Quản lý Nhân viên
        JPanel staffPanel = createStaffPanel();
        tabbedPane.addTab("Quản lý Nhân viên", staffPanel);

        // Tab 3: Lịch sử Phiên & Đánh giá
        JPanel historyPanel = createHistoryPanel();
        tabbedPane.addTab("Lịch sử Hỗ trợ & CSAT", historyPanel);

        // Sự kiện khi chuyển tab thì tự động reload dữ liệu bảng
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                refreshLiveSessions();
            } else if (selectedIndex == 1) {
                loadStaffData();
            } else if (selectedIndex == 2) {
                loadSessionsData();
            }
        });

        mainContainer.add(adminHeader, BorderLayout.NORTH);
        mainContainer.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(mainContainer);
    }

    // ==========================================
    // TAB 1: GIÁM SÁT HỆ THỐNG & GIÁM SÁT TRỰC TIẾP
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

        JButton btnRefreshLive = new JButton("Cập nhật");
        btnRefreshLive.addActionListener(e -> refreshLiveSessions());

        btnStart.addActionListener((ActionEvent e) -> toggleServer());

        topPanel.add(btnStart);
        topPanel.add(lblStatus);
        topPanel.add(btnClearLog);
        topPanel.add(btnRefreshLive);

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

        // SplitPane: Bảng giám sát phiên trực tiếp ở trên, Nhật ký hệ thống ở dưới
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.45);

        String[] liveCols = {"Mã phiên", "Khách hàng", "SĐT / Email", "CSKH phụ trách", "Chuyên môn", "Chủ đề tư vấn", "Bắt đầu lúc"};
        liveSessionsTableModel = new DefaultTableModel(liveCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblLiveSessions = new JTable(liveSessionsTableModel);
        tblLiveSessions.setRowHeight(24);
        tblLiveSessions.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblLiveSessions.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblLiveSessions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollLive = new JScrollPane(tblLiveSessions);
        scrollLive.setBorder(BorderFactory.createTitledBorder("Giám sát phiên chat trực tiếp thời gian thực (Live Supervisor Monitoring)"));
        splitPane.setTopComponent(scrollLive);

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Nhật ký hệ thống (System Log)"));
        splitPane.setBottomComponent(scrollLog);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    public void refreshLiveSessions() {
        if (liveSessionsTableModel == null) return;
        liveSessionsTableModel.setRowCount(0);
        if (serverEngine != null && serverEngine.getSessionManager() != null) {
            List<ChatSession> activeList = serverEngine.getSessionManager().getActiveSessions();
            for (ChatSession s : activeList) {
                String sessId = s.getSessionId();
                String custName = (s.getCustomer() != null) ? s.getCustomer().getName() : "Không rõ";
                String custPhone = (s.getCustomer() != null) ? s.getCustomer().getCustomerPhone() : "Chưa có";
                String staffName = (s.getStaff() != null) ? s.getStaff().getName() : "Chưa có";
                String staffSkill = (s.getStaff() != null) ? s.getStaff().getPrimarySkill() : "Tất cả";
                String topic = (s.getIssueTopic() != null) ? s.getIssueTopic() : "Chung";
                String startTime = s.getStartTime() > 0 ? dateFormat.format(new Date(s.getStartTime())) : "";
                liveSessionsTableModel.addRow(new Object[]{sessId, custName, custPhone, staffName, staffSkill, topic, startTime});
            }
        }
    }

    // ==========================================
    // TAB 2: QUẢN LÝ NHÂN VIÊN
    // ==========================================
    private JPanel createStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tiêu đề & Công cụ
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnAdd = new JButton("Thêm Nhân viên");
        JButton btnChangePass = new JButton("Đổi Mật khẩu");
        JButton btnDelete = new JButton("Khóa / Xóa");
        JButton btnExportStaff = new JButton("Xuất KPI (CSV)");
        JButton btnRefresh = new JButton("Làm mới");

        btnAdd.addActionListener(e -> showAddStaffDialog());
        btnChangePass.addActionListener(e -> showChangePasswordDialog());
        btnDelete.addActionListener(e -> handleDeleteStaff());
        btnExportStaff.addActionListener(e -> exportStaffToCsv());
        btnRefresh.addActionListener(e -> loadStaffData());

        toolBar.add(btnAdd);
        toolBar.add(btnChangePass);
        toolBar.add(btnDelete);
        toolBar.add(btnExportStaff);
        toolBar.add(btnRefresh);

        panel.add(toolBar, BorderLayout.NORTH);

        // Bảng dữ liệu
        String[] columns = {"Mã NV", "Tên đăng nhập", "Họ và Tên", "Vai trò", "Chuyên môn", "Số phiên đã hỗ trợ", "Đánh giá TB (Sao)"};
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
            String ratingStr = s.avgRating > 0 ? String.format("%.1f/5 sao", s.avgRating) : "Chưa có";
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

    private void exportStaffToCsv() {
        List<DatabaseManager.StaffRecord> list = DatabaseManager.getInstance().getAllStaffWithStats();
        String[] headers = {"Mã Nhân Viên", "Tên Đăng Nhập", "Họ Và Tên", "Vai Trò", "Chuyên Môn", "Số Phiên Đã Hỗ Trợ", "Đánh Giá TB"};
        List<String[]> data = new ArrayList<>();
        for (DatabaseManager.StaffRecord s : list) {
            String ratingStr = s.avgRating > 0 ? String.format("%.1f", s.avgRating) : "0";
            data.add(new String[]{
                    s.staffId,
                    s.username,
                    s.fullName,
                    s.role,
                    s.primarySkill,
                    String.valueOf(s.sessionCount),
                    ratingStr
            });
        }
        exportTableToCsv("Bao_Cao_KPI_Nhan_Vien.csv", "Xuất Báo Cáo KPI Nhân Viên", data, headers);
    }

    // ==========================================
    // TAB 3: LỊCH SỬ PHIÊN & CSAT
    // ==========================================
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnRefresh = new JButton("Làm mới");
        JButton btnExportSessions = new JButton("Xuất Báo Cáo (CSV)");
        JButton btnSeed = new JButton("Nạp Dữ Liệu Mẫu (Demo)");

        btnRefresh.addActionListener(e -> loadSessionsData());
        btnExportSessions.addActionListener(e -> exportSessionsToCsv());

        btnSeed.addActionListener(e -> {
            JPasswordField txtConfirmPass = new JPasswordField(15);
            JPanel confirmPanel = new JPanel(new GridLayout(2, 1, 5, 5));
            confirmPanel.add(new JLabel("<html><b style='color:red;'>CẢNH BÁO:</b> Thao tác này sẽ XÓA TOÀN BỘ dữ liệu thực tế và đặt lại dữ liệu mẫu.<br>Vui lòng nhập mật khẩu Admin để xác nhận:</html>"));
            confirmPanel.add(txtConfirmPass);

            int cf = JOptionPane.showConfirmDialog(this, confirmPanel, "Xác nhận Đặt lại Dữ liệu", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (cf == JOptionPane.OK_OPTION) {
                String pass = new String(txtConfirmPass.getPassword()).trim();
                DatabaseManager.StaffRecord admin = DatabaseManager.getInstance().authenticateStaff("admin", pass);
                if (admin != null && "ADMIN".equalsIgnoreCase(admin.role)) {
                    DatabaseManager.getInstance().resetAndSeedSampleData();
                    loadSessionsData();
                    loadStaffData();
                    JOptionPane.showMessageDialog(this, "Đã nạp dữ liệu mẫu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Mật khẩu Admin không chính xác! Thao tác bị từ chối.", "Từ chối thực thi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        toolBar.add(btnRefresh);
        toolBar.add(btnExportSessions);
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
            String ratingStr = s.rating > 0 ? (s.rating + "/5 sao") : "Chưa đánh giá";
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

    private void exportSessionsToCsv() {
        List<DatabaseManager.SessionRecord> list = DatabaseManager.getInstance().getAllSessions();
        String[] headers = {"Mã Phiên", "Mã Khách Hàng", "Tên Khách Hàng", "SĐT / Email", "Mã CSKH", "CSKH Phụ Trách", "Chủ Đề", "Trạng Thái", "Đánh Giá (Sao)", "Nhận Xét", "Thời Gian Bắt Đầu", "Thời Gian Đóng"};
        List<String[]> data = new ArrayList<>();
        for (DatabaseManager.SessionRecord s : list) {
            String createdStr = s.createdAt > 0 ? dateFormat.format(new Date(s.createdAt)) : "";
            String closedStr = s.closedAt > 0 ? dateFormat.format(new Date(s.closedAt)) : "";
            data.add(new String[]{
                    s.sessionId,
                    s.customerId != null ? s.customerId : "",
                    s.customerName != null ? s.customerName : "",
                    s.customerPhone != null ? s.customerPhone : "",
                    s.staffId != null ? s.staffId : "",
                    s.staffName != null ? s.staffName : "",
                    s.issueTopic != null ? s.issueTopic : "",
                    s.status != null ? s.status : "",
                    String.valueOf(s.rating),
                    s.feedback != null ? s.feedback : "",
                    createdStr,
                    closedStr
            });
        }
        exportTableToCsv("Bao_Cao_Lich_Su_Phien_CSAT.csv", "Xuất Báo Cáo Lịch Sử Phiên Hỗ Trợ", data, headers);
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
                    sb.append("   [Trả lời @").append(m.replyToSender).append("]: ").append(m.replyToText).append("\n");
                }

                if (m.fileName != null && !m.fileName.isEmpty()) {
                    sb.append("   [Tệp đính kèm]: ").append(m.fileName)
                      .append(" (").append(m.fileSize / 1024).append(" KB)\n");
                }

                if (m.content != null && !m.content.isEmpty()) {
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
    // TIỆN ÍCH XUẤT CSV
    // ==========================================
    private void exportTableToCsv(String defaultFilename, String title, List<String[]> data, String[] headers) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultFilename));
        chooser.setDialogTitle(title);
        int userChoice = chooser.showSaveDialog(this);
        if (userChoice != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {

            // UTF-8 BOM để Excel hiển thị đúng dấu tiếng Việt
            writer.write("\uFEFF");

            // Ghi tiêu đề cột
            for (int i = 0; i < headers.length; i++) {
                writer.write(escapeCsv(headers[i]));
                if (i < headers.length - 1) writer.write(",");
            }
            writer.newLine();

            // Ghi từng dòng dữ liệu
            for (String[] row : data) {
                for (int i = 0; i < row.length; i++) {
                    writer.write(escapeCsv(row[i]));
                    if (i < row.length - 1) writer.write(",");
                }
                writer.newLine();
            }

            JOptionPane.showMessageDialog(this, "Xuất báo cáo thành công!\nĐường dẫn: " + file.getAbsolutePath(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escapeCsv(String val) {
        if (val == null) return "\"\"";
        String s = val.replace("\"", "\"\"");
        return "\"" + s + "\"";
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

            refreshLiveSessions();
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
            refreshLiveSessions();
        });
    }

    // ==========================================
    // XÁC THỰC QUẢN TRỊ VIÊN (ADMIN AUTH)
    // ==========================================
    public boolean promptAdminLogin() {
        JTextField txtUser = new JTextField("admin", 15);
        JPasswordField txtPass = new JPasswordField(15);
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.add(new JLabel("Tài khoản Quản trị:"));
        panel.add(txtUser);
        panel.add(new JLabel("Mật khẩu:"));
        panel.add(txtPass);

        int res = JOptionPane.showConfirmDialog(this, panel, "Xác thực Quản trị viên Server (Admin Login)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword()).trim();
            DatabaseManager.StaffRecord admin = DatabaseManager.getInstance().authenticateStaff(u, p);
            if (admin != null && "ADMIN".equalsIgnoreCase(admin.role)) {
                this.currentAdminName = admin.fullName;
                if (lblAdminStatus != null) {
                    lblAdminStatus.setText("Quản trị viên: " + currentAdminName);
                }
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Xác thực thất bại! Sai tên đăng nhập/mật khẩu hoặc không có quyền ADMIN.", "Lỗi Xác Thực", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    private void lockDashboard() {
        setVisible(false);
        if (promptAdminLogin()) {
            setVisible(true);
        } else {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            ServerGUI gui = new ServerGUI();
            if (!gui.promptAdminLogin()) {
                System.exit(0);
                return;
            }
            gui.setVisible(true);
        });
    }
}