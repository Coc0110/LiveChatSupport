package com.cmp180.livechat.client.staff;

public class Main {
    public static void main(String[] args) {
        // Gọi class StaffApp từ một class không kế thừa Application 
        // để trick Java 17 cho phép chạy JavaFX từ Classpath
        StaffApp.main(args);
    }
}
