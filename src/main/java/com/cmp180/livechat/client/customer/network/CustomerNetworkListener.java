package com.cmp180.livechat.client.customer.network;

public interface CustomerNetworkListener {
    void onConnectedToQueue();
    default void onReturnedToQueue(String message) {}
    void onPairedWithStaff(String staffName, String staffId);
    void onMessageReceived(String senderName, String message);
    default void onMessageReceived(String senderName, String message, String messageId, String replyToSender, String replyToText) {
        onMessageReceived(senderName, message);
    }
    default void onFileReceived(String senderName, String messageId, String caption, String replyToSender, String replyToText, String fileName, long fileSize, byte[] fileData, boolean isImage) {}
    default void onMessageRecalled(String messageId) {}
    void onSessionEnded(String reason);
    void onError(String errorMessage);
    default void onPhoneChecked(boolean isExisting, String customerName, java.util.List<com.cmp180.livechat.client.customer.model.CustomerPastSession> pastSessions) {}
    default void onSessionMessagesLoaded(String sessionId, java.util.List<com.cmp180.livechat.client.customer.model.CustomerPastMessage> messages) {}
}
