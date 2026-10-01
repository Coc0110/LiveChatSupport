package com.cmp180.livechat.server;

import com.cmp180.livechat.common.Protocol;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Pattern;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ServerMain server; // Dùng để gọi hàm log()
    private final SessionManager sessionManager;
    
    private BufferedReader in;
    private PrintWriter out;
    
    private String name;
    private String role;
    private ChatSession session;

    public ClientHandler(Socket socket, ServerMain server, SessionManager sessionManager) {
        this.socket = socket;
        this.server = server;
        this.sessionManager = sessionManager;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            server.log("Lỗi khởi tạo I/O cho Client: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String message;
            // Vòng lặp chờ dữ liệu từ mạng (Luồng sẽ bị block ở đây cho đến khi có tin nhắn tới)
            while ((message = in.readLine()) != null) {
                processMessage(message);
            }
        } catch (IOException e) {
            server.log("[NGẮT KẾT NỐI] Client " + (name != null ? name : "Unknown") + " đã rời đi đột ngột.");
        } finally {
            // Đảm bảo dọn dẹp hàng đợi và đóng session khi Client mất mạng
            sessionManager.handleDisconnect(this);
            closeConnection();
        }
    }

    private void processMessage(String rawMessage) {
        // Tách chuỗi dựa trên ký tự phân cách SEP (Ví dụ: SEP là "|")
        // Giới hạn tách thành 3 phần: Lệnh | Tham số 1 | Nội dung (nếu có)
        String[] parts = rawMessage.split(Pattern.quote(Protocol.SEP), 3);
        if (parts.length < 1) return;

        String cmd = parts[0];

        switch (cmd) {
            // Lệnh đăng nhập từ Khách hàng (VD: CUST_LOGIN|Nguyễn Văn A)
            case Protocol.CMD_CUST_LOGIN:
                this.name = parts.length > 1 ? parts[1] : "Guest";
                this.role = Protocol.ROLE_CUSTOMER;
                sessionManager.registerClient(this);
                break;

            // Lệnh đăng nhập từ Nhân viên (VD: STAFF_LOGIN|NV_01)
            case Protocol.CMD_STAFF_LOGIN:
                this.name = parts.length > 1 ? parts[1] : "Staff";
                this.role = Protocol.ROLE_STAFF;
                sessionManager.registerClient(this);
                break;

            // Nhân viên gửi lệnh tiếp nhận khách (VD: ACCEPT_CUST|Nguyễn Văn A)
            case Protocol.CMD_ACCEPT_CUST:
                if (parts.length > 1 && Protocol.ROLE_STAFF.equals(this.role)) {
                    String customerName = parts[1];
                    sessionManager.acceptCustomer(this, customerName);
                }
                break;

            // Lệnh chat (VD: CHAT_MSG|Xin chào admin)
            case Protocol.CMD_CHAT:
                if (session != null && parts.length > 1) {
                    String chatContent = parts[1];
                    String senderName = this.name;
                    
                    // Tìm đối tác để forward tin nhắn
                    ClientHandler partner = (this == session.getCustomer()) ? session.getStaff() : session.getCustomer();
                    
                    // ĐÃ SỬA TÊN BIẾN THÀNH rawMessage
                    server.log("[CHUYỂN TIẾP TIN NHẮN] " + rawMessage); 
                    
                    if (partner != null) {
                        // Forward tin nhắn sang bên kia (VD: CHAT_MSG|Nguyễn Văn A|Xin chào admin)
                        partner.sendMessage(Protocol.CMD_CHAT + Protocol.SEP + senderName + Protocol.SEP + chatContent);
                    }
                } else {
                    sendMessage(Protocol.CMD_ERROR + Protocol.SEP + "Bạn chưa được kết nối với ai để chat.");
                }
                break;
                
            // Lệnh chủ động kết thúc phiên (VD: END_SESSION|Đã giải quyết xong)
            case Protocol.CMD_END_SESSION:
                String reason = parts.length > 1 ? parts[1] : "Đã kết thúc chủ động.";
                sessionManager.closeSession(this.session, this.name + " đã đóng phiên chat: " + reason);
                break;
                
            default:
                server.log("Lệnh không hợp lệ từ " + name + ": " + cmd);
                break;
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    private void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Các hàm Getters & Setters
    public String getName() { return name; }
    public String getRole() { return role; }
    public ChatSession getSession() { return session; }
    public void setSession(ChatSession session) { this.session = session; }
}