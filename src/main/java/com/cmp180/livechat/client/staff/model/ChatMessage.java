package com.cmp180.livechat.client.staff.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class ChatMessage {
    public enum Type {
        MY_MSG, CUSTOMER_MSG, SYSTEM_MSG
    }

    private String id;
    private String text;
    private Type type;
    private String senderName;
    private long timestamp;
    private String status; // "Đã gửi", "Đã đọc", etc.
    private String replyToSender;
    private String replyToText;
    private boolean recalled;

    public ChatMessage(String text, Type type, String senderName) {
        this(UUID.randomUUID().toString(), text, type, senderName, System.currentTimeMillis(), null, null);
    }

    public ChatMessage(String text, Type type, String senderName, long timestamp) {
        this(UUID.randomUUID().toString(), text, type, senderName, timestamp, null, null);
    }

    public ChatMessage(String id, String text, Type type, String senderName, long timestamp, String replyToSender, String replyToText) {
        this.id = (id != null && !id.isEmpty()) ? id : UUID.randomUUID().toString();
        this.text = text;
        this.type = type;
        this.senderName = senderName;
        this.timestamp = timestamp;
        this.status = (type == Type.MY_MSG) ? "Đã gửi" : "";
        this.replyToSender = replyToSender;
        this.replyToText = replyToText;
        this.recalled = false;
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Type getType() { return type; }
    public String getSenderName() { return senderName; }
    public long getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReplyToSender() { return replyToSender; }
    public String getReplyToText() { return replyToText; }
    public boolean isRecalled() { return recalled; }
    public void setRecalled(boolean recalled) { this.recalled = recalled; }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new Date(timestamp));
    }
}
