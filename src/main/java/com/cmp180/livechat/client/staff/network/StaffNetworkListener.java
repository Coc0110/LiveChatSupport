package com.cmp180.livechat.client.staff.network;

import com.cmp180.livechat.client.staff.model.CustomerItem;
import java.util.List;

public interface StaffNetworkListener {
    void onLoginSuccess();
    void onQueueUpdated(List<CustomerItem> waitingCustomers);
    void onPairedWithCustomer(String customerName, String customerId);
    void onMessageReceived(String senderName, String senderId, String message);
    void onSessionEnded(String reason);
    void onError(String errorMessage);
}
