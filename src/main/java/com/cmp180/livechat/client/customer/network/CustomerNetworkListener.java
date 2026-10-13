package com.cmp180.livechat.client.customer.network;

public interface CustomerNetworkListener {
    void onConnectedToQueue();
    default void onReturnedToQueue(String message) {}
    void onPairedWithStaff(String staffName, String staffId);
    void onMessageReceived(String senderName, String message);
    void onSessionEnded(String reason);
    void onError(String errorMessage);
}
