package com.cmp180.livechat.server;

import com.cmp180.livechat.common.Message;
import com.cmp180.livechat.common.ProfanityFilter;
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
    public static final int MAX_ACTIVE_SESSIONS_PER_STAFF = 3;
    private final List<ChatSession> activeSessionsList = new CopyOnWriteArrayList<>();

    public SessionManager(ServerMain server) {
        this.server = server;
    }

    public int getActiveSessionCount(ClientHandler staff) {
        if (staff == null) return 0;
        int count = 0;
        for (ChatSession session : activeSessionsList) {
            if (session.getStaff() == staff) {
                count++;
            }
        }
        return count;
    }

    public synchronized boolean isStaffOnline(String staffId) {
        if (staffId == null) return false;
        for (ClientHandler staff : allStaffs) {
            if (staffId.equals(staff.getClientId()) && staff.getSocket() != null && !staff.getSocket().isClosed()) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isCustomerOnline(String dbCustomerId, String customerPhone, ClientHandler current) {
        for (ClientHandler c : waitingCustomers) {
            if (c != current && c.getSocket() != null && !c.getSocket().isClosed()) {
                if ((dbCustomerId != null && dbCustomerId.equals(c.getDbCustomerId())) ||
                    (customerPhone != null && !customerPhone.isEmpty() && customerPhone.equals(c.getCustomerPhone()))) {
                    return true;
                }
            }
        }
        for (ChatSession s : activeSessionsList) {
            ClientHandler c = s.getCustomer();
            if (c != null && c != current && c.getSocket() != null && !c.getSocket().isClosed()) {
                if ((dbCustomerId != null && dbCustomerId.equals(c.getDbCustomerId())) ||
                    (customerPhone != null && !customerPhone.isEmpty() && customerPhone.equals(c.getCustomerPhone()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized void registerClient(ClientHandler client) {
        if (Protocol.ROLE_CUSTOMER.equalsIgnoreCase(client.getRole())) {
            waitingCustomers.removeIf(c -> c.getClientId().equals(client.getClientId()));
            waitingCustomers.add(client);
            client.sendMessage(new Message(Protocol.CMD_WAITING, "Server", "Đang chờ nhân viên kết nối..."));
            server.log("[HÀNG CHỜ] Khách hàng '" + client.getName() + "' (SĐT: " + client.getCustomerPhone() + ") đã vào hàng chờ." + (client.isInterrupted() ? " (Đang tư vấn dở)" : ""));
            broadcastQueueToStaff();
            tryAutoDispatch();
            
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
            tryAutoDispatch();
        }
    }

    public synchronized void acceptCustomer(ClientHandler staff, String customerId) {
        if (getActiveSessionCount(staff) >= MAX_ACTIVE_SESSIONS_PER_STAFF) {
            staff.sendMessage(new Message(Protocol.CMD_ERROR, "Server", "Bạn đang tiếp nhận tối đa " + MAX_ACTIVE_SESSIONS_PER_STAFF + " khách hàng cùng lúc. Vui lòng hoàn thành phiên hiện tại trước!"));
            return;
        }

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

    private boolean isSkillMatch(String staffSkill, String customerTopic) {
        if (staffSkill == null || staffSkill.trim().isEmpty() || "Tất cả".equalsIgnoreCase(staffSkill.trim())) {
            return true;
        }
        if (customerTopic == null || customerTopic.trim().isEmpty()) {
            return true;
        }
        String s = staffSkill.toLowerCase();
        String t = customerTopic.toLowerCase();
        if (s.contains(t) || t.contains(s)) return true;

        if ((t.contains("laptop") || t.contains("pc") || t.contains("mua")) &&
            (s.contains("laptop") || s.contains("pc") || s.contains("mua"))) {
            return true;
        }
        if ((t.contains("kỹ thuật") || t.contains("cài đặt")) &&
            (s.contains("kỹ thuật") || s.contains("cài đặt"))) {
            return true;
        }
        if ((t.contains("bảo hành") || t.contains("đổi trả")) &&
            (s.contains("bảo hành") || s.contains("đổi trả"))) {
            return true;
        }
        if ((t.contains("đơn hàng") || t.contains("giao vận")) &&
            (s.contains("đơn hàng") || s.contains("giao vận"))) {
            return true;
        }
        return false;
    }

    private ClientHandler findBestStaffForCustomer(ClientHandler customer) {
        if (allStaffs.isEmpty()) return null;

        // Tầng 1: Ưu tiên Nhân viên quen (Preferred Staff) từng hỗ trợ tốt và đang rảnh
        String preferredStaffId = DatabaseManager.getInstance().getPreferredStaffIdForCustomer(customer.getDbCustomerId());
        if (preferredStaffId != null) {
            for (ClientHandler staff : allStaffs) {
                if (staff.getClientId().equals(preferredStaffId) && getActiveSessionCount(staff) < MAX_ACTIVE_SESSIONS_PER_STAFF) {
                    return staff;
                }
            }
        }

        // Tầng 2: Ưu tiên Nhân viên có Chuyên môn chính (Primary Skill) trùng với Chủ đề của khách
        ClientHandler bestMatchedStaff = null;
        int minSessionsMatched = Integer.MAX_VALUE;
        for (ClientHandler staff : allStaffs) {
            int activeCount = getActiveSessionCount(staff);
            if (activeCount < MAX_ACTIVE_SESSIONS_PER_STAFF) {
                if (isSkillMatch(staff.getPrimarySkill(), customer.getIssueTopic())) {
                    if (activeCount < minSessionsMatched) {
                        minSessionsMatched = activeCount;
                        bestMatchedStaff = staff;
                    }
                }
            }
        }
        if (bestMatchedStaff != null) return bestMatchedStaff;

        // Tầng 3: Fallback - Nhân viên đa nhiệm có ít phiên nhất (Least Connections)
        ClientHandler fallbackStaff = null;
        int minSessionsGeneral = Integer.MAX_VALUE;
        for (ClientHandler staff : allStaffs) {
            int activeCount = getActiveSessionCount(staff);
            if (activeCount < MAX_ACTIVE_SESSIONS_PER_STAFF) {
                if (activeCount < minSessionsGeneral) {
                    minSessionsGeneral = activeCount;
                    fallbackStaff = staff;
                }
            }
        }
        return fallbackStaff;
    }

    public synchronized void tryAutoDispatch() {
        if (waitingCustomers.isEmpty() || allStaffs.isEmpty()) return;

        List<ClientHandler> queueCopy = new ArrayList<>(waitingCustomers);
        boolean dispatchedAny = false;

        for (ClientHandler customer : queueCopy) {
            if (!waitingCustomers.contains(customer)) continue;

            ClientHandler staff = findBestStaffForCustomer(customer);
            if (staff != null) {
                waitingCustomers.remove(customer);
                createSession(customer, staff);
                dispatchedAny = true;
                server.log("-> [TỰ ĐỘNG ĐIỀU PHỐI] Khách hàng '" + customer.getName() + "' (Chủ đề: " + customer.getIssueTopic() + ") -> CSKH '" + staff.getName() + "' (Chuyên môn: " + staff.getPrimarySkill() + ")");
            }
        }

        if (dispatchedAny) {
            broadcastQueueToStaff();
        }
    }

    private void createSession(ClientHandler customer, ClientHandler staff) {
        boolean wasInterrupted = customer.isInterrupted();
        customer.setInterrupted(false);

        ChatSession session = new ChatSession(customer, staff, customer.getIssueTopic());
        activeSessionsList.add(session);

        // Lưu phiên vào SQLite Database
        DatabaseManager.getInstance().createSession(session.getSessionId(), customer.getDbCustomerId(), staff.getClientId(), customer.getIssueTopic());

        // Nếu khách hàng có lịch sử nạp từ chuỗi phiên cũ (Mở lại phiên / Đang tư vấn dở), gửi lịch sử cho cả Nhân viên và Khách hàng
        if (customer.getChatHistory().isEmpty() && customer.getDbCustomerId() != null && DatabaseManager.isChainedTopic(customer.getIssueTopic())) {
            List<DatabaseManager.MessageRecord> chainMsgs = DatabaseManager.getInstance().getMessagesForCustomerTopicChain(customer.getDbCustomerId(), customer.getIssueTopic());
            for (DatabaseManager.MessageRecord m : chainMsgs) {
                ClientHandler.HistoryEntry he = new ClientHandler.HistoryEntry(
                    m.messageId, m.senderRole, m.senderName, m.content, m.replyToSender, m.replyToText, m.replyToId,
                    m.timestamp, m.fileName, m.fileSize, m.fileData, m.isImage
                );
                he.isRecalled = m.isRecalled;
                customer.getChatHistory().add(he);
            }
        }

        if (!customer.getChatHistory().isEmpty()) {
            for (ClientHandler.HistoryEntry entry : customer.getChatHistory()) {
                String meta = entry.senderRole + "|" + entry.senderName + "|" + entry.timestamp + "|" + (entry.isRecalled ? "RECALLED" : "NORMAL");
                staff.sendMessage(new Message(Protocol.CMD_CHAT_HISTORY, meta, customer.getClientId(), entry.encryptedContent, entry.messageId, entry.replyToSender, entry.replyToText, entry.replyToId, entry.fileName, entry.fileSize, entry.fileData, entry.isImage));
                customer.sendMessage(new Message(Protocol.CMD_CHAT_HISTORY, meta, staff.getClientId(), entry.encryptedContent, entry.messageId, entry.replyToSender, entry.replyToText, entry.replyToId, entry.fileName, entry.fileSize, entry.fileData, entry.isImage));
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

        // Kiểm duyệt ngôn từ vi phạm tiêu chuẩn cộng đồng trước khi xử lý định tuyến & lưu DB
        if (Protocol.CMD_CHAT.equals(msg.getCommand()) || Protocol.CMD_FILE.equals(msg.getCommand())) {
            if (msg.getContent() != null && !msg.getContent().isEmpty()) {
                String plain = com.cmp180.livechat.common.SecurityManager.decrypt(msg.getContent());
                if (ProfanityFilter.containsProfanity(plain)) {
                    String clean = ProfanityFilter.filter(plain);
                    msg.setContent(com.cmp180.livechat.common.SecurityManager.encrypt(clean));
                }
            }
            if (msg.getReplyToText() != null && !msg.getReplyToText().isEmpty()) {
                if (ProfanityFilter.containsProfanity(msg.getReplyToText())) {
                    msg.setReplyToText(ProfanityFilter.filter(msg.getReplyToText()));
                }
            }
        }

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

        // 1.1. Xử lý trạng thái đang soạn tin (Typing Indicator)
        if (Protocol.CMD_TYPING.equals(msg.getCommand())) {
            if (Protocol.ROLE_CUSTOMER.equalsIgnoreCase(sender.getRole())) {
                for (ChatSession session : activeSessionsList) {
                    if (session.getCustomer() == sender && session.getStaff() != null) {
                        session.getStaff().sendMessage(new Message(Protocol.CMD_TYPING, sender.getName(), sender.getClientId(), msg.getContent()));
                        return;
                    }
                }
            } else if (Protocol.ROLE_STAFF.equalsIgnoreCase(sender.getRole())) {
                for (ChatSession session : activeSessionsList) {
                    if (session.getStaff() == sender && session.getCustomer().getClientId().equals(receiverId)) {
                        session.getCustomer().sendMessage(new Message(Protocol.CMD_TYPING, sender.getName(), sender.getClientId(), msg.getContent()));
                        return;
                    }
                }
            }
            return;
        }

        // 1.2. Xử lý tin nhắn thoại (Voice Message)
        if (Protocol.CMD_VOICE_MSG.equals(msg.getCommand())) {
            if (Protocol.ROLE_CUSTOMER.equalsIgnoreCase(sender.getRole())) {
                for (ChatSession session : activeSessionsList) {
                    if (session.getCustomer() == sender) {
                        DatabaseManager.getInstance().saveMessage(session.getSessionId(), msg.getMessageId(), "CUSTOMER", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText(), msg.getReplyToId(), msg.getFileName(), msg.getFileSize(), false, System.currentTimeMillis(), msg.getFileData());
                        sender.addFileHistory(msg.getMessageId(), "CUSTOMER", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText(), msg.getReplyToId(), msg.getFileName(), msg.getFileSize(), msg.getFileData(), false);
                        session.getStaff().sendMessage(new Message(Protocol.CMD_VOICE_MSG, sender.getName(), sender.getClientId(), msg.getContent(), msg.getMessageId(), msg.getReplyToSender(), msg.getReplyToText(), msg.getReplyToId(), msg.getFileName(), msg.getFileSize(), msg.getFileData(), false));
                        return;
                    }
                }
            } else if (Protocol.ROLE_STAFF.equalsIgnoreCase(sender.getRole())) {
                for (ChatSession session : activeSessionsList) {
                    if (session.getStaff() == sender && session.getCustomer().getClientId().equals(receiverId)) {
                        DatabaseManager.getInstance().saveMessage(session.getSessionId(), msg.getMessageId(), "STAFF", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText(), msg.getReplyToId(), msg.getFileName(), msg.getFileSize(), false, System.currentTimeMillis(), msg.getFileData());
                        session.getCustomer().addFileHistory(msg.getMessageId(), "STAFF", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText(), msg.getReplyToId(), msg.getFileName(), msg.getFileSize(), msg.getFileData(), false);
                        session.getCustomer().sendMessage(new Message(Protocol.CMD_VOICE_MSG, sender.getName(), sender.getClientId(), msg.getContent(), msg.getMessageId(), msg.getReplyToSender(), msg.getReplyToText(), msg.getReplyToId(), msg.getFileName(), msg.getFileSize(), msg.getFileData(), false));
                        return;
                    }
                }
            }
            sender.sendMessage(new Message(Protocol.CMD_ERROR, "Server", "Không thể gửi tin nhắn thoại."));
            return;
        }

        // 1.3. Xử lý cuộc gọi thoại & cuộc gọi video (Voice & Video Call Signaling & Streaming)
        if (Protocol.CMD_CALL_REQ.equals(msg.getCommand()) ||
            Protocol.CMD_VIDEO_CALL_REQ.equals(msg.getCommand()) ||
            Protocol.CMD_CALL_ACCEPT.equals(msg.getCommand()) ||
            Protocol.CMD_CALL_REJECT.equals(msg.getCommand()) ||
            Protocol.CMD_CALL_END.equals(msg.getCommand()) ||
            Protocol.CMD_CALL_DATA.equals(msg.getCommand()) ||
            Protocol.CMD_VIDEO_DATA.equals(msg.getCommand())) {

            if (Protocol.ROLE_CUSTOMER.equalsIgnoreCase(sender.getRole())) {
                for (ChatSession session : activeSessionsList) {
                    if (session.getCustomer() == sender && session.getStaff() != null) {
                        session.getStaff().sendMessage(new Message(msg.getCommand(), sender.getName(), sender.getClientId(), msg.getContent(), msg.getMessageId(), null, null, null, 0, msg.getFileData(), false));
                        return;
                    }
                }
            } else if (Protocol.ROLE_STAFF.equalsIgnoreCase(sender.getRole())) {
                for (ChatSession session : activeSessionsList) {
                    if (session.getStaff() == sender && session.getCustomer() != null && session.getCustomer().getClientId().equals(receiverId)) {
                        session.getCustomer().sendMessage(new Message(msg.getCommand(), sender.getName(), sender.getClientId(), msg.getContent(), msg.getMessageId(), null, null, null, 0, msg.getFileData(), false));
                        return;
                    }
                }
            }
            if (Protocol.CMD_CALL_REQ.equals(msg.getCommand()) || Protocol.CMD_VIDEO_CALL_REQ.equals(msg.getCommand())) {
                sender.sendMessage(new Message(Protocol.CMD_CALL_REJECT, "Server", "Đối phương hiện không sẵn sàng nhận cuộc gọi."));
            }
            return;
        }

        // 2. Xử lý gửi tệp đính kèm / hình ảnh
        if (Protocol.CMD_FILE.equals(msg.getCommand())) {
            if (Protocol.ROLE_CUSTOMER.equalsIgnoreCase(sender.getRole())) {
                for (ChatSession session : activeSessionsList) {
                    if (session.getCustomer() == sender) {
                        DatabaseManager.getInstance().saveMessage(session.getSessionId(), msg.getMessageId(), "CUSTOMER", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText(), msg.getReplyToId(), msg.getFileName(), msg.getFileSize(), msg.isImage(), System.currentTimeMillis(), msg.getFileData());
                        sender.addFileHistory(msg.getMessageId(), "CUSTOMER", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText(), msg.getReplyToId(), msg.getFileName(), msg.getFileSize(), msg.getFileData(), msg.isImage());
                        session.getStaff().sendMessage(new Message(Protocol.CMD_FILE, sender.getName(), sender.getClientId(), msg.getContent(), msg.getMessageId(), msg.getReplyToSender(), msg.getReplyToText(), msg.getReplyToId(), msg.getFileName(), msg.getFileSize(), msg.getFileData(), msg.isImage()));
                        return;
                    }
                }
            } else if (Protocol.ROLE_STAFF.equalsIgnoreCase(sender.getRole())) {
                for (ChatSession session : activeSessionsList) {
                    if (session.getStaff() == sender && session.getCustomer().getClientId().equals(receiverId)) {
                        DatabaseManager.getInstance().saveMessage(session.getSessionId(), msg.getMessageId(), "STAFF", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText(), msg.getReplyToId(), msg.getFileName(), msg.getFileSize(), msg.isImage(), System.currentTimeMillis(), msg.getFileData());
                        session.getCustomer().addFileHistory(msg.getMessageId(), "STAFF", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText(), msg.getReplyToId(), msg.getFileName(), msg.getFileSize(), msg.getFileData(), msg.isImage());
                        session.getCustomer().sendMessage(new Message(Protocol.CMD_FILE, sender.getName(), sender.getClientId(), msg.getContent(), msg.getMessageId(), msg.getReplyToSender(), msg.getReplyToText(), msg.getReplyToId(), msg.getFileName(), msg.getFileSize(), msg.getFileData(), msg.isImage()));
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
                    DatabaseManager.getInstance().saveMessage(session.getSessionId(), msg.getMessageId(), "CUSTOMER", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText(), msg.getReplyToId(), null, 0, false, System.currentTimeMillis(), null);
                    sender.addHistory(msg.getMessageId(), "CUSTOMER", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText(), msg.getReplyToId());
                    session.getStaff().sendMessage(new Message(Protocol.CMD_CHAT, sender.getName(), sender.getClientId(), msg.getContent(), msg.getMessageId(), msg.getReplyToSender(), msg.getReplyToText(), msg.getReplyToId(), null, 0, null, false));
                    return;
                }
            }
        } else if (Protocol.ROLE_STAFF.equalsIgnoreCase(sender.getRole())) {
            for (ChatSession session : activeSessionsList) {
                if (session.getStaff() == sender && session.getCustomer().getClientId().equals(receiverId)) {
                    DatabaseManager.getInstance().saveMessage(session.getSessionId(), msg.getMessageId(), "STAFF", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText(), msg.getReplyToId(), null, 0, false, System.currentTimeMillis(), null);
                    session.getCustomer().addHistory(msg.getMessageId(), "STAFF", sender.getName(), msg.getContent(), msg.getReplyToSender(), msg.getReplyToText(), msg.getReplyToId());
                    session.getCustomer().sendMessage(new Message(Protocol.CMD_CHAT, sender.getName(), sender.getClientId(), msg.getContent(), msg.getMessageId(), msg.getReplyToSender(), msg.getReplyToText(), msg.getReplyToId(), null, 0, null, false));
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
        tryAutoDispatch();
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
                    String disconnectNote = "Nhân viên " + client.getName() + " mất kết nối. Đang trong hàng chờ...";
                    customer.addHistory("SYSTEM", "Hệ thống", com.cmp180.livechat.common.SecurityManager.encrypt(disconnectNote));
                    if (!waitingCustomers.contains(customer)) {
                        waitingCustomers.add(customer);
                    }
                    customer.sendMessage(new Message(Protocol.CMD_WAITING, "Server",
                            "Nhân viên " + client.getName() + " mất kết nối. Đang trong hàng chờ..."));
                    server.log("[CHUYỂN HÀNG CHỜ] Nhân viên '" + client.getName() + "' mất kết nối. Khách hàng '" + customer.getName() + "' được đưa lại vào hàng chờ (Đang trong hàng chờ).");
                }
            } else {
                // Khách hàng ngắt kết nối -> Kết thúc phiên như bình thường
                closeSession(session, client.getName() + " đã mất kết nối.");
            }
        }
        
        broadcastQueueToStaff();
        tryAutoDispatch();
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

    public synchronized void sendCustomerChatHistoryToStaff(ClientHandler staff, String customerId) {
        if (staff == null || customerId == null) return;

        ChatSession targetSession = null;
        for (ChatSession s : activeSessionsList) {
            if (s.getCustomer().getClientId().equals(customerId)) {
                targetSession = s;
                break;
            }
        }

        ClientHandler customer = null;
        if (targetSession != null) {
            customer = targetSession.getCustomer();
        } else {
            for (ClientHandler c : waitingCustomers) {
                if (c.getClientId().equals(customerId)) {
                    customer = c;
                    break;
                }
            }
        }

        if (customer == null) return;

        // Nếu customer.chatHistory rỗng nhưng là chủ đề ghép hoặc đã có tin nhắn trong phiên hiện tại
        if (customer.getChatHistory().isEmpty()) {
            if (targetSession != null) {
                List<DatabaseManager.MessageRecord> dbMsgs = DatabaseManager.getInstance().getMessagesBySession(targetSession.getSessionId());
                for (DatabaseManager.MessageRecord m : dbMsgs) {
                    ClientHandler.HistoryEntry he = new ClientHandler.HistoryEntry(
                        m.messageId, m.senderRole, m.senderName, m.content, m.replyToSender, m.replyToText, m.replyToId,
                        m.timestamp, m.fileName, m.fileSize, m.fileData, m.isImage
                    );
                    he.isRecalled = m.isRecalled;
                    customer.getChatHistory().add(he);
                }
            }
            if (customer.getDbCustomerId() != null && DatabaseManager.isChainedTopic(customer.getIssueTopic())) {
                List<DatabaseManager.MessageRecord> chainMsgs = DatabaseManager.getInstance().getMessagesForCustomerTopicChain(customer.getDbCustomerId(), customer.getIssueTopic());
                for (DatabaseManager.MessageRecord m : chainMsgs) {
                    boolean already = false;
                    for (ClientHandler.HistoryEntry existing : customer.getChatHistory()) {
                        if (existing.messageId.equals(m.messageId)) {
                            already = true;
                            break;
                        }
                    }
                    if (!already) {
                        ClientHandler.HistoryEntry he = new ClientHandler.HistoryEntry(
                            m.messageId, m.senderRole, m.senderName, m.content, m.replyToSender, m.replyToText, m.replyToId,
                            m.timestamp, m.fileName, m.fileSize, m.fileData, m.isImage
                        );
                        he.isRecalled = m.isRecalled;
                        customer.getChatHistory().add(he);
                    }
                }
            }
        }

        // Gửi toàn bộ lịch sử này cho nhân viên yêu cầu
        for (ClientHandler.HistoryEntry entry : customer.getChatHistory()) {
            String meta = entry.senderRole + "|" + entry.senderName + "|" + entry.timestamp + "|" + (entry.isRecalled ? "RECALLED" : "NORMAL");
            staff.sendMessage(new Message(Protocol.CMD_CHAT_HISTORY, meta, customer.getClientId(), entry.encryptedContent, entry.messageId, entry.replyToSender, entry.replyToText, entry.replyToId, entry.fileName, entry.fileSize, entry.fileData, entry.isImage));
        }
    }

    public void sendOnlineStaffList(ClientHandler requestingStaff) {
        if (requestingStaff == null) return;
        StringBuilder sb = new StringBuilder();
        for (ClientHandler staff : allStaffs) {
            if (!staff.getClientId().equals(requestingStaff.getClientId())) {
                int activeCount = getActiveSessionCount(staff);
                // format: staffId|fullName|primarySkill|activeSessionCount;
                sb.append(staff.getClientId()).append("|")
                  .append(staff.getName()).append("|")
                  .append(staff.getPrimarySkill() != null ? staff.getPrimarySkill() : "Tất cả").append("|")
                  .append(activeCount).append(";");
            }
        }
        requestingStaff.sendMessage(new Message(Protocol.CMD_ONLINE_STAFF_LIST, "Server", sb.toString()));
    }

    public synchronized void transferSession(ClientHandler requestingStaff, String customerId, String content) {
        if (requestingStaff == null || customerId == null) return;

        String targetStaffId = content;
        String transferNote = "";
        if (content != null && content.contains("|")) {
            String[] parts = content.split("\\|", 2);
            targetStaffId = parts[0];
            transferNote = parts[1];
        }

        ChatSession targetSession = null;
        for (ChatSession session : activeSessionsList) {
            if (session.getStaff() == requestingStaff && session.getCustomer().getClientId().equals(customerId)) {
                targetSession = session;
                break;
            }
        }

        if (targetSession == null) {
            requestingStaff.sendMessage(new Message(Protocol.CMD_ERROR, "Server", "Không tìm thấy phiên chat với khách hàng này!"));
            return;
        }

        ClientHandler targetStaff = null;
        for (ClientHandler staff : allStaffs) {
            if (staff.getClientId().equals(targetStaffId)) {
                targetStaff = staff;
                break;
            }
        }

        if (targetStaff == null) {
            requestingStaff.sendMessage(new Message(Protocol.CMD_ERROR, "Server", "Nhân viên nhận chuyển tiếp hiện không trực tuyến!"));
            return;
        }

        if (getActiveSessionCount(targetStaff) >= MAX_ACTIVE_SESSIONS_PER_STAFF) {
            requestingStaff.sendMessage(new Message(Protocol.CMD_ERROR, "Server", "Nhân viên '" + targetStaff.getName() + "' đã nhận tối đa " + MAX_ACTIVE_SESSIONS_PER_STAFF + " khách hàng cùng lúc!"));
            return;
        }

        ClientHandler customer = targetSession.getCustomer();

        // 1. Ghi nhận đóng phiên cũ với lý do chuyển tiếp
        activeSessionsList.remove(targetSession);
        DatabaseManager.getInstance().closeSession(targetSession.getSessionId(), 0, "Chuyển tiếp cho CSKH " + targetStaff.getName());

        // 2. Thông báo kết thúc phiên phía nhân viên cũ
        requestingStaff.sendMessage(new Message(Protocol.CMD_END, "Server", customer.getClientId(), "Đã chuyển phiên cho " + targetStaff.getName(), targetSession.getSessionId(), null, null));

        // 3. Nếu có ghi chú bàn giao, lưu vào lịch sử của khách hàng
        String encNote = null;
        if (transferNote != null && !transferNote.trim().isEmpty()) {
            String noteText = "[Chuyển tiếp từ NV " + requestingStaff.getName() + "]: " + transferNote.trim();
            encNote = com.cmp180.livechat.common.SecurityManager.encrypt(noteText);
            customer.addHistory("SYSTEM", "Hệ thống", encNote);
        }

        // 4. Tạo phiên mới với nhân viên đích (toàn bộ lịch sử bao gồm ghi chú bàn giao sẽ được gửi tới nhân viên đích)
        createSession(customer, targetStaff);

        // Lưu ghi chú bàn giao vào Database cho phiên mới
        if (encNote != null) {
            for (ChatSession s : activeSessionsList) {
                if (s.getCustomer() == customer && s.getStaff() == targetStaff) {
                    DatabaseManager.getInstance().saveMessage(s.getSessionId(), java.util.UUID.randomUUID().toString(), "SYSTEM", "Hệ thống", encNote, null, null, null, 0, false, System.currentTimeMillis());
                    break;
                }
            }
        }

        // 5. Thông báo cho khách hàng
        String notice = "Bạn đã được chuyển tiếp đến hỗ trợ viên " + targetStaff.getName() + " (Chuyên môn: " + targetStaff.getPrimarySkill() + ").";
        customer.sendMessage(new Message(Protocol.CMD_CHAT, "Hệ thống", targetStaff.getClientId(), com.cmp180.livechat.common.SecurityManager.encrypt(notice), java.util.UUID.randomUUID().toString(), null, null));

        server.log("-> [CHUYỂN PHIÊN THÀNH CÔNG] CSKH '" + requestingStaff.getName() + "' -> CSKH '" + targetStaff.getName() + "' (Khách: '" + customer.getName() + "', Ghi chú: " + transferNote + ")");
        broadcastQueueToStaff();
    }

    public synchronized void shutdown() {
        Message shutdownMsg = new Message(Protocol.CMD_END, "Server", "", "Máy chủ đang tạm dừng để bảo trì. Kết nối đã đóng.");
        for (ChatSession session : activeSessionsList) {
            if (session.getCustomer() != null) session.getCustomer().sendMessage(shutdownMsg);
            if (session.getStaff() != null) session.getStaff().sendMessage(shutdownMsg);
        }
        activeSessionsList.clear();

        for (ClientHandler cust : waitingCustomers) {
            cust.sendMessage(shutdownMsg);
        }
        waitingCustomers.clear();

        for (ClientHandler staff : allStaffs) {
            staff.sendMessage(shutdownMsg);
        }
        allStaffs.clear();
        updateDashboard();
    }

    public List<ChatSession> getActiveSessions() {
        return new ArrayList<>(activeSessionsList);
    }

    public List<ClientHandler> getWaitingCustomers() {
        return new ArrayList<>(waitingCustomers);
    }
}