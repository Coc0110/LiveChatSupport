package com.cmp180.livechat.common;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Trình quản lý Video Call (Video Engine) cho Live Chat Support.
 *
 * Tính năng:
 * 1. Hỗ trợ bắt hình từ Webcam vật lý (Physical Webcam) qua thư viện Webcam Capture.
 * 2. Tự động dự phòng thông minh (Graceful Fallback) sang Camera ảo (Virtual Avatar) hoặc Chia sẻ màn hình (Screen Share)
 *    khi máy tính không có webcam hoặc khi chạy 2 client trên cùng 1 máy (tránh xung đột khóa webcam DirectShow).
 * 3. Nén khung hình sang định dạng JPEG dung lượng thấp (< 15KB) để truyền không trễ qua UDP.
 * 4. Tích hợp thanh sóng âm thanh từ VoiceManager để hiển thị trực quan người đang nói trên luồng video.
 */
public class VideoManager {

    public enum VideoSource {
        WEBCAM("Webcam vật lý"),
        VIRTUAL_AVATAR("Camera ảo / Avatar"),
        SCREEN_SHARE("Chia sẻ màn hình");

        private final String displayName;
        VideoSource(String displayName) {
            this.displayName = displayName;
        }
        public String getDisplayName() {
            return displayName;
        }
    }

    private static VideoManager instance;

    public static synchronized VideoManager getInstance() {
        if (instance == null) {
            instance = new VideoManager();
        }
        return instance;
    }

    private VideoSource currentSource = VideoSource.WEBCAM;
    private final AtomicBoolean isCapturing = new AtomicBoolean(false);
    private final AtomicBoolean isCameraEnabled = new AtomicBoolean(true);

    private Webcam webcam = null;
    private Thread captureThread = null;
    private Robot screenRobot = null;

    private String currentUserName = "User";
    private Consumer<byte[]> localPreviewConsumer;
    private Consumer<byte[]> networkSendConsumer;

    private long frameCount = 0;

    private VideoManager() {
        // Tắt log ồn ào của BridJ
        System.setProperty("bridj.quiet", "true");
        System.setProperty("webcam.debug", "false");
        try {
            this.screenRobot = new Robot();
        } catch (Exception ignored) {}
    }

    /**
     * Kiểm tra xem máy tính có Webcam vật lý khả dụng hay không.
     */
    public synchronized boolean isWebcamAvailable() {
        try {
            Webcam w = Webcam.getDefault();
            return w != null;
        } catch (Throwable t) {
            return false;
        }
    }

    public synchronized VideoSource getVideoSource() {
        return currentSource;
    }

    public synchronized void setVideoSource(VideoSource source) {
        if (source != null) {
            this.currentSource = source;
        }
    }

    public boolean isCameraEnabled() {
        return isCameraEnabled.get();
    }

    public void setCameraEnabled(boolean enabled) {
        this.isCameraEnabled.set(enabled);
    }

    /**
     * Bắt đầu thu và truyền phát video.
     */
    public synchronized boolean startVideoCapture(String userName, Consumer<byte[]> localPreviewConsumer, Consumer<byte[]> networkSendConsumer) {
        if (isCapturing.get()) {
            stopVideoCapture();
        }

        this.currentUserName = (userName != null && !userName.trim().isEmpty()) ? userName.trim() : "User";
        this.localPreviewConsumer = localPreviewConsumer;
        this.networkSendConsumer = networkSendConsumer;
        this.isCapturing.set(true);
        this.isCameraEnabled.set(true);
        this.frameCount = 0;

        // Nếu chọn webcam nhưng máy không có, tự chuyển sang Virtual Avatar
        if (currentSource == VideoSource.WEBCAM && !isWebcamAvailable()) {
            currentSource = VideoSource.VIRTUAL_AVATAR;
        }

        // Khởi động luồng thu hình
        captureThread = new Thread(this::captureLoop, "Video-Capture-Thread");
        captureThread.setDaemon(true);
        captureThread.start();

        System.out.println("[VideoManager] Bắt đầu luồng video (" + currentSource.getDisplayName() + ") cho " + currentUserName);
        return true;
    }

    private void captureLoop() {
        // Thử mở webcam nếu ở chế độ WEBCAM
        if (currentSource == VideoSource.WEBCAM) {
            try {
                webcam = Webcam.getDefault();
                if (webcam != null) {
                    // Chọn độ phân giải vừa vặn để truyền UDP nhanh
                    Dimension[] dims = webcam.getViewSizes();
                    Dimension bestDim = new Dimension(320, 240);
                    for (Dimension d : dims) {
                        if (d.width <= 480 && d.height <= 360) {
                            bestDim = d;
                            break;
                        }
                    }
                    webcam.setViewSize(bestDim);
                    webcam.open();
                } else {
                    currentSource = VideoSource.VIRTUAL_AVATAR;
                }
            } catch (Throwable t) {
                System.err.println("[VideoManager] Không thể mở webcam vật lý, chuyển sang camera mô phỏng: " + t.getMessage());
                currentSource = VideoSource.VIRTUAL_AVATAR;
            }
        }

        // Tần số khung hình ~15-18 FPS (~60ms mỗi frame)
        final long intervalMs = 60;

        while (isCapturing.get()) {
            long startTime = System.currentTimeMillis();
            frameCount++;

            try {
                BufferedImage frame = null;

                if (!isCameraEnabled.get()) {
                    // Khi camera bị tắt, tạo khung hình hiển thị "Camera đã tắt"
                    frame = generateCameraOffFrame(currentUserName);
                } else {
                    switch (currentSource) {
                        case WEBCAM:
                            if (webcam != null && webcam.isOpen()) {
                                frame = webcam.getImage();
                            }
                            if (frame == null) {
                                frame = generateVirtualAvatarFrame(currentUserName, frameCount);
                            }
                            break;

                        case SCREEN_SHARE:
                            frame = captureScreenFrame();
                            if (frame == null) {
                                frame = generateVirtualAvatarFrame(currentUserName, frameCount);
                            }
                            break;

                        case VIRTUAL_AVATAR:
                        default:
                            frame = generateVirtualAvatarFrame(currentUserName, frameCount);
                            break;
                    }
                }

                if (frame != null) {
                    byte[] jpegBytes = compressToJpeg(frame);
                    if (jpegBytes != null && jpegBytes.length > 0) {
                        if (localPreviewConsumer != null) {
                            localPreviewConsumer.accept(jpegBytes);
                        }
                        if (networkSendConsumer != null) {
                            networkSendConsumer.accept(jpegBytes);
                        }
                    }
                }
            } catch (Exception e) {
                // Tránh ngắt luồng
            }

            long elapsed = System.currentTimeMillis() - startTime;
            long sleepTime = Math.max(10, intervalMs - elapsed);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                break;
            }
        }

        // Dọn dẹp webcam khi luồng kết thúc
        if (webcam != null) {
            try {
                if (webcam.isOpen()) webcam.close();
            } catch (Throwable ignored) {}
            webcam = null;
        }
    }

    private BufferedImage captureScreenFrame() {
        if (screenRobot == null) return null;
        try {
            Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
            // Lấy 80% vùng trung tâm màn hình để hiển thị rõ
            int w = (int) (screenDim.width * 0.85);
            int h = (int) (screenDim.height * 0.85);
            int x = (screenDim.width - w) / 2;
            int y = (screenDim.height - h) / 2;
            BufferedImage full = screenRobot.createScreenCapture(new Rectangle(x, y, w, h));

            // Thu nhỏ xuống 480x270 (16:9) để gửi nhanh qua UDP
            BufferedImage scaled = new BufferedImage(480, 270, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(full, 0, 0, 480, 270, null);

            // Vẽ badge "Đang chia sẻ màn hình"
            g.setColor(new java.awt.Color(16, 185, 129, 210));
            g.fillRoundRect(10, 10, 150, 22, 10, 10);
            g.setColor(java.awt.Color.WHITE);
            g.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 10));
            g.drawString("🖥 Đang chia sẻ màn hình", 16, 25);
            g.dispose();

            return scaled;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Tạo khung hình Camera ảo hiện đại với Avatar, hoạt ảnh sóng âm và thông tin người dùng.
     */
    public BufferedImage generateVirtualAvatarFrame(String name, long frameSeq) {
        int w = 400;
        int h = 300;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Nền chuyển màu Gradient tối hiện đại
        GradientPaint bgGradient = new GradientPaint(
                0, 0, new java.awt.Color(15, 23, 42),
                w, h, new java.awt.Color(30, 41, 59)
        );
        g.setPaint(bgGradient);
        g.fillRect(0, 0, w, h);

        // Vòng tròn ánh sáng lan tỏa nhẹ phía sau avatar
        double pulse = 0.5 + 0.5 * Math.sin(frameSeq * 0.15);
        int glowSize = (int) (140 + 20 * pulse);
        int glowX = (w - glowSize) / 2;
        int glowY = (h - glowSize) / 2 - 20;
        g.setColor(new java.awt.Color(59, 130, 246, (int) (35 + 25 * pulse)));
        g.fill(new Ellipse2D.Double(glowX, glowY, glowSize, glowSize));

        // Hình tròn Avatar
        int avatarSize = 96;
        int avatarX = (w - avatarSize) / 2;
        int avatarY = (h - avatarSize) / 2 - 20;

        GradientPaint avatarGrad = new GradientPaint(
                avatarX, avatarY, new java.awt.Color(37, 99, 235),
                avatarX + avatarSize, avatarY + avatarSize, new java.awt.Color(79, 70, 229)
        );
        g.setPaint(avatarGrad);
        g.fill(new Ellipse2D.Double(avatarX, avatarY, avatarSize, avatarSize));

        // Viền Avatar
        g.setColor(new java.awt.Color(255, 255, 255, 180));
        g.setStroke(new BasicStroke(2.5f));
        g.draw(new Ellipse2D.Double(avatarX, avatarY, avatarSize, avatarSize));

        // Chữ cái đầu của tên người dùng
        String initial = (name != null && !name.trim().isEmpty())
                ? name.trim().substring(0, 1).toUpperCase()
                : "U";
        g.setColor(java.awt.Color.WHITE);
        g.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 38));
        FontMetrics fmInit = g.getFontMetrics();
        int initX = avatarX + (avatarSize - fmInit.stringWidth(initial)) / 2;
        int initY = avatarY + (avatarSize + fmInit.getAscent() - fmInit.getDescent()) / 2 - 2;
        g.drawString(initial, initX, initY);

        // Tên người dùng bên dưới Avatar
        g.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        FontMetrics fmName = g.getFontMetrics();
        int nameX = (w - fmName.stringWidth(name)) / 2;
        int nameY = avatarY + avatarSize + 28;
        g.setColor(new java.awt.Color(241, 245, 249));
        g.drawString(name, nameX, nameY);

        // Sóng âm thanh động dưới tên người dùng
        double liveVol = VoiceManager.getInstance().getLiveVolume();
        int numBars = 9;
        int barWidth = 4;
        int barGap = 4;
        int totalWaveW = numBars * barWidth + (numBars - 1) * barGap;
        int waveStartX = (w - totalWaveW) / 2;
        int waveCenterY = nameY + 20;

        for (int i = 0; i < numBars; i++) {
            double wavePhase = Math.sin((frameSeq * 0.25) + (i * 0.7));
            int barHeight = Math.max(3, (int) (4 + (liveVol * 26 * (0.6 + 0.4 * Math.abs(wavePhase)))));
            int barX = waveStartX + i * (barWidth + barGap);
            int barY = waveCenterY - barHeight / 2;

            g.setColor(liveVol > 0.08 ? new java.awt.Color(56, 189, 248) : new java.awt.Color(148, 163, 184, 140));
            g.fill(new RoundRectangle2D.Double(barX, barY, barWidth, barHeight, 2, 2));
        }

        // Huy hiệu "LIVE" ở góc trên trái
        g.setColor(new java.awt.Color(0, 0, 0, 120));
        g.fill(new RoundRectangle2D.Double(12, 12, 60, 20, 8, 8));
        g.setColor(new java.awt.Color(34, 197, 94));
        g.fill(new Ellipse2D.Double(18, 18, 8, 8));
        g.setColor(java.awt.Color.WHITE);
        g.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 10));
        g.drawString("LIVE", 31, 26);

        // Nguồn video ở góc trên phải
        String srcText = currentSource == VideoSource.SCREEN_SHARE ? "Màn hình" : "Camera ảo";
        g.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 10));
        FontMetrics fmSrc = g.getFontMetrics();
        int srcW = fmSrc.stringWidth(srcText);
        g.setColor(new java.awt.Color(0, 0, 0, 120));
        g.fill(new RoundRectangle2D.Double(w - srcW - 20, 12, srcW + 14, 20, 8, 8));
        g.setColor(new java.awt.Color(203, 213, 225));
        g.drawString(srcText, w - srcW - 13, 26);

        g.dispose();
        return img;
    }

    /**
     * Tạo khung hình thông báo Camera đã tắt.
     */
    public BufferedImage generateCameraOffFrame(String name) {
        int w = 400;
        int h = 300;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Nền xám đen
        g.setColor(new java.awt.Color(24, 24, 27));
        g.fillRect(0, 0, w, h);

        // Vòng tròn mờ
        int avatarSize = 80;
        int avatarX = (w - avatarSize) / 2;
        int avatarY = (h - avatarSize) / 2 - 15;
        g.setColor(new java.awt.Color(63, 63, 70));
        g.fill(new Ellipse2D.Double(avatarX, avatarY, avatarSize, avatarSize));

        String initial = (name != null && !name.trim().isEmpty())
                ? name.trim().substring(0, 1).toUpperCase()
                : "U";
        g.setColor(new java.awt.Color(161, 161, 170));
        g.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 32));
        FontMetrics fmInit = g.getFontMetrics();
        int initX = avatarX + (avatarSize - fmInit.stringWidth(initial)) / 2;
        int initY = avatarY + (avatarSize + fmInit.getAscent() - fmInit.getDescent()) / 2 - 2;
        g.drawString(initial, initX, initY);

        // Thông báo "Camera đã tắt"
        g.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        FontMetrics fmMsg = g.getFontMetrics();
        String offMsg = "Camera đã tắt";
        int msgX = (w - fmMsg.stringWidth(offMsg)) / 2;
        int msgY = avatarY + avatarSize + 28;
        g.setColor(new java.awt.Color(212, 212, 216));
        g.drawString(offMsg, msgX, msgY);

        g.dispose();
        return img;
    }

    private byte[] compressToJpeg(BufferedImage image) {
        if (image == null) return null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Dừng thu và giải phóng tài nguyên video.
     */
    public synchronized void stopVideoCapture() {
        isCapturing.set(false);
        if (captureThread != null) {
            captureThread.interrupt();
            captureThread = null;
        }
        if (webcam != null) {
            try {
                if (webcam.isOpen()) webcam.close();
            } catch (Throwable ignored) {}
            webcam = null;
        }
        System.out.println("[VideoManager] Đã dừng luồng video.");
    }
}
