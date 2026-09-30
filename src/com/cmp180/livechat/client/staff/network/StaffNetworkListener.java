package com.cmp180.livechat.client.staff.network;

import java.util.Map;

public interface StaffNetworkListener {
    void onLoginSuccess();
    void onQueueUpdated(Map<String, String> waitingCustomers); // Key: UUID, Value: Tên
    void onPairedWithCustomer(String customerName);
    void onMessageReceived(String senderName, String message);
    void onSessionEnded(String reason);
    void onError(String errorMessage);
}
