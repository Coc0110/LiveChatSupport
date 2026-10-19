package com.cmp180.livechat;

import com.cmp180.livechat.common.UdpMediaClient;
import com.cmp180.livechat.common.UdpMediaPacket;
import com.cmp180.livechat.server.UdpRelayServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class UdpMediaTest {

    @Test
    @DisplayName("Kiểm tra đóng gói và giải mã gói tin âm thanh Voice qua UDP")
    void testVoicePacketSerialization() {
        String sender = "staff_1";
        String receiver = "cust_99";
        int seq = 12345;
        byte[] pcmData = new byte[1280]; // 40ms PCM 16kHz mono
        for (int i = 0; i < pcmData.length; i++) {
            pcmData[i] = (byte) (i & 0xFF);
        }

        UdpMediaPacket packet = UdpMediaPacket.createVoice(sender, receiver, seq, pcmData);
        assertEquals(UdpMediaPacket.TYPE_VOICE, packet.getType());
        assertEquals(sender, packet.getSenderId());
        assertEquals(receiver, packet.getReceiverId());
        assertEquals(seq, packet.getSequenceNumber());
        assertArrayEquals(pcmData, packet.getPayload());

        byte[] rawBytes = packet.toBytes();
        assertNotNull(rawBytes);
        assertTrue(rawBytes.length > 1280);

        UdpMediaPacket decoded = UdpMediaPacket.fromBytes(rawBytes);
        assertNotNull(decoded);
        assertEquals(UdpMediaPacket.TYPE_VOICE, decoded.getType());
        assertEquals(sender, decoded.getSenderId());
        assertEquals(receiver, decoded.getReceiverId());
        assertEquals(seq, decoded.getSequenceNumber());
        assertArrayEquals(pcmData, decoded.getPayload());
    }

    @Test
    @DisplayName("Kiểm tra đóng gói và giải mã gói tin hình ảnh Video (sẵn sàng cho Video Call) qua UDP")
    void testVideoPacketSerialization() {
        String sender = "cust_99";
        String receiver = "staff_1";
        int seq = 777;
        byte[] videoFrame = new byte[8192]; // 8KB video frame (JPEG/H.264)
        for (int i = 0; i < videoFrame.length; i++) {
            videoFrame[i] = (byte) ((i * 3) & 0xFF);
        }

        UdpMediaPacket packet = UdpMediaPacket.createVideo(sender, receiver, seq, videoFrame);
        assertEquals(UdpMediaPacket.TYPE_VIDEO, packet.getType());
        assertEquals(sender, packet.getSenderId());
        assertEquals(receiver, packet.getReceiverId());
        assertEquals(seq, packet.getSequenceNumber());
        assertArrayEquals(videoFrame, packet.getPayload());

        byte[] rawBytes = packet.toBytes();
        assertNotNull(rawBytes);

        UdpMediaPacket decoded = UdpMediaPacket.fromBytes(rawBytes);
        assertNotNull(decoded);
        assertEquals(UdpMediaPacket.TYPE_VIDEO, decoded.getType());
        assertEquals(sender, decoded.getSenderId());
        assertEquals(receiver, decoded.getReceiverId());
        assertEquals(seq, decoded.getSequenceNumber());
        assertArrayEquals(videoFrame, decoded.getPayload());
    }

    @Test
    @DisplayName("Kiểm tra gói tin REGISTER và HEARTBEAT duy trì kết nối UDP")
    void testRegisterAndHeartbeatPackets() {
        UdpMediaPacket reg = UdpMediaPacket.createRegister("client_abc");
        assertEquals(UdpMediaPacket.TYPE_REGISTER, reg.getType());
        assertEquals("client_abc", reg.getSenderId());
        byte[] regBytes = reg.toBytes();
        UdpMediaPacket decodedReg = UdpMediaPacket.fromBytes(regBytes);
        assertNotNull(decodedReg);
        assertEquals(UdpMediaPacket.TYPE_REGISTER, decodedReg.getType());
        assertEquals("client_abc", decodedReg.getSenderId());

        UdpMediaPacket hb = UdpMediaPacket.createHeartbeat("client_abc", "peer_xyz");
        assertEquals(UdpMediaPacket.TYPE_HEARTBEAT, hb.getType());
        assertEquals("client_abc", hb.getSenderId());
        assertEquals("peer_xyz", hb.getReceiverId());
        byte[] hbBytes = hb.toBytes();
        UdpMediaPacket decodedHb = UdpMediaPacket.fromBytes(hbBytes);
        assertNotNull(decodedHb);
        assertEquals(UdpMediaPacket.TYPE_HEARTBEAT, decodedHb.getType());
    }

    @Test
    @DisplayName("Kiểm tra xử lý an toàn khi nhận gói tin hỏng hoặc không đúng định dạng")
    void testMalformedPacketHandling() {
        assertNull(UdpMediaPacket.fromBytes((byte[]) null));
        assertNull(UdpMediaPacket.fromBytes(new byte[0]));
        assertNull(UdpMediaPacket.fromBytes(new byte[15])); // bé hơn MIN_HEADER_SIZE (28)

        byte[] invalidMagic = new byte[32];
        invalidMagic[0] = 'B';
        invalidMagic[1] = 'A';
        invalidMagic[2] = 'D';
        invalidMagic[3] = '!';
        assertNull(UdpMediaPacket.fromBytes(invalidMagic));
    }

    @Test
    @DisplayName("Kiểm tra truyền nhận Voice và Video đa phương tiện thời gian thực qua UdpRelayServer")
    void testEndToEndUdpRelayVoiceAndVideo() throws Exception {
        UdpRelayServer server = new UdpRelayServer(0);
        server.start();
        int serverPort = server.getPort();
        assertTrue(serverPort > 0, "Server phải được cấp một cổng ngẫu nhiên hợp lệ");

        String staffId = "staff_test_01";
        String customerId = "cust_test_01";

        UdpMediaClient staffClient = new UdpMediaClient(staffId, customerId, "127.0.0.1", serverPort);
        UdpMediaClient customerClient = new UdpMediaClient(customerId, staffId, "127.0.0.1", serverPort);

        CountDownLatch voiceLatch = new CountDownLatch(1);
        AtomicReference<byte[]> receivedVoice = new AtomicReference<>();
        customerClient.setVoiceConsumer(chunk -> {
            receivedVoice.set(chunk);
            voiceLatch.countDown();
        });

        CountDownLatch videoLatch = new CountDownLatch(1);
        AtomicReference<byte[]> receivedVideo = new AtomicReference<>();
        staffClient.setVideoConsumer(frame -> {
            receivedVideo.set(frame);
            videoLatch.countDown();
        });

        assertTrue(staffClient.start());
        assertTrue(customerClient.start());

        // Đợi 200ms để gói REGISTER từ 2 client gửi tới server
        Thread.sleep(200);

        // 1. Staff gửi Voice chunk -> Customer nhận
        byte[] testVoiceData = new byte[1280];
        for (int i = 0; i < testVoiceData.length; i++) {
            testVoiceData[i] = (byte) ((i + 7) & 0xFF);
        }
        staffClient.sendVoice(testVoiceData);

        boolean voiceReceived = voiceLatch.await(3, TimeUnit.SECONDS);
        assertTrue(voiceReceived, "Customer phải nhận được gói Voice từ Staff qua UDP Relay");
        assertArrayEquals(testVoiceData, receivedVoice.get(), "Nội dung âm thanh Voice nhận được phải chính xác từng byte");

        // 2. Customer gửi Video frame -> Staff nhận (sẵn sàng cho tính năng Video Call)
        byte[] testVideoData = new byte[4096];
        for (int i = 0; i < testVideoData.length; i++) {
            testVideoData[i] = (byte) ((i * 5) & 0xFF);
        }
        customerClient.sendVideo(testVideoData);

        boolean videoReceived = videoLatch.await(3, TimeUnit.SECONDS);
        assertTrue(videoReceived, "Staff phải nhận được gói Video từ Customer qua UDP Relay");
        assertArrayEquals(testVideoData, receivedVideo.get(), "Nội dung khung hình Video nhận được phải chính xác từng byte");

        // Dọn dẹp tài nguyên
        staffClient.close();
        customerClient.close();
        server.stop();
    }

    @Test
    @DisplayName("Kiểm tra định tuyến qua bí danh Alias (Số điện thoại khách hàng)")
    void testAliasRoutingInRelayServer() throws Exception {
        UdpRelayServer server = new UdpRelayServer(0);
        server.start();
        int serverPort = server.getPort();

        String staffId = "staff_alice";
        String customerPhone = "0987654321";
        String customerClientId = "client_uuid_999";

        // Khách hàng đăng ký endpoint với ClientId thật
        UdpMediaClient customerClient = new UdpMediaClient(customerClientId, staffId, "127.0.0.1", serverPort);
        customerClient.start();

        // Server liên kết số điện thoại với ClientId thật của khách hàng
        server.registerAlias(customerPhone, customerClientId);

        // Nhân viên gửi cuộc gọi tới số điện thoại của khách hàng
        UdpMediaClient staffClient = new UdpMediaClient(staffId, customerPhone, "127.0.0.1", serverPort);
        staffClient.start();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<byte[]> receivedAudio = new AtomicReference<>();
        customerClient.setVoiceConsumer(audio -> {
            receivedAudio.set(audio);
            latch.countDown();
        });

        Thread.sleep(200);

        byte[] audio = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        staffClient.sendVoice(audio);

        boolean received = latch.await(3, TimeUnit.SECONDS);
        assertTrue(received, "Khách hàng phải nhận được âm thanh dù nhân viên gửi qua số điện thoại alias");
        assertArrayEquals(audio, receivedAudio.get());

        staffClient.close();
        customerClient.close();
        server.stop();
    }
}
