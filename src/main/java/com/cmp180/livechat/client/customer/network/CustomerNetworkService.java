package com.cmp180.livechat.client.customer.network;

import com.cmp180.livechat.common.Message;
import com.cmp180.livechat.common.Protocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CustomerNetworkService {
    private String serverHost = "127.0.0.1";
    private int serverPort = 5000;
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    private final List<CustomerNetworkListener> listeners = new CopyOnWriteArrayList<>();
    
    public void addListener(CustomerNetworkListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void setServerAddress(String host, int port) {
        if (host != null && !host.trim().isEmpty()) this.serverHost = host.trim();
        if (port > 0) this.serverPort = port;
    }
    
    public void connectAndLogin(String customerName) {
        connectAndLogin(this.serverHost, this.serverPort, customerName);
    }

    public void connectAndLogin(String host, int port, String customerName) {
        if (host != null && !host.trim().isEmpty()) this.serverHost = host.trim();
        if (port > 0) this.serverPort = port;

        new Thread(() -> {
            try {
                socket = new Socket(this.serverHost, this.serverPort);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                
                // Gửi lệnh đăng nhập
                sendMessage(new Message(Protocol.CMD_CUST_LOGIN, "Customer", customerName));
                
                for (CustomerNetworkListener l : listeners) l.onConnectedToQueue();
                
                readLoop();
                
            } catch (IOException e) {
                for (CustomerNetworkListener l : listeners) l.onError("Không thể kết nối Server (" + this.serverHost + ":" + this.serverPort + "): " + e.getMessage());
            }
        }).start();
    }
    
    private void readLoop() {
        try {
            Message msg;
            while ((msg = (Message) in.readObject()) != null) {
                String cmd = msg.getCommand();
                
                switch (cmd) {
                    case Protocol.CMD_WAITING:
                        for (CustomerNetworkListener l : listeners) l.onReturnedToQueue(msg.getContent());
                        break;

                    case Protocol.CMD_PAIRED:
                        for (CustomerNetworkListener l : listeners) l.onPairedWithStaff(msg.getSenderId(), msg.getReceiverId());
                        break;
                        
                    case Protocol.CMD_CHAT:
                        String decryptedContent = com.cmp180.livechat.common.SecurityManager.decrypt(msg.getContent());
                        for (CustomerNetworkListener l : listeners) {
                            l.onMessageReceived(msg.getSenderId(), decryptedContent, msg.getMessageId(), msg.getReplyToSender(), msg.getReplyToText());
                        }
                        break;

                    case Protocol.CMD_RECALL_MSG:
                        for (CustomerNetworkListener l : listeners) {
                            l.onMessageRecalled(msg.getMessageId());
                        }
                        break;
                        
                    case Protocol.CMD_END:
                        for (CustomerNetworkListener l : listeners) l.onSessionEnded(msg.getContent());
                        break;
                        
                    case Protocol.CMD_ERROR:
                        for (CustomerNetworkListener l : listeners) l.onError(msg.getContent());
                        break;
                }
            }
        } catch (Exception e) {
            for (CustomerNetworkListener l : listeners) l.onError("Mất kết nối từ Server: " + e.getMessage());
        }
    }
    
    public void sendChatMessage(String text, String staffId) {
        sendChatMessage(text, staffId, java.util.UUID.randomUUID().toString(), null, null);
    }

    public void sendChatMessage(String text, String staffId, String messageId, String replyToSender, String replyToText) {
        String encryptedContent = com.cmp180.livechat.common.SecurityManager.encrypt(text);
        sendMessage(new Message(Protocol.CMD_CHAT, "Customer", staffId, encryptedContent, messageId, replyToSender, replyToText));
    }

    public void recallMessage(String messageId, String staffId) {
        sendMessage(new Message(Protocol.CMD_RECALL_MSG, "Customer", staffId, "", messageId, null, null));
    }
    
    public void endSession(String staffId) {
        sendMessage(new Message(Protocol.CMD_END_SESSION, "Customer", staffId, "Khách hàng đã đóng phiên."));
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
}
