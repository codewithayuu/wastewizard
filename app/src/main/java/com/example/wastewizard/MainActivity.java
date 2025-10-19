package com.example.wastewizard;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private ImageView imageView;
  private MaterialButton btnPick, btnCapture, btnPredict;
  private MaterialCardView resultCard;
  private MaterialTextView txtPredicted, txtTips;
  private MaterialCardView cardReduce, cardRecycle;
  private MaterialTextView txtReduce, txtRecycle, txtReduceTitle,
      txtRecycleTitle;
  private CircularProgressIndicator progressIndicator;

  private Uri currentImageUri = null;
  private Uri cameraImageUri = null;

  private ActivityResultLauncher<String> pickImageLauncher;
  private ActivityResultLauncher<Uri> takePictureLauncher;
  private ActivityResultLauncher<String> requestCameraPermissionLauncher;
  private ActivityResultLauncher<String> requestStoragePermissionLauncher;

  private TFLiteClassifier classifier;

  private static final String PREFS = "settings";
  private static final String KEY_NIGHT_MODE = "night_mode";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    try {
      android.util.Log.d("WasteWizard", "onCreate started");
      applySavedTheme(); // must be before setContentView
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      android.util.Log.d("WasteWizard", "setContentView completed");
    } catch (Exception e) {
      android.util.Log.e("WasteWizard", "Error in onCreate: " + e.getMessage(),
                         e);
      try {
        // Fallback theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      } catch (Exception fallbackError) {
        android.util.Log.e("WasteWizard",
                           "Fallback onCreate failed: " +
                               fallbackError.getMessage(),
                           fallbackError);
        // Last resort - just finish the activity
        finish();
        return;
      }
    }

    try {
      MaterialToolbar toolbar = findViewById(R.id.topAppBar);
      setSupportActionBar(toolbar);

      imageView = findViewById(R.id.imageView);
      btnPick = findViewById(R.id.btnPick);
      btnCapture = findViewById(R.id.btnCapture);
      btnPredict = findViewById(R.id.btnPredict);
      resultCard = findViewById(R.id.resultCard);
      txtPredicted = findViewById(R.id.txtPredicted);
      txtTips = findViewById(R.id.txtTips);
      cardReduce = findViewById(R.id.cardReduce);
      cardRecycle = findViewById(R.id.cardRecycle);
      txtReduce = findViewById(R.id.txtReduce);
      txtRecycle = findViewById(R.id.txtRecycle);
      txtReduceTitle = findViewById(R.id.txtReduceTitle);
      txtRecycleTitle = findViewById(R.id.txtRecycleTitle);
      progressIndicator = findViewById(R.id.progressIndicator);

      android.util.Log.d("WasteWizard", "UI components initialized");
    } catch (Exception e) {
      android.util.Log.e("WasteWizard",
                         "Error initializing UI components: " + e.getMessage(),
                         e);
      // Try to continue with null checks
    }

    // Hide old single tips text; we'll use the two mini cards
    if (txtTips != null) {
      txtTips.setVisibility(View.GONE);
    }

    // Show loading message
    if (imageView != null) {
      Snackbar
          .make(imageView, "Loading AI model...", Snackbar.LENGTH_INDEFINITE)
          .show();
    }

    // Load model asynchronously to prevent blocking UI thread
    loadModelAsync();

    setupActivityResultLaunchers();

    // Set up button click listeners with null checks
    if (btnPick != null) {
      btnPick.setOnClickListener(v -> {
        try {
          if (resultCard != null) {
            resultCard.setVisibility(View.GONE);
          }
          checkStoragePermissionAndPickImage();
        } catch (Exception e) {
          android.util.Log.e("WasteWizard",
                             "Error in btnPick click: " + e.getMessage(), e);
        }
      });
    }

    if (btnCapture != null) {
      btnCapture.setOnClickListener(v -> {
        try {
          if (resultCard != null) {
            resultCard.setVisibility(View.GONE);
          }
          ensureCameraAndLaunch();
        } catch (Exception e) {
          android.util.Log.e("WasteWizard",
                             "Error in btnCapture click: " + e.getMessage(), e);
        }
      });
    }

    if (btnPredict != null) {
      btnPredict.setOnClickListener(v -> {
        try {
          runPrediction();
        } catch (Exception e) {
          android.util.Log.e("WasteWizard",
                             "Error in btnPredict click: " + e.getMessage(), e);
        }
      });
    }

    if (resultCard != null) {
      resultCard.setVisibility(View.GONE);
    }
    if (btnPredict != null) {
      btnPredict.setEnabled(false);
    }
  }

  private void loadModelAsync() {
    new Thread(() -> {
      try {
        android.util.Log.d("WasteWizard",
                           "Starting model loading in background...");
        android.util.Log.d("WasteWizard",
                           "Android version: " + Build.VERSION.RELEASE +
                               " (API " + Build.VERSION.SDK_INT + ")");
        android.util.Log.d("WasteWizard",
                           "Available memory: " +
                               Runtime.getRuntime().maxMemory() / 1024 / 1024 +
                               " MB");

        classifier = new TFLiteClassifier(this);

        // Verify model is ready
        if (!classifier.isModelReady()) {
          throw new IOException("Model failed to initialize properly");
        }

        // Log model info for debugging
        android.util.Log.d("WasteWizard",
                           "Model loaded successfully. Classes: " +
                               classifier.getNumClasses());
        android.util.Log.d("WasteWizard",
                           "Available labels: " + classifier.getLabels());

        // Update UI on main thread
        runOnUiThread(() -> {
          if (btnPredict != null) {
            btnPredict.setEnabled(true);
          }
          if (imageView != null) {
            Snackbar
                .make(imageView,
                      "AI Model ready! (" + classifier.getNumClasses() +
                          " waste categories)",
                      Snackbar.LENGTH_SHORT)
                .show();
          }
        });

      } catch (Exception e) {
        android.util.Log.e("WasteWizard",
                           "Failed to load model: " + e.getMessage(), e);

        // Update UI on main thread
        runOnUiThread(() -> {
          if (imageView != null) {
            Snackbar
                .make(imageView, "Model loading failed: " + e.getMessage(),
                      Snackbar.LENGTH_LONG)
                .show();
          }
          if (btnPredict != null) {
            btnPredict.setEnabled(false);
          }
          classifier = null;
        });
      }
    }).start();
  }

  private void setupActivityResultLaunchers() {
    pickImageLauncher = registerForActivityResult(
        new ActivityResultContracts.GetContent(), uri -> {
          if (uri != null) {
            currentImageUri = uri;
            showImage(uri);
            btnPredict.setEnabled(true);
          }
        });

    takePictureLauncher = registerForActivityResult(
        new ActivityResultContracts.TakePicture(), success -> {
          if (success && cameraImageUri != null) {
            currentImageUri = cameraImageUri;
            showImage(currentImageUri);
            btnPredict.setEnabled(true);
          } else {
            Snackbar
                .make(imageView, getString(R.string.camera_permission_denied),
                      Snackbar.LENGTH_SHORT)
                .show();
          }
        });

    requestCameraPermissionLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(), isGranted -> {
          if (isGranted) {
            launchCamera();
          } else {
            Snackbar
                .make(imageView, getString(R.string.camera_permission_denied),
                      Snackbar.LENGTH_SHORT)
                .show();
          }
        });

    requestStoragePermissionLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(), isGranted -> {
          if (isGranted) {
            pickImageLauncher.launch("image/*");
          } else {
            Snackbar
                .make(imageView,
                      "Storage permission required for gallery access",
                      Snackbar.LENGTH_SHORT)
                .show();
          }
        });
  }

  private void checkStoragePermissionAndPickImage() {
    String permission;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      permission = Manifest.permission.READ_MEDIA_IMAGES;
    } else {
      permission = Manifest.permission.READ_EXTERNAL_STORAGE;
    }

    if (ContextCompat.checkSelfPermission(this, permission) ==
        PackageManager.PERMISSION_GRANTED) {
      pickImageLauncher.launch("image/*");
    } else {
      requestStoragePermissionLauncher.launch(permission);
    }
  }

  private void ensureCameraAndLaunch() {
    // Some OEMs require CAMERA permission for TakePicture contract
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED) {
      launchCamera();
    } else {
      requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }
  }

  private void launchCamera() {
    cameraImageUri = createImageUri();
    if (cameraImageUri == null) {
      Snackbar
          .make(imageView, getString(R.string.image_load_failed),
                Snackbar.LENGTH_SHORT)
          .show();
      return;
    }
    takePictureLauncher.launch(cameraImageUri);
  }

  private Uri createImageUri() {
    try {
      File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
      if (storageDir == null)
        return null;
      String timeStamp =
          new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
      File image = File.createTempFile("WasteWizard_" + timeStamp + "_", ".jpg",
                                       storageDir);
      return FileProvider.getUriForFile(
          this, getPackageName() + ".fileprovider", image);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private void showImage(Uri uri) {
    try {
      Bitmap bitmap;
      if (Build.VERSION.SDK_INT >= 28) {
        ImageDecoder.Source source =
            ImageDecoder.createSource(getContentResolver(), uri);
        bitmap = ImageDecoder.decodeBitmap(source);
      } else {
        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
      }
      imageView.setImageBitmap(bitmap);
    } catch (Exception e) {
      e.printStackTrace();
      Snackbar
          .make(imageView, getString(R.string.image_load_failed),
                Snackbar.LENGTH_SHORT)
          .show();
    }
  }

  private Bitmap loadBitmap(Uri uri) throws IOException {
    if (Build.VERSION.SDK_INT >= 28) {
      ImageDecoder.Source source =
          ImageDecoder.createSource(getContentResolver(), uri);
      return ImageDecoder.decodeBitmap(source);
    } else {
      return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
    }
  }

  private void runPrediction() {
    if (classifier == null) {
      Snackbar
          .make(imageView, getString(R.string.model_loading_failed),
                Snackbar.LENGTH_SHORT)
          .show();
      return;
    }
    if (currentImageUri == null) {
      Snackbar
          .make(imageView, getString(R.string.no_image_selected),
                Snackbar.LENGTH_SHORT)
          .show();
      return;
    }

    // Show loading indicator
    progressIndicator.setVisibility(View.VISIBLE);
    btnPredict.setEnabled(false);

    new Thread(() -> {
      try {
        Bitmap bitmap = loadBitmap(currentImageUri);
        TFLiteClassifier.Result res = classifier.classify(bitmap);

        String label = res.label;
        float conf = res.confidence;

        // Update UI on main thread
        runOnUiThread(() -> {
          progressIndicator.setVisibility(View.GONE);
          btnPredict.setEnabled(true);

          // Display prediction with confidence score
          String predictionText = getString(R.string.predicted, label) + " (" +
                                  String.format("%.1f%%", conf * 100) + ")";
          txtPredicted.setText(predictionText);

          // Log prediction details
          android.util.Log.d("WasteWizard", "Prediction: " + label +
                                                " (confidence: " + conf +
                                                ")");

          populateCardsFor(label);
          resultCard.setVisibility(View.VISIBLE);
        });

      } catch (Exception e) {
        e.printStackTrace();
        runOnUiThread(() -> {
          progressIndicator.setVisibility(View.GONE);
          btnPredict.setEnabled(true);
          Snackbar
              .make(imageView, getString(R.string.prediction_failed),
                    Snackbar.LENGTH_LONG)
              .show();
        });
      }
    }).start();
  }

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

  private void populateCardsFor(String label) {
    // Text content
    txtReduce.setText(getReduceTips(label));
    txtRecycle.setText(getRecycleTips(label));

    // Accent color per class (stroke color only; safe across themes)
    int strokeColor = getClassColor(label);
    cardReduce.setStrokeColor(strokeColor);
    cardRecycle.setStrokeColor(strokeColor);
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

  private String getReduceTips(String label) {
    switch (label.toLowerCase()) {
    case "plastic":
      return "Carry a reusable bottle/bag; avoid single-use items.";
    case "glass":
      return "Reuse jars and bottles whenever possible.";
    case "paper":
      return "Go digital; print only when necessary; use both sides.";
    case "metal":
      return "Choose larger packs and durable metal containers.";
    case "cardboard":
      return "Flatten and reuse boxes; store dry; avoid contamination.";
    default:
      return "Reduce single-use items; choose reusables.";
    }
  }

  private String getRecycleTips(String label) {
    switch (label.toLowerCase()) {
    case "plastic":
      return "Rinse and dry; check codes #1 (PET) & #2 (HDPE).";
    case "glass":
      return "Clean and dry; remove caps; sort by color if required.";
    case "paper":
      return "Keep clean and dry; exclude waxed/greasy paper.";
    case "metal":
      return "Rinse cans; crush to save space; keep lids with cans.";
    case "cardboard":
      return "Remove tape; keep dry; greasy pizza boxes → compost (if "
          + "allowed).";
    default:
      return "Follow local recycling rules; keep items clean and dry.";
    }
  }

  private void applySavedTheme() {
    SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
    int mode = prefs.getInt(KEY_NIGHT_MODE,
                            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    AppCompatDelegate.setDefaultNightMode(mode);
  }

  private void toggleTheme() {
    SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
    int current = prefs.getInt(KEY_NIGHT_MODE,
                               AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    int next = (current == AppCompatDelegate.MODE_NIGHT_YES)
                   ? AppCompatDelegate.MODE_NIGHT_NO
                   : AppCompatDelegate.MODE_NIGHT_YES;
    prefs.edit().putInt(KEY_NIGHT_MODE, next).apply();
    AppCompatDelegate.setDefaultNightMode(next);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_toggle_theme) {
      toggleTheme();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (classifier != null)
      classifier.close();
  }
}
