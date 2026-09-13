package com.cmp180.livechat.client;

import com.cmp180.livechat.common.Protocol;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Client dành cho Khách hàng kết nối nhận hỗ trợ từ CSKH.
 */
public class CustomerClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in, "UTF-8");
        System.out.println("=== KHÁCH HÀNG LIVE CHAT SUPPORT ===");
        System.out.print("Nhập tên hiển thị của bạn: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) name = "Khách Hàng " + (int)(Math.random() * 1000);

        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            // Gửi thông tin kết nối
            writer.println(Protocol.CMD_CONNECT + Protocol.SEP + Protocol.ROLE_CUSTOMER + Protocol.SEP + name);

            // Luồng nhận tin nhắn từ Server (Multithreading - Bài 5)
            Thread receiveThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(Protocol.DELIMITER, 2);
                        String header = parts[0];
                        String body = parts.length > 1 ? parts[1] : "";

                        switch (header) {
                            case Protocol.CMD_WAITING:
                                System.out.println("\n[HỆ THỐNG] " + body);
                                break;
                            case Protocol.CMD_PAIRED:
                                System.out.println("\n[KẾT NỐI THÀNH CÔNG] " + body);
                                System.out.println("-> Hãy nhập tin nhắn bên dưới để trò chuyện:");
                                break;
                            case Protocol.CMD_MSG:
                                System.out.println("\n" + body);
                                System.out.print("> ");
                                break;
                            case Protocol.CMD_END:
                                System.out.println("\n[KẾT THÚC PHIÊN] " + body);
                                break;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("\n[HỆ THỐNG] Ngắt kết nối khỏi Server.");
                }
            });
            receiveThread.setDaemon(true);
            receiveThread.start();

            // Luồng chính nhập dữ liệu từ bàn phím
            System.out.println("Gõ tin nhắn và gõ Enter để gửi. Gõ 'END' để kết thúc phiên.");
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine();
                if ("END".equalsIgnoreCase(input.trim())) {
                    writer.println(Protocol.CMD_END + Protocol.SEP);
                    break;
                }
                if (!input.trim().isEmpty()) {
                    writer.println(Protocol.CMD_MSG + Protocol.SEP + input);
                }
            }

            socket.close();
        } catch (IOException e) {
            System.err.println("Không thể kết nối tới Server: " + e.getMessage());
        }
    }
}
