package com.example.wastewizard;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.DynamicColors;

public class AppThemeManager {

    private static final String PREFS = "settings_prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_THEME_MODE = "theme_mode"; // system|light|dark
    private static final String KEY_DYNAMIC_COLOR = "dynamic_color"; // boolean
    private static final String KEY_REALTIME = "realtime_enabled";
    private static final String KEY_AUTO_CLASSIFY = "auto_classify";

    private static SharedPreferences prefs;

    public static void init(@NonNull Context context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        }
        if (!prefs.contains(KEY_THEME_MODE)) {
            prefs.edit().putString(KEY_THEME_MODE, "system").apply();
        }
        if (!prefs.contains(KEY_DYNAMIC_COLOR)) {
            prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, true).apply();
        }
        if (!prefs.contains(KEY_REALTIME)) {
            prefs.edit().putBoolean(KEY_REALTIME, true).apply();        // default ON
        }
        if (!prefs.contains(KEY_AUTO_CLASSIFY)) {
            prefs.edit().putBoolean(KEY_AUTO_CLASSIFY, true).apply(); // default ON
        }
    }

    public static void applyToApp(@NonNull Application app) {
        AppCompatDelegate.setDefaultNightMode(getNightMode());
        if (isDynamicEnabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivitiesIfAvailable(app);
        }
    }

    // Username
    public static String getUsername() {
        return prefs.getString(KEY_USERNAME, "Waste Wizard");
    }

    public static void setUsername(String name) {
        prefs.edit().putString(KEY_USERNAME, name.trim()).apply();
    }

    // Theme mode
    public static void setThemeMode(String mode) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply();
        AppCompatDelegate.setDefaultNightMode(getNightMode());
    }

    public static String getThemeMode() {
        return prefs.getString(KEY_THEME_MODE, "system");
    }

    private static int getNightMode() {
        String mode = getThemeMode();
        switch (mode) {
            case "light": return AppCompatDelegate.MODE_NIGHT_NO;
            case "dark": return AppCompatDelegate.MODE_NIGHT_YES;
            default: return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }

    // Dynamic color
    public static boolean isDynamicEnabled() {
        return prefs.getBoolean(KEY_DYNAMIC_COLOR, true);
    }

    public static void setDynamicEnabled(boolean enabled, Application app) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply();
        // Re-apply dynamic colors on next activity creation; trigger recreate from settings
    }

    // Real-time classification
    public static boolean isRealtimeEnabled() {
        return prefs.getBoolean(KEY_REALTIME, true);
    }
    public static void setRealtimeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_REALTIME, enabled).apply();
    }

    // Auto-classify
    public static boolean isAutoClassifyEnabled() {
        return prefs.getBoolean(KEY_AUTO_CLASSIFY, true);
    }
    public static void setAutoClassifyEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_CLASSIFY, enabled).apply();
    }
}



