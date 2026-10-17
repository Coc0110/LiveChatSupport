package com.cmp180.livechat.client.customer.network;

import com.cmp180.livechat.client.customer.model.CustomerPastMessage;
import com.cmp180.livechat.client.customer.model.CustomerPastSession;
import com.cmp180.livechat.common.Message;
import com.cmp180.livechat.common.Protocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class CustomerNetworkService {
    private String serverHost = "127.0.0.1";
    private int serverPort = 5000;
    private String customerName = "Khách hàng";
    private String customerPhone = "";
    private String issueTopic = "Tư vấn chung";
    private String currentSessionId = null;
    private final String customerId = UUID.randomUUID().toString();
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    private final List<CustomerNetworkListener> listeners = new CopyOnWriteArrayList<>();
    
    private volatile boolean isRunning = false;
    private volatile boolean isConnected = false;
    private volatile boolean hasConnectedOnce = false;
    private Thread connectionThread;

    public void addListener(CustomerNetworkListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(CustomerNetworkListener listener) {
        listeners.remove(listener);
    }

    public void setServerAddress(String host, int port) {
        if (host != null && !host.trim().isEmpty()) this.serverHost = host.trim();
        if (port > 0) this.serverPort = port;
    }

    public synchronized void checkPhone(String host, int port, String phone) {
        if (host != null && !host.trim().isEmpty()) this.serverHost = host.trim();
        if (port > 0) this.serverPort = port;
        if (phone != null && !phone.trim().isEmpty()) this.customerPhone = phone.trim();

        isRunning = true;
        new Thread(() -> {
            try {
                ensureConnected();
                sendMessage(new Message(Protocol.CMD_CHECK_PHONE, customerPhone, customerPhone));
            } catch (Exception e) {
                for (CustomerNetworkListener l : listeners) {
                    l.onError("Không thể kết nối đến máy chủ: " + e.getMessage());
                }
            }
        }, "Customer-CheckPhone-Thread").start();
    }
    
    public void connectAndLogin(String customerName) {
        connectAndLogin(this.serverHost, this.serverPort, this.customerPhone, customerName, this.issueTopic);
    }

    public void connectAndLogin(String phone, String customerName, String issueTopic) {
        connectAndLogin(this.serverHost, this.serverPort, phone, customerName, issueTopic);
    }

    public synchronized void connectAndLogin(String host, int port, String phone, String customerName, String issueTopic) {
        if (host != null && !host.trim().isEmpty()) this.serverHost = host.trim();
        if (port > 0) this.serverPort = port;
        if (phone != null && !phone.trim().isEmpty()) this.customerPhone = phone.trim();
        if (customerName != null && !customerName.trim().isEmpty()) this.customerName = customerName.trim();
        if (issueTopic != null && !issueTopic.trim().isEmpty()) this.issueTopic = issueTopic.trim();

        isRunning = true;
        new Thread(() -> {
            try {
                ensureConnected();
                boolean isReconnecting = hasConnectedOnce;
                hasConnectedOnce = true;

                String payload = this.customerName + "|" + this.issueTopic;
                String senderPhone = (customerPhone != null && !customerPhone.isEmpty()) ? customerPhone : customerId;
                sendMessage(new Message(Protocol.CMD_CUST_LOGIN, senderPhone, isReconnecting ? "RECONNECT" : null, payload));

                for (CustomerNetworkListener l : listeners) {
                    l.onConnectedToQueue();
                }
            } catch (Exception e) {
                for (CustomerNetworkListener l : listeners) {
                    l.onError("Không thể kết nối máy chủ: " + e.getMessage());
                }
            }
        }, "Customer-Login-Thread").start();
    }

    private synchronized void ensureConnected() throws IOException {
        if (isConnected && socket != null && !socket.isClosed()) {
            return;
        }

        Socket s = new Socket();
        s.connect(new InetSocketAddress(this.serverHost, this.serverPort), 3000);
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
        oos.flush();
        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

        this.socket = s;
        this.out = oos;
        this.in = ois;
        this.isConnected = true;

        if (connectionThread == null || !connectionThread.isAlive()) {
            connectionThread = new Thread(this::readLoop, "Customer-Read-Thread");
            connectionThread.setDaemon(true);
            connectionThread.start();
        }
    }
    
    private void readLoop() {
        try {
            Message msg;
            while (isRunning && (msg = (Message) in.readObject()) != null) {
                String cmd = msg.getCommand();
                
                switch (cmd) {
                    case Protocol.CMD_PHONE_RESULT:
                        boolean isExisting = "EXISTING".equalsIgnoreCase(msg.getSenderId());
                        String custName = msg.getReceiverId();
                        List<CustomerPastSession> sessions = parsePastSessions(msg.getContent());
                        for (CustomerNetworkListener l : listeners) {
                            l.onPhoneChecked(isExisting, custName, sessions);
                        }
                        break;

                    case Protocol.CMD_SESSION_MSGS:
                        String sessId = msg.getSenderId();
                        List<CustomerPastMessage> msgsList = parsePastMessages(msg.getContent());
                        for (CustomerNetworkListener l : listeners) {
                            l.onSessionMessagesLoaded(sessId, msgsList);
                        }
                        break;

                    case Protocol.CMD_WAITING:
                        for (CustomerNetworkListener l : listeners) l.onReturnedToQueue(msg.getContent());
                        break;

                    case Protocol.CMD_PAIRED:
                        this.currentSessionId = msg.getMessageId();
                        for (CustomerNetworkListener l : listeners) l.onPairedWithStaff(msg.getSenderId(), msg.getReceiverId());
                        break;
                        
                    case Protocol.CMD_CHAT:
                        String decryptedContent = com.cmp180.livechat.common.SecurityManager.decrypt(msg.getContent());
                        for (CustomerNetworkListener l : listeners) {
                            l.onMessageReceived(msg.getSenderId(), decryptedContent, msg.getMessageId(), msg.getReplyToSender(), msg.getReplyToText());
                        }
                        break;

                    case Protocol.CMD_FILE:
                        String decCaption = (msg.getContent() != null && !msg.getContent().isEmpty()) 
                                ? com.cmp180.livechat.common.SecurityManager.decrypt(msg.getContent()) 
                                : "";
                        byte[] decData = com.cmp180.livechat.common.SecurityManager.decryptBytes(msg.getFileData());
                        for (CustomerNetworkListener l : listeners) {
                            l.onFileReceived(msg.getSenderId(), msg.getMessageId(), decCaption, msg.getReplyToSender(), msg.getReplyToText(), msg.getFileName(), msg.getFileSize(), decData, msg.isImage());
                        }
                        break;

                    case Protocol.CMD_RECALL_MSG:
                        for (CustomerNetworkListener l : listeners) {
                            l.onMessageRecalled(msg.getMessageId());
                        }
                        break;
                        
                    case Protocol.CMD_END:
                        if (msg.getMessageId() != null && !msg.getMessageId().isEmpty()) {
                            this.currentSessionId = msg.getMessageId();
                        }
                        for (CustomerNetworkListener l : listeners) l.onSessionEnded(msg.getContent());
                        break;
                        
                    case Protocol.CMD_ERROR:
                        for (CustomerNetworkListener l : listeners) l.onError(msg.getContent());
                        break;
                }
            }
        } catch (Exception e) {
            synchronized (this) {
                isConnected = false;
                closeSocketSilently();
            }
            if (isRunning) {
                for (CustomerNetworkListener l : listeners) {
                    l.onError("Mất kết nối máy chủ. Vui lòng thử lại!");
                }
            }
        }
    }

    private List<CustomerPastSession> parsePastSessions(String data) {
        List<CustomerPastSession> list = new ArrayList<>();
        if (data == null || data.isEmpty()) return list;
        String[] items = data.split(";");
        for (String item : items) {
            if (item.trim().isEmpty()) continue;
            String[] parts = item.split("\\|");
            if (parts.length >= 7) {
                try {
                    String id = parts[0];
                    String topic = parts[1];
                    String staff = parts[2];
                    String status = parts[3];
                    int rating = Integer.parseInt(parts[4]);
                    String feedback = parts[5];
                    long created = Long.parseLong(parts[6]);
                    list.add(new CustomerPastSession(id, topic, staff, status, rating, feedback, created));
                } catch (Exception ignored) {}
            }
        }
        return list;
    }

    private List<CustomerPastMessage> parsePastMessages(String data) {
        List<CustomerPastMessage> list = new ArrayList<>();
        if (data == null || data.isEmpty()) return list;
        String[] items = data.split(";");
        for (String item : items) {
            if (item.trim().isEmpty()) continue;
            String[] parts = item.split("\\|");
            if (parts.length >= 6) {
                try {
                    String role = parts[0];
                    String name = parts[1];
                    String rawContent = parts[2].replace("{SEMICOLON}", ";").replace("{PIPE}", "|");
                    String content = com.cmp180.livechat.common.SecurityManager.decrypt(rawContent);
                    long ts = Long.parseLong(parts[3]);
                    boolean recalled = "1".equals(parts[4]);
                    String fName = parts[5];
                    list.add(new CustomerPastMessage(role, name, content, ts, recalled, fName));
                } catch (Exception ignored) {}
            }
        }
        return list;
    }
    
    public void requestSessionMessages(String sessionId) {
        if (!isConnected || sessionId == null) return;
        sendMessage(new Message(Protocol.CMD_GET_SESSION_MSGS, customerPhone, sessionId));
    }

    public void sendChatMessage(String text, String staffId) {
        sendChatMessage(text, staffId, UUID.randomUUID().toString(), null, null);
    }

    public void sendChatMessage(String text, String staffId, String messageId, String replyToSender, String replyToText) {
        if (!isConnected) return;
        String encryptedContent = com.cmp180.livechat.common.SecurityManager.encrypt(text);
        sendMessage(new Message(Protocol.CMD_CHAT, "Customer", staffId, encryptedContent, messageId, replyToSender, replyToText));
    }

    public void sendFileMessage(String fileName, long fileSize, byte[] fileData, boolean isImage, String caption, String staffId, String messageId, String replyToSender, String replyToText) {
        if (!isConnected) return;
        String encCaption = (caption != null && !caption.isEmpty()) ? com.cmp180.livechat.common.SecurityManager.encrypt(caption) : "";
        byte[] encData = com.cmp180.livechat.common.SecurityManager.encryptBytes(fileData);
        sendMessage(new Message(Protocol.CMD_FILE, "Customer", staffId, encCaption, messageId, replyToSender, replyToText, fileName, fileSize, encData, isImage));
    }

    public void recallMessage(String messageId, String staffId) {
        if (!isConnected) return;
        sendMessage(new Message(Protocol.CMD_RECALL_MSG, "Customer", staffId, "", messageId, null, null));
    }
    
    public void endSession(String staffId) {
        if (!isConnected) return;
        sendMessage(new Message(Protocol.CMD_END_SESSION, "Customer", staffId, "Khách hàng đã đóng phiên."));
    }
    
    private synchronized void sendMessage(Message msg) {
        try {
            if (out != null && isConnected) {
                out.writeObject(msg);
                out.flush();
            }
        } catch (IOException e) {
            isConnected = false;
            closeSocketSilently();
        }
    }

    private synchronized void closeSocketSilently() {
        try { if (in != null) in.close(); } catch (Exception ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (Exception ignored) {}
        in = null;
        out = null;
        socket = null;
    }

    public synchronized void disconnect() {
        isRunning = false;
        isConnected = false;
        closeSocketSilently();
        if (connectionThread != null) {
            connectionThread.interrupt();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String phone) {
        if (phone != null) this.customerPhone = phone.trim();
    }

    public String getCurrentSessionId() {
        return currentSessionId;
    }

    public void sendRating(int stars, String feedback) {
        if (!isConnected || currentSessionId == null) return;
        String payload = stars + "|" + (feedback != null ? feedback : "");
        sendMessage(new Message(Protocol.CMD_RATING, customerPhone, currentSessionId, payload));
    }
}
