package com.cmp180.livechat.client.customer.network;

import com.cmp180.livechat.common.Message;
import com.cmp180.livechat.common.Protocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class CustomerNetworkService {
    private String serverHost = "127.0.0.1";
    private int serverPort = 5000;
    private String customerName = "Khách hàng";
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

    public void setServerAddress(String host, int port) {
        if (host != null && !host.trim().isEmpty()) this.serverHost = host.trim();
        if (port > 0) this.serverPort = port;
    }
    
    public void connectAndLogin(String customerName) {
        connectAndLogin(this.serverHost, this.serverPort, customerName);
    }

    public synchronized void connectAndLogin(String host, int port, String customerName) {
        if (host != null && !host.trim().isEmpty()) this.serverHost = host.trim();
        if (port > 0) this.serverPort = port;
        if (customerName != null && !customerName.trim().isEmpty()) this.customerName = customerName.trim();

        isRunning = true;
        if (connectionThread == null || !connectionThread.isAlive()) {
            connectionThread = new Thread(this::connectionLoop, "Customer-Network-Thread");
            connectionThread.setDaemon(true);
            connectionThread.start();
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

                boolean isReconnecting = hasConnectedOnce;
                hasConnectedOnce = true;

                // Gửi lệnh đăng nhập kèm customerId và cờ RECONNECT nếu đã từng kết nối
                sendMessage(new Message(Protocol.CMD_CUST_LOGIN, customerId, isReconnecting ? "RECONNECT" : null, customerName));

                for (CustomerNetworkListener l : listeners) {
                    l.onConnectedToQueue();
                }

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

                for (CustomerNetworkListener l : listeners) {
                    l.onError(msg);
                }

                try {
                    Thread.sleep(2500);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }
    }
    
    private void readLoop() throws IOException, ClassNotFoundException {
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
                    for (CustomerNetworkListener l : listeners) l.onSessionEnded(msg.getContent());
                    break;
                    
                case Protocol.CMD_ERROR:
                    for (CustomerNetworkListener l : listeners) l.onError(msg.getContent());
                    break;
            }
        }
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
}
