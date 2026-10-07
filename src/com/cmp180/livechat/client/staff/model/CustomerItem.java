package com.cmp180.livechat.client.staff.model;

public class CustomerItem {
    private String id;
    private String fullName;
    private boolean isActive;
    private long joinedTime;
    
    // Local UI states
    private boolean hasUnread;
    private long lastMessageTime;

    public CustomerItem(String id, String fullName, boolean isActive, long joinedTime) {
        this.id = id;
        this.fullName = fullName;
        this.isActive = isActive;
        this.joinedTime = joinedTime;
        this.hasUnread = false;
        this.lastMessageTime = 0; // 0 means no messages yet
    }

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public boolean isActive() { return isActive; }
    public long getJoinedTime() { return joinedTime; }
    
    public void setActive(boolean active) { this.isActive = active; }
    public void setFullName(String name) { this.fullName = name; }
    
    public boolean hasUnread() { return hasUnread; }
    public void setUnread(boolean unread) { this.hasUnread = unread; }
    
    public long getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(long time) { this.lastMessageTime = time; }
}
