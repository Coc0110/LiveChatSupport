package com.cmp180.livechat.client.staff.network;

import com.cmp180.livechat.client.staff.model.CustomerItem;
import java.util.List;

public interface StaffNetworkListener {
    void onLoginSuccess();
    void onQueueUpdated(List<CustomerItem> waitingCustomers);
    void onPairedWithCustomer(String customerName, String customerId);
    default void onChatHistoryReceived(String customerId, String senderRole, String senderName, long timestamp, String message) {}
    default void onChatHistoryReceived(String customerId, String senderRole, String senderName, long timestamp, String message, String messageId, String replyToSender, String replyToText, boolean isRecalled) {
        onChatHistoryReceived(customerId, senderRole, senderName, timestamp, message);
    }
    void onMessageReceived(String senderName, String senderId, String message);
    default void onMessageReceived(String senderName, String senderId, String message, String messageId, String replyToSender, String replyToText) {
        onMessageReceived(senderName, senderId, message);
    }
    default void onMessageRecalled(String messageId) {}
    void onSessionEnded(String reason);
    void onError(String errorMessage);
}
