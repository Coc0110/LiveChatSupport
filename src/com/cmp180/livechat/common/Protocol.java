package com.cmp180.livechat.common;

/**
 * Định nghĩa các Hằng số Lệnh Giao thức (Application Protocol)
 * Giúp mã nguồn rõ ràng, tránh gõ sai chuỗi.
 */
public class Protocol {
    public static final String DELIMITER = "\\|";
    public static final String SEP = "|";

    // Các lệnh từ Client -> Server
    public static final String CMD_CONNECT = "CONNECT";
    public static final String CMD_MSG = "MSG";
    public static final String CMD_END = "END";

    // Các lệnh phản hồi từ Server -> Client
    public static final String CMD_WAITING = "WAITING";
    public static final String CMD_PAIRED = "PAIRED";

    // Vai trò hệ thống
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_STAFF = "STAFF";
}
