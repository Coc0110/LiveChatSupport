package com.cmp180.livechat.server;

import com.cmp180.livechat.common.ChatSession;
import com.cmp180.livechat.common.Protocol;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Server chính lắng nghe kết nối TCP và điều phối ghép phiên Chat 1-1.
 */
public class ServerMain {
    private static final int PORT = 5000;
    private final Queue<ClientHandler> waitingCustomers = new ConcurrentLinkedQueue<>();
    private final Queue<ClientHandler> availableStaffs = new ConcurrentLinkedQueue<>();
    private ServerGUI gui;
    public ServerMain() {
    }
    public ServerMain(ServerGUI gui) {
        this.gui = gui;
    }

    public void start() {
        log("==================================================");
        log("   HỆ THỐNG LIVE CHAT SUPPORT SERVER (PORT " + PORT + ")");
        log("==================================================");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                log("[KẾT NỐI MỚI] Địa chỉ: " + clientSocket.getRemoteSocketAddress());

                // Mở 1 Thread riêng cho mỗi Client kết nối (Bài 5 Multithreading)
                ClientHandler handler = new ClientHandler(clientSocket, this);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Lỗi Server: " + e.getMessage());
        }
    }

    /**
     * Đăng ký kết nối của Client và tiến hành ghép phiên nếu đủ điều kiện.
     */
    public synchronized void registerClient(ClientHandler client) {
        if (Protocol.ROLE_CUSTOMER.equalsIgnoreCase(client.getRole())) {
            if (!availableStaffs.isEmpty()) {
                ClientHandler staff = availableStaffs.poll();
                createSession(client, staff);
            } else {
                waitingCustomers.add(client);
                client.sendMessage(Protocol.CMD_WAITING + Protocol.SEP + "Đang chờ kết nối với nhân viên CSKH...");
                log("[HÀNG CHỜ] Khách hàng '" + client.getName() + "' đã vào hàng chờ.");
            }
        } else if (Protocol.ROLE_STAFF.equalsIgnoreCase(client.getRole())) {
            if (!waitingCustomers.isEmpty()) {
                ClientHandler customer = waitingCustomers.poll();
                createSession(customer, client);
            } else {
                availableStaffs.add(client);
                client.sendMessage(Protocol.CMD_WAITING + Protocol.SEP + "Đang ở trạng thái SẴN SÀNG nhận yêu cầu từ khách...");
                log("[CSKH RẢNH] Nhân viên '" + client.getName() + "' đang sẵn sàng.");
            }
        }
    }

    /**
     * Tạo một phiên Chat 1-1 giữa Khách hàng và CSKH.
     */
    private void createSession(ClientHandler customer, ClientHandler staff) {
        ChatSession session = new ChatSession(customer, staff);
        customer.sendMessage(Protocol.CMD_PAIRED + Protocol.SEP + "Đã kết nối với CSKH: " + staff.getName());
        staff.sendMessage(Protocol.CMD_PAIRED + Protocol.SEP + "Đang hỗ trợ khách hàng: " + customer.getName());
        log("-> [GHẾP PHIÊN THÀNH CÔNG] " + customer.getName() + " <---> " + staff.getName());
    }

    /**
     * Đóng phiên chat và thông báo lý do.
     */
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
            // Nhân viên tự động quay lại trạng thái Sẵn sàng nhận khách mới
            registerClient(staff);
        }
    }

    /**
     * Xử lý khi Client ngắt kết nối ngột ngột.
     */
    public synchronized void handleDisconnect(ClientHandler client) {
        waitingCustomers.remove(client);
        availableStaffs.remove(client);
        if (client.getSession() != null) {
            closeSession(client.getSession(), client.getName() + " đã ngắt kết nối.");
        }
    }

    private void log(String msg) {
        // 1. Luôn in ra màn hình Console
        log(msg); 
        
        // 2. Nếu đang chạy bằng GUI thì mới in lên GUI
        if (gui != null) {
            gui.log(msg);
        }
    }

    public static void main(String[] args) {
        new ServerMain().start();
    }
}
