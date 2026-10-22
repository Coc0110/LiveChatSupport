package com.cmp180.livechat.server;

import com.cmp180.livechat.client.customer.ui.CustomerLoginController;
import com.cmp180.livechat.common.Message;
import com.cmp180.livechat.common.ProfanityFilter;
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
        public String replyToText;
        public final String replyToId;
        public final long timestamp;
        public volatile boolean isRecalled;
        public final String fileName;
        public final long fileSize;
        public final byte[] fileData;
        public final boolean isImage;

        public HistoryEntry(String messageId, String senderRole, String senderName, String encryptedContent, String replyToSender, String replyToText, long timestamp) {
            this(messageId, senderRole, senderName, encryptedContent, replyToSender, replyToText, null, timestamp, null, 0, null, false);
        }

        public HistoryEntry(String messageId, String senderRole, String senderName, String encryptedContent, String replyToSender, String replyToText, String replyToId, long timestamp) {
            this(messageId, senderRole, senderName, encryptedContent, replyToSender, replyToText, replyToId, timestamp, null, 0, null, false);
        }

        public HistoryEntry(String messageId, String senderRole, String senderName, String encryptedContent, String replyToSender, String replyToText, long timestamp, String fileName, long fileSize, byte[] fileData, boolean isImage) {
            this(messageId, senderRole, senderName, encryptedContent, replyToSender, replyToText, null, timestamp, fileName, fileSize, fileData, isImage);
        }

        public HistoryEntry(String messageId, String senderRole, String senderName, String encryptedContent, String replyToSender, String replyToText, String replyToId, long timestamp, String fileName, long fileSize, byte[] fileData, boolean isImage) {
            this.messageId = messageId;
            this.senderRole = senderRole;
            this.senderName = senderName;
            this.encryptedContent = encryptedContent;
            this.replyToSender = replyToSender;
            this.replyToText = replyToText;
            this.replyToId = replyToId;
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
    
    // Thuộc tính khách hàng gắn với Database
    private String customerPhone;
    private String dbCustomerId;
    private String issueTopic = "Tư vấn chung";
    private String primarySkill = "Tất cả";

    // Thời gian tham gia hàng đợi
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
            socket.setSoTimeout(40000);
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
        } catch (java.net.SocketTimeoutException e) {
            server.log("[ZOMBIE TIMEOUT] Không nhận được heartbeat/tín hiệu từ " + (name != null ? name : clientId) + " sau 40s. Đang dọn dẹp kết nối.");
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
                handleCustomerLogin(msg);
                break;

            case Protocol.CMD_STAFF_LOGIN:
                handleStaffLogin(msg);
                break;

            case Protocol.CMD_ACCEPT_CUST:
                if (Protocol.ROLE_STAFF.equals(this.role)) {
                    sessionManager.acceptCustomer(this, msg.getContent());
                }
                break;

            case Protocol.CMD_CHAT:
            case Protocol.CMD_FILE:
            case Protocol.CMD_RECALL_MSG:
            case Protocol.CMD_TYPING:
            case Protocol.CMD_VOICE_MSG:
            case Protocol.CMD_CALL_REQ:
            case Protocol.CMD_VIDEO_CALL_REQ:
            case Protocol.CMD_CALL_ACCEPT:
            case Protocol.CMD_CALL_REJECT:
            case Protocol.CMD_CALL_END:
            case Protocol.CMD_CALL_DATA:
            case Protocol.CMD_VIDEO_DATA:
                sessionManager.routeMessage(this, msg);
                break;
                
            case Protocol.CMD_END_SESSION:
                sessionManager.endSession(this, msg.getReceiverId(), this.name + " đã kết thúc phiên chat.");
                break;
                
            case Protocol.CMD_GET_QUEUE:
                sessionManager.sendQueueToStaff(this);
                break;

            case Protocol.CMD_RATING:
                handleRating(msg);
                break;

            case Protocol.CMD_CUST_HISTORY:
                handleCustomerHistory();
                break;

            case Protocol.CMD_CHECK_PHONE:
                handleCheckPhone(msg);
                break;

            case Protocol.CMD_GET_SESSION_MSGS:
                handleGetSessionMessages(msg);
                break;

            case Protocol.CMD_REQ_CHAT_HISTORY:
                if (Protocol.ROLE_STAFF.equals(this.role)) {
                    sessionManager.sendCustomerChatHistoryToStaff(this, msg.getReceiverId());
                }
                break;

            case Protocol.CMD_GET_ONLINE_STAFF:
                if (Protocol.ROLE_STAFF.equals(this.role)) {
                    sessionManager.sendOnlineStaffList(this);
                }
                break;

            case Protocol.CMD_TRANSFER_SESSION:
                if (Protocol.ROLE_STAFF.equals(this.role)) {
                    sessionManager.transferSession(this, msg.getReceiverId(), msg.getContent());
                }
                break;

            case Protocol.CMD_HEARTBEAT:
                // Heartbeat packet from client, socket read timeout is automatically reset
                break;
                
            default:
                server.log("Lệnh không hợp lệ từ " + name + ": " + cmd);
                break;
        }
    }

    private void handleCustomerLogin(Message msg) {
        String phone = msg.getSenderId();
        String content = msg.getContent();
        String fullName = content;
        String topic = "Tư vấn chung";

        if (content != null && content.contains("|")) {
            String[] parts = content.split("\\|", 2);
            fullName = parts[0];
            topic = parts[1];
        }

        if (fullName != null && fullName.contains("(")) {
            fullName = fullName.replaceAll("\\s*\\([^)]*\\)$", "").trim();
        }

        if (topic != null) {
            String clean = topic.replaceAll("^(Hỏi tiếp:\\s*)+", "").trim();
            if (topic.contains("Hỏi tiếp:")) {
                topic = "Hỏi tiếp: " + (clean.isEmpty() ? "Tư vấn" : clean);
            } else {
                topic = clean.isEmpty() ? "Tư vấn chung" : clean;
            }
        }

        this.issueTopic = topic;
        String cleanPhone = (phone != null) ? phone.replaceAll("[\\s\\-\\.\\(\\)]", "").trim() : "";
        if (cleanPhone.startsWith("+84")) cleanPhone = "0" + cleanPhone.substring(3);
        else if (cleanPhone.startsWith("84") && cleanPhone.length() == 11) cleanPhone = "0" + cleanPhone.substring(2);
        this.customerPhone = !cleanPhone.isEmpty() ? cleanPhone : "Khách";

        // Kiểm tra từ ngữ vi phạm tiêu chuẩn cộng đồng hoặc ký tự không hợp lệ trong tên
        if (ProfanityFilter.containsProfanity(fullName)) {
            String masked = ProfanityFilter.filter(fullName);
            server.log("[TÊN KHÔNG HỢP LỆ] Khách hàng '" + cleanPhone + "' dùng tên vi phạm tiêu chuẩn: '" + fullName + "' -> '" + masked + "'");
            sendMessage(new Message(Protocol.CMD_ERROR, "Server", "Tên chứa từ ngữ vi phạm tiêu chuẩn cộng đồng: " + masked));
            return;
        }
        if (fullName != null && !fullName.trim().isEmpty() && !CustomerLoginController.isValidCustomerName(fullName)) {
            server.log("[TÊN KHÔNG HỢP LỆ] Khách hàng '" + cleanPhone + "' đặt tên không đúng định dạng: '" + fullName + "'");
            sendMessage(new Message(Protocol.CMD_ERROR, "Server", "Tên không hợp lệ! Tên không được có ký tự như / . , + (duy nhất chấp nhận dấu '-')"));
            return;
        }

        // Lưu / Tra cứu trong SQLite
        DatabaseManager.CustomerRecord rec = DatabaseManager.getInstance().registerOrUpdateCustomer(this.customerPhone, fullName);
        if (rec != null) {
            this.dbCustomerId = rec.customerId;
            this.clientId = rec.customerId;
            this.name = rec.fullName;
        } else {
            this.name = fullName;
        }
        this.role = Protocol.ROLE_CUSTOMER;
        if (server.getUdpRelayServer() != null) {
            server.getUdpRelayServer().registerAlias(this.customerPhone, this.clientId);
        }

        // Kiểm tra xem khách hàng này đã đang kết nối ở thiết bị khác chưa
        if (sessionManager.isCustomerOnline(this.dbCustomerId, this.customerPhone, this)) {
            server.log("[ĐĂNG NHẬP THẤT BẠI] Khách hàng '" + this.name + "' (SĐT: " + this.customerPhone + ") đã đang kết nối ở một thiết bị khác.");
            sendMessage(new Message(Protocol.CMD_ERROR, "Server", "Số điện thoại " + this.customerPhone + " hiện đang được kết nối trong một phiên chat khác!"));
            return;
        }

        sendMessage(new Message(Protocol.CMD_LOGIN_SUCCESS, this.clientId, this.name));

        if (msg.getReceiverId() != null && msg.getReceiverId().startsWith("REOPEN:")) {
            String reopenSessionId = msg.getReceiverId().substring("REOPEN:".length());
            this.interrupted = true;
            List<DatabaseManager.MessageRecord> oldMsgs = DatabaseManager.getInstance().getMessagesForReopenSession(reopenSessionId);
            for (DatabaseManager.MessageRecord m : oldMsgs) {
                HistoryEntry he = new HistoryEntry(m.messageId, m.senderRole, m.senderName, m.content, m.replyToSender, m.replyToText, m.replyToId, m.timestamp, m.fileName, m.fileSize, m.fileData, m.isImage);
                he.isRecalled = m.isRecalled;
                this.chatHistory.add(he);
            }
            server.log("[MỞ LẠI PHIÊN CŨ] Khách hàng '" + this.name + "' nạp lịch sử chuỗi phiên (" + oldMsgs.size() + " tin nhắn liên quan).");
        } else if ("RECONNECT".equalsIgnoreCase(msg.getReceiverId())) {
            this.interrupted = true;
        }

        sessionManager.registerClient(this);
    }

    private void handleStaffLogin(Message msg) {
        String username = msg.getSenderId();
        String password = msg.getContent();

        // Xác thực tài khoản với Database
        DatabaseManager.StaffRecord staffRec = DatabaseManager.getInstance().authenticateStaff(username, password);
        if (staffRec == null) {
            server.log("[ĐĂNG NHẬP THẤT BẠI] CSKH '" + username + "' sai thông tin đăng nhập.");
            sendMessage(new Message(Protocol.CMD_ERROR, "Server", "Đăng nhập thất bại: Tên đăng nhập hoặc mật khẩu không chính xác!"));
            return;
        }

        this.clientId = staffRec.staffId;
        this.name = staffRec.fullName;
        this.role = Protocol.ROLE_STAFF;
        this.primarySkill = (staffRec.primarySkill != null) ? staffRec.primarySkill : "Tất cả";

        // Kiểm tra xem nhân viên này đã đang đăng nhập ở thiết bị khác chưa
        if (sessionManager.isStaffOnline(staffRec.staffId)) {
            server.log("[ĐĂNG NHẬP THẤT BẠI] CSKH '" + staffRec.fullName + "' (Username: " + staffRec.username + ") hiện đang đăng nhập ở một thiết bị khác.");
            sendMessage(new Message(Protocol.CMD_ERROR, "Server", "Tài khoản '" + staffRec.username + "' hiện đang đăng nhập ở một thiết bị/cửa sổ khác! Vui lòng đăng xuất trước."));
            return;
        }

        server.log("[ĐĂNG NHẬP THÀNH CÔNG] CSKH '" + staffRec.fullName + "' (Username: " + staffRec.username + ", Chuyên môn: " + this.primarySkill + ")");
        sendMessage(new Message(Protocol.CMD_LOGIN_SUCCESS, staffRec.staffId, staffRec.fullName));
        if (server.getUdpRelayServer() != null) {
            server.getUdpRelayServer().registerAlias(staffRec.username, staffRec.staffId);
        }
        sessionManager.registerClient(this);
    }

    private void handleRating(Message msg) {
        String sessionId = msg.getReceiverId();
        String ratingData = msg.getContent();
        int rating = 5;
        String feedback = "";

        if (ratingData != null) {
            String[] parts = ratingData.split("\\|", 2);
            try {
                rating = Integer.parseInt(parts[0]);
            } catch (Exception ignored) {}
            if (parts.length > 1) feedback = parts[1];
        }

        DatabaseManager.getInstance().closeSession(sessionId, rating, feedback);
        server.log("-> [CSAT RATING] Khách hàng '" + name + "' đánh giá phiên: " + rating + " ⭐. Phản hồi: " + feedback);
    }

    private void handleCustomerHistory() {
        if (this.dbCustomerId != null) {
            List<DatabaseManager.SessionRecord> past = DatabaseManager.getInstance().getSessionsByCustomer(this.dbCustomerId);
            StringBuilder sb = new StringBuilder();
            for (DatabaseManager.SessionRecord s : past) {
                sb.append(s.sessionId).append("|")
                  .append(s.issueTopic).append("|")
                  .append(s.staffName).append("|")
                  .append(s.status).append("|")
                  .append(s.rating).append("|")
                  .append(s.createdAt).append(";");
            }
            sendMessage(new Message(Protocol.CMD_CUST_HISTORY, "Server", sb.toString()));
        }
    }

    private void handleCheckPhone(Message msg) {
        String rawPhone = msg.getContent() != null ? msg.getContent().trim() : (msg.getSenderId() != null ? msg.getSenderId().trim() : "");
        String phone = rawPhone.replaceAll("[\\s\\-\\.\\(\\)]", "").trim();
        if (phone.startsWith("+84")) phone = "0" + phone.substring(3);
        else if (phone.startsWith("84") && phone.length() == 11) phone = "0" + phone.substring(2);

        if (!phone.matches("^0[35789][0-9]{8}$")) {
            sendMessage(new Message(Protocol.CMD_ERROR, "Server", "Số điện thoại không hợp lệ (cần 10 chữ số di động Việt Nam)!"));
            return;
        }

        DatabaseManager.CustomerRecord rec = DatabaseManager.getInstance().findCustomerByPhone(phone);
        if (rec == null) {
            // Khách hàng mới chưa có trong hệ thống
            sendMessage(new Message(Protocol.CMD_PHONE_RESULT, "NEW", "", ""));
        } else {
            // Khách hàng cũ đã tồn tại trong CSDL
            this.dbCustomerId = rec.customerId;
            this.customerPhone = rec.phoneOrEmail;
            this.name = rec.fullName;

            List<DatabaseManager.SessionRecord> past = DatabaseManager.getInstance().getSessionsByCustomer(rec.customerId);
            StringBuilder sb = new StringBuilder();
            for (DatabaseManager.SessionRecord s : past) {
                String fb = (s.feedback != null) ? s.feedback.replace("|", " ").replace(";", " ") : "";
                sb.append(s.sessionId).append("|")
                  .append(s.issueTopic).append("|")
                  .append(s.staffName).append("|")
                  .append(s.status).append("|")
                  .append(s.rating).append("|")
                  .append(fb).append("|")
                  .append(s.createdAt).append(";");
            }
            sendMessage(new Message(Protocol.CMD_PHONE_RESULT, "EXISTING", rec.fullName, sb.toString()));
        }
    }

    private void handleGetSessionMessages(Message msg) {
        String sessionId = msg.getContent();
        if (sessionId == null || sessionId.isEmpty()) return;

        List<DatabaseManager.MessageRecord> msgs = DatabaseManager.getInstance().getMessagesBySession(sessionId);
        StringBuilder sb = new StringBuilder();
        for (DatabaseManager.MessageRecord m : msgs) {
            String contentClean = (m.content != null) ? m.content.replace(";", "{SEMICOLON}").replace("|", "{PIPE}") : "";
            String fName = (m.fileName != null) ? m.fileName.replace(";", "").replace("|", "") : "";
            sb.append(m.senderRole).append("|")
              .append(m.senderName).append("|")
              .append(contentClean).append("|")
              .append(m.timestamp).append("|")
              .append(m.isRecalled ? "1" : "0").append("|")
              .append(fName).append(";");
        }
        sendMessage(new Message(Protocol.CMD_SESSION_MSGS, sessionId, sb.toString()));
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
        } catch (IOException ignored) { }
    }

    public Socket getSocket() { return socket; }
    public boolean isSocketOpen() { return socket != null && !socket.isClosed(); }
    public String getClientId() { return clientId; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public long getJoinedTime() { return joinedTime; }

    public String getCustomerPhone() { return customerPhone; }
    public String getDbCustomerId() { return dbCustomerId != null ? dbCustomerId : clientId; }
    public String getIssueTopic() { return issueTopic; }
    public String getPrimarySkill() { return primarySkill != null ? primarySkill : "Tất cả"; }

    public boolean isInterrupted() { return interrupted; }
    public void setInterrupted(boolean interrupted) { this.interrupted = interrupted; }
    public List<HistoryEntry> getChatHistory() { return chatHistory; }

    public void addHistory(String messageId, String senderRole, String senderName, String encryptedContent, String replyToSender, String replyToText, String replyToId) {
        chatHistory.add(new HistoryEntry(messageId, senderRole, senderName, encryptedContent, replyToSender, replyToText, replyToId, System.currentTimeMillis()));
    }

    public void addHistory(String messageId, String senderRole, String senderName, String encryptedContent, String replyToSender, String replyToText) {
        addHistory(messageId, senderRole, senderName, encryptedContent, replyToSender, replyToText, null);
    }

    public void addFileHistory(String messageId, String senderRole, String senderName, String encryptedContent, String replyToSender, String replyToText, String replyToId, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        chatHistory.add(new HistoryEntry(messageId, senderRole, senderName, encryptedContent, replyToSender, replyToText, replyToId, System.currentTimeMillis(), fileName, fileSize, fileData, isImage));
    }

    public void addFileHistory(String messageId, String senderRole, String senderName, String encryptedContent, String replyToSender, String replyToText, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        addFileHistory(messageId, senderRole, senderName, encryptedContent, replyToSender, replyToText, null, fileName, fileSize, fileData, isImage);
    }

    public void addHistory(String senderRole, String senderName, String encryptedContent) {
        addHistory(UUID.randomUUID().toString(), senderRole, senderName, encryptedContent, null, null);
    }

    public void recallHistory(String messageId) {
        if (messageId == null) return;
        String recalledContent = null;
        for (HistoryEntry e : chatHistory) {
            if (messageId.equals(e.messageId)) {
                e.isRecalled = true;
                recalledContent = e.encryptedContent;
            }
        }
        for (HistoryEntry e : chatHistory) {
            if (messageId.equals(e.replyToId) || (recalledContent != null && recalledContent.equals(e.replyToText))) {
                e.replyToText = "Tin nhắn đã được thu hồi";
            }
        }
    }
}