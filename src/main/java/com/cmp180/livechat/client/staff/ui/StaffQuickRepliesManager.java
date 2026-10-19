package com.cmp180.livechat.client.staff.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Quản lý tin nhắn nhanh (canned replies) cá nhân của nhân viên CSKH.
 * Ban đầu để trống và lưu trữ bền vững theo tài khoản nhân viên.
 */
public class StaffQuickRepliesManager {

    private static final Preferences prefs = Preferences.userNodeForPackage(StaffQuickRepliesManager.class);
    private static final String PREF_PREFIX = "quick_reply_staff_";
    private static final String DELIMITER = "<::QUICK_REPLY_SEP::>";

    private static String getSafeKey(String staffKey) {
        if (staffKey == null || staffKey.trim().isEmpty()) staffKey = "default";
        return PREF_PREFIX + staffKey.replaceAll("[^a-zA-Z0-9_\\-\\.]", "_");
    }

    public static List<String> getQuickReplies(String staffKey) {
        String key = getSafeKey(staffKey);
        String raw = prefs.get(key, "");
        List<String> list = new ArrayList<>();
        if (raw != null && !raw.trim().isEmpty()) {
            String[] parts = raw.split(DELIMITER);
            for (String p : parts) {
                if (p != null && !p.trim().isEmpty()) {
                    list.add(p.trim());
                }
            }
        }
        return list;
    }

    public static void saveQuickReplies(String staffKey, List<String> list) {
        String key = getSafeKey(staffKey);
        if (list == null || list.isEmpty()) {
            prefs.remove(key);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(DELIMITER);
            sb.append(list.get(i).trim());
        }
        prefs.put(key, sb.toString());
    }

    public static void addQuickReply(String staffKey, String text) {
        if (text == null || text.trim().isEmpty()) return;
        List<String> list = getQuickReplies(staffKey);
        list.add(text.trim());
        saveQuickReplies(staffKey, list);
    }

    public static void updateQuickReply(String staffKey, int index, String newText) {
        if (newText == null || newText.trim().isEmpty()) return;
        List<String> list = getQuickReplies(staffKey);
        if (index >= 0 && index < list.size()) {
            list.set(index, newText.trim());
            saveQuickReplies(staffKey, list);
        }
    }

    public static void deleteQuickReply(String staffKey, int index) {
        List<String> list = getQuickReplies(staffKey);
        if (index >= 0 && index < list.size()) {
            list.remove(index);
            saveQuickReplies(staffKey, list);
        }
    }
}
