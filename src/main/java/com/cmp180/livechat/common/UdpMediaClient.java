package com.cmp180.livechat.common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Client truyền nhận dữ liệu Voice Call & Video Call qua giao thức UDP.
 *
 * Tính năng:
 * 1. Mở cổng UDP tạm (ephemeral port) kết nối tới Server UDP Relay.
 * 2. Tự động gửi gói đăng ký (REGISTER) và gói nhịp tim (HEARTBEAT) duy trì NAT.
 * 3. Truyền nhận âm thanh PCM thời gian thực không độ trễ.
 * 4. Tích hợp sẵn kênh video (TYPE_VIDEO) để lập trình viên sẵn sàng gắn camera/webcam khi phát triển Video Call.
 */
public class UdpMediaClient {

    private final String myId;
    private final String targetId;
    private final String serverHost;
    private final int serverPort;

    private DatagramSocket socket;
    private InetSocketAddress serverAddress;
    private final AtomicInteger seqCounter = new AtomicInteger(0);
    private volatile boolean isRunning = false;

    private Consumer<byte[]> voiceConsumer;
    private Consumer<byte[]> videoConsumer;

    private Thread receiveThread;
    private ScheduledExecutorService heartbeatExecutor;

    private final AtomicLong packetsSent = new AtomicLong(0);
    private final AtomicLong packetsReceived = new AtomicLong(0);

    public UdpMediaClient(String myId, String targetId, String serverHost, int serverPort) {
        this.myId = myId != null ? myId : "unknown";
        this.targetId = targetId != null ? targetId : "unknown";
        this.serverHost = (serverHost != null && !serverHost.trim().isEmpty()) ? serverHost.trim() : "127.0.0.1";
        this.serverPort = serverPort > 0 ? serverPort : 5000;
    }

    public void setVoiceConsumer(Consumer<byte[]> consumer) {
        this.voiceConsumer = consumer;
    }

    public void setVideoConsumer(Consumer<byte[]> consumer) {
        this.videoConsumer = consumer;
    }

    /**
     * Khởi động UDP Media Client.
     */
    public synchronized boolean start() {
        if (isRunning) return true;
        try {
            // Khởi tạo socket UDP với cổng ngẫu nhiên do hệ điều hành cấp phát
            this.socket = new DatagramSocket(0);
            try {
                this.socket.setReceiveBufferSize(1024 * 1024);
                this.socket.setSendBufferSize(1024 * 1024);
            } catch (Exception ignored) {}

            this.serverAddress = new InetSocketAddress(serverHost, serverPort);
            this.isRunning = true;

            // Gửi gói REGISTER tới Server 3 lần để chắc chắn server nhận diện được endpoint ngay cả khi mạng chập chờn
            sendRegisterBurst();

            // Luồng nhận các gói tin UDP từ Server
            receiveThread = new Thread(this::receiveLoop, "UDP-MediaClient-" + myId);
            receiveThread.setDaemon(true);
            receiveThread.start();

            // Duy trì Keepalive mỗi 2.5 giây để giữ mở cổng NAT / Router
            heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "UDP-Heartbeat-" + myId);
                t.setDaemon(true);
                return t;
            });
            heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeat, 2500, 2500, TimeUnit.MILLISECONDS);

            System.out.println(String.format("[UDP CLIENT START] Đã kích hoạt UDP Media Client (MyId: %s, Peer: %s, LocalPort: %d, Server: %s:%d)",
                    myId, targetId, socket.getLocalPort(), serverHost, serverPort));
            return true;
        } catch (SocketException e) {
            System.err.println("Lỗi mở UDP Socket: " + e.getMessage());
            close();
            return false;
        }
    }

    private void sendRegisterBurst() {
        new Thread(() -> {
            for (int i = 0; i < 3 && isRunning && socket != null && !socket.isClosed(); i++) {
                try {
                    UdpMediaPacket reg = UdpMediaPacket.createRegister(myId);
                    byte[] data = reg.toBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress);
                    socket.send(packet);
                    Thread.sleep(30);
                } catch (Exception ignored) {}
            }
        }, "UDP-RegisterBurst").start();
    }

    private void sendHeartbeat() {
        if (!isRunning || socket == null || socket.isClosed()) return;
        try {
            UdpMediaPacket hb = UdpMediaPacket.createHeartbeat(myId, targetId);
            byte[] data = hb.toBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress);
            socket.send(packet);
        } catch (Exception ignored) {}
    }

    private void receiveLoop() {
        byte[] buffer = new byte[65535];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (isRunning && socket != null && !socket.isClosed()) {
            try {
                socket.receive(packet);
                UdpMediaPacket media = UdpMediaPacket.fromBytes(packet.getData(), packet.getOffset(), packet.getLength());
                if (media == null) continue;

                packetsReceived.incrementAndGet();

                if (media.getType() == UdpMediaPacket.TYPE_VOICE) {
                    if (voiceConsumer != null) {
                        voiceConsumer.accept(media.getPayload());
                    }
                } else if (media.getType() == UdpMediaPacket.TYPE_VIDEO) {
                    if (videoConsumer != null) {
                        videoConsumer.accept(media.getPayload());
                    }
                }
            } catch (SocketException se) {
                break;
            } catch (IOException ioe) {
                if (isRunning) {
                    System.err.println("Lỗi nhận gói UDP Client: " + ioe.getMessage());
                }
            } catch (Exception ignored) {}
        }
    }

    /**
     * Gửi một chunk âm thanh thời gian thực qua UDP.
     */
    public void sendVoice(byte[] pcmChunk) {
        if (!isRunning || socket == null || socket.isClosed() || pcmChunk == null || pcmChunk.length == 0) return;
        try {
            UdpMediaPacket pkt = UdpMediaPacket.createVoice(myId, targetId, seqCounter.incrementAndGet(), pcmChunk);
            byte[] data = pkt.toBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress);
            socket.send(packet);
            packetsSent.incrementAndGet();
        } catch (Exception e) {
            // Không ném exception để tránh làm gián đoạn luồng thu âm
        }
    }

    /**
     * Gửi một khung hình video thời gian thực qua UDP (sẵn sàng cho Video Call).
     */
    public void sendVideo(byte[] videoChunk) {
        if (!isRunning || socket == null || socket.isClosed() || videoChunk == null || videoChunk.length == 0) return;
        try {
            UdpMediaPacket pkt = UdpMediaPacket.createVideo(myId, targetId, seqCounter.incrementAndGet(), videoChunk);
            byte[] data = pkt.toBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress);
            socket.send(packet);
            packetsSent.incrementAndGet();
        } catch (Exception e) {
            // Không ném exception
        }
    }

    /**
     * Đóng kênh truyền thông UDP.
     */
    public synchronized void close() {
        isRunning = false;
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdownNow();
            heartbeatExecutor = null;
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
            socket = null;
        }
        if (receiveThread != null) {
            receiveThread.interrupt();
            receiveThread = null;
        }
        System.out.println(String.format("[UDP CLIENT STOP] Đã đóng UDP Media Client (Sent: %d, Received: %d)",
                packetsSent.get(), packetsReceived.get()));
    }

    public boolean isRunning() {
        return isRunning;
    }

    public long getPacketsSent() {
        return packetsSent.get();
    }

    public long getPacketsReceived() {
        return packetsReceived.get();
    }
}
