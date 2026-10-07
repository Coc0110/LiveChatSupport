package com.cmp180.livechat.client.staff.network;

import com.cmp180.livechat.common.Message;
import com.cmp180.livechat.common.Protocol;
import com.cmp180.livechat.client.staff.model.CustomerItem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
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
    
    public void connectAndLogin(String staffIdOrName) {
        new Thread(() -> {
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                
                sendMessage(new Message(Protocol.CMD_STAFF_LOGIN, "Staff", staffIdOrName));
                
                for (StaffNetworkListener l : listeners) l.onLoginSuccess();
                
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
                        List<CustomerItem> queue = parseQueueData(msg.getContent());
                        for (StaffNetworkListener l : listeners) l.onQueueUpdated(queue);
                        break;
                        
                    case Protocol.CMD_PAIRED:
                        for (StaffNetworkListener l : listeners) l.onPairedWithCustomer(msg.getSenderId(), msg.getReceiverId());
                        break;
                        
                    case Protocol.CMD_CHAT:
                        for (StaffNetworkListener l : listeners) l.onMessageReceived(msg.getSenderId(), msg.getReceiverId(), msg.getContent());
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
    
    public void requestQueueUpdate() {
        sendMessage(new Message(Protocol.CMD_GET_QUEUE, "Staff", ""));
    }
    
    public void acceptCustomer(String customerId) {
        sendMessage(new Message(Protocol.CMD_ACCEPT_CUST, "Staff", null, customerId));
    }
    
    public void sendChatMessage(String text, String customerId) {
        sendMessage(new Message(Protocol.CMD_CHAT, "Staff", customerId, text));
    }
    
    public void endSession(String customerId) {
        sendMessage(new Message(Protocol.CMD_END_SESSION, "Staff", customerId, "Nhân viên đã đóng phiên."));
    }
    
    private void sendMessage(Message msg) {
        try {
            if (out != null) {
                out.writeObject(msg);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<CustomerItem> parseQueueData(String data) {
        List<CustomerItem> list = new ArrayList<>();
        if (data == null || data.isEmpty()) return list;
        
        String[] parts = data.split(";");
        for (String p : parts) {
            String[] kv = p.split("\\|"); // pipe needs escaping in regex
            if (kv.length >= 4) {
                String id = kv[0];
                String name = kv[1];
                boolean isActive = "ACTIVE".equals(kv[2]);
                long joinedTime = Long.parseLong(kv[3]);
                list.add(new CustomerItem(id, name, isActive, joinedTime));
            }
        }
        return list;
    }
}
