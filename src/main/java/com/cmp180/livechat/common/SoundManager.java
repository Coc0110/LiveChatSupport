package com.cmp180.livechat.common;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundManager {
    private static final ExecutorService soundExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "SoundNotifierThread");
        t.setDaemon(true);
        return t;
    });

    private static volatile boolean soundEnabled = true;

    public static boolean isSoundEnabled() {
        return soundEnabled;
    }

    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }

    /**
     * Phát âm thanh thông báo nhẹ nhàng (soft chime) khi có tin nhắn mới hoặc khách hàng mới kết nối.
     */
    public static void playNotificationSound() {
        if (!soundEnabled) return;
        soundExecutor.submit(() -> {
            try {
                int sampleRate = 44100;
                int duration1 = (int) (sampleRate * 0.08); // 80ms tone 1
                int duration2 = (int) (sampleRate * 0.14); // 140ms tone 2
                int totalSamples = duration1 + duration2;

                byte[] buffer = new byte[totalSamples * 2];
                int idx = 0;

                // Tone 1: 784 Hz (G5)
                for (int i = 0; i < duration1; i++) {
                    double progress = (double) i / duration1;
                    double amp = Math.sin(2.0 * Math.PI * 783.99 * i / sampleRate) * (1.0 - progress * 0.5) * 0.22;
                    short sample = (short) (amp * 32767);
                    buffer[idx++] = (byte) (sample & 0xFF);
                    buffer[idx++] = (byte) ((sample >> 8) & 0xFF);
                }

                // Tone 2: 1046.5 Hz (C6) với exponential decay
                for (int i = 0; i < duration2; i++) {
                    double progress = (double) i / duration2;
                    double amp = Math.sin(2.0 * Math.PI * 1046.5 * i / sampleRate) * Math.exp(-progress * 4.0) * 0.28;
                    short sample = (short) (amp * 32767);
                    buffer[idx++] = (byte) (sample & 0xFF);
                    buffer[idx++] = (byte) ((sample >> 8) & 0xFF);
                }

                AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
                try (AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(buffer), format, totalSamples)) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(ais);
                    clip.start();
                    Thread.sleep(260);
                    clip.close();
                }
            } catch (Exception e) {
                try {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                } catch (Exception ignored) {}
            }
        });
    }
}
