package com.cmp180.livechat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    private static final int PORT = 5000;
    private ServerGUI gui;
    private final SessionManager sessionManager; 
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private ServerSocket serverSocket;
    private volatile boolean isRunning = false;

    public ServerMain() {
        this.sessionManager = new SessionManager(this);
    }
    
    public ServerMain(ServerGUI gui) {
        this.gui = gui;
        this.sessionManager = new SessionManager(this);
    }

    public void start() {
        isRunning = true;
        log("=== HỆ THỐNG LIVE CHAT SUPPORT SERVER (PORT " + PORT + ") ===");
        try {
            serverSocket = new ServerSocket(PORT);
            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                log("[KẾT NỐI MỚI] " + clientSocket.getRemoteSocketAddress());
                
                ClientHandler handler = new ClientHandler(clientSocket, this, sessionManager);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            if (isRunning) {
                log("Lỗi Server: " + e.getMessage());
            } else {
                log("Server socket đã đóng thành công.");
            }
        }
    }
    
    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log("Lỗi khi đóng server: " + e.getMessage());
        }
    }
 
    public void log(String msg) {
        System.out.println(msg); 
        if (gui != null) gui.log(msg);
    }

    public ServerGUI getGui() {
        return gui;
    }

    public static void main(String[] args) {
        new ServerMain(null).start();
    }
}