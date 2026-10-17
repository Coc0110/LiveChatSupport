package com.cmp180.livechat.client.customer.model;

public class CustomerPastSession {
    public final String sessionId;
    public final String issueTopic;
    public final String staffName;
    public final String status;
    public final int rating;
    public final String feedback;
    public final long createdAt;

    public CustomerPastSession(String sessionId, String issueTopic, String staffName, String status, int rating, String feedback, long createdAt) {
        this.sessionId = sessionId;
        this.issueTopic = issueTopic;
        this.staffName = staffName;
        this.status = status;
        this.rating = rating;
        this.feedback = feedback;
        this.createdAt = createdAt;
    }
}
