package com.example.wastewizard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SimpleSplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_splash);
        
        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Navigate to enhanced main activity after splash
        new Handler().postDelayed(() -> {
            try {
                Intent intent = new Intent(SimpleSplashActivity.this, EnhancedMainActivity.class);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                // Fallback to original MainActivity if EnhancedMainActivity fails
                Intent intent = new Intent(SimpleSplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_DURATION);
    }
}
