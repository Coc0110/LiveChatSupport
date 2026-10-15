package com.cmp180.livechat.client.ui;

import java.util.prefs.Preferences;

public class ServerConfig {
    private static final Preferences prefs = Preferences.userNodeForPackage(ServerConfig.class);
    private static final String PREF_SERVER_HOST = "server_host";
    private static final String PREF_SERVER_PORT = "server_port";

    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 5000;

    private static String host = prefs.get(PREF_SERVER_HOST, DEFAULT_HOST);
    private static int port = prefs.getInt(PREF_SERVER_PORT, DEFAULT_PORT);

    public static String getHost() {
        return host;
    }

    public static void setHost(String newHost) {
        if (newHost == null || newHost.trim().isEmpty()) {
            newHost = DEFAULT_HOST;
        }
        host = newHost.trim();
        prefs.put(PREF_SERVER_HOST, host);
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int newPort) {
        if (newPort <= 0 || newPort > 65535) {
            newPort = DEFAULT_PORT;
        }
        port = newPort;
        prefs.putInt(PREF_SERVER_PORT, port);
    }

    public static void resetToDefault() {
        setHost(DEFAULT_HOST);
        setPort(DEFAULT_PORT);
    }
}
