package com.cmp180.livechat.client.staff.network;

import com.cmp180.livechat.common.Message;
import com.cmp180.livechat.common.Protocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class StaffNetworkService {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 5000;
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    private final List<StaffNetworkListener> listeners = new CopyOnWriteArrayList<>();
    
    public void addListener(StaffNetworkListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(StaffNetworkListener listener) {
        listeners.remove(listener);
    }
    
    public void connectAndLogin(String staffIdOrName) {
        new Thread(() -> {
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                
                // Gửi lệnh đăng nhập
                sendMessage(new Message(Protocol.CMD_STAFF_LOGIN, "Staff", staffIdOrName));
                
                // Báo UI login thành công
                for (StaffNetworkListener l : listeners) l.onLoginSuccess();
                
                // Bắt đầu vòng lặp đọc dữ liệu từ server
                readLoop();
                
            } catch (IOException e) {
                for (StaffNetworkListener l : listeners) l.onError("Không thể kết nối Server: " + e.getMessage());
            }
        }).start();
    }
    
    private void readLoop() {
        try {
            Message msg;
            while ((msg = (Message) in.readObject()) != null) {
                String cmd = msg.getCommand();
                
                switch (cmd) {
                    case Protocol.CMD_QUEUE_UPDATE:
                        Map<String, String> queue = parseQueueData(msg.getContent());
                        for (StaffNetworkListener l : listeners) l.onQueueUpdated(queue);
                        break;
                        
                    case Protocol.CMD_PAIRED:
                        // Nội dung từ server gửi về chứa thông báo đã kết nối
                        for (StaffNetworkListener l : listeners) l.onPairedWithCustomer(msg.getContent());
                        break;
                        
                    case Protocol.CMD_CHAT:
                        for (StaffNetworkListener l : listeners) l.onMessageReceived(msg.getSenderId(), msg.getContent());
                        break;
                        
                    case Protocol.CMD_END:
                        for (StaffNetworkListener l : listeners) l.onSessionEnded(msg.getContent());
                        break;
                        
                    case Protocol.CMD_ERROR:
                        for (StaffNetworkListener l : listeners) l.onError(msg.getContent());
                        break;
                }
            }
        } catch (Exception e) {
            for (StaffNetworkListener l : listeners) l.onError("Mất kết nối từ Server: " + e.getMessage());
        }
    }
    
    // Parse chuỗi "ID1:Tên1;ID2:Tên2;" thành Map<UUID, Name>
    private Map<String, String> parseQueueData(String data) {
        Map<String, String> map = new HashMap<>();
        if (data == null || data.isEmpty()) return map;
        String[] entries = data.split(";");
        for (String entry : entries) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                map.put(parts[0], parts[1]);
            }
        }
        return map;
    }
    
    public void acceptCustomer(String customerId) {
        sendMessage(new Message(Protocol.CMD_ACCEPT_CUST, "Staff", customerId));
    }
    
    public void sendChatMessage(String text) {
        sendMessage(new Message(Protocol.CMD_CHAT, "Staff", text));
    }
    
    public void endSession() {
        sendMessage(new Message(Protocol.CMD_END_SESSION, "Staff", "Nhân viên đã đóng phiên."));
    }
    
    private void sendMessage(Message msg) {
        try {
            if (out != null) {
                out.writeObject(msg);
                out.flush(); // Luôn flush sau khi gửi Object
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
