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
    public static final String CMD_END = "END_SESSION";
    public static final String CMD_END_SESSION = "END_SESSION";
    public static final String CMD_WAITING = "WAITING";
    public static final String CMD_QUEUE_UPDATE = "QUEUE_UPDATE";
    public static final String CMD_PAIRED = "PAIRED";
    public static final String CMD_ERROR = "ERROR";
}