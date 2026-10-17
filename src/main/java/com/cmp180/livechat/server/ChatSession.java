package com.cmp180.livechat.server;

import java.util.UUID;

public class ChatSession {
    private final String sessionId;
    private final ClientHandler customer;
    private final ClientHandler staff;
    private final String issueTopic;
    private final long startTime;

    public ChatSession(ClientHandler customer, ClientHandler staff, String issueTopic) {
        this(UUID.randomUUID().toString(), customer, staff, issueTopic);
    }

    public ChatSession(String sessionId, ClientHandler customer, ClientHandler staff, String issueTopic) {
        this.sessionId = sessionId;
        this.customer = customer;
        this.staff = staff;
        this.issueTopic = issueTopic;
        this.startTime = System.currentTimeMillis();
    }

    public String getSessionId() { return sessionId; }
    public ClientHandler getCustomer() { return customer; }
    public ClientHandler getStaff() { return staff; }
    public String getIssueTopic() { return issueTopic; }
    public long getStartTime() { return startTime; }
}