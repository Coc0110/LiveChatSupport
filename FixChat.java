import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class FixChat {
    public static void main(String[] args) throws IOException {
        Path path = Paths.get("src/com/cmp180/livechat/client/staff/ui/ChatPanel.java");
        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        
        content = content.replace("JLabel activeUser = new JLabel(", "activeUser = new JLabel(");
        content = content.replace("endBtn.addActionListener(e -> parentFrame.navigateTo(\"QUEUE_SCREEN\"));", 
            "endBtn.addActionListener(e -> {\n            StaffContext.getInstance().getNetworkService().endSession();\n            chatArea.removeAll();\n            parentFrame.navigateTo(\"QUEUE_SCREEN\");\n        });");
        content = content.replace("JPanel chatArea = new JPanel();", 
            "chatArea = new JPanel();\n        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));");
        content = content.replace("PlaceholderTextField typeField = new PlaceholderTextField(", 
            "typeField = new PlaceholderTextField(");
        content = content.replace("chatInput.add(sendBtn, BorderLayout.EAST);", 
            "sendBtn.addActionListener(e -> {\n            String text = typeField.getText().trim();\n            if (!text.isEmpty()) {\n                StaffContext.getInstance().getNetworkService().sendChatMessage(text);\n                addMessageBubble(\"Bạn\", text, true);\n                typeField.setText(\"\");\n            }\n        });\n        chatInput.add(sendBtn, BorderLayout.EAST);");
        
        String append = "\n    private void addMessageBubble(String sender, String text, boolean isSelf) {\n" +
            "        JPanel bubble = new JPanel(new FlowLayout(isSelf ? FlowLayout.RIGHT : FlowLayout.LEFT));\n" +
            "        bubble.setOpaque(false);\n" +
            "        JLabel lbl = new JLabel(\"<html><b>\" + sender + \":</b> \" + text + \"</html>\");\n" +
            "        lbl.setOpaque(true);\n" +
            "        lbl.setBackground(isSelf ? new Color(0, 122, 255) : Color.WHITE);\n" +
            "        lbl.setForeground(isSelf ? Color.WHITE : Color.BLACK);\n" +
            "        lbl.setBorder(new EmptyBorder(10, 15, 10, 15));\n" +
            "        bubble.add(lbl);\n" +
            "        chatArea.add(bubble);\n" +
            "        chatArea.revalidate();\n" +
            "        chatArea.repaint();\n" +
            "    }\n\n" +
            "    @Override public void onLoginSuccess() { }\n" +
            "    @Override public void onQueueUpdated(java.util.Map<String, String> waitingCustomers) { }\n" +
            "    @Override public void onPairedWithCustomer(String customerName) {\n" +
            "        SwingUtilities.invokeLater(() -> {\n" +
            "            activeUser.setText(\"<html><b>\" + customerName + \"</b><br><span style='color:green; font-size:10px;'>&#128994; Đang hoạt động</span></html>\");\n" +
            "            chatArea.removeAll();\n" +
            "            addMessageBubble(\"Hệ thống\", \"Đã kết nối với \" + customerName, false);\n" +
            "        });\n" +
            "    }\n" +
            "    @Override public void onMessageReceived(String sender, String msg) {\n" +
            "        SwingUtilities.invokeLater(() -> { addMessageBubble(sender, msg, false); });\n" +
            "    }\n" +
            "    @Override public void onSessionEnded(String reason) {\n" +
            "        SwingUtilities.invokeLater(() -> {\n" +
            "            JOptionPane.showMessageDialog(this, \"Phiên chat đã kết thúc: \" + reason);\n" +
            "            parentFrame.navigateTo(\"QUEUE_SCREEN\");\n" +
            "        });\n" +
            "    }\n" +
            "    @Override public void onError(String msg) {\n" +
            "        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msg, \"Lỗi\", JOptionPane.ERROR_MESSAGE));\n" +
            "    }\n" +
            "}\n";
            
        content = content.replace("return item;\n    }\n}", "return item;\n    }\n" + append);
        content = content.replace("return item;\r\n    }\r\n}", "return item;\r\n    }\r\n" + append);
        
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }
}
