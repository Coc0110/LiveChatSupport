package com.cmp180.livechat.client.staff.network;

import com.cmp180.livechat.client.staff.model.CustomerItem;
import com.cmp180.livechat.client.staff.model.OnlineStaffItem;
import java.util.List;

public interface StaffNetworkListener {
    void onLoginSuccess();
    void onQueueUpdated(List<CustomerItem> waitingCustomers);
    void onPairedWithCustomer(String customerName, String customerId);
    default void onOnlineStaffListReceived(List<OnlineStaffItem> staffList) {}
    default void onChatHistoryReceived(String customerId, String senderRole, String senderName, long timestamp, String message) {}
    default void onChatHistoryReceived(String customerId, String senderRole, String senderName, long timestamp, String message, String messageId, String replyToSender, String replyToText, boolean isRecalled) {
        onChatHistoryReceived(customerId, senderRole, senderName, timestamp, message);
    }
    default void onChatHistoryReceived(String customerId, String senderRole, String senderName, long timestamp, String message, String messageId, String replyToSender, String replyToText, boolean isRecalled, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        onChatHistoryReceived(customerId, senderRole, senderName, timestamp, message, messageId, replyToSender, replyToText, isRecalled);
    }
    default void onChatHistoryReceived(String customerId, String senderRole, String senderName, long timestamp, String message, String messageId, String replyToSender, String replyToText, String replyToId, boolean isRecalled, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        onChatHistoryReceived(customerId, senderRole, senderName, timestamp, message, messageId, replyToSender, replyToText, isRecalled, fileName, fileSize, fileData, isImage);
    }
    void onMessageReceived(String senderName, String senderId, String message);
    default void onMessageReceived(String senderName, String senderId, String message, String messageId, String replyToSender, String replyToText) {
        onMessageReceived(senderName, senderId, message);
    }
    default void onMessageReceived(String senderName, String senderId, String message, String messageId, String replyToSender, String replyToText, String replyToId) {
        onMessageReceived(senderName, senderId, message, messageId, replyToSender, replyToText);
    }
    default void onFileReceived(String senderName, String senderId, String messageId, String caption, String replyToSender, String replyToText, String fileName, long fileSize, byte[] fileData, boolean isImage) {}
    default void onFileReceived(String senderName, String senderId, String messageId, String caption, String replyToSender, String replyToText, String replyToId, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        onFileReceived(senderName, senderId, messageId, caption, replyToSender, replyToText, fileName, fileSize, fileData, isImage);
    }
    default void onMessageRecalled(String messageId) {}
    void onSessionEnded(String reason);
    void onError(String errorMessage);
    default void onTypingStatusChanged(String customerId, String senderName, boolean isTyping) {}
    default void onVoiceMessageReceived(String senderName, String senderId, String messageId, String durationText, String replyToSender, String replyToText, byte[] audioData) {}
    default void onVoiceMessageReceived(String senderName, String senderId, String messageId, String durationText, String replyToSender, String replyToText, String replyToId, byte[] audioData) {
        onVoiceMessageReceived(senderName, senderId, messageId, durationText, replyToSender, replyToText, audioData);
    }
    default void onIncomingCall(String callerName, String callerId) {}
    default void onIncomingVideoCall(String callerName, String callerId) {}
    default void onCallAccepted(String peerName) {}
    default void onCallRejected(String reason) {}
    default void onCallEnded() {}
    default void onCallDataReceived(byte[] audioData) {}
    default void onVideoDataReceived(byte[] videoData) {}
}

