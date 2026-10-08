package com.cmp180.livechat.server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Giao diện Bảng điều khiển Server - Do bạn tự phát triển
 */
public class ServerGUI extends JFrame {

    // 1. Khai báo các thành phần Giao diện (UI Components)
    private JButton btnStart;
    private JLabel lblStatus;
    private JTextArea txtLog;
    private boolean isRunning = false; // Trạng thái Server (Đang chạy hay Đã dừng)
    private ServerMain serverEngine;   // Tham chiếu đến Backend ServerMain

    private JLabel lblWait, lblStaff, lblActive;

    // Hàm khởi tạo (Constructor)
    public ServerGUI() {
        // Cấu hình Cửa sổ chính
        setTitle("LIVE CHAT SUPPORT - SERVER DASHBOARD");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Hiển thị giữa màn hình

        // Gọi hàm dựng giao diện
        initUI();
    }

    private void initUI() {
        // Sử dụng BorderLayout cho Cửa sổ chính
        setLayout(new BorderLayout(10, 10));

        // --- PHẦN 1: THANH ĐIỀU KHIỂN & DASHBOARD Ở TRÊN (NORTH) ---
        JPanel northPanel = new JPanel(new BorderLayout());
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        btnStart = new JButton("Khởi Động Server");
        lblStatus = new JLabel("Trạng thái: ĐÃ DỪNG");
        lblStatus.setForeground(Color.RED); // Màu đỏ khi dừng

        btnStart.addActionListener((ActionEvent e) -> {
            toggleServer();
        });

        topPanel.add(btnStart);
        topPanel.add(lblStatus);
        
        JPanel dashPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        dashPanel.setBorder(BorderFactory.createTitledBorder("Thống kê hệ thống"));
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
        add(northPanel, BorderLayout.NORTH);

        // --- PHẦN 2: KHUNG NHẬT KÝ (LOG) Ở GIỮA (CENTER) ---
        txtLog = new JTextArea();
        txtLog.setEditable(false); // Không cho người dùng sửa chữ trong log
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 13));

        // Bọc JTextArea vào JScrollPane để có thanh cuộn khi log quá dài
        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Nhật ký hệ thống (System Log)"));

        add(scrollLog, BorderLayout.CENTER);
    }

    /**
     * Xử lý bật / tắt Server khi bấm nút
     */
    private void toggleServer() {
        if (!isRunning) {
            // === XỬ LÝ KHI BẮT ĐẦU BẬT SERVER ===
            isRunning = true;
            btnStart.setText("Dừng Server");
            lblStatus.setText("Trạng thái: ĐANG CHẠY (PORT 5000)");
            lblStatus.setForeground(new Color(34, 139, 34)); // Màu xanh lá

            log("-> Khởi động Server thành công!");

            // BÀI 5 (ĐA LUỒNG): Phải chạy ServerMain trong 1 Thread riêng 
            // để KHÔNG làm đơ giao diện Swing!
            new Thread(() -> {
                serverEngine = new ServerMain(this); // Truyền 'this' để Server biết GUI
                serverEngine.start();
            }).start();

        } else {
            // === XỬ LÝ KHI BẤM DỪNG SERVER ===
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

    /**
     * Hàm dùng để ghi 1 dòng tin nhắn vào Khung Log giao diện
     */
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(message + "\n");
            // Tự động cuộn xuống dòng mới nhất
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }

    public void updateDashboard(int waiting, int staff, int active) {
        SwingUtilities.invokeLater(() -> {
            if (lblWait != null) lblWait.setText("Khách đợi: " + waiting);
            if (lblStaff != null) lblStaff.setText("CSKH sẵn sàng: " + staff);
            if (lblActive != null) lblActive.setText("Phiên đang mở: " + active);
        });
    }

    // Hàm main để chạy riêng thử nghiệm Giao diện
    public static void main(String[] args) {
        // Đổi giao diện sang giao diện hệ thống cho đẹp
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new ServerGUI().setVisible(true);
        });
    }
}