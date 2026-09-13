package com.cmp180.livechat.common;

import com.cmp180.livechat.server.ClientHandler;

/**
 * Quản lý phiên làm việc 1-1 giữa Khách hàng và CSKH.
 */
public class ChatSession {
    private final ClientHandler customer;
    private final ClientHandler staff;

    public ChatSession(ClientHandler customer, ClientHandler staff) {
        this.customer = customer;
        this.staff = staff;
        this.customer.setSession(this);
        this.staff.setSession(this);
    }

    /**
     * Trả về người đối thoại của sender trong phiên này.
     */
    public ClientHandler getPartner(ClientHandler sender) {
        return (sender == customer) ? staff : customer;
    }

    public ClientHandler getCustomer() {
        return customer;
    }

    public ClientHandler getStaff() {
        return staff;
    }
}
