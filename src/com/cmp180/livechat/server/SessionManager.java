package com.cmp180.livechat.server;

import com.cmp180.livechat.common.Protocol;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SessionManager {
    private final ServerMain server;
    // Sử dụng ConcurrentLinkedQueue để an toàn khi nhiều Thread cùng thao tác
    private final Queue<ClientHandler> waitingCustomers = new ConcurrentLinkedQueue<>();
    private final Queue<ClientHandler> availableStaffs = new ConcurrentLinkedQueue<>();

    public SessionManager(ServerMain server) {
        this.server = server;
    }

    /**
     * Phân loại Client khi vừa kết nối. 
     * Khách hàng vào hàng chờ. Nhân viên vào danh sách sẵn sàng.
     */
    public synchronized void registerClient(ClientHandler client) {
        if (Protocol.ROLE_CUSTOMER.equalsIgnoreCase(client.getRole())) {
            waitingCustomers.add(client);
            client.sendMessage(Protocol.CMD_WAITING + Protocol.SEP + "Đang chờ nhân viên CSKH tiếp nhận...");
            server.log("[HÀNG CHỜ] Khách hàng '" + client.getName() + "' đã vào hàng chờ.");
            
            // Báo cho tất cả nhân viên biết có khách mới để cập nhật giao diện
            broadcastQueueToStaff();
            
        } else if (Protocol.ROLE_STAFF.equalsIgnoreCase(client.getRole())) {
            availableStaffs.add(client);
            client.sendMessage(Protocol.CMD_WAITING + Protocol.SEP + "Đã kết nối. Vui lòng chọn khách hàng từ danh sách chờ.");
            server.log("[CSKH ONLINE] Nhân viên '" + client.getName() + "' đang sẵn sàng.");
            
            // Gửi ngay danh sách hàng chờ hiện tại cho nhân viên mới đăng nhập
            broadcastQueueToStaff();
        }
    }

    /**
     * Nhân viên chủ động chọn 1 khách hàng từ danh sách chờ (Khớp FR-S04)
     */
    public synchronized void acceptCustomer(ClientHandler staff, String customerName) {
        ClientHandler targetCustomer = null;
        
        // Tìm khách hàng trong hàng đợi
        for (ClientHandler c : waitingCustomers) {
            if (c.getName().equals(customerName)) {
                targetCustomer = c;
                break;
            }
        }

        if (targetCustomer != null) {
            // Rút khách hàng khỏi hàng đợi
            waitingCustomers.remove(targetCustomer);
            availableStaffs.remove(staff); // Tạm thời xóa nhân viên khỏi danh sách rảnh (nếu v1 chỉ cho phép 1-1)
            
            // Cập nhật lại giao diện danh sách chờ cho các nhân viên khác
            broadcastQueueToStaff();
            
            // Khởi tạo phiên
            createSession(targetCustomer, staff);
        } else {
            staff.sendMessage(Protocol.CMD_ERROR + Protocol.SEP + "Khách hàng này không còn trong hàng chờ.");
        }
    }

    private void createSession(ClientHandler customer, ClientHandler staff) {
        ChatSession session = new ChatSession(customer, staff);
        customer.setSession(session);
        staff.setSession(session);
        
        customer.sendMessage(Protocol.CMD_PAIRED + Protocol.SEP + "Đã kết nối với CSKH: " + staff.getName());
        staff.sendMessage(Protocol.CMD_PAIRED + Protocol.SEP + "Đang hỗ trợ khách hàng: " + customer.getName());
        server.log("-> [GHẾP PHIÊN THÀNH CÔNG] " + customer.getName() + " <---> " + staff.getName());
    }

    public synchronized void closeSession(ChatSession session, String reason) {
        if (session == null) return;

        ClientHandler customer = session.getCustomer();
        ClientHandler staff = session.getStaff();

        if (customer != null) {
            customer.setSession(null);
            customer.sendMessage(Protocol.CMD_END + Protocol.SEP + reason);
        }
        if (staff != null) {
            staff.setSession(null);
            staff.sendMessage(Protocol.CMD_END + Protocol.SEP + reason);
            // Đưa nhân viên về lại danh sách rảnh
            availableStaffs.add(staff);
            broadcastQueueToStaff();
        }
    }

    public synchronized void handleDisconnect(ClientHandler client) {
        waitingCustomers.remove(client);
        availableStaffs.remove(client);
        
        if (client.getSession() != null) {
            closeSession(client.getSession(), client.getName() + " đã mất kết nối.");
        }
        
        // Cập nhật lại danh sách nếu có sự thay đổi
        broadcastQueueToStaff();
    }

    /**
     * Đóng gói danh sách chờ và gửi cho toàn bộ nhân viên rảnh
     */
    private void broadcastQueueToStaff() {
        if (availableStaffs.isEmpty()) return;
        
        StringBuilder queueData = new StringBuilder();
        for (ClientHandler c : waitingCustomers) {
            queueData.append(c.getName()).append(",");
        }
        
        // VD: CMD_QUEUE_UPDATE|KH01,KH02,KH03
        String message = Protocol.CMD_QUEUE_UPDATE + Protocol.SEP + queueData.toString();
        for (ClientHandler staff : availableStaffs) {
            staff.sendMessage(message);
        }
    }
}