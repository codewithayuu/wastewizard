package com.example.wastewizard;

import android.app.Application;

public class WasteWizardApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppThemeManager.init(this);
        // Only set night mode here; dynamic color is applied per-Activity
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            AppThemeManager.getThemeMode().equals("dark") ? 
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES :
            AppThemeManager.getThemeMode().equals("light") ?
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO :
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        );
    }
}



