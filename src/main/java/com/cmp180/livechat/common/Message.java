package com.cmp180.livechat.common;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String command;
    private String senderId;
    private String receiverId; // Bổ sung định tuyến 1-N
    private String content;
    private String messageId;
    private String replyToSender;
    private String replyToText;
    private String replyToId;
    private String fileName;
    private long fileSize;
    private byte[] fileData;
    private boolean isImage;

    public Message(String command, String senderId, String content) {
        this.command = command;
        this.senderId = senderId;
        this.receiverId = null;
        this.content = content;
        this.messageId = null;
        this.replyToSender = null;
        this.replyToText = null;
        this.replyToId = null;
    }

    public Message(String command, String senderId, String receiverId, String content) {
        this.command = command;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.messageId = null;
        this.replyToSender = null;
        this.replyToText = null;
        this.replyToId = null;
    }

    public Message(String command, String senderId, String receiverId, String content, String messageId, String replyToSender, String replyToText) {
        this(command, senderId, receiverId, content, messageId, replyToSender, replyToText, null, null, 0, null, false);
    }

    public Message(String command, String senderId, String receiverId, String content, String messageId, String replyToSender, String replyToText, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        this(command, senderId, receiverId, content, messageId, replyToSender, replyToText, null, fileName, fileSize, fileData, isImage);
    }

    public Message(String command, String senderId, String receiverId, String content, String messageId, String replyToSender, String replyToText, String replyToId, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        this.command = command;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.messageId = messageId;
        this.replyToSender = replyToSender;
        this.replyToText = replyToText;
        this.replyToId = replyToId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileData = fileData;
        this.isImage = isImage;
    }

    public String getCommand() { return command; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMessageId() { return messageId; }
    public String getReplyToSender() { return replyToSender; }
    public String getReplyToText() { return replyToText; }
    public void setReplyToText(String replyToText) { this.replyToText = replyToText; }
    public String getReplyToId() { return replyToId; }
    public void setReplyToId(String replyToId) { this.replyToId = replyToId; }
    public String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }
    public byte[] getFileData() { return fileData; }
    public boolean isImage() { return isImage; }
}
