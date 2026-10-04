package com.cmp180.livechat.server;

import com.cmp180.livechat.common.Message;
import com.cmp180.livechat.common.Protocol;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ServerMain server;
    private final SessionManager sessionManager;
    
    private ObjectInputStream in;
    private ObjectOutputStream out;
    
    private final String clientId; // ĐỊNH DANH DUY NHẤT
    private String name;
    private String role;
    private ChatSession session;

    public ClientHandler(Socket socket, ServerMain server, SessionManager sessionManager) {
        this.socket = socket;
        this.server = server;
        this.sessionManager = sessionManager;
        this.clientId = UUID.randomUUID().toString(); // Khởi tạo UUID
        
        try {
            // Lưu ý: Luôn khởi tạo OutputStream trước InputStream trong Object streams
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            server.log("Lỗi khởi tạo I/O cho Client: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            Message message;
            // Lắng nghe Object thay vì String
            while ((message = (Message) in.readObject()) != null) {
                processMessage(message);
            }
        } catch (Exception e) {
            server.log("[NGẮT KẾT NỐI] Client " + (name != null ? name : clientId) + " đã rời đi.");
        } finally {
            sessionManager.handleDisconnect(this);
            closeConnection();
        }
    }

    private void processMessage(Message msg) {
        String cmd = msg.getCommand();

        switch (cmd) {
            case Protocol.CMD_CUST_LOGIN:
                this.name = msg.getContent(); // Nội dung là tên KH
                this.role = Protocol.ROLE_CUSTOMER;
                sessionManager.registerClient(this);
                break;

            case Protocol.CMD_STAFF_LOGIN:
                this.name = msg.getContent();
                this.role = Protocol.ROLE_STAFF;
                sessionManager.registerClient(this);
                break;

            case Protocol.CMD_ACCEPT_CUST:
                if (Protocol.ROLE_STAFF.equals(this.role)) {
                    String targetUUID = msg.getContent(); // Truyền lên UUID của khách
                    sessionManager.acceptCustomer(this, targetUUID);
                }
                break;

            case Protocol.CMD_CHAT:
                if (session != null) {
                    ClientHandler partner = (this == session.getCustomer()) ? session.getStaff() : session.getCustomer();
                    if (partner != null) {
                        // Gửi Message qua bên kia
                        partner.sendMessage(new Message(Protocol.CMD_CHAT, this.name, msg.getContent()));
                    }
                } else {
                    sendMessage(new Message(Protocol.CMD_ERROR, "Server", "Bạn chưa được kết nối với ai để chat."));
                }
                break;
                
            case Protocol.CMD_END_SESSION:
                sessionManager.closeSession(this.session, this.name + " đã kết thúc phiên chat.");
                break;
                
            default:
                server.log("Lệnh không hợp lệ từ " + name + ": " + cmd);
                break;
        }
    }

    public synchronized void sendMessage(Message message) {
        try {
            if (out != null) {
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            server.log("Lỗi gửi dữ liệu cho " + (name != null ? name : clientId) + ": " + e.getMessage());
        }
    }

    private void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) { }
    }

    public String getClientId() { return clientId; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public ChatSession getSession() { return session; }
    public void setSession(ChatSession session) { this.session = session; }
}