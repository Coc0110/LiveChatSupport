package com.cmp180.livechat.server;

import com.cmp180.livechat.common.Message;
import com.cmp180.livechat.common.Protocol;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class SessionManager {
    private final ServerMain server;
    private final Queue<ClientHandler> waitingCustomers = new ConcurrentLinkedQueue<>();
    private final Queue<ClientHandler> allStaffs = new ConcurrentLinkedQueue<>();
    private final List<ChatSession> activeSessionsList = new CopyOnWriteArrayList<>();

    public SessionManager(ServerMain server) {
        this.server = server;
    }

    public synchronized void registerClient(ClientHandler client) {
        if (Protocol.ROLE_CUSTOMER.equalsIgnoreCase(client.getRole())) {
            waitingCustomers.removeIf(c -> c.getClientId().equals(client.getClientId()));
            waitingCustomers.add(client);
            client.sendMessage(new Message(Protocol.CMD_WAITING, "Server", "Đang chờ nhân viên CSKH tiếp nhận..."));
            server.log("[HÀNG CHỜ] Khách hàng '" + client.getName() + "' (SĐT: " + client.getCustomerPhone() + ") đã vào hàng chờ." + (client.isInterrupted() ? " (Đang tư vấn dở)" : ""));
            broadcastQueueToStaff();
            
        } else if (Protocol.ROLE_STAFF.equalsIgnoreCase(client.getRole())) {
            for (ClientHandler existingStaff : allStaffs) {
                if (existingStaff.getClientId().equals(client.getClientId()) && existingStaff != client) {
                    existingStaff.sendMessage(new Message(Protocol.CMD_ERROR, "Server", "Tài khoản của bạn vừa được đăng nhập từ một thiết bị khác!"));
                    server.log("[CSKH TRÙNG LẶP] Ngắt phiên cũ của CSKH '" + existingStaff.getName() + "' do có thiết bị mới đăng nhập.");
                }
            }
            allStaffs.removeIf(s -> s.getClientId().equals(client.getClientId()));
            allStaffs.add(client);
            client.sendMessage(new Message(Protocol.CMD_WAITING, "Server", "Đã kết nối. Vui lòng chọn khách hàng từ danh sách chờ."));
            server.log("[CSKH ONLINE] Nhân viên '" + client.getName() + "' đang sẵn sàng.");
            broadcastQueueToStaff();
        }
    }

    public synchronized void acceptCustomer(ClientHandler staff, String customerId) {
        ClientHandler targetCustomer = null;
        for (ClientHandler c : waitingCustomers) {
            if (c.getClientId().equals(customerId)) {
                targetCustomer = c;
                break;
            }
        }

        if (targetCustomer != null) {
            waitingCustomers.remove(targetCustomer);
            createSession(targetCustomer, staff);
            broadcastQueueToStaff();
        } else {
            staff.sendMessage(new Message(Protocol.CMD_ERROR, "Server", "Khách hàng này không còn trong hàng chờ."));
        }
    }

    private void createSession(ClientHandler customer, ClientHandler staff) {
        boolean wasInterrupted = customer.isInterrupted();
        customer.setInterrupted(false);

        ChatSession session = new ChatSession(customer, staff, customer.getIssueTopic());
        activeSessionsList.add(session);

        // Lưu phiên vào SQLite Database
        DatabaseManager.getInstance().createSession(session.getSessionId(), customer.getDbCustomerId(), staff.getClientId(), customer.getIssueTopic());

        // Nếu khách hàng đang tư vấn dở và có lịch sử chat trước đó, gửi toàn bộ lịch sử cho nhân viên mới trước
        if (!customer.getChatHistory().isEmpty()) {
            for (ClientHandler.HistoryEntry entry : customer.getChatHistory()) {
                String meta = entry.senderRole + "|" + entry.senderName + "|" + entry.timestamp + "|" + (entry.isRecalled ? "RECALLED" : "NORMAL");
                staff.sendMessage(new Message(Protocol.CMD_CHAT_HISTORY, meta, customer.getClientId(), entry.encryptedContent, entry.messageId, entry.replyToSender, entry.replyToText, entry.fileName, entry.fileSize, entry.fileData, entry.isImage));
            }
        }
        
        // Gửi thông báo PAIRED kèm sessionId trong trường messageId
        customer.sendMessage(new Message(Protocol.CMD_PAIRED, staff.getName(), staff.getClientId(), "Đã kết nối với CSKH: " + staff.getName(), session.getSessionId(), null, null));
        staff.sendMessage(new Message(Protocol.CMD_PAIRED, customer.getName(), customer.getClientId(),
                wasInterrupted ? ("Đã tiếp nhận tư vấn tiếp cho: " + customer.getName()) : ("Đang hỗ trợ khách hàng: " + customer.getName()), session.getSessionId(), null, null));
        
        server.log("-> [GHÉP PHIÊN THÀNH CÔNG] " + customer.getName() + " <---> " + staff.getName() + " [Mã phiên: " + session.getSessionId() + "]" + (wasInterrupted ? " (Tiếp nhận tư vấn dở)" : ""));
        updateDashboard();
    }
    
    public void routeMessage(ClientHandler sender, Message msg) {
        String receiverId = msg.getReceiverId();

        // 1. Xử lý thu hồi tin nhắn
        if (Protocol.CMD_RECALL_MSG.equals(msg.getCommand())) {
            DatabaseManager.getInstance().recallMessage(msg.getMessageId());
            if (Protocol.ROLE_CUSTOMER.equalsIgnoreCase(sender.getRole())) {
                for (ChatSession session : activeSessionsList) {
                    if (session.getCustomer() == sender) {
                        sender.recallHistory(msg.getMessageId());
                        session.getStaff().sendMessage(msg);
                        return;
                    }
                }
            } else if (Protocol.ROLE_STAFF.equalsIgnoreCase(sender.getRole())) {
                for (ChatSession session : activeSessionsList) {
                    if (session.getStaff() == sender && session.getCustomer().getClientId().equals(receiverId)) {
                        session.getCustomer().recallHistory(msg.getMessageId());
                        session.getCustomer().sendMessage(msg);
                        return;
                    }
                }
            }
            return;
        }

        // 2. Xử lý gửi tệp đính kèm / hình ảnh
        if (Protocol.CMD_FILE.equals(msg.getCommand())) {
            if (Protocol.ROLE_CUSTOMER.equalsIgnoreCase(sender.getRole())) {
                for (ChatSession session : activeSessionsList) {
                    if (session.getCustomer() == sender) {
                        DatabaseManager.getInstance().saveMessage(session.getSessionId(), msg.getMessageId(), "CUSTOMER", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText(), msg.getFileName(), msg.getFileSize(), msg.isImage(), System.currentTimeMillis());
                        sender.addFileHistory(msg.getMessageId(), "CUSTOMER", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText(), msg.getFileName(), msg.getFileSize(), msg.getFileData(), msg.isImage());
                        session.getStaff().sendMessage(new Message(Protocol.CMD_FILE, sender.getName(), sender.getClientId(), msg.getContent(), msg.getMessageId(), msg.getReplyToSender(), msg.getReplyToText(), msg.getFileName(), msg.getFileSize(), msg.getFileData(), msg.isImage()));
                        return;
                    }
                }
            } else if (Protocol.ROLE_STAFF.equalsIgnoreCase(sender.getRole())) {
                for (ChatSession session : activeSessionsList) {
                    if (session.getStaff() == sender && session.getCustomer().getClientId().equals(receiverId)) {
                        DatabaseManager.getInstance().saveMessage(session.getSessionId(), msg.getMessageId(), "STAFF", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText(), msg.getFileName(), msg.getFileSize(), msg.isImage(), System.currentTimeMillis());
                        session.getCustomer().addFileHistory(msg.getMessageId(), "STAFF", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText(), msg.getFileName(), msg.getFileSize(), msg.getFileData(), msg.isImage());
                        session.getCustomer().sendMessage(new Message(Protocol.CMD_FILE, sender.getName(), sender.getClientId(), msg.getContent(), msg.getMessageId(), msg.getReplyToSender(), msg.getReplyToText(), msg.getFileName(), msg.getFileSize(), msg.getFileData(), msg.isImage()));
                        return;
                    }
                }
            }
            sender.sendMessage(new Message(Protocol.CMD_ERROR, "Server", "Không thể gửi tệp đính kèm."));
            return;
        }
        
        // 3. Xử lý tin nhắn chat thông thường (kèm reply)
        if (Protocol.ROLE_CUSTOMER.equalsIgnoreCase(sender.getRole())) {
            for (ChatSession session : activeSessionsList) {
                if (session.getCustomer() == sender) {
                    DatabaseManager.getInstance().saveMessage(session.getSessionId(), msg.getMessageId(), "CUSTOMER", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText(), null, 0, false, System.currentTimeMillis());
                    sender.addHistory(msg.getMessageId(), "CUSTOMER", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText());
                    session.getStaff().sendMessage(new Message(Protocol.CMD_CHAT, sender.getName(), sender.getClientId(), msg.getContent(), msg.getMessageId(), msg.getReplyToSender(), msg.getReplyToText()));
                    return;
                }
            }
        } else if (Protocol.ROLE_STAFF.equalsIgnoreCase(sender.getRole())) {
            for (ChatSession session : activeSessionsList) {
                if (session.getStaff() == sender && session.getCustomer().getClientId().equals(receiverId)) {
                    DatabaseManager.getInstance().saveMessage(session.getSessionId(), msg.getMessageId(), "STAFF", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText(), null, 0, false, System.currentTimeMillis());
                    session.getCustomer().addHistory(msg.getMessageId(), "STAFF", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText());
                    session.getCustomer().sendMessage(new Message(Protocol.CMD_CHAT, sender.getName(), sender.getClientId(), msg.getContent(), msg.getMessageId(), msg.getReplyToSender(), msg.getReplyToText()));
                    return;
                }
            }
        }
        sender.sendMessage(new Message(Protocol.CMD_ERROR, "Server", "Không thể gửi tin nhắn."));
    }
    
    public synchronized void endSession(ClientHandler sender, String targetId, String reason) {
        ChatSession toRemove = null;
        for (ChatSession session : activeSessionsList) {
            if (session.getCustomer() == sender || session.getStaff() == sender) {
                if (targetId == null || session.getCustomer().getClientId().equals(targetId) || session.getStaff().getClientId().equals(targetId)) {
                    toRemove = session;
                    break;
                }
            }
        }
        
        if (toRemove != null) {
            closeSession(toRemove, reason);
        }
    }

    public synchronized void closeSession(ChatSession session, String reason) {
        if (session == null) return;

        ClientHandler customer = session.getCustomer();
        ClientHandler staff = session.getStaff();

        DatabaseManager.getInstance().closeSession(session.getSessionId(), 0, reason);

        if (customer != null) {
            customer.sendMessage(new Message(Protocol.CMD_END, "Server", staff != null ? staff.getClientId() : "", reason, session.getSessionId(), null, null));
        }
        if (staff != null) {
            staff.sendMessage(new Message(Protocol.CMD_END, "Server", customer != null ? customer.getClientId() : "", reason, session.getSessionId(), null, null));
        }
        
        activeSessionsList.remove(session);
        broadcastQueueToStaff();
        updateDashboard();
    }

    public synchronized void handleDisconnect(ClientHandler client) {
        waitingCustomers.remove(client);
        allStaffs.remove(client);
        
        List<ChatSession> toRemove = new ArrayList<>();
        for (ChatSession session : activeSessionsList) {
            if (session.getCustomer() == client || session.getStaff() == client) {
                toRemove.add(session);
            }
        }
        for (ChatSession session : toRemove) {
            if (session.getStaff() == client) {
                // Nhân viên mất kết nối đột ngột -> Đưa khách hàng quay lại hàng chờ với cờ "Đang tư vấn dở"
                activeSessionsList.remove(session);
                ClientHandler customer = session.getCustomer();
                if (customer != null) {
                    customer.setInterrupted(true);
                    String disconnectNote = "Nhân viên " + client.getName() + " đã mất kết nối.";
                    customer.addHistory("SYSTEM", "Hệ thống", com.cmp180.livechat.common.SecurityManager.encrypt(disconnectNote));
                    if (!waitingCustomers.contains(customer)) {
                        waitingCustomers.add(customer);
                    }
                    customer.sendMessage(new Message(Protocol.CMD_WAITING, "Server",
                            "Nhân viên " + client.getName() + " đã mất kết nối. Bạn đã được đưa lại vào hàng chờ để nhân viên khác tiếp nhận..."));
                    server.log("[CHUYỂN HÀNG CHỜ] Nhân viên '" + client.getName() + "' mất kết nối. Khách hàng '" + customer.getName() + "' được đưa lại vào hàng chờ (Đang tư vấn dở).");
                }
            } else {
                // Khách hàng ngắt kết nối -> Kết thúc phiên như bình thường
                closeSession(session, client.getName() + " đã mất kết nối.");
            }
        }
        
        broadcastQueueToStaff();
    }
    
    public void sendQueueToStaff(ClientHandler staff) {
        if (staff == null) return;
        StringBuilder queueData = new StringBuilder();
        
        // 1. Khách hàng ĐANG TƯ VẤN (Đưa lên đầu)
        for (ChatSession session : activeSessionsList) {
            if (session.getStaff() == staff) {
                ClientHandler c = session.getCustomer();
                String displayName = c.getName() + (c.getIssueTopic() != null && !c.getIssueTopic().isEmpty() ? " (" + c.getIssueTopic() + ")" : "");
                // Định dạng: ID|Name|Status|JoinedTime;
                queueData.append(c.getClientId()).append("|")
                         .append(displayName).append("|")
                         .append("ACTIVE").append("|")
                         .append(c.getJoinedTime()).append(";");
            }
        }
        
        // 2. Khách hàng ĐANG CHỜ (Hoặc ĐANG TƯ VẤN DỞ khi nhân viên cũ rớt mạng)
        for (ClientHandler c : waitingCustomers) {
            String status = c.isInterrupted() ? Protocol.STATUS_INTERRUPTED : "WAITING";
            String displayName = c.getName() + (c.getIssueTopic() != null && !c.getIssueTopic().isEmpty() ? " (" + c.getIssueTopic() + ")" : "");
            queueData.append(c.getClientId()).append("|")
                     .append(displayName).append("|")
                     .append(status).append("|")
                     .append(c.getJoinedTime()).append(";");
        }
        
        Message msg = new Message(Protocol.CMD_QUEUE_UPDATE, "Server", queueData.toString());
        staff.sendMessage(msg);
    }

    private void broadcastQueueToStaff() {
        for (ClientHandler staff : allStaffs) {
            sendQueueToStaff(staff);
        }
        updateDashboard();
    }

    private void updateDashboard() {
        if (server.getGui() != null) {
            server.getGui().updateDashboard(waitingCustomers.size(), allStaffs.size(), activeSessionsList.size());
        }
    }
}