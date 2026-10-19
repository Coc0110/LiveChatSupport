package com.cmp180.livechat.common;

import javax.sound.sampled.*;
import javax.sound.sampled.Mixer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Trình quản lý Âm thanh (Audio Engine) cho hệ thống Live Chat.
 * Hỗ trợ:
 * 1. Thu âm và phát tin nhắn thoại (Voice Messages / Voice Notes).
 * 2. Đóng gói chuẩn WAV (44-byte RIFF/WAVE header).
 * 3. Thu và phát luồng âm thanh cuộc gọi trực tiếp thời gian thực (Live Voice Call Streaming).
 * 4. Phát chuông chờ cuộc gọi đến (Synthesized Ringtone).
 */
public class VoiceManager {

    // Định dạng âm thanh chuẩn: 16kHz, 16-bit, Mono, Signed, Little-Endian
    public static final AudioFormat AUDIO_FORMAT = new AudioFormat(16000.0f, 16, 1, true, false);
    public static final int CHUNK_SIZE = 3200; // ~100ms dữ liệu âm thanh mỗi gói tin

    private static VoiceManager instance;

    public static synchronized VoiceManager getInstance() {
        if (instance == null) {
            instance = new VoiceManager();
        }
        return instance;
    }

    // --- State Voice Recording ---
    private TargetDataLine recordLine;
    private ByteArrayOutputStream recordStream;
    private Thread recordThread;
    private volatile boolean isRecording = false;
    private long recordStartTime = 0;
    private volatile double currentLiveVolume = 0.0;
    private volatile int maxRecordedAmplitude = 0;
    private volatile boolean soundDetected = false;

    public static class RecordingResult {
        public enum Status { SUCCESS, TOO_SHORT, SILENT_OR_NO_MIC, ERROR }
        public final Status status;
        public final byte[] wavData;
        public final int durationSeconds;

        public RecordingResult(Status status, byte[] wavData, int durationSeconds) {
            this.status = status;
            this.wavData = wavData;
            this.durationSeconds = durationSeconds;
        }
    }

    // --- State Voice Playback ---
    private Clip currentClip;
    private volatile boolean isPlaying = false;
    private Consumer<Boolean> playbackCallback;

    // --- State Live Call ---
    private TargetDataLine callCaptureLine;
    private SourceDataLine callPlaybackLine;
    private Thread callCaptureThread;
    private final AtomicBoolean isCallActive = new AtomicBoolean(false);
    private final AtomicBoolean isMuted = new AtomicBoolean(false);

    // --- State Ringtone ---
    private volatile boolean isRinging = false;
    private Thread ringtoneThread;

    private VoiceManager() {}

    // ==========================================
    // 1. THU ÂM & PHÁT TIN NHẮN THOẠI (VOICE NOTE)
    // ==========================================

    /**
     * Kiểm tra xem máy tính có thiết bị Microphone khả dụng hay không.
     */
    public static boolean isMicrophoneAvailable() {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);
            if (!AudioSystem.isLineSupported(info)) return false;
            Mixer.Info[] mixers = AudioSystem.getMixerInfo();
            if (mixers == null || mixers.length == 0) return false;
            for (Mixer.Info mi : mixers) {
                try {
                    Mixer mixer = AudioSystem.getMixer(mi);
                    if (mixer.isLineSupported(info)) return true;
                } catch (Exception ignored) {}
            }
            return AudioSystem.isLineSupported(info);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Bắt đầu thu âm tin nhắn thoại từ Microphone.
     */
    public synchronized boolean startRecording() {
        if (isRecording) return false;
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Microphone không hỗ trợ định dạng âm thanh chuẩn!");
                return false;
            }
            recordLine = (TargetDataLine) AudioSystem.getLine(info);
            recordLine.open(AUDIO_FORMAT);
            recordLine.start();

            recordStream = new ByteArrayOutputStream();
            isRecording = true;
            recordStartTime = System.currentTimeMillis();
            currentLiveVolume = 0.0;
            maxRecordedAmplitude = 0;
            soundDetected = false;

            recordThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                while (isRecording && recordLine != null && recordLine.isOpen()) {
                    int count = recordLine.read(buffer, 0, buffer.length);
                    if (count > 0) {
                        recordStream.write(buffer, 0, count);

                        long sumSquares = 0;
                        int localMax = 0;
                        int samples = count / 2;
                        for (int i = 0; i < count - 1; i += 2) {
                            short sample = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xFF));
                            int abs = Math.abs(sample);
                            if (abs > localMax) localMax = abs;
                            sumSquares += (long) sample * sample;
                        }
                        if (localMax > maxRecordedAmplitude) {
                            maxRecordedAmplitude = localMax;
                        }
                        if (localMax > 220) {
                            soundDetected = true;
                        }
                        double rms = samples > 0 ? Math.sqrt((double) sumSquares / samples) : 0;
                        currentLiveVolume = Math.min(1.0, rms / 3500.0);
                    }
                }
            }, "Voice-Record-Thread");
            recordThread.setDaemon(true);
            recordThread.start();
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo thiết bị thu âm: " + e.getMessage());
            isRecording = false;
            return false;
        }
    }

    /**
     * Dừng thu âm và kiểm tra hợp lệ toàn diện (thời lượng, âm lượng, tín hiệu giọng nói).
     */
    public synchronized RecordingResult finishRecording() {
        if (!isRecording) return new RecordingResult(RecordingResult.Status.ERROR, null, 0);
        long durationMs = System.currentTimeMillis() - recordStartTime;
        byte[] wav = stopRecording();
        if (wav == null || wav.length <= 44) {
            return new RecordingResult(RecordingResult.Status.ERROR, null, 0);
        }
        int durationSec = Math.max(1, (int) (durationMs / 1000));
        if (durationMs < 800) {
            return new RecordingResult(RecordingResult.Status.TOO_SHORT, null, 0);
        }
        if (!soundDetected || maxRecordedAmplitude < 180) {
            return new RecordingResult(RecordingResult.Status.SILENT_OR_NO_MIC, null, durationSec);
        }
        return new RecordingResult(RecordingResult.Status.SUCCESS, wav, durationSec);
    }

    public double getLiveVolume() {
        return currentLiveVolume;
    }

    public boolean isSoundDetected() {
        return soundDetected;
    }

    public int getMaxRecordedAmplitude() {
        return maxRecordedAmplitude;
    }

    /**
     * Dừng thu âm và đóng gói thành file định dạng WAV chuẩn (có 44-byte RIFF header).
     * @return byte[] của file WAV hoàn chỉnh, hoặc null nếu lỗi.
     */
    public synchronized byte[] stopRecording() {
        if (!isRecording) return null;
        isRecording = false;
        try {
            if (recordLine != null) {
                recordLine.stop();
                recordLine.close();
            }
            if (recordThread != null) {
                recordThread.join(500);
            }
            if (recordStream != null) {
                byte[] pcmData = recordStream.toByteArray();
                recordStream.close();
                return addWavHeader(pcmData, AUDIO_FORMAT);
            }
        } catch (Exception e) {
            System.err.println("Lỗi dừng thu âm: " + e.getMessage());
        }
        return null;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public int getRecordingDurationSeconds() {
        if (!isRecording) return 0;
        return (int) ((System.currentTimeMillis() - recordStartTime) / 1000);
    }

    /**
     * Phát file âm thanh WAV.
     */
    public synchronized void playWav(byte[] wavBytes, Consumer<Boolean> onStatusChanged) {
        stopPlayback();
        if (wavBytes == null || wavBytes.length <= 44) {
            if (onStatusChanged != null) onStatusChanged.accept(false);
            return;
        }
        this.playbackCallback = onStatusChanged;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(wavBytes);
            AudioInputStream ais = AudioSystem.getAudioInputStream(bais);
            currentClip = AudioSystem.getClip();
            currentClip.open(ais);
            isPlaying = true;

            currentClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    stopPlayback();
                }
            });

            if (playbackCallback != null) playbackCallback.accept(true);
            currentClip.start();
        } catch (Exception e) {
            System.err.println("Lỗi phát âm thanh WAV: " + e.getMessage());
            stopPlayback();
        }
    }

    public synchronized void stopPlayback() {
        isPlaying = false;
        if (currentClip != null) {
            try {
                if (currentClip.isRunning()) currentClip.stop();
                currentClip.close();
            } catch (Exception ignored) {}
            currentClip = null;
        }
        if (playbackCallback != null) {
            Consumer<Boolean> cb = playbackCallback;
            playbackCallback = null;
            cb.accept(false);
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    // ==========================================
    // 2. CUỘC GỌI THOẠI TRỰC TIẾP (LIVE VOICE CALL)
    // ==========================================

    /**
     * Khởi động thu âm cho cuộc gọi thoại trực tiếp.
     * Mỗi khi có một chunk âm thanh (~100ms), gọi consumer để gửi qua Socket.
     */
    public synchronized boolean startCallAudio(Consumer<byte[]> onChunkCaptured) {
        stopCallAudio();
        try {
            // 1. Line thu âm (Microphone)
            DataLine.Info captureInfo = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);
            if (!AudioSystem.isLineSupported(captureInfo)) {
                System.err.println("Microphone không hỗ trợ Live Call!");
                return false;
            }
            callCaptureLine = (TargetDataLine) AudioSystem.getLine(captureInfo);
            callCaptureLine.open(AUDIO_FORMAT, CHUNK_SIZE * 4);
            callCaptureLine.start();

            // 2. Line phát âm thanh (Speaker)
            DataLine.Info playInfo = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
            if (!AudioSystem.isLineSupported(playInfo)) {
                System.err.println("Loa không hỗ trợ Live Call!");
                callCaptureLine.close();
                return false;
            }
            callPlaybackLine = (SourceDataLine) AudioSystem.getLine(playInfo);
            callPlaybackLine.open(AUDIO_FORMAT, CHUNK_SIZE * 4);
            callPlaybackLine.start();

            isCallActive.set(true);
            isMuted.set(false);

            // Luồng đọc microphone và gửi qua mạng
            callCaptureThread = new Thread(() -> {
                byte[] chunk = new byte[CHUNK_SIZE];
                while (isCallActive.get() && callCaptureLine != null && callCaptureLine.isOpen()) {
                    int bytesRead = callCaptureLine.read(chunk, 0, chunk.length);
                    if (bytesRead > 0 && !isMuted.get()) {
                        byte[] toSend = new byte[bytesRead];
                        System.arraycopy(chunk, 0, toSend, 0, bytesRead);
                        if (onChunkCaptured != null) {
                            onChunkCaptured.accept(toSend);
                        }
                    }
                }
            }, "Live-Call-Capture-Thread");
            callCaptureThread.setDaemon(true);
            callCaptureThread.start();
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khởi động Live Call Audio: " + e.getMessage());
            stopCallAudio();
            return false;
        }
    }

    /**
     * Phát một chunk âm thanh nhận được từ đối phương trong cuộc gọi.
     */
    public void playCallChunk(byte[] chunk) {
        if (!isCallActive.get() || callPlaybackLine == null || !callPlaybackLine.isOpen() || chunk == null) {
            return;
        }
        try {
            callPlaybackLine.write(chunk, 0, chunk.length);
        } catch (Exception e) {
            System.err.println("Lỗi phát chunk cuộc gọi: " + e.getMessage());
        }
    }

    public synchronized void stopCallAudio() {
        isCallActive.set(false);
        if (callCaptureLine != null) {
            try {
                callCaptureLine.stop();
                callCaptureLine.close();
            } catch (Exception ignored) {}
            callCaptureLine = null;
        }
        if (callPlaybackLine != null) {
            try {
                callPlaybackLine.stop();
                callPlaybackLine.close();
            } catch (Exception ignored) {}
            callPlaybackLine = null;
        }
        if (callCaptureThread != null) {
            callCaptureThread.interrupt();
            callCaptureThread = null;
        }
    }

    public boolean isCallActive() {
        return isCallActive.get();
    }

    public void setMuted(boolean mute) {
        isMuted.set(mute);
    }

    public boolean isMuted() {
        return isMuted.get();
    }

    // ==========================================
    // 3. TẠO CHUÔNG GỌI ĐẾN (SYNTHESIZED RINGTONE)
    // ==========================================

    /**
     * Bắt đầu phát chuông gọi đến dạng âm thanh điện thoại (440Hz + 480Hz).
     */
    public synchronized void startRingtone() {
        if (isRinging) return;
        isRinging = true;

        ringtoneThread = new Thread(() -> {
            try {
                AudioFormat ringFormat = new AudioFormat(8000f, 8, 1, true, false);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, ringFormat);
                if (!AudioSystem.isLineSupported(info)) return;

                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(ringFormat, 4000);
                line.start();

                // Chu kỳ: 1.5 giây reo, 2.0 giây im lặng
                while (isRinging) {
                    byte[] ringBuffer = generateTone(440, 480, 1500);
                    line.write(ringBuffer, 0, ringBuffer.length);

                    // Nghỉ 2 giây
                    for (int s = 0; s < 20 && isRinging; s++) {
                        Thread.sleep(100);
                    }
                }

                line.drain();
                line.stop();
                line.close();
            } catch (Exception ignored) {}
        }, "Ringtone-Thread");
        ringtoneThread.setDaemon(true);
        ringtoneThread.start();
    }

    /**
     * Tạo dữ liệu PCM âm thanh kép (Dual-Tone) cho nhạc chuông.
     */
    public static byte[] generateTone(int freq1, int freq2, int durationMs) {
        int sampleRate = 8000;
        int numSamples = (int) (sampleRate * (durationMs / 1000.0));
        byte[] buffer = new byte[numSamples];
        for (int i = 0; i < buffer.length; i++) {
            double t = (double) i / sampleRate;
            double wave = 0.5 * Math.sin(2 * Math.PI * freq1 * t) + 0.5 * Math.sin(2 * Math.PI * freq2 * t);
            buffer[i] = (byte) (wave * 60);
        }
        return buffer;
    }

    public synchronized void stopRingtone() {
        isRinging = false;
        if (ringtoneThread != null) {
            ringtoneThread.interrupt();
            ringtoneThread = null;
        }
    }

    // ==========================================
    // 4. TIỆN ÍCH ĐÓNG GÓI WAV HEADER (44 BYTES)
    // ==========================================

    /**
     * Tạo file WAV chuẩn từ mảng byte PCM thô.
     */
    public static byte[] addWavHeader(byte[] pcmData, AudioFormat format) {
        int totalAudioLen = pcmData.length;
        int totalDataLen = totalAudioLen + 36;
        int sampleRate = (int) format.getSampleRate();
        int channels = format.getChannels();
        int bitsPerSample = format.getSampleSizeInBits();
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        int blockAlign = channels * bitsPerSample / 8;

        ByteBuffer buffer = ByteBuffer.allocate(44 + totalAudioLen);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // RIFF header
        buffer.put((byte) 'R').put((byte) 'I').put((byte) 'F').put((byte) 'F');
        buffer.putInt(totalDataLen);
        buffer.put((byte) 'W').put((byte) 'A').put((byte) 'V').put((byte) 'E');

        // fmt chunk
        buffer.put((byte) 'f').put((byte) 'm').put((byte) 't').put((byte) ' ');
        buffer.putInt(16); // Subchunk1Size (16 for PCM)
        buffer.putShort((short) 1); // AudioFormat (1 for PCM)
        buffer.putShort((short) channels);
        buffer.putInt(sampleRate);
        buffer.putInt(byteRate);
        buffer.putShort((short) blockAlign);
        buffer.putShort((short) bitsPerSample);

        // data chunk
        buffer.put((byte) 'd').put((byte) 'a').put((byte) 't').put((byte) 'a');
        buffer.putInt(totalAudioLen);
        buffer.put(pcmData);

        return buffer.array();
    }
}
