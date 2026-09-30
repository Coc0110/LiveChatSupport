package com.cmp180.livechat.client.staff;

import com.cmp180.livechat.client.staff.ui.StaffMainFrame;
import javax.swing.SwingUtilities;

public class StaffClient {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StaffMainFrame().setVisible(true);
        });
    }
}