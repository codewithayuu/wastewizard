package com.example.wastewizard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Get views
        ImageView logoImageView = findViewById(R.id.logoImageView);
        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView subtitleTextView = findViewById(R.id.subtitleTextView);

        // Set status bar color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primary_color));

        // Start animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);

        logoImageView.startAnimation(fadeIn);
        titleTextView.startAnimation(slideUp);
        subtitleTextView.startAnimation(bounce);

        // Navigate to enhanced main activity after splash
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, EnhancedMainActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }, SPLASH_DURATION);
    }
}
