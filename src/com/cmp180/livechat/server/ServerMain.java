package com.cmp180.livechat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    private static final int PORT = 5000;
    private ServerGUI gui;
    private final SessionManager sessionManager; 

    public ServerMain() {
        this.sessionManager = new SessionManager(this);
    }
    
    public ServerMain(ServerGUI gui) {
        this.gui = gui;
        this.sessionManager = new SessionManager(this);
    }

    public void start() {
        log("=== HỆ THỐNG LIVE CHAT SUPPORT SERVER (PORT " + PORT + ") ===");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                log("[KẾT NỐI MỚI] " + clientSocket.getRemoteSocketAddress());
                
                ClientHandler handler = new ClientHandler(clientSocket, this, sessionManager);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            log("Lỗi Server: " + e.getMessage());
        }
    }
 
    public void log(String msg) {
        System.out.println(msg); 
        if (gui != null) gui.log(msg);
    }

    public static void main(String[] args) {
        new ServerMain(null).start();
    }
}