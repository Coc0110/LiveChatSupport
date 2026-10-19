package com.cmp180.livechat.common;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Định dạng gói tin truyền thông đa phương tiện thời gian thực qua UDP.
 * Thiết kế tối ưu cho Voice Call (hiện tại) và Video Call (mở rộng tương lai).
 *
 * Cấu trúc Binary Packet:
 * [0..3]:   Magic "VOIP" (4 bytes: 0x56, 0x4F, 0x49, 0x50)
 * [4]:      Packet Type (1 byte)
 * [5..8]:   Sequence Number (int - 4 bytes)
 * [9]:      SenderId Length (1 byte, 0..127)
 * [10..]:   SenderId (UTF-8)
 * [...]:    ReceiverId Length (1 byte, 0..127)
 * [...]:    ReceiverId (UTF-8)
 * [...]:    Payload Length (int - 4 bytes)
 * [...]:    Payload Data (byte[])
 */
public class UdpMediaPacket {

    public static final byte[] MAGIC = new byte[]{'V', 'O', 'I', 'P'};

    // Các loại gói tin
    public static final byte TYPE_REGISTER     = 0x01; // Đăng ký địa chỉ UDP với Server
    public static final byte TYPE_REGISTER_ACK = 0x02; // Server xác nhận đăng ký
    public static final byte TYPE_VOICE        = 0x10; // Gói âm thanh cuộc gọi (PCM chunk)
    public static final byte TYPE_VIDEO        = 0x20; // Gói hình ảnh video cuộc gọi (Frame chunk)
    public static final byte TYPE_HEARTBEAT    = 0x30; // Duy trì kết nối NAT keepalive

    private final byte type;
    private final String senderId;
    private final String receiverId;
    private final int sequenceNumber;
    private final byte[] payload;

    public UdpMediaPacket(byte type, String senderId, String receiverId, int sequenceNumber, byte[] payload) {
        this.type = type;
        this.senderId = senderId != null ? senderId : "";
        this.receiverId = receiverId != null ? receiverId : "";
        this.sequenceNumber = sequenceNumber;
        this.payload = payload != null ? payload : new byte[0];
    }

    public byte getType() {
        return type;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public byte[] getPayload() {
        return payload;
    }

    /**
     * Tạo gói đăng ký địa chỉ UDP gửi tới Server Relay.
     */
    public static UdpMediaPacket createRegister(String myId) {
        return new UdpMediaPacket(TYPE_REGISTER, myId, "SERVER", 0, null);
    }

    /**
     * Tạo gói thoại thời gian thực qua UDP.
     */
    public static UdpMediaPacket createVoice(String senderId, String receiverId, int seq, byte[] pcmChunk) {
        return new UdpMediaPacket(TYPE_VOICE, senderId, receiverId, seq, pcmChunk);
    }

    /**
     * Tạo gói hình ảnh Video Call thời gian thực qua UDP (chuẩn bị cho tính năng Video Call).
     */
    public static UdpMediaPacket createVideo(String senderId, String receiverId, int seq, byte[] videoChunk) {
        return new UdpMediaPacket(TYPE_VIDEO, senderId, receiverId, seq, videoChunk);
    }

    /**
     * Tạo gói Heartbeat duy trì kết nối NAT / Router.
     */
    public static UdpMediaPacket createHeartbeat(String senderId, String receiverId) {
        return new UdpMediaPacket(TYPE_HEARTBEAT, senderId, receiverId, 0, null);
    }

    /**
     * Chuyển đổi gói tin thành mảng byte để gửi qua DatagramSocket.
     */
    public byte[] toBytes() {
        byte[] sBytes = senderId.getBytes(StandardCharsets.UTF_8);
        byte[] rBytes = receiverId.getBytes(StandardCharsets.UTF_8);

        int sLen = Math.min(sBytes.length, 127);
        int rLen = Math.min(rBytes.length, 127);
        int pLen = payload.length;

        int totalSize = 4 + 1 + 4 + 1 + sLen + 1 + rLen + 4 + pLen;
        ByteBuffer buf = ByteBuffer.allocate(totalSize);

        buf.put(MAGIC);
        buf.put(type);
        buf.putInt(sequenceNumber);

        buf.put((byte) sLen);
        buf.put(sBytes, 0, sLen);

        buf.put((byte) rLen);
        buf.put(rBytes, 0, rLen);

        buf.putInt(pLen);
        if (pLen > 0) {
            buf.put(payload, 0, pLen);
        }

        return buf.array();
    }

    /**
     * Giải mã toàn bộ mảng byte thành UdpMediaPacket.
     */
    public static UdpMediaPacket fromBytes(byte[] data) {
        if (data == null) return null;
        return fromBytes(data, 0, data.length);
    }

    /**
     * Giải mã dữ liệu nhị phân từ DatagramPacket thành UdpMediaPacket.
     */
    public static UdpMediaPacket fromBytes(byte[] data, int offset, int length) {
        if (data == null || length < 15) return null;

        ByteBuffer buf = ByteBuffer.wrap(data, offset, length);

        // Kiểm tra Magic
        for (int i = 0; i < 4; i++) {
            if (buf.get() != MAGIC[i]) return null;
        }

        byte type = buf.get();
        int seq = buf.getInt();

        int sLen = buf.get() & 0xFF;
        if (buf.remaining() < sLen + 1) return null;
        byte[] sBytes = new byte[sLen];
        buf.get(sBytes);
        String sender = new String(sBytes, StandardCharsets.UTF_8);

        int rLen = buf.get() & 0xFF;
        if (buf.remaining() < rLen + 4) return null;
        byte[] rBytes = new byte[rLen];
        buf.get(rBytes);
        String receiver = new String(rBytes, StandardCharsets.UTF_8);

        int pLen = buf.getInt();
        if (pLen < 0 || buf.remaining() < pLen) return null;
        byte[] payload = new byte[pLen];
        if (pLen > 0) {
            buf.get(payload);
        }

        return new UdpMediaPacket(type, sender, receiver, seq, payload);
    }
}
