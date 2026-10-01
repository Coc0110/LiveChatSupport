package com.cmp180.livechat.client.staff.context;

import com.cmp180.livechat.client.staff.network.StaffNetworkService;

public class StaffContext {
    private static StaffContext instance;
    private String staffName;
    private StaffNetworkService networkService;
    
    // Singleton pattern
    private StaffContext() {
        networkService = new StaffNetworkService();
    }
    
    public static StaffContext getInstance() {
        if (instance == null) {
            instance = new StaffContext();
        }
        return instance;
    }
    
    public String getStaffName() { 
        return staffName; 
    }
    
    public void setStaffName(String name) { 
        this.staffName = name; 
    }
    
    public StaffNetworkService getNetworkService() { 
        return networkService; 
    }
}
