package com.cmp180.livechat.client.customer;

import com.cmp180.livechat.client.customer.desktop.DesktopApp;
import com.cmp180.livechat.client.customer.mobile.MobileApp;
import javax.swing.SwingUtilities;

public class CustomerClient {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            /* =========================================================
             * LỰA CHỌN GIAO DIỆN ĐỂ CHẠY (Bật/Tắt comment để sử dụng)
             * ========================================================= */
            
            // Chạy form giao diện DESKTOP
            new DesktopApp().setVisible(true);

            // Chạy form giao diện MOBILE
            // new MobileApp().setVisible(true);
        });
    }
}