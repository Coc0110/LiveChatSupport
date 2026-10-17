package com.cmp180.livechat.client.staff.network;

import com.cmp180.livechat.common.Message;
import com.cmp180.livechat.common.Protocol;
import com.cmp180.livechat.client.staff.model.CustomerItem;
import com.cmp180.livechat.client.staff.model.OnlineStaffItem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class StaffNetworkService {
    private String serverHost = "127.0.0.1";
    private int serverPort = 5000;
    private String staffName = "Staff";
    private String staffPassword = "";
    private String staffId = UUID.randomUUID().toString();
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    private final List<StaffNetworkListener> listeners = new CopyOnWriteArrayList<>();

    private volatile boolean isRunning = false;
    private volatile boolean isConnected = false;
    private volatile boolean hasConnectedOnce = false;
    private Thread connectionThread;

    public void addListener(StaffNetworkListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(StaffNetworkListener listener) {
        listeners.remove(listener);
    }

    public synchronized void setServerAddress(String host, int port) {
        boolean changed = false;
        if (host != null && !host.trim().isEmpty() && !host.trim().equals(this.serverHost)) {
            this.serverHost = host.trim();
            changed = true;
        }
        if (port > 0 && port != this.serverPort) {
            this.serverPort = port;
            changed = true;
        }
        if (changed && isConnected) {
            closeSocketSilently();
            isConnected = false;
            if (connectionThread != null && connectionThread.isAlive()) {
                connectionThread.interrupt();
            }
        }
    }
    
    public void connectAndLogin(String staffIdOrName) {
        connectAndLogin(this.serverHost, this.serverPort, staffIdOrName, "");
    }

    public void connectAndLogin(String username, String password) {
        connectAndLogin(this.serverHost, this.serverPort, username, password);
    }

    public synchronized void connectAndLogin(String host, int port, String username, String password) {
        if (host != null && !host.trim().isEmpty()) {
            if (!host.trim().equals(this.serverHost)) {
                this.serverHost = host.trim();
                closeSocketSilently();
                isConnected = false;
            }
        }
        if (port > 0) {
            if (port != this.serverPort) {
                this.serverPort = port;
                closeSocketSilently();
                isConnected = false;
            }
        }
        if (username != null && !username.trim().isEmpty()) this.staffName = username.trim();
        this.staffPassword = (password != null) ? password.trim() : "";

        isRunning = true;
        if (isConnected && socket != null && !socket.isClosed() && out != null) {
            new Thread(() -> {
                sendMessage(new Message(Protocol.CMD_STAFF_LOGIN, this.staffName, this.staffPassword));
            }, "Staff-Login-Send-Thread").start();
        } else if (connectionThread == null || !connectionThread.isAlive()) {
            connectionThread = new Thread(this::connectionLoop, "Staff-Network-Thread");
            connectionThread.setDaemon(true);
            connectionThread.start();
        } else {
            connectionThread.interrupt();
        }
    }

    private void connectionLoop() {
        while (isRunning) {
            Socket s = null;
            boolean successfullyConnected = false;
            try {
                s = new Socket();
                s.connect(new InetSocketAddress(this.serverHost, this.serverPort), 3000);

                ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

                synchronized (this) {
                    this.socket = s;
                    this.out = oos;
                    this.in = ois;
                    this.isConnected = true;
                    successfullyConnected = true;
                }

                hasConnectedOnce = true;

                // Gửi đăng nhập kèm username và password
                sendMessage(new Message(Protocol.CMD_STAFF_LOGIN, staffName, staffPassword));

                readLoop();

            } catch (Exception e) {
                boolean hadConnected = successfullyConnected;
                synchronized (this) {
                    isConnected = false;
                    closeSocketSilently();
                }

                if (!isRunning) break;

                String msg = hadConnected
                        ? "Mất kết nối máy chủ. Đang tự động kết nối lại..."
                        : "Chưa kết nối được máy chủ (" + this.serverHost + ":" + this.serverPort + "). Đang thử lại...";

                for (StaffNetworkListener l : listeners) {
                    l.onError(msg);
                }

                try {
                    Thread.sleep(2500);
                } catch (InterruptedException ie) {
                    if (!isRunning) break;
                }
            }
        }
    }
    
    private void readLoop() throws IOException, ClassNotFoundException {
        Message msg;
        while ((msg = (Message) in.readObject()) != null) {
            String cmd = msg.getCommand();
            
            switch (cmd) {
                case Protocol.CMD_LOGIN_SUCCESS:
                    this.staffId = msg.getSenderId();
                    this.staffName = msg.getContent();
                    for (StaffNetworkListener l : listeners) l.onLoginSuccess();
                    break;

                case Protocol.CMD_QUEUE_UPDATE:
                    List<CustomerItem> queue = parseQueueData(msg.getContent());
                    for (StaffNetworkListener l : listeners) l.onQueueUpdated(queue);
                    break;
                    
                case Protocol.CMD_PAIRED:
                    for (StaffNetworkListener l : listeners) l.onPairedWithCustomer(msg.getSenderId(), msg.getReceiverId());
                    break;

                case Protocol.CMD_CHAT_HISTORY:
                    String decryptedHistory = com.cmp180.livechat.common.SecurityManager.decrypt(msg.getContent());
                    byte[] decHistData = com.cmp180.livechat.common.SecurityManager.decryptBytes(msg.getFileData());
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
                        l.onChatHistoryReceived(msg.getReceiverId(), role, senderName, ts, decryptedHistory, msg.getMessageId(), msg.getReplyToSender(), msg.getReplyToText(), isRecalled, msg.getFileName(), msg.getFileSize(), decHistData, msg.isImage());
                    }
                    break;
                    
                case Protocol.CMD_CHAT:
                    String decryptedContent = com.cmp180.livechat.common.SecurityManager.decrypt(msg.getContent());
                    for (StaffNetworkListener l : listeners) {
                        l.onMessageReceived(msg.getSenderId(), msg.getReceiverId(), decryptedContent, msg.getMessageId(), msg.getReplyToSender(), msg.getReplyToText());
                    }
                    break;

                case Protocol.CMD_FILE:
                    String decFileCaption = (msg.getContent() != null && !msg.getContent().isEmpty()) 
                            ? com.cmp180.livechat.common.SecurityManager.decrypt(msg.getContent()) 
                            : "";
                    byte[] decStaffData = com.cmp180.livechat.common.SecurityManager.decryptBytes(msg.getFileData());
                    for (StaffNetworkListener l : listeners) {
                        l.onFileReceived(msg.getSenderId(), msg.getReceiverId(), msg.getMessageId(), decFileCaption, msg.getReplyToSender(), msg.getReplyToText(), msg.getFileName(), msg.getFileSize(), decStaffData, msg.isImage());
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

                case Protocol.CMD_ONLINE_STAFF_LIST:
                    List<OnlineStaffItem> onlineStaff = parseOnlineStaffData(msg.getContent());
                    for (StaffNetworkListener l : listeners) {
                        l.onOnlineStaffListReceived(onlineStaff);
                    }
                    break;
                    
                case Protocol.CMD_ERROR:
                    for (StaffNetworkListener l : listeners) l.onError(msg.getContent());
                    break;
            }
        }
    }
    
    public void requestQueueUpdate() {
        if (!isConnected) return;
        sendMessage(new Message(Protocol.CMD_GET_QUEUE, "Staff", ""));
    }
    
    public void acceptCustomer(String customerId) {
        if (!isConnected) return;
        sendMessage(new Message(Protocol.CMD_ACCEPT_CUST, "Staff", null, customerId));
    }
    
    public void sendChatMessage(String text, String customerId) {
        sendChatMessage(text, customerId, UUID.randomUUID().toString(), null, null);
    }

    public void sendChatMessage(String text, String customerId, String messageId, String replyToSender, String replyToText) {
        if (!isConnected) return;
        String encryptedContent = com.cmp180.livechat.common.SecurityManager.encrypt(text);
        sendMessage(new Message(Protocol.CMD_CHAT, "Staff", customerId, encryptedContent, messageId, replyToSender, replyToText));
    }

    public void sendFileMessage(String fileName, long fileSize, byte[] fileData, boolean isImage, String caption, String customerId, String messageId, String replyToSender, String replyToText) {
        if (!isConnected) return;
        String encCaption = (caption != null && !caption.isEmpty()) ? com.cmp180.livechat.common.SecurityManager.encrypt(caption) : "";
        byte[] encData = com.cmp180.livechat.common.SecurityManager.encryptBytes(fileData);
        sendMessage(new Message(Protocol.CMD_FILE, "Staff", customerId, encCaption, messageId, replyToSender, replyToText, fileName, fileSize, encData, isImage));
    }

    public void recallMessage(String messageId, String customerId) {
        if (!isConnected) return;
        sendMessage(new Message(Protocol.CMD_RECALL_MSG, "Staff", customerId, "", messageId, null, null));
    }
    
    public void endSession(String customerId) {
        if (!isConnected) return;
        sendMessage(new Message(Protocol.CMD_END_SESSION, "Staff", customerId, "Nhân viên đã đóng phiên."));
    }

    public void requestOnlineStaffList() {
        if (!isConnected) return;
        sendMessage(new Message(Protocol.CMD_GET_ONLINE_STAFF, "Staff", ""));
    }

    public void transferSession(String customerId, String targetStaffId, String note) {
        if (!isConnected) return;
        String payload = targetStaffId + "|" + (note != null ? note.trim() : "");
        sendMessage(new Message(Protocol.CMD_TRANSFER_SESSION, this.staffId, customerId, payload));
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

    public String getStaffId() {
        return staffId;
    }

    private List<CustomerItem> parseQueueData(String data) {
        List<CustomerItem> list = new ArrayList<>();
        if (data == null || data.isEmpty()) return list;
        
        String[] parts = data.split(";");
        for (String p : parts) {
            String[] kv = p.split("\\|");
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

    private List<OnlineStaffItem> parseOnlineStaffData(String data) {
        List<OnlineStaffItem> list = new ArrayList<>();
        if (data == null || data.trim().isEmpty()) return list;

        String[] parts = data.split(";");
        for (String p : parts) {
            if (p.trim().isEmpty()) continue;
            String[] kv = p.split("\\|");
            if (kv.length >= 4) {
                String id = kv[0];
                String name = kv[1];
                String skill = kv[2];
                int activeCount = 0;
                try {
                    activeCount = Integer.parseInt(kv[3]);
                } catch (Exception ignored) {}
                list.add(new OnlineStaffItem(id, name, skill, activeCount));
            }
        }
        return list;
    }
}
