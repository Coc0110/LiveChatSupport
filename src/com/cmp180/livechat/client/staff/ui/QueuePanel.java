package com.cmp180.livechat.client.staff.ui;

import com.cmp180.livechat.client.customer.components.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import com.cmp180.livechat.client.staff.context.StaffContext;
import com.cmp180.livechat.client.staff.network.StaffNetworkListener;
import java.util.Map;

public class QueuePanel extends JPanel implements StaffNetworkListener{
    private StaffMainFrame parentFrame;

    private JPanel rowsPanel;
    private JPanel th;

    public QueuePanel(StaffMainFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        StaffContext.getInstance().getNetworkService().setListener(this);

        add(createSidebar(), BorderLayout.WEST);
        add(createContent(), BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(70, 0));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        
        String[] icons = {"💬", "👥", "📊", "⚙️"};
        sidebar.add(Box.createVerticalStrut(20));
        for (String icon : icons) {
            JLabel lbl = new JLabel(icon, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
            lbl.setMaximumSize(new Dimension(50, 50));
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            sidebar.add(lbl);
            sidebar.add(Box.createVerticalStrut(20));
        }
        sidebar.add(Box.createVerticalGlue());
        
        JLabel avatar = new JLabel("NM");
        avatar.setOpaque(true);
        avatar.setBackground(new Color(220, 240, 255));
        avatar.setForeground(new Color(0, 122, 255));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        avatar.setMaximumSize(new Dimension(40, 40));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(avatar);
        sidebar.add(Box.createVerticalStrut(20));
        return sidebar;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setBackground(new Color(245, 247, 250));
        content.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Phần Header: Tiêu đề và 3 thẻ thống kê
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Danh sách khách hàng đang chờ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel sub = new JLabel("Hệ thống xếp hàng tự động theo thời gian gửi tin nhắn");
        sub.setForeground(Color.GRAY);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(sub);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.add(createStatCard("Đang chờ", "8 khách", new Color(255, 235, 238), Color.RED));
        statsPanel.add(createStatCard("Đang hỗ trợ", "3 khách", new Color(230, 242, 255), new Color(0, 122, 255)));
        statsPanel.add(createStatCard("Đã hoàn thành", "15 khách", new Color(235, 255, 240), new Color(40, 167, 69)));

        header.add(titlePanel, BorderLayout.WEST);
        header.add(statsPanel, BorderLayout.EAST);

        // Phần Bảng danh sách
        RoundedPanel listContainer = new RoundedPanel(15, Color.WHITE);
        listContainer.setLayout(new BorderLayout());
        listContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterBar.setOpaque(false);
        PlaceholderTextField searchField = new PlaceholderTextField(10, "🔍 Tìm kiếm khách hàng...");
        searchField.setPreferredSize(new Dimension(300, 35));
        filterBar.add(searchField);
        
        String[] topics = {"Tất cả chủ đề", "Lỗi kỹ thuật", "Tư vấn"};
        RoundedComboBox<String> topicCb = new RoundedComboBox<>(topics, 10);
        topicCb.setPreferredSize(new Dimension(150, 35));
        filterBar.add(topicCb);

        JPanel rowsPanel = new JPanel();
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
        rowsPanel.setBackground(Color.WHITE);

        th = new JPanel(new GridLayout(1, 6, 10, 0));
        th.setBackground(Color.WHITE);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        th.add(new JLabel("STT"));
        th.add(new JLabel("Khách hàng"));
        th.add(new JLabel("Thời gian chờ"));
        th.add(new JLabel("Chủ đề hỗ trợ"));
        th.add(new JLabel("Trạng thái"));
        th.add(new JLabel("Thao tác"));
        th.setMaximumSize(new Dimension(9999, 40));
        rowsPanel.add(th);

        listContainer.add(filterBar, BorderLayout.NORTH);
        listContainer.add(new JScrollPane(rowsPanel), BorderLayout.CENTER);

        content.add(header, BorderLayout.NORTH);
        content.add(listContainer, BorderLayout.CENTER);

        return content;
    }

    private JPanel createStatCard(String title, String value, Color bg, Color fg) {
        RoundedPanel card = new RoundedPanel(10, bg);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(10, 15, 10, 15));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(Color.DARK_GRAY);
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 18));
        v.setForeground(fg);
        card.add(t);
        card.add(v);
        return card;
    }

    private JPanel createQueueRow(String stt, String avt, String name, String time, boolean isUrgent, String topic, String customerId) {
        JPanel row = new JPanel(new GridLayout(1, 6, 10, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));
        row.setMaximumSize(new Dimension(9999, 60));

        row.add(new JLabel(stt));
        
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        namePanel.setOpaque(false);
        JLabel avatar = new JLabel(avt, SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(220, 240, 255));
        avatar.setForeground(new Color(0, 122, 255));
        avatar.setPreferredSize(new Dimension(30, 30));
        namePanel.add(avatar);
        namePanel.add(new JLabel("<html><b>" + name + "</b></html>"));
        row.add(namePanel);

        JLabel timeLbl = new JLabel(time);
        timeLbl.setForeground(isUrgent ? Color.RED : Color.BLACK);
        timeLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        row.add(timeLbl);
        row.add(new JLabel(topic));
        
        JLabel status = new JLabel("Chờ tiếp nhận");
        status.setForeground(new Color(255, 100, 100));
        row.add(status);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        actionPanel.setOpaque(false);
        RoundedButton acceptBtn = new RoundedButton("Tiếp nhận", 8, new Color(0, 122, 255), Color.WHITE);
        acceptBtn.setPreferredSize(new Dimension(100, 35));
        
        acceptBtn.addActionListener(e -> {
        // Gọi Service lên Server yêu cầu tiếp nhận Khách hàng này bằng UUID
        StaffContext.getInstance().getNetworkService().acceptCustomer(customerId);
        });
        
        actionPanel.add(acceptBtn);
        row.add(actionPanel);

        return row;
    }
    @Override
    public void onLoginSuccess() { }
    @Override
    public void onQueueUpdated(Map<String, String> waitingCustomers) {
        // LUÔN UPDATE UI TRONG INVOKE_LATER ĐỂ TRÁNH LỖI LUỒNG
        SwingUtilities.invokeLater(() -> {
            rowsPanel.removeAll(); // Xóa sạch dữ liệu cũ
            rowsPanel.add(th); // Thêm lại tiêu đề bảng
            
            int stt = 1;
            // Duyệt danh sách khách được Server gửi về (Tự động cập nhật động)
            for (Map.Entry<String, String> entry : waitingCustomers.entrySet()) {
                String customerId = entry.getKey();
                String customerName = entry.getValue();
                
                JPanel row = createQueueRow(String.valueOf(stt++), "KH", customerName, "Vừa xong", false, "Chưa rõ", customerId);
                rowsPanel.add(row);
            }
            
            rowsPanel.revalidate();
            rowsPanel.repaint();
        });
    }
    @Override
    public void onPairedWithCustomer(String msg) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, msg);
            parentFrame.navigateTo("CHAT_SCREEN");
        });
    }
    @Override
    public void onMessageReceived(String sender, String msg) { }
    @Override
    public void onSessionEnded(String reason) { }
    @Override
    public void onError(String msg) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE));
    }
}