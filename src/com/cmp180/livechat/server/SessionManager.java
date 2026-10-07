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

    public void registerClient(ClientHandler client) {
        if (Protocol.ROLE_CUSTOMER.equalsIgnoreCase(client.getRole())) {
            waitingCustomers.add(client);
            client.sendMessage(new Message(Protocol.CMD_WAITING, "Server", "Đang chờ nhân viên CSKH tiếp nhận..."));
            server.log("[HÀNG CHỜ] Khách hàng '" + client.getName() + "' đã vào hàng chờ.");
            broadcastQueueToStaff();
            
        } else if (Protocol.ROLE_STAFF.equalsIgnoreCase(client.getRole())) {
            allStaffs.add(client);
            client.sendMessage(new Message(Protocol.CMD_WAITING, "Server", "Đã kết nối. Vui lòng chọn khách hàng từ danh sách chờ."));
            server.log("[CSKH ONLINE] Nhân viên '" + client.getName() + "' đang sẵn sàng.");
            broadcastQueueToStaff();
        }
    }

    public void acceptCustomer(ClientHandler staff, String customerId) {
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
        ChatSession session = new ChatSession(customer, staff);
        activeSessionsList.add(session);
        
        customer.sendMessage(new Message(Protocol.CMD_PAIRED, staff.getName(), staff.getClientId(), "Đã kết nối với CSKH: " + staff.getName()));
        staff.sendMessage(new Message(Protocol.CMD_PAIRED, customer.getName(), customer.getClientId(), "Đang hỗ trợ khách hàng: " + customer.getName()));
        
        server.log("-> [GHÉP PHIÊN THÀNH CÔNG] " + customer.getName() + " <---> " + staff.getName());
        updateDashboard();
    }
    
    public void routeMessage(ClientHandler sender, Message msg) {
        String receiverId = msg.getReceiverId();
        
        if (Protocol.ROLE_CUSTOMER.equalsIgnoreCase(sender.getRole())) {
            for (ChatSession session : activeSessionsList) {
                if (session.getCustomer() == sender) {
                    session.getStaff().sendMessage(new Message(Protocol.CMD_CHAT, sender.getName(), sender.getClientId(), msg.getContent()));
                    return;
                }
            }
        } else if (Protocol.ROLE_STAFF.equalsIgnoreCase(sender.getRole())) {
            for (ChatSession session : activeSessionsList) {
                if (session.getStaff() == sender && session.getCustomer().getClientId().equals(receiverId)) {
                    session.getCustomer().sendMessage(new Message(Protocol.CMD_CHAT, sender.getName(), sender.getClientId(), msg.getContent()));
                    return;
                }
            }
        }
        sender.sendMessage(new Message(Protocol.CMD_ERROR, "Server", "Không thể gửi tin nhắn."));
    }
    
    public void endSession(ClientHandler sender, String targetId, String reason) {
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

    public void closeSession(ChatSession session, String reason) {
        if (session == null) return;

        ClientHandler customer = session.getCustomer();
        ClientHandler staff = session.getStaff();

        if (customer != null) {
            customer.sendMessage(new Message(Protocol.CMD_END, "Server", staff.getClientId(), reason));
        }
        if (staff != null) {
            staff.sendMessage(new Message(Protocol.CMD_END, "Server", customer.getClientId(), reason));
        }
        
        activeSessionsList.remove(session);
        broadcastQueueToStaff();
        updateDashboard();
    }

    public void handleDisconnect(ClientHandler client) {
        waitingCustomers.remove(client);
        allStaffs.remove(client);
        
        List<ChatSession> toRemove = new ArrayList<>();
        for (ChatSession session : activeSessionsList) {
            if (session.getCustomer() == client || session.getStaff() == client) {
                toRemove.add(session);
            }
        }
        for (ChatSession session : toRemove) {
            closeSession(session, client.getName() + " đã mất kết nối.");
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
                // Định dạng: ID|Name|Status|JoinedTime;
                queueData.append(c.getClientId()).append("|")
                         .append(c.getName()).append("|")
                         .append("ACTIVE").append("|")
                         .append(c.getJoinedTime()).append(";");
            }
        }
        
        // 2. Khách hàng ĐANG CHỜ (FIFO, nên người chờ lâu nhất tự động ở trên cùng của nhóm này)
        for (ClientHandler c : waitingCustomers) {
            queueData.append(c.getClientId()).append("|")
                     .append(c.getName()).append("|")
                     .append("WAITING").append("|")
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