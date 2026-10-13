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
    private String serverHost = "127.0.0.1";
    private int serverPort = 5000;
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    private final List<StaffNetworkListener> listeners = new CopyOnWriteArrayList<>();
    
    public void addListener(StaffNetworkListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void setServerAddress(String host, int port) {
        if (host != null && !host.trim().isEmpty()) this.serverHost = host.trim();
        if (port > 0) this.serverPort = port;
    }
    
    public void connectAndLogin(String staffIdOrName) {
        connectAndLogin(this.serverHost, this.serverPort, staffIdOrName);
    }

    public void connectAndLogin(String host, int port, String staffIdOrName) {
        if (host != null && !host.trim().isEmpty()) this.serverHost = host.trim();
        if (port > 0) this.serverPort = port;

        new Thread(() -> {
            try {
                socket = new Socket(this.serverHost, this.serverPort);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                
                sendMessage(new Message(Protocol.CMD_STAFF_LOGIN, "Staff", staffIdOrName));
                
                for (StaffNetworkListener l : listeners) l.onLoginSuccess();
                
                readLoop();
                
            } catch (IOException e) {
                for (StaffNetworkListener l : listeners) l.onError("Không thể kết nối Server (" + this.serverHost + ":" + this.serverPort + "): " + e.getMessage());
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

                    case Protocol.CMD_CHAT_HISTORY:
                        String decryptedHistory = com.cmp180.livechat.common.SecurityManager.decrypt(msg.getContent());
                        String meta = msg.getSenderId();
                        String role = "CUSTOMER";
                        String senderName = "Khách hàng";
                        long ts = System.currentTimeMillis();
                        boolean isRecalled = false;
                        if (meta != null) {
                            String[] parts = meta.split("\\|");
                            if (parts.length >= 1) role = parts[0];
                            if (parts.length >= 2) senderName = parts[1];
                            if (parts.length >= 3) {
                                try { ts = Long.parseLong(parts[2]); } catch (NumberFormatException ignored) {}
                            }
                            if (parts.length >= 4 && "RECALLED".equals(parts[3])) {
                                isRecalled = true;
                            }
                        }
                        for (StaffNetworkListener l : listeners) {
                            l.onChatHistoryReceived(msg.getReceiverId(), role, senderName, ts, decryptedHistory, msg.getMessageId(), msg.getReplyToSender(), msg.getReplyToText(), isRecalled);
                        }
                        break;
                        
                    case Protocol.CMD_CHAT:
                        String decryptedContent = com.cmp180.livechat.common.SecurityManager.decrypt(msg.getContent());
                        for (StaffNetworkListener l : listeners) {
                            l.onMessageReceived(msg.getSenderId(), msg.getReceiverId(), decryptedContent, msg.getMessageId(), msg.getReplyToSender(), msg.getReplyToText());
                        }
                        break;

                    case Protocol.CMD_RECALL_MSG:
                        for (StaffNetworkListener l : listeners) {
                            l.onMessageRecalled(msg.getMessageId());
                        }
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
        sendChatMessage(text, customerId, java.util.UUID.randomUUID().toString(), null, null);
    }

    public void sendChatMessage(String text, String customerId, String messageId, String replyToSender, String replyToText) {
        String encryptedContent = com.cmp180.livechat.common.SecurityManager.encrypt(text);
        sendMessage(new Message(Protocol.CMD_CHAT, "Staff", customerId, encryptedContent, messageId, replyToSender, replyToText));
    }

    public void recallMessage(String messageId, String customerId) {
        sendMessage(new Message(Protocol.CMD_RECALL_MSG, "Staff", customerId, "", messageId, null, null));
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
                boolean isInterrupted = Protocol.STATUS_INTERRUPTED.equals(kv[2]);
                long joinedTime = Long.parseLong(kv[3]);
                list.add(new CustomerItem(id, name, isActive, isInterrupted, joinedTime));
            }
        }
        return list;
    }
}
