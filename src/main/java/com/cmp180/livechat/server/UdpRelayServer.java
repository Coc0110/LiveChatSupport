package com.cmp180.livechat.server;

import com.cmp180.livechat.common.UdpMediaPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * UDP Media Relay Server:
 * Chuyển tiếp các gói tin âm thanh (Voice Call) và hình ảnh (Video Call) thời gian thực
 * giữa Customer và Staff qua giao thức UDP không chặn (non-blocking / low-latency).
 *
 * Tính năng chính:
 * 1. Không bị ảnh hưởng bởi NAT / Firewall gia đình vì cả 2 Client đều kết nối Outbound tới Server.
 * 2. Tốc độ chuyển tiếp cực nhanh trong bộ nhớ (< 1ms).
 * 3. Hỗ trợ cả gói TYPE_VOICE và TYPE_VIDEO để sẵn sàng cho Video Call.
 */
public class UdpRelayServer {

    private final int port;
    private final ServerMain serverMain;
    private DatagramSocket socket;
    private volatile boolean isRunning = false;
    private Thread serverThread;

    // Bảng lưu địa chỉ UDP của các Client: ClientId -> SocketAddress (IP:Port)
    private final Map<String, SocketAddress> activeEndpoints = new ConcurrentHashMap<>();
    // Bảng liên kết bí danh (vd: số điện thoại <-> clientId)
    private final Map<String, String> aliasMap = new ConcurrentHashMap<>();
    // Thời điểm hoạt động gần nhất để dọn dẹp các endpoint không còn kết nối
    private final Map<String, Long> lastSeen = new ConcurrentHashMap<>();

    private ScheduledExecutorService cleanupExecutor;
    private final AtomicLong relayedVoicePackets = new AtomicLong(0);
    private final AtomicLong relayedVideoPackets = new AtomicLong(0);

    public UdpRelayServer(int port) {
        this(port, null);
    }

    public UdpRelayServer(int port, ServerMain serverMain) {
        this.port = port;
        this.serverMain = serverMain;
    }

    public int getPort() {
        return socket != null ? socket.getLocalPort() : port;
    }

    /**
     * Khởi động UDP Relay Server.
     */
    public synchronized void start() {
        if (isRunning) return;
        isRunning = true;

        try {
            this.socket = new DatagramSocket(this.port);
            // Kích thước bộ đệm nhận gói tin UDP tối ưu cho đa phương tiện (2MB)
            try {
                this.socket.setReceiveBufferSize(2 * 1024 * 1024);
                this.socket.setSendBufferSize(2 * 1024 * 1024);
            } catch (Exception ignored) {}

            log("-> UDP Media Relay Server đang lắng nghe trên cổng " + port + " (Voice & Video Call)");

            serverThread = new Thread(this::listenLoop, "UDP-Relay-Server-Thread");
            serverThread.setDaemon(true);
            serverThread.start();

            // Định kỳ dọn dẹp endpoint cũ sau 2 phút không có hoạt động
            cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "UDP-Relay-Cleanup");
                t.setDaemon(true);
                return t;
            });
            cleanupExecutor.scheduleAtFixedRate(this::cleanupStaleEndpoints, 1, 1, TimeUnit.MINUTES);

        } catch (SocketException e) {
            log("Lỗi khởi động UDP Relay Server trên cổng " + port + ": " + e.getMessage());
        }
    }

    private void listenLoop() {
        byte[] buffer = new byte[65535];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (isRunning && socket != null && !socket.isClosed()) {
            try {
                socket.receive(packet);

                UdpMediaPacket media = UdpMediaPacket.fromBytes(packet.getData(), packet.getOffset(), packet.getLength());
                if (media == null) continue;

                String senderId = media.getSenderId();
                SocketAddress senderAddr = packet.getSocketAddress();

                if (senderId != null && !senderId.isEmpty()) {
                    activeEndpoints.put(senderId, senderAddr);
                    lastSeen.put(senderId, System.currentTimeMillis());
                }

                switch (media.getType()) {
                    case UdpMediaPacket.TYPE_REGISTER:
                        // Phản hồi ACK cho Client
                        UdpMediaPacket ack = new UdpMediaPacket(UdpMediaPacket.TYPE_REGISTER_ACK, "SERVER", senderId, 0, null);
                        byte[] ackBytes = ack.toBytes();
                        DatagramPacket ackPacket = new DatagramPacket(ackBytes, ackBytes.length, senderAddr);
                        socket.send(ackPacket);
                        break;

                    case UdpMediaPacket.TYPE_VOICE:
                    case UdpMediaPacket.TYPE_VIDEO:
                        String receiverId = media.getReceiverId();
                        SocketAddress destAddr = resolveDestination(receiverId);

                        if (destAddr != null) {
                            // Chuyển tiếp gói tin trực tiếp tới người nhận qua UDP
                            packet.setSocketAddress(destAddr);
                            socket.send(packet);

                            if (media.getType() == UdpMediaPacket.TYPE_VOICE) {
                                long count = relayedVoicePackets.incrementAndGet();
                                if (count == 1 || count % 500 == 0) {
                                    log(String.format("[UDP VOICE RELAY] Đã chuyển tiếp %d gói thoại (%s -> %s)", count, senderId, receiverId));
                                }
                            } else {
                                long count = relayedVideoPackets.incrementAndGet();
                                if (count == 1 || count % 100 == 0) {
                                    log(String.format("[UDP VIDEO RELAY] Đã chuyển tiếp %d khung hình video (%s -> %s)", count, senderId, receiverId));
                                }
                            }
                        }
                        break;

                    case UdpMediaPacket.TYPE_HEARTBEAT:
                        // Gói tin duy trì kết nối NAT, không cần chuyển tiếp
                        break;

                    default:
                        break;
                }

            } catch (SocketException se) {
                // Socket đã đóng
                break;
            } catch (IOException ioe) {
                if (isRunning) {
                    System.err.println("Lỗi chuyển tiếp gói UDP: " + ioe.getMessage());
                }
            } catch (Exception ex) {
                // Bỏ qua lỗi packet hỏng
            }
        }
    }

    /**
     * Tìm SocketAddress của người nhận dựa trên ID hoặc bí danh (SĐT/ClientID).
     */
    private SocketAddress resolveDestination(String receiverId) {
        if (receiverId == null || receiverId.isEmpty()) return null;

        SocketAddress addr = activeEndpoints.get(receiverId);
        if (addr != null) return addr;

        String alias = aliasMap.get(receiverId);
        if (alias != null) {
            addr = activeEndpoints.get(alias);
            if (addr != null) return addr;
        }

        // Tìm ngược trong bảng alias
        for (Map.Entry<String, String> entry : aliasMap.entrySet()) {
            if (receiverId.equals(entry.getValue())) {
                addr = activeEndpoints.get(entry.getKey());
                if (addr != null) return addr;
            }
        }

        return null;
    }

    /**
     * Đăng ký liên kết giữa số điện thoại và clientId để định tuyến chính xác.
     */
    public void registerAlias(String phoneOrAlias, String primaryId) {
        if (phoneOrAlias != null && primaryId != null) {
            aliasMap.put(phoneOrAlias, primaryId);
        }
    }

    private void cleanupStaleEndpoints() {
        long now = System.currentTimeMillis();
        long timeoutMs = TimeUnit.MINUTES.toMillis(3);

        lastSeen.entrySet().removeIf(entry -> {
            if (now - entry.getValue() > timeoutMs) {
                activeEndpoints.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    public synchronized void stop() {
        isRunning = false;
        if (cleanupExecutor != null) {
            cleanupExecutor.shutdownNow();
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (serverThread != null) {
            serverThread.interrupt();
        }
        activeEndpoints.clear();
        aliasMap.clear();
        lastSeen.clear();
    }

    public long getRelayedVoicePackets() {
        return relayedVoicePackets.get();
    }

    public long getRelayedVideoPackets() {
        return relayedVideoPackets.get();
    }

    private void log(String msg) {
        if (serverMain != null) {
            serverMain.log(msg);
        } else {
            System.out.println(msg);
        }
    }
}
