package com.cmp180.livechat.client.customer.network;

public interface CustomerNetworkListener {
    void onConnectedToQueue();
    default void onReturnedToQueue(String message) {}
    void onPairedWithStaff(String staffName, String staffId);
    void onMessageReceived(String senderName, String message);
    default void onMessageReceived(String senderName, String message, String messageId, String replyToSender, String replyToText) {
        onMessageReceived(senderName, message);
    }
    default void onMessageReceived(String senderName, String message, String messageId, String replyToSender, String replyToText, String replyToId) {
        onMessageReceived(senderName, message, messageId, replyToSender, replyToText);
    }
    default void onFileReceived(String senderName, String messageId, String caption, String replyToSender, String replyToText, String fileName, long fileSize, byte[] fileData, boolean isImage) {}
    default void onFileReceived(String senderName, String messageId, String caption, String replyToSender, String replyToText, String replyToId, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        onFileReceived(senderName, messageId, caption, replyToSender, replyToText, fileName, fileSize, fileData, isImage);
    }
    default void onChatHistoryReceived(String senderRole, String senderName, long timestamp, String message, String messageId, String replyToSender, String replyToText, boolean isRecalled, String fileName, long fileSize, byte[] fileData, boolean isImage) {}
    default void onChatHistoryReceived(String senderRole, String senderName, long timestamp, String message, String messageId, String replyToSender, String replyToText, String replyToId, boolean isRecalled, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        onChatHistoryReceived(senderRole, senderName, timestamp, message, messageId, replyToSender, replyToText, isRecalled, fileName, fileSize, fileData, isImage);
    }
    default void onMessageRecalled(String messageId) {}
    void onSessionEnded(String reason);
    void onError(String errorMessage);
    default void onPhoneChecked(boolean isExisting, String customerName, java.util.List<com.cmp180.livechat.client.customer.model.CustomerPastSession> pastSessions) {}
    default void onSessionMessagesLoaded(String sessionId, java.util.List<com.cmp180.livechat.client.customer.model.CustomerPastMessage> messages) {}
    default void onTypingStatusChanged(String senderName, boolean isTyping) {}
    default void onVoiceMessageReceived(String senderName, String messageId, String durationText, String replyToSender, String replyToText, byte[] audioData) {}
    default void onVoiceMessageReceived(String senderName, String messageId, String durationText, String replyToSender, String replyToText, String replyToId, byte[] audioData) {
        onVoiceMessageReceived(senderName, messageId, durationText, replyToSender, replyToText, audioData);
    }
    default void onIncomingCall(String callerName, String callerId) {}
    default void onCallAccepted(String peerName) {}
    default void onCallRejected(String reason) {}
    default void onCallEnded() {}
    default void onCallDataReceived(byte[] audioData) {}
}

