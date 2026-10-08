package com.cmp180.livechat.client.customer.context;

import com.cmp180.livechat.client.customer.network.CustomerNetworkService;

public class CustomerContext {
    private static CustomerContext instance;
    private String customerName;
    private CustomerNetworkService networkService;

    private CustomerContext() {
        networkService = new CustomerNetworkService();
    }

    public static synchronized CustomerContext getInstance() {
        if (instance == null) {
            instance = new CustomerContext();
        }
        return instance;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String name) {
        this.customerName = name;
    }

    public CustomerNetworkService getNetworkService() {
        return networkService;
    }
}
