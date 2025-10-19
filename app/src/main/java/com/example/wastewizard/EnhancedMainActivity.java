package com.example.wastewizard;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import java.io.IOException;

public class EnhancedMainActivity extends AppCompatActivity {

  // UI Components
  private ImageView imagePreview;
  private Button btnPick, btnCapture, btnPredict;
  private TextView txtPredicted, txtTips, txtReduce, txtRecycle;
  private CardView resultCard;
  private com.google.android.material.card.MaterialCardView cardReduce,
      cardRecycle;
  private CircularProgressIndicator progressIndicator;
  private FloatingActionButton fabStats, fabAchievements;
  private Toolbar toolbar;

  // Game System
  private GameManager gameManager;
  private TextView txtLevel, txtPoints, txtStreak, txtAccuracy;

  // Core functionality
  private TFLiteClassifier classifier;
  private Uri currentImageUri;
  private boolean isModelLoaded = false;

  // Activity result launchers
  private ActivityResultLauncher<Intent> imagePickerLauncher;
  private ActivityResultLauncher<Intent> cameraLauncher;
  private ActivityResultLauncher<String> requestStoragePermissionLauncher;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_enhanced_main);

    // Initialize game manager
    gameManager = new GameManager(this);

    // Initialize UI components
    initializeViews();
    setupToolbar();
    setupActivityResultLaunchers();
    updateGameStats();

    // Load model asynchronously
    loadModelAsync();

    // Setup click listeners
    setupClickListeners();
  }

  private void initializeViews() {
    toolbar = findViewById(R.id.toolbar);
    imagePreview = findViewById(R.id.imagePreview);
    btnPick = findViewById(R.id.btnPick);
    btnCapture = findViewById(R.id.btnCapture);
    btnPredict = findViewById(R.id.btnPredict);
    txtPredicted = findViewById(R.id.txtPredicted);
    txtTips = findViewById(R.id.txtTips);
    txtReduce = findViewById(R.id.txtReduce);
    txtRecycle = findViewById(R.id.txtRecycle);
    resultCard = findViewById(R.id.resultCard);
    cardReduce = findViewById(R.id.cardReduce);
    cardRecycle = findViewById(R.id.cardRecycle);
    progressIndicator = findViewById(R.id.progressIndicator);
    fabStats = findViewById(R.id.fabStats);
    fabAchievements = findViewById(R.id.fabAchievements);

    // Game stats views
    txtLevel = findViewById(R.id.txtLevel);
    txtPoints = findViewById(R.id.txtPoints);
    txtStreak = findViewById(R.id.txtStreak);
    txtAccuracy = findViewById(R.id.txtAccuracy);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayShowTitleEnabled(true);
    }
  }

  private void setupClickListeners() {
    btnPick.setOnClickListener(v -> checkStoragePermissionAndPickImage());
    btnCapture.setOnClickListener(v -> openCamera());
    btnPredict.setOnClickListener(v -> predictWaste());
    fabStats.setOnClickListener(v -> openStatsActivity());
    fabAchievements.setOnClickListener(v -> openAchievementsActivity());
  }

  private void setupActivityResultLaunchers() {
    imagePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(), result -> {
          if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri imageUri = result.getData().getData();
            if (imageUri != null) {
              currentImageUri = imageUri;
              displayImage(imageUri);
              animateImageSelection();
            }
          }
        });

    cameraLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(), result -> {
          if (result.getResultCode() == RESULT_OK) {
            displayImage(currentImageUri);
            animateImageSelection();
          }
        });

    requestStoragePermissionLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(), isGranted -> {
          if (isGranted) {
            openImagePicker();
          } else {
            showSnackbar("Storage permission is required to pick images");
          }
        });
  }

  private void loadModelAsync() {
    progressIndicator.setVisibility(View.VISIBLE);

    new Thread(() -> {
      try {
        classifier = new TFLiteClassifier(this);
        isModelLoaded = true;

        runOnUiThread(() -> {
          progressIndicator.setVisibility(View.GONE);
          showSnackbar("AI model loaded successfully!");
          animateSuccess();
        });

      } catch (Exception e) {
        runOnUiThread(() -> {
          progressIndicator.setVisibility(View.GONE);
          showSnackbar("Failed to load AI model: " + e.getMessage());
        });
      }
    }).start();
  }

  private void checkStoragePermissionAndPickImage() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(
              this, Manifest.permission.READ_MEDIA_IMAGES) !=
          PackageManager.PERMISSION_GRANTED) {
        requestStoragePermissionLauncher.launch(
            Manifest.permission.READ_MEDIA_IMAGES);
        return;
      }
    } else {
      if (ContextCompat.checkSelfPermission(
              this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
          PackageManager.PERMISSION_GRANTED) {
        requestStoragePermissionLauncher.launch(
            Manifest.permission.READ_EXTERNAL_STORAGE);
        return;
      }
    }
    openImagePicker();
  }

  private void openImagePicker() {
    Intent intent = new Intent(Intent.ACTION_PICK,
                               MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    imagePickerLauncher.launch(intent);
  }

  private void openCamera() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
        PackageManager.PERMISSION_GRANTED) {
      requestStoragePermissionLauncher.launch(Manifest.permission.CAMERA);
      return;
    }

    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (intent.resolveActivity(getPackageManager()) != null) {
      cameraLauncher.launch(intent);
    }
  }

  private void displayImage(Uri imageUri) {
    try {
      Bitmap bitmap = loadBitmap(imageUri);
      imagePreview.setImageBitmap(bitmap);
      imagePreview.setVisibility(View.VISIBLE);
      btnPredict.setEnabled(true);
    } catch (Exception e) {
      showSnackbar("Failed to load image: " + e.getMessage());
    }
  }

  private Bitmap loadBitmap(Uri imageUri) throws IOException {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      ImageDecoder.Source source =
          ImageDecoder.createSource(getContentResolver(), imageUri);
      return ImageDecoder.decodeBitmap(source);
    } else {
      return BitmapFactory.decodeStream(
          getContentResolver().openInputStream(imageUri));
    }
  }

  private void predictWaste() {
    if (!isModelLoaded) {
      showSnackbar("AI model is still loading...");
      return;
    }

    if (currentImageUri == null) {
      showSnackbar("Please select an image first");
      return;
    }

    btnPredict.setEnabled(false);
    progressIndicator.setVisibility(View.VISIBLE);

    new Thread(() -> {
      try {
        Bitmap bitmap = loadBitmap(currentImageUri);
        TFLiteClassifier.Result result = classifier.classify(bitmap);

        runOnUiThread(() -> {
          progressIndicator.setVisibility(View.GONE);
          btnPredict.setEnabled(true);

          // Display prediction with enhanced UI
          displayPredictionResult(result);

          // Record prediction for gamification
          boolean isCorrect =
              true; // For demo purposes - you can implement validation
          gameManager.recordPrediction(isCorrect);

          // Check for level up
          if (gameManager.checkLevelUp()) {
            showLevelUpAnimation();
          }

          updateGameStats();
          animatePredictionResult();
        });

      } catch (Exception e) {
        runOnUiThread(() -> {
          progressIndicator.setVisibility(View.GONE);
          btnPredict.setEnabled(true);
          showSnackbar("Prediction failed: " + e.getMessage());
        });
      }
    }).start();
  }

  private void displayPredictionResult(TFLiteClassifier.Result result) {
    String label = result.label;
    float confidence = result.confidence;

    // Display prediction with confidence
    String predictionText = getString(R.string.predicted, label) + " (" +
                            String.format("%.1f%%", confidence * 100) + ")";
    txtPredicted.setText(predictionText);

    // Update tips based on prediction
    txtTips.setText(getTipsFor(label));
    txtReduce.setText(getReduceTips(label));
    txtRecycle.setText(getRecycleTips(label));

    // Update card colors
    updateCardColors(label);

    // Show result card
    resultCard.setVisibility(View.VISIBLE);

    // Add haptic feedback
    vibratePhone();
  }

  private void updateGameStats() {
    txtLevel.setText("Level " + gameManager.getLevel());
    txtPoints.setText(gameManager.getTotalPoints() + " pts");
    txtStreak.setText(gameManager.getCurrentStreak() + " streak");
    txtAccuracy.setText(String.format("%.1f%%", gameManager.getAccuracy()));
  }

  private void animateImageSelection() {
    Animation scaleAnimation =
        AnimationUtils.loadAnimation(this, R.anim.scale_in);
    imagePreview.startAnimation(scaleAnimation);
  }

  private void animatePredictionResult() {
    Animation slideUpAnimation =
        AnimationUtils.loadAnimation(this, R.anim.slide_up);
    resultCard.startAnimation(slideUpAnimation);
  }

  private void animateSuccess() {
    Animation bounceAnimation =
        AnimationUtils.loadAnimation(this, R.anim.bounce);
    fabStats.startAnimation(bounceAnimation);
  }

  private void showLevelUpAnimation() {
    // Create level up popup
    View levelUpView =
        getLayoutInflater().inflate(R.layout.level_up_popup, null);
    Toast levelUpToast = new Toast(this);
    levelUpToast.setView(levelUpView);
    levelUpToast.setDuration(Toast.LENGTH_LONG);
    levelUpToast.show();

    // Vibrate for celebration
    vibratePhone();
  }

  private void vibratePhone() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
      if (vibrator != null) {
        vibrator.vibrate(VibrationEffect.createOneShot(
            100, VibrationEffect.DEFAULT_AMPLITUDE));
      }
    }
  }

  private void openStatsActivity() {
    // TODO: Create StatsActivity
    showSnackbar("Statistics feature coming soon!");
  }

  private void openAchievementsActivity() {
    // TODO: Create AchievementsActivity
    showSnackbar("Achievements feature coming soon!");
  }

  private void showSnackbar(String message) {
    Snackbar
        .make(findViewById(android.R.id.content), message,
              Snackbar.LENGTH_SHORT)
        .show();
  }

  // Helper methods for tips and colors (same as original)
  private String getTipsFor(String label) {
    switch (label.toLowerCase()) {
    case "plastic":
      return "Reduce: use reusable Plastic Items; avoid "
          + "single-use.\nRecycle: rinse, dry; check codes #1, #2.";
    case "glass":
      return "Reduce: reuse jars/bottles.\nRecycle: clean, remove caps; "
          + "don't break.";
    case "paper":
      return "Reduce: go digital; use both sides.\nRecycle: keep clean/dry; "
          + "avoid waxed or greasy.";
    case "metal":
      return "Reduce: choose metal when recyclable.\nRecycle: rinse cans; "
          + "crush if needed.";
    case "cardboard":
      return "Reduce: flatten/reuse boxes.\nRecycle: remove tape; keep dry; "
          + "greasy parts to compost if allowed.";
    default:
      return "General: reduce single-use, reuse, and follow local recycling "
          + "rules.";
    }
  }

  private String getReduceTips(String label) {
    switch (label.toLowerCase()) {
    case "plastic":
      return "Carry a reusable bottle/bag; avoid single-use items.";
    case "glass":
      return "Reuse jars and bottles for storage.";
    case "paper":
      return "Go digital; print on both sides.";
    case "metal":
      return "Choose metal containers when possible.";
    case "cardboard":
      return "Flatten and reuse boxes.";
    default:
      return "Minimize consumption and waste.";
    }
  }

  private String getRecycleTips(String label) {
    switch (label.toLowerCase()) {
    case "plastic":
      return "Check recycling codes; rinse and dry.";
    case "glass":
      return "Remove caps; separate by color if possible.";
    case "paper":
      return "Keep clean and dry; remove staples.";
    case "metal":
      return "Rinse cans; crush aluminum cans.";
    case "cardboard":
      return "Remove tape; keep dry.";
    default:
      return "Follow local recycling guidelines.";
    }
  }

  private void updateCardColors(String label) {
    int color = getClassColor(label);
    cardReduce.setStrokeColor(color);
    cardRecycle.setStrokeColor(color);
  }

  private int getClassColor(String label) {
    switch (label.toLowerCase()) {
    case "plastic":
      return ContextCompat.getColor(this, R.color.cat_plastic);
    case "glass":
      return ContextCompat.getColor(this, R.color.cat_glass);
    case "paper":
      return ContextCompat.getColor(this, R.color.cat_paper);
    case "metal":
      return ContextCompat.getColor(this, R.color.cat_metal);
    case "cardboard":
      return ContextCompat.getColor(this, R.color.cat_cardboard);
    default:
      return ContextCompat.getColor(this,
                                    com.google.android.material.R.color
                                        .m3_ref_palette_dynamic_secondary50);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_enhanced_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_theme) {
      toggleTheme();
      return true;
    } else if (id == R.id.action_stats) {
      openStatsActivity();
      return true;
    } else if (id == R.id.action_achievements) {
      openAchievementsActivity();
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
