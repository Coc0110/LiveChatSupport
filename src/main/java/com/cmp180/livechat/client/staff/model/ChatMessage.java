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
    private String replyToId;
    private boolean recalled;
    private String fileName;
    private long fileSize;
    private byte[] fileData;
    private boolean isImage;

    public ChatMessage(String text, Type type, String senderName) {
        this(UUID.randomUUID().toString(), text, type, senderName, System.currentTimeMillis(), null, null);
    }

    public ChatMessage(String text, Type type, String senderName, long timestamp) {
        this(UUID.randomUUID().toString(), text, type, senderName, timestamp, null, null);
    }

    public ChatMessage(String id, String text, Type type, String senderName, long timestamp, String replyToSender, String replyToText) {
        this(id, text, type, senderName, timestamp, replyToSender, replyToText, null, null, 0, null, false);
    }

    public ChatMessage(String id, String text, Type type, String senderName, long timestamp, String replyToSender, String replyToText, String replyToId) {
        this(id, text, type, senderName, timestamp, replyToSender, replyToText, replyToId, null, 0, null, false);
    }

    public ChatMessage(String id, String text, Type type, String senderName, long timestamp, String replyToSender, String replyToText, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        this(id, text, type, senderName, timestamp, replyToSender, replyToText, null, fileName, fileSize, fileData, isImage);
    }

    public ChatMessage(String id, String text, Type type, String senderName, long timestamp, String replyToSender, String replyToText, String replyToId, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        this.id = (id != null && !id.isEmpty()) ? id : UUID.randomUUID().toString();
        this.text = text;
        this.type = type;
        this.senderName = senderName;
        this.timestamp = timestamp;
        this.status = (type == Type.MY_MSG) ? "Đã gửi" : "";
        this.replyToSender = replyToSender;
        this.replyToText = replyToText;
        this.replyToId = replyToId;
        this.recalled = false;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileData = fileData;
        this.isImage = isImage;
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
    public void setReplyToText(String replyToText) { this.replyToText = replyToText; }
    public String getReplyToId() { return replyToId; }
    public void setReplyToId(String replyToId) { this.replyToId = replyToId; }
    public boolean isRecalled() { return recalled; }
    public void setRecalled(boolean recalled) { this.recalled = recalled; }
    public String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }
    public byte[] getFileData() { return fileData; }
    public boolean isImage() { return isImage; }
    public boolean isFile() { return fileName != null && !fileName.isEmpty(); }
    public boolean isVoice() { return fileName != null && fileName.startsWith("voice_") && fileName.endsWith(".wav"); }

    public String getFormattedFileSize() {
        if (fileSize <= 0) return "";
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
    }

    public String getFileExtension() {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf('.') + 1).toUpperCase();
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new Date(timestamp));
    }
}
