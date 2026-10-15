package com.cmp180.livechat.server;

import com.cmp180.livechat.common.Message;
import com.cmp180.livechat.common.Protocol;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientHandler implements Runnable {
    public static class HistoryEntry {
        public final String messageId;
        public final String senderRole;
        public final String senderName;
        public final String encryptedContent;
        public final String replyToSender;
        public final String replyToText;
        public final long timestamp;
        public volatile boolean isRecalled;
        public final String fileName;
        public final long fileSize;
        public final byte[] fileData;
        public final boolean isImage;

        public HistoryEntry(String messageId, String senderRole, String senderName, String encryptedContent, String replyToSender, String replyToText, long timestamp) {
            this(messageId, senderRole, senderName, encryptedContent, replyToSender, replyToText, timestamp, null, 0, null, false);
        }

        public HistoryEntry(String messageId, String senderRole, String senderName, String encryptedContent, String replyToSender, String replyToText, long timestamp, String fileName, long fileSize, byte[] fileData, boolean isImage) {
            this.messageId = messageId;
            this.senderRole = senderRole;
            this.senderName = senderName;
            this.encryptedContent = encryptedContent;
            this.replyToSender = replyToSender;
            this.replyToText = replyToText;
            this.timestamp = timestamp;
            this.isRecalled = false;
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.fileData = fileData;
            this.isImage = isImage;
        }
    }

    private final Socket socket;
    private final ServerMain server;
    private final SessionManager sessionManager;
    
    private ObjectInputStream in;
    private ObjectOutputStream out;
    
    private String clientId;
    private String name;
    private String role;
    
    // Thêm thời gian tham gia hàng đợi
    private final long joinedTime;

    // Cờ đánh dấu "Đang tư vấn dở" và lịch sử chat của khách hàng
    private volatile boolean interrupted = false;
    private final List<HistoryEntry> chatHistory = new CopyOnWriteArrayList<>();

    public ClientHandler(Socket socket, ServerMain server, SessionManager sessionManager) {
        this.socket = socket;
        this.server = server;
        this.sessionManager = sessionManager;
        this.clientId = UUID.randomUUID().toString();
        this.joinedTime = System.currentTimeMillis();
        
        try {
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
                if (msg.getSenderId() != null && !msg.getSenderId().isEmpty() && !"Customer".equalsIgnoreCase(msg.getSenderId())) {
                    this.clientId = msg.getSenderId();
                }
                if ("RECONNECT".equalsIgnoreCase(msg.getReceiverId())) {
                    this.interrupted = true;
                }
                this.name = msg.getContent();
                this.role = Protocol.ROLE_CUSTOMER;
                sessionManager.registerClient(this);
                break;

            case Protocol.CMD_STAFF_LOGIN:
                if (msg.getSenderId() != null && !msg.getSenderId().isEmpty() && !"Staff".equalsIgnoreCase(msg.getSenderId())) {
                    this.clientId = msg.getSenderId();
                }
                this.name = msg.getContent();
                this.role = Protocol.ROLE_STAFF;
                sessionManager.registerClient(this);
                break;

            case Protocol.CMD_ACCEPT_CUST:
                if (Protocol.ROLE_STAFF.equals(this.role)) {
                    sessionManager.acceptCustomer(this, msg.getContent());
                }
                break;

            case Protocol.CMD_CHAT:
            case Protocol.CMD_FILE:
            case Protocol.CMD_RECALL_MSG:
                sessionManager.routeMessage(this, msg);
                break;
                
            case Protocol.CMD_END_SESSION:
                sessionManager.endSession(this, msg.getReceiverId(), this.name + " đã kết thúc phiên chat.");
                break;
                
            case Protocol.CMD_GET_QUEUE:
                sessionManager.sendQueueToStaff(this);
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
    public long getJoinedTime() { return joinedTime; }

    public boolean isInterrupted() { return interrupted; }
    public void setInterrupted(boolean interrupted) { this.interrupted = interrupted; }
    public List<HistoryEntry> getChatHistory() { return chatHistory; }

    public void addHistory(String messageId, String senderRole, String senderName, String encryptedContent, String replyToSender, String replyToText) {
        chatHistory.add(new HistoryEntry(messageId, senderRole, senderName, encryptedContent, replyToSender, replyToText, System.currentTimeMillis()));
    }

    public void addFileHistory(String messageId, String senderRole, String senderName, String encryptedContent, String replyToSender, String replyToText, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        chatHistory.add(new HistoryEntry(messageId, senderRole, senderName, encryptedContent, replyToSender, replyToText, System.currentTimeMillis(), fileName, fileSize, fileData, isImage));
    }

    public void addHistory(String senderRole, String senderName, String encryptedContent) {
        addHistory(UUID.randomUUID().toString(), senderRole, senderName, encryptedContent, null, null);
    }

    public void recallHistory(String messageId) {
        if (messageId == null) return;
        for (HistoryEntry e : chatHistory) {
            if (messageId.equals(e.messageId)) {
                e.isRecalled = true;
                break;
            }
        }
    }
}