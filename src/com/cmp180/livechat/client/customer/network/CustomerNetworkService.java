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
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 5000;
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    private final List<CustomerNetworkListener> listeners = new CopyOnWriteArrayList<>();
    
    public void addListener(CustomerNetworkListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void connectAndLogin(String customerName) {
        new Thread(() -> {
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                
                // Gửi lệnh đăng nhập
                sendMessage(new Message(Protocol.CMD_CUST_LOGIN, "Customer", customerName));
                
                for (CustomerNetworkListener l : listeners) l.onConnectedToQueue();
                
                readLoop();
                
            } catch (IOException e) {
                for (CustomerNetworkListener l : listeners) l.onError("Không thể kết nối Server: " + e.getMessage());
            }
        }).start();
    }
    
    private void readLoop() {
        try {
            Message msg;
            while ((msg = (Message) in.readObject()) != null) {
                String cmd = msg.getCommand();
                
                switch (cmd) {
                    case Protocol.CMD_PAIRED:
                        for (CustomerNetworkListener l : listeners) l.onPairedWithStaff(msg.getSenderId(), msg.getReceiverId());
                        break;
                        
                    case Protocol.CMD_CHAT:
                        for (CustomerNetworkListener l : listeners) l.onMessageReceived(msg.getSenderId(), msg.getContent());
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
        sendMessage(new Message(Protocol.CMD_CHAT, "Customer", staffId, text));
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
