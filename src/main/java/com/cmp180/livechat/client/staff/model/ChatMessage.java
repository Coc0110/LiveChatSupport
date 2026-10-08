package com.cmp180.livechat.client.staff.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatMessage {
    public enum Type {
        MY_MSG, CUSTOMER_MSG, SYSTEM_MSG
    }

    private String text;
    private Type type;
    private String senderName;
    private long timestamp;
    private String status; // "Đã gửi", "Đã đọc", etc.

    public ChatMessage(String text, Type type, String senderName) {
        this.text = text;
        this.type = type;
        this.senderName = senderName;
        this.timestamp = System.currentTimeMillis();
        this.status = (type == Type.MY_MSG) ? "Đã gửi" : ""; // Default status
    }

    public String getText() { return text; }
    public Type getType() { return type; }
    public String getSenderName() { return senderName; }
    public long getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    
    public void setStatus(String status) { this.status = status; }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new Date(timestamp));
    }
}
