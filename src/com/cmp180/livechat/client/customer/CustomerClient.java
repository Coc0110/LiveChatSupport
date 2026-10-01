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
            
            // Chạy form giao diện DESKTOP (Đang bật)
            new DesktopApp().setVisible(true);

            // Chạy form giao diện MOBILE (Bỏ dấu // ở đầu dòng dưới để chạy)
            // new MobileApp().setVisible(true);
        });
    }
}