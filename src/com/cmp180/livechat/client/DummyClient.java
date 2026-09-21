package com.cmp180.livechat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import com.cmp180.livechat.common.Protocol;
import com.cmp180.livechat.common.SecurityManager;

public class DummyClient {
    public static void main(String[] args) {
        // Ép UTF-8 cho chữ xuất ra màn hình Terminal
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        
        String serverAddress = "127.0.0.1";
        int port = 5000;

        try (Socket socket = new Socket(serverAddress, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             // Ép UTF-8 cho chữ gõ vào từ bàn phím
             Scanner scanner = new Scanner(System.in, "UTF-8")) {

            System.out.println("=== ĐÃ KẾT NỐI TỚI SERVER ===");
            System.out.println("Gõ lệnh theo chuẩn Protocol (VD: CUST_LOGIN|KH01). Gõ QUIT để thoát.");
            System.out.print("> ");

            Thread readThread = new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        // Giải mã nếu có nội dung tin nhắn, bỏ qua với các lệnh hệ thống
                        String displayMsg = response;
                        if (response.startsWith(Protocol.CMD_CHAT)) {
                            String[] parts = response.split("\\" + Protocol.SEPARATOR, 3);
                            if (parts.length == 3) {
                                String decryptedContent = SecurityManager.decrypt(parts[2]);
                                displayMsg = parts[0] + Protocol.SEPARATOR + parts[1] + Protocol.SEPARATOR + decryptedContent;
                            }
                        }
                        System.out.println("\n[SERVER TRẢ VỀ] " + displayMsg);
                        System.out.print("> ");
                    }
                } catch (IOException e) {
                    System.out.println("\n[NGẮT KẾT NỐI] Mất kết nối tới Server.");
                }
            });
            readThread.setDaemon(true);
            readThread.start();

            while (true) {
                String msg = scanner.nextLine();
                if ("QUIT".equalsIgnoreCase(msg)) break;
                if (msg.startsWith(Protocol.CMD_CHAT)) {
                    String[] parts = msg.split("\\" + Protocol.SEPARATOR, 2);
                    if (parts.length == 2) {
                        String encryptedContent = SecurityManager.encrypt(parts[1]);
                        out.println(parts[0] + Protocol.SEPARATOR + encryptedContent);
                    } else {
                        out.println(msg);
                    }
                } else {
                    out.println(msg); 
                }
            }
        } catch (IOException e) {
            System.err.println("Không thể kết nối tới Server: " + e.getMessage());
        }
    }
}