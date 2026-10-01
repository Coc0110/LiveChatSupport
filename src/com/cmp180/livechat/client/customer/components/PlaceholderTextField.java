package com.cmp180.livechat.client.customer.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class PlaceholderTextField extends JTextField {
    private String placeholder;
    private int cornerRadius;

    public PlaceholderTextField(int radius, String placeholder) {
        super();
        this.cornerRadius = radius;
        this.placeholder = placeholder;
        setOpaque(false);
        setBorder(new EmptyBorder(5, 15, 5, 15));
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setForeground(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Vẽ nền bo góc
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, cornerRadius, cornerRadius));
        g2.dispose();
        
        super.paintComponent(g);
        
        // Trực tiếp vẽ placeholder nếu TextField đang trống (Không làm bẩn giá trị thật)
        if (getText().isEmpty() && placeholder != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(150, 150, 150));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            FontMetrics fm = g2d.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2d.drawString(placeholder, getInsets().left, y);
            g2d.dispose();
        }
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (isFocusOwner()) {
            g2.setColor(new Color(0, 122, 255));
            g2.setStroke(new BasicStroke(1.5f));
        } else {
            g2.setColor(new Color(220, 220, 220));
            g2.setStroke(new BasicStroke(1.0f));
        }
        g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, cornerRadius, cornerRadius));
        g2.dispose();
    }
}