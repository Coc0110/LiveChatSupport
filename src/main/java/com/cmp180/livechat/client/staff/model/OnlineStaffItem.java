package com.cmp180.livechat.client.staff.model;

public class OnlineStaffItem {
    private final String staffId;
    private final String fullName;
    private final String primarySkill;
    private final int activeSessions;

    public OnlineStaffItem(String staffId, String fullName, String primarySkill, int activeSessions) {
        this.staffId = staffId;
        this.fullName = fullName;
        this.primarySkill = primarySkill != null && !primarySkill.trim().isEmpty() ? primarySkill.trim() : "Tất cả";
        this.activeSessions = activeSessions;
    }

    public String getStaffId() {
        return staffId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPrimarySkill() {
        return primarySkill;
    }

    public int getActiveSessions() {
        return activeSessions;
    }

    public boolean isBusy() {
        return activeSessions >= 3;
    }

    @Override
    public String toString() {
        return fullName + " (" + primarySkill + ") - " + activeSessions + "/3 phiên";
    }
}
