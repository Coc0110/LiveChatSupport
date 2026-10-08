package com.cmp180.livechat.common;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String command;
    private String senderId;
    private String receiverId; // Bổ sung định tuyến 1-N
    private String content;

    public Message(String command, String senderId, String content) {
        this.command = command;
        this.senderId = senderId;
        this.receiverId = null;
        this.content = content;
    }

    public Message(String command, String senderId, String receiverId, String content) {
        this.command = command;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
    }

    public String getCommand() { return command; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getContent() { return content; }
}
