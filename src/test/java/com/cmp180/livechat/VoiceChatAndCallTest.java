package com.cmp180.livechat;

import com.cmp180.livechat.client.customer.model.ChatMessage;
import com.cmp180.livechat.common.Protocol;
import com.cmp180.livechat.common.VoiceManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

public class VoiceChatAndCallTest {

    @Test
    @DisplayName("Kiểm tra Protocol constants cho Voice Chat và Voice Call")
    void testProtocolConstants() {
        assertEquals("VOICE_MSG", Protocol.CMD_VOICE_MSG);
        assertEquals("CALL_REQ", Protocol.CMD_CALL_REQ);
        assertEquals("CALL_ACCEPT", Protocol.CMD_CALL_ACCEPT);
        assertEquals("CALL_REJECT", Protocol.CMD_CALL_REJECT);
        assertEquals("CALL_END", Protocol.CMD_CALL_END);
        assertEquals("CALL_DATA", Protocol.CMD_CALL_DATA);
    }

    @Test
    @DisplayName("Kiểm tra ChatMessage nhận diện đúng tin nhắn thoại (isVoice)")
    void testChatMessageIsVoice() {
        ChatMessage voiceMsg = new ChatMessage("id-1", "Tin nhắn thoại", ChatMessage.Type.STAFF_MSG, "Staff 1",
                System.currentTimeMillis(), null, null, "voice_17123456789.wav", 32044, new byte[32044], false);
        assertTrue(voiceMsg.isVoice(), "File bắt đầu bằng 'voice_' và kết thúc bằng '.wav' phải được nhận diện là tin nhắn thoại");

        ChatMessage normalFile = new ChatMessage("id-2", "Tệp ghi âm", ChatMessage.Type.STAFF_MSG, "Staff 1",
                System.currentTimeMillis(), null, null, "recording.wav", 1024, new byte[1024], false);
        assertFalse(normalFile.isVoice(), "File wav thông thường không bắt đầu bằng 'voice_' không phải là voice note");

        ChatMessage imgMsg = new ChatMessage("id-3", "Ảnh chụp", ChatMessage.Type.STAFF_MSG, "Staff 1",
                System.currentTimeMillis(), null, null, "photo.png", 5000, new byte[5000], true);
        assertFalse(imgMsg.isVoice(), "Ảnh không phải là voice note");

        ChatMessage textMsg = new ChatMessage("Xin chào", ChatMessage.Type.MY_MSG, "Bạn");
        assertFalse(textMsg.isVoice(), "Tin nhắn text không phải là voice note");
    }

    @Test
    @DisplayName("Kiểm tra VoiceManager đóng gói WAV Header chuẩn RIFF/WAVE (44 bytes)")
    void testAddWavHeader() {
        int sampleRate = 16000;
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);

        int pcmLength = 32000;
        byte[] pcmData = new byte[pcmLength];
        for (int i = 0; i < pcmLength; i++) {
            pcmData[i] = (byte) (i % 128);
        }

        byte[] wavBytes = VoiceManager.addWavHeader(pcmData, format);
        assertNotNull(wavBytes);
        assertEquals(44 + pcmLength, wavBytes.length, "Độ dài file WAV phải bằng 44 bytes header + độ dài PCM");

        assertEquals('R', (char) wavBytes[0]);
        assertEquals('I', (char) wavBytes[1]);
        assertEquals('F', (char) wavBytes[2]);
        assertEquals('F', (char) wavBytes[3]);

        ByteBuffer bb = ByteBuffer.wrap(wavBytes).order(ByteOrder.LITTLE_ENDIAN);
        int chunkSize = bb.getInt(4);
        assertEquals(36 + pcmLength, chunkSize);

        assertEquals('W', (char) wavBytes[8]);
        assertEquals('A', (char) wavBytes[9]);
        assertEquals('V', (char) wavBytes[10]);
        assertEquals('E', (char) wavBytes[11]);

        assertEquals('f', (char) wavBytes[12]);
        assertEquals('m', (char) wavBytes[13]);
        assertEquals('t', (char) wavBytes[14]);
        assertEquals(' ', (char) wavBytes[15]);

        assertEquals(16, bb.getInt(16));
        assertEquals(1, bb.getShort(20));
        assertEquals(1, bb.getShort(22));
        assertEquals(16000, bb.getInt(24));
        assertEquals(32000, bb.getInt(28));
        assertEquals(2, bb.getShort(32));
        assertEquals(16, bb.getShort(34));

        assertEquals('d', (char) wavBytes[36]);
        assertEquals('a', (char) wavBytes[37]);
        assertEquals('t', (char) wavBytes[38]);
        assertEquals('a', (char) wavBytes[39]);

        assertEquals(pcmLength, bb.getInt(40));
        assertEquals(pcmData[0], wavBytes[44]);
        assertEquals(pcmData[100], wavBytes[44 + 100]);
    }

    @Test
    @DisplayName("Kiểm tra tính toán thời lượng tin nhắn thoại từ dung lượng PCM")
    void testVoiceDurationCalculation() {
        int oneSecondPcm = 32000;
        int durationOneSec = Math.max(1, oneSecondPcm / (16000 * 2));
        assertEquals(1, durationOneSec);

        int tenSecondsPcm = 320000;
        int durationTenSec = Math.max(1, tenSecondsPcm / (16000 * 2));
        assertEquals(10, durationTenSec);
    }

    @Test
    @DisplayName("Kiểm tra tạo âm chuông nhạc chờ (Dual-Tone ringtone generation)")
    void testToneGeneration() {
        int durationMs = 200;
        byte[] tone = VoiceManager.generateTone(440, 480, durationMs);
        assertNotNull(tone);
        assertEquals(1600, tone.length);

        boolean hasNonZero = false;
        for (byte b : tone) {
            if (b != 0) {
                hasNonZero = true;
                break;
            }
        }
        assertTrue(hasNonZero, "Âm thanh chuông phải chứa tín hiệu sóng âm thực tế");
    }

    @Test
    @DisplayName("Kiểm tra cấu trúc và tính hợp lệ của RecordingResult")
    void testRecordingResultValidation() {
        VoiceManager.RecordingResult successRes = new VoiceManager.RecordingResult(
                VoiceManager.RecordingResult.Status.SUCCESS, new byte[100], 3);
        assertEquals(VoiceManager.RecordingResult.Status.SUCCESS, successRes.status);
        assertNotNull(successRes.wavData);
        assertEquals(3, successRes.durationSeconds);

        VoiceManager.RecordingResult tooShortRes = new VoiceManager.RecordingResult(
                VoiceManager.RecordingResult.Status.TOO_SHORT, null, 0);
        assertEquals(VoiceManager.RecordingResult.Status.TOO_SHORT, tooShortRes.status);
        assertNull(tooShortRes.wavData);

        VoiceManager.RecordingResult silentRes = new VoiceManager.RecordingResult(
                VoiceManager.RecordingResult.Status.SILENT_OR_NO_MIC, null, 2);
        assertEquals(VoiceManager.RecordingResult.Status.SILENT_OR_NO_MIC, silentRes.status);
        assertNull(silentRes.wavData);

        // Kiểm tra không gây ngoại lệ khi kiểm tra microphone
        assertDoesNotThrow(VoiceManager::isMicrophoneAvailable);
    }
}
