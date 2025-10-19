package com.example.wastewizard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class TestMainActivity extends AppCompatActivity {

    private ImageView imageView;
    private MaterialButton btnPick, btnCapture, btnPredict;
    private MaterialCardView resultCard;
    private TextView txtPredicted, txtTips;
    private Uri currentImageUri;
    
    // Activity result launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> requestStoragePermissionLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize UI components
        initializeViews();
        setupToolbar();
        setupActivityResultLaunchers();
        setupClickListeners();
        
        Toast.makeText(this, "App loaded successfully!", Toast.LENGTH_SHORT).show();
    }
    
    private void initializeViews() {
        imageView = findViewById(R.id.imagePreview);
        btnPick = findViewById(R.id.btnPick);
        btnCapture = findViewById(R.id.btnCapture);
        btnPredict = findViewById(R.id.btnPredict);
        resultCard = findViewById(R.id.resultCard);
        txtPredicted = findViewById(R.id.txtPredicted);
        txtTips = findViewById(R.id.txtTips);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    
    private void setupClickListeners() {
        btnPick.setOnClickListener(v -> checkStoragePermissionAndPickImage());
        btnCapture.setOnClickListener(v -> openCamera());
        btnPredict.setOnClickListener(v -> predictWaste());
    }
    
    private void setupActivityResultLaunchers() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        currentImageUri = imageUri;
                        imageView.setImageURI(imageUri);
                        imageView.setVisibility(View.VISIBLE);
                        btnPredict.setEnabled(true);
                        Toast.makeText(this, "Image selected!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
        
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    imageView.setImageURI(currentImageUri);
                    imageView.setVisibility(View.VISIBLE);
                    btnPredict.setEnabled(true);
                    Toast.makeText(this, "Photo captured!", Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        requestStoragePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    private void checkStoragePermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                return;
            }
        }
        openImagePicker();
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
    
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermissionLauncher.launch(Manifest.permission.CAMERA);
            return;
        }
        
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        }
    }
    
    private void predictWaste() {
        if (currentImageUri == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Simple test prediction
        txtPredicted.setText("Test Prediction: Plastic (85.2%)");
        txtTips.setText("This is a test prediction. The actual AI model is not loaded in this test version.");
        resultCard.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Test prediction completed!", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_theme) {
            toggleTheme();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void toggleTheme() {
        int currentTheme = AppCompatDelegate.getDefaultNightMode();
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }
}
