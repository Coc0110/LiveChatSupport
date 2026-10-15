package com.cmp180.livechat.client.ui;

import javafx.scene.Scene;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class ThemeManager {
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    private static final String PREF_PRIMARY_COLOR = "theme_primary_color";
    private static final String PREF_DARK_MODE = "theme_dark_mode";

    public static final String DEFAULT_COLOR = "#00838F"; // Deep Cyan

    public static final String[] PRESET_COLORS = {
        "#00838F", // Cyan
        "#0084FF", // Xanh Zalo / Dương
        "#2E7D32", // Xanh lá Emerald
        "#7B1FA2", // Tím Hoàng Gia
        "#E65100", // Cam Sunset
        "#C2185B", // Hồng Ruby
        "#C62828", // Đỏ Crimson
        "#455A64"  // Xám Khói Slate
    };

    public static final String[] PRESET_NAMES = {
        "Cyan (Mặc định)",
        "Xanh dương",
        "Xanh lá",
        "Tím",
        "Cam",
        "Hồng",
        "Đỏ",
        "Xám khói"
    };

    private static String currentColor = prefs.get(PREF_PRIMARY_COLOR, DEFAULT_COLOR);
    private static boolean isDarkMode = prefs.getBoolean(PREF_DARK_MODE, false);

    private static final List<Runnable> listeners = new ArrayList<>();

    public static void addListener(Runnable listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    public static String getPrimaryColor() {
        return currentColor;
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static void setPrimaryColor(String hex) {
        currentColor = hex;
        prefs.put(PREF_PRIMARY_COLOR, hex);
        notifyListeners();
    }

    public static void setDarkMode(boolean dark) {
        isDarkMode = dark;
        prefs.putBoolean(PREF_DARK_MODE, dark);
        notifyListeners();
    }

    public static void resetToDefault() {
        currentColor = DEFAULT_COLOR;
        isDarkMode = false;
        prefs.put(PREF_PRIMARY_COLOR, DEFAULT_COLOR);
        prefs.putBoolean(PREF_DARK_MODE, false);
        notifyListeners();
    }

    private static void notifyListeners() {
        for (Runnable r : new ArrayList<>(listeners)) {
            try {
                r.run();
            } catch (Exception ignored) {}
        }
    }

    public static String getLightTint(String hex) {
        try {
            Color c = Color.web(hex);
            return String.format("rgba(%d, %d, %d, 0.15)", (int)(c.getRed() * 255), (int)(c.getGreen() * 255), (int)(c.getBlue() * 255));
        } catch (Exception e) {
            return "rgba(0, 131, 143, 0.15)";
        }
    }

    public static String getBgTint(String hex) {
        try {
            Color c = Color.web(hex);
            double r = c.getRed() * 0.08 + 0.92;
            double g = c.getGreen() * 0.08 + 0.92;
            double b = c.getBlue() * 0.08 + 0.92;
            return String.format("#%02X%02X%02X", (int)(r * 255), (int)(g * 255), (int)(b * 255));
        } catch (Exception e) {
            return "#e8f4f5";
        }
    }

    public static void applyTheme(Scene scene) {
        if (scene == null || scene.getRoot() == null) return;
        var root = scene.getRoot();
        if (isDarkMode) {
            if (!root.getStyleClass().contains("dark-mode")) {
                root.getStyleClass().add("dark-mode");
            }
        } else {
            root.getStyleClass().remove("dark-mode");
        }

        String color = currentColor;
        String light = getLightTint(color);
        String bg = getBgTint(color);

        root.setStyle(String.format("-fx-brand-color: %s; -fx-brand-light: %s; -fx-brand-bg: %s;", color, light, bg));
    }
}
