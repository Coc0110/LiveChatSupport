package com.cmp180.livechat.client.customer.model;

public class CustomerPastMessage {
    public final String senderRole;
    public final String senderName;
    public final String content;
    public final long timestamp;
    public final boolean isRecalled;
    public final String fileName;

    public CustomerPastMessage(String senderRole, String senderName, String content, long timestamp, boolean isRecalled, String fileName) {
        this.senderRole = senderRole;
        this.senderName = senderName;
        this.content = content;
        this.timestamp = timestamp;
        this.isRecalled = isRecalled;
        this.fileName = fileName;
    }
}
