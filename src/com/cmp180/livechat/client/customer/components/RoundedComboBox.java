package com.cmp180.livechat.client.customer.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedComboBox<E> extends JComboBox<E> {
    private int cornerRadius;

    public RoundedComboBox(E[] items, int radius) {
        super(items);
        this.cornerRadius = radius;
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
        setBorder(new EmptyBorder(5, 10, 5, 10));
        
        setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton("▼");
                button.setContentAreaFilled(false);
                button.setBorderPainted(false);
                button.setFocusPainted(false);
                button.setForeground(new Color(120, 120, 120));
                button.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                return button;
            }
            
            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {}

            @Override
            protected javax.swing.plaf.basic.ComboPopup createPopup() {
                javax.swing.plaf.basic.BasicComboPopup popup = new javax.swing.plaf.basic.BasicComboPopup(comboBox) {
                    @Override
                    protected JScrollPane createScroller() {
                        JScrollPane scroller = super.createScroller();
                        scroller.getViewport().setOpaque(true);
                        scroller.getViewport().setBackground(Color.WHITE);
                        scroller.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
                        return scroller;
                    }
                };
                popup.getList().setOpaque(true);
                popup.getList().setBackground(Color.WHITE);
                return popup;
            }
        });

        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setOpaque(true);
                label.setBorder(new EmptyBorder(8, 12, 8, 12));
                label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                
                if (isSelected) {
                    label.setBackground(new Color(230, 240, 255));
                    label.setForeground(new Color(0, 100, 255));
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(Color.BLACK);
                }
                return label;
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, cornerRadius, cornerRadius));
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (isFocusOwner()) {
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1.5f)); 
        } else {
            g2.setColor(new Color(220, 220, 220));
            g2.setStroke(new BasicStroke(1.0f));
        }
        g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, cornerRadius, cornerRadius));
        g2.dispose();
    }
}