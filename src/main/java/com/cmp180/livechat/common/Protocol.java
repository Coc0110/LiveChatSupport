package com.cmp180.livechat.common;

public class Protocol {
    public static final String SEP = "|";
    public static final String SEPARATOR = "|";
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_STAFF = "STAFF";
    
    public static final String CMD_CUST_LOGIN = "CUST_LOGIN";
    public static final String CMD_STAFF_LOGIN = "STAFF_LOGIN";
    public static final String CMD_ACCEPT_CUST = "ACCEPT_CUST";
    public static final String CMD_CHAT = "CHAT_MSG";
    public static final String CMD_FILE = "FILE_MSG";
    public static final String CMD_END = "END_SESSION";
    public static final String CMD_END_SESSION = "END_SESSION";
    public static final String CMD_WAITING = "WAITING";
    public static final String CMD_QUEUE_UPDATE = "QUEUE_UPDATE";
    public static final String CMD_PAIRED = "PAIRED";
    public static final String CMD_ERROR = "ERROR";
    public static final String CMD_GET_QUEUE = "GET_QUEUE";
    public static final String CMD_CHAT_HISTORY = "CHAT_HISTORY";
    public static final String CMD_RECALL_MSG = "RECALL_MSG";
    public static final String CMD_LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String CMD_RATING = "RATING";
    public static final String CMD_CUST_HISTORY = "CUST_HISTORY";
    public static final String CMD_CHECK_PHONE = "CHECK_PHONE";
    public static final String CMD_PHONE_RESULT = "PHONE_RESULT";
    public static final String CMD_GET_SESSION_MSGS = "GET_SESSION_MSGS";
    public static final String CMD_SESSION_MSGS = "SESSION_MSGS";
    public static final String CMD_GET_ONLINE_STAFF = "GET_ONLINE_STAFF";
    public static final String CMD_ONLINE_STAFF_LIST = "ONLINE_STAFF_LIST";
    public static final String CMD_TRANSFER_SESSION = "TRANSFER_SESSION";
    public static final String STATUS_INTERRUPTED = "INTERRUPTED";
    public static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25 MB
}
