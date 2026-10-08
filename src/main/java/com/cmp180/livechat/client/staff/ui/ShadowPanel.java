package com.cmp180.livechat.client.staff.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ShadowPanel extends JPanel {
    private int radius;
    private Color bgColor;
    private int shadowSize = 15;

    public ShadowPanel(int radius, Color bgColor) {
        this.radius = radius;
        this.bgColor = bgColor;
        setOpaque(false);
        // Chừa chỗ để cái bóng đổ ra ngoài mà không bị cắt lẹm
        setBorder(new EmptyBorder(shadowSize, shadowSize, shadowSize + 5, shadowSize)); 
    }

    public ShadowPanel(int radius, Color bgColor, int shadowSize) {
        this.radius = radius;
        this.bgColor = bgColor;
        this.shadowSize = shadowSize;
        setOpaque(false);
        setBorder(new EmptyBorder(shadowSize, shadowSize, shadowSize + 5, shadowSize)); 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth() - shadowSize * 2;
        int height = getHeight() - shadowSize * 2 - 5; // Trừ hao trục Y một chút để bóng rớt xuống dưới

        // Vẽ bóng đổ (Lặp tạo độ mờ dần)
        for (int i = 0; i < shadowSize; i++) {
            g2.setColor(new Color(133, 189, 215, 2 + (shadowSize - i))); 
            g2.fillRoundRect(i, i + 5, width + shadowSize * 2 - i * 2, height + shadowSize * 2 - i * 2 - 5, radius, radius);
        }

        // Vẽ nền chính của Panel
        if (bgColor != null) {
            g2.setColor(bgColor);
            g2.fillRoundRect(shadowSize, shadowSize, width, height, radius, radius);
        }

        // Viền sáng bóng nhẹ (Glass effect)
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(new Color(255, 255, 255, 200));
        g2.drawRoundRect(shadowSize, shadowSize, width - 1, height - 1, radius, radius);

        g2.dispose();
    }
}
