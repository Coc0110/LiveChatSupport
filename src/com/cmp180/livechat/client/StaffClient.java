package com.cmp180.livechat.client;

import com.cmp180.livechat.common.Protocol;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Client dành cho Nhân viên CSKH trực tuyến.
 */
public class StaffClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in, "UTF-8");
        System.out.println("=== NHÂN VIÊN CSKH LIVE CHAT SUPPORT ===");
        System.out.print("Nhập tên hiển thị CSKH: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) name = "CSKH " + (int)(Math.random() * 100);

        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            // Gửi thông tin kết nối với vai trò CSKH
            writer.println(Protocol.CMD_CONNECT + Protocol.SEP + Protocol.ROLE_STAFF + Protocol.SEP + name);

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
                                System.out.println("\n[BẮT ĐẦU HỖ TRỢ] " + body);
                                System.out.println("-> Hãy nhập câu trả lời tư vấn bên dưới:");
                                break;
                            case Protocol.CMD_MSG:
                                System.out.println("\n" + body);
                                System.out.print("> ");
                                break;
                            case Protocol.CMD_END:
                                System.out.println("\n[KẾT THÚC PHIÊN] " + body);
                                System.out.println("[HỆ THỐNG] Đang chuyển về trạng thái SẴN SÀNG chờ khách tiếp theo...");
                                break;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("\n[HỆ THỐNG] Ngắt kết nối khỏi Server.");
                }
            });
            receiveThread.setDaemon(true);
            receiveThread.start();

            // Luồng chính nhập dữ liệu gửi cho khách
            System.out.println("Gõ câu trả lời và nhấn Enter để gửi. Gõ 'END' để kết thúc phiên hỗ trợ hiện tại.");
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine();
                if ("END".equalsIgnoreCase(input.trim())) {
                    writer.println(Protocol.CMD_END + Protocol.SEP);
                    continue;
                }
                if (!input.trim().isEmpty()) {
                    writer.println(Protocol.CMD_MSG + Protocol.SEP + input);
                }
            }

        } catch (IOException e) {
            System.err.println("Không thể kết nối tới Server: " + e.getMessage());
        }
    }
}
