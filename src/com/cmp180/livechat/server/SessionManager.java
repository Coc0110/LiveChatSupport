package com.cmp180.livechat.server;

import com.cmp180.livechat.common.Message;
import com.cmp180.livechat.common.Protocol;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SessionManager {
    private final ServerMain server;
    private final Queue<ClientHandler> waitingCustomers = new ConcurrentLinkedQueue<>();
    private final Queue<ClientHandler> availableStaffs = new ConcurrentLinkedQueue<>();
    private int activeSessions = 0;

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
            availableStaffs.add(client);
            client.sendMessage(new Message(Protocol.CMD_WAITING, "Server", "Đã kết nối. Vui lòng chọn khách hàng từ danh sách chờ."));
            server.log("[CSKH ONLINE] Nhân viên '" + client.getName() + "' đang sẵn sàng.");
            broadcastQueueToStaff();
        }
    }

    public void acceptCustomer(ClientHandler staff, String customerId) {
        ClientHandler targetCustomer = null;
        
        // Tìm theo UUID thay vì Tên
        for (ClientHandler c : waitingCustomers) {
            if (c.getClientId().equals(customerId)) {
                targetCustomer = c;
                break;
            }
        }

        if (targetCustomer != null) {
            waitingCustomers.remove(targetCustomer);
            availableStaffs.remove(staff);
            broadcastQueueToStaff();
            createSession(targetCustomer, staff);
        } else {
            staff.sendMessage(new Message(Protocol.CMD_ERROR, "Server", "Khách hàng này không còn trong hàng chờ."));
        }
    }

    private void createSession(ClientHandler customer, ClientHandler staff) {
        ChatSession session = new ChatSession(customer, staff);
        customer.setSession(session);
        staff.setSession(session);
        
        customer.sendMessage(new Message(Protocol.CMD_PAIRED, "Server", "Đã kết nối với CSKH: " + staff.getName()));
        staff.sendMessage(new Message(Protocol.CMD_PAIRED, "Server", "Đang hỗ trợ khách hàng: " + customer.getName()));
        server.log("-> [GHÉP PHIÊN THÀNH CÔNG] " + customer.getName() + " <---> " + staff.getName());
        
        activeSessions++;
        updateDashboard();
    }

    public void closeSession(ChatSession session, String reason) {
        if (session == null) return;

        ClientHandler customer = session.getCustomer();
        ClientHandler staff = session.getStaff();

        if (customer != null) {
            customer.setSession(null);
            customer.sendMessage(new Message(Protocol.CMD_END, "Server", reason));
        }
        if (staff != null) {
            staff.setSession(null);
            staff.sendMessage(new Message(Protocol.CMD_END, "Server", reason));
            availableStaffs.add(staff);
            broadcastQueueToStaff();
        }
        activeSessions--;
        updateDashboard();
    }

    public void handleDisconnect(ClientHandler client) {
        waitingCustomers.remove(client);
        availableStaffs.remove(client);
        
        if (client.getSession() != null) {
            closeSession(client.getSession(), client.getName() + " đã mất kết nối.");
        }
        broadcastQueueToStaff();
    }

    private void broadcastQueueToStaff() {
        if (availableStaffs.isEmpty()) return;
        
        // Tạo chuỗi JSON siêu cơ bản hoặc chuỗi tuỳ chỉnh để chứa Id và Tên
        // Dễ nhất ở đây là gửi định dạng: ID1:Tên1;ID2:Tên2
        StringBuilder queueData = new StringBuilder();
        for (ClientHandler c : waitingCustomers) {
            queueData.append(c.getClientId()).append(":").append(c.getName()).append(";");
        }
        
        Message msg = new Message(Protocol.CMD_QUEUE_UPDATE, "Server", queueData.toString());
        for (ClientHandler staff : availableStaffs) {
            staff.sendMessage(msg);
        }
        updateDashboard();
    }

    private void updateDashboard() {
        if (server.getGui() != null) {
            server.getGui().updateDashboard(waitingCustomers.size(), availableStaffs.size(), activeSessions);
        }
    }
}