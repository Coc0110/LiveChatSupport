package com.cmp180.livechat.client.ui;

import java.util.prefs.Preferences;

public class ServerConfig {
    private static final Preferences prefs = Preferences.userNodeForPackage(ServerConfig.class);
    private static final String PREF_SERVER_HOST = "server_host";
    private static final String PREF_SERVER_PORT = "server_port";
    private static final String PREF_SERVER_UDP_PORT = "server_udp_port";
    private static final String PREF_SOUND_ENABLED = "sound_enabled";

    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 5000;
    public static final int DEFAULT_UDP_PORT = 5000;
    public static final boolean DEFAULT_SOUND_ENABLED = true;

    private static String host = prefs.get(PREF_SERVER_HOST, DEFAULT_HOST);
    private static int port = prefs.getInt(PREF_SERVER_PORT, DEFAULT_PORT);
    private static int udpPort = prefs.getInt(PREF_SERVER_UDP_PORT, DEFAULT_UDP_PORT);
    private static boolean soundEnabled = prefs.getBoolean(PREF_SOUND_ENABLED, DEFAULT_SOUND_ENABLED);

    static {
        com.cmp180.livechat.common.SoundManager.setSoundEnabled(soundEnabled);
    }

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

    public static int getUdpPort() {
        return udpPort;
    }

    public static void setUdpPort(int newUdpPort) {
        if (newUdpPort <= 0 || newUdpPort > 65535) {
            newUdpPort = DEFAULT_UDP_PORT;
        }
        udpPort = newUdpPort;
        prefs.putInt(PREF_SERVER_UDP_PORT, udpPort);
    }

    public static boolean isSoundEnabled() {
        return soundEnabled;
    }

    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
        prefs.putBoolean(PREF_SOUND_ENABLED, enabled);
        com.cmp180.livechat.common.SoundManager.setSoundEnabled(enabled);
    }

    public static void resetToDefault() {
        setHost(DEFAULT_HOST);
        setPort(DEFAULT_PORT);
        setUdpPort(DEFAULT_UDP_PORT);
        setSoundEnabled(DEFAULT_SOUND_ENABLED);
    }
}
