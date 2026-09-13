package com.cmp180.livechat.server;

import com.cmp180.livechat.common.ChatSession;
import com.cmp180.livechat.common.Protocol;

import java.io.*;
import java.net.Socket;

/**
 * Luồng riêng biệt xử lý TCP Socket từng kết nối từ Client (Bài 3 & 5).
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ServerMain server;
    private BufferedReader reader;
    private PrintWriter writer;
    private String role;
    private String name;
    private ChatSession currentSession;

    public ClientHandler(Socket socket, ServerMain server) {
        this.socket = socket;
        this.server = server;
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        } catch (IOException e) {
            System.err.println("Lỗi khởi tạo stream cho Client: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(Protocol.DELIMITER, 3);
                String command = parts[0];

                switch (command) {
                    case Protocol.CMD_CONNECT:
                        if (parts.length >= 3) {
                            this.role = parts[1];
                            this.name = parts[2];
                            server.registerClient(this);
                        }
                        break;

                    case Protocol.CMD_MSG:
                        if (currentSession != null && parts.length > 1) {
                            ClientHandler partner = currentSession.getPartner(this);
                            if (partner != null) {
                                partner.sendMessage(Protocol.CMD_MSG + Protocol.SEP + this.name + ": " + parts[1]);
                            }
                        }
                        break;

                    case Protocol.CMD_END:
                        server.closeSession(currentSession, this.name + " đã kết thúc phiên trò chuyện.");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client ngắt kết nối: " + (name != null ? name : socket.getRemoteSocketAddress()));
        } finally {
            server.handleDisconnect(this);
            closeConnection();
        }
    }

    public void sendMessage(String msg) {
        if (writer != null) {
            writer.println(msg);
        }
    }

    public void setSession(ChatSession session) {
        this.currentSession = session;
    }

    public ChatSession getSession() {
        return currentSession;
    }

    public String getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
    }
}
