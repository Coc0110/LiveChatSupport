package com.cmp180.livechat.server;

public class ChatSession {
    private ClientHandler customer;
    private ClientHandler staff;

    public ChatSession(ClientHandler customer, ClientHandler staff) {
        this.customer = customer;
        this.staff = staff;
    }

    public ClientHandler getCustomer() { return customer; }
    public ClientHandler getStaff() { return staff; }
}