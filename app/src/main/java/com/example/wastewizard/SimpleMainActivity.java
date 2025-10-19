package com.example.wastewizard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.io.IOException;

public class SimpleMainActivity extends AppCompatActivity {

  private ImageView imagePreview;
  private MaterialButton btnPickImage, btnCapture, btnPredict;
  private MaterialCardView resultCard;
  private TextView txtPredicted, txtTips;
  private Uri currentImageUri;
  private Bitmap currentBitmap;
  private TFLiteClassifier classifier;
  private boolean isModelLoaded = false;

  // Activity result launchers
  private ActivityResultLauncher<Intent> imagePickerLauncher;
  private ActivityResultLauncher<Intent> cameraLauncher;
  private ActivityResultLauncher<String> requestPermissionLauncher;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_simple_main);

    initializeViews();
    setupActivityResultLaunchers();
    setupClickListeners();
    loadModelAsync();

    Toast.makeText(this, "WasteWizard Ready!", Toast.LENGTH_SHORT).show();
  }

  private void initializeViews() {
    imagePreview = findViewById(R.id.imagePreview);
    btnPickImage = findViewById(R.id.btnPickImage);
    btnCapture = findViewById(R.id.btnCapture);
    btnPredict = findViewById(R.id.btnPredict);
    resultCard = findViewById(R.id.resultCard);
    txtPredicted = findViewById(R.id.txtPredicted);
    txtTips = findViewById(R.id.txtTips);
  }

  private void setupClickListeners() {
    btnPickImage.setOnClickListener(v -> {
      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          if (ContextCompat.checkSelfPermission(
                  this, Manifest.permission.READ_MEDIA_IMAGES) !=
              PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                Manifest.permission.READ_MEDIA_IMAGES);
            return;
          }
        } else {
          if (ContextCompat.checkSelfPermission(
                  this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
              PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                Manifest.permission.READ_EXTERNAL_STORAGE);
            return;
          }
        }
        openImagePicker();
      } catch (Exception e) {
        Toast
            .makeText(this, "Failed to open gallery: " + e.getMessage(),
                      Toast.LENGTH_SHORT)
            .show();
      }
    });

    btnCapture.setOnClickListener(v -> {
      try {
        if (ContextCompat.checkSelfPermission(this,
                                              Manifest.permission.CAMERA) !=
            PackageManager.PERMISSION_GRANTED) {
          requestPermissionLauncher.launch(Manifest.permission.CAMERA);
          return;
        }
        openCamera();
      } catch (Exception e) {
        Toast
            .makeText(this, "Failed to open camera: " + e.getMessage(),
                      Toast.LENGTH_SHORT)
            .show();
      }
    });

    btnPredict.setOnClickListener(v -> predictWaste());
  }

  private void setupActivityResultLaunchers() {
    imagePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(), result -> {
          if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri imageUri = result.getData().getData();
            if (imageUri != null) {
              currentImageUri = imageUri;
              displayImage(imageUri);
            }
          }
        });

    cameraLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(), result -> {
          if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Bitmap bitmap = (Bitmap)result.getData().getExtras().get("data");
            if (bitmap != null) {
              currentBitmap = bitmap;
              displayBitmap(bitmap);
            }
          }
        });

    requestPermissionLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(), isGranted -> {
          if (isGranted) {
            Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT)
                .show();
          } else {
            Toast
                .makeText(this, "Permission required for this feature",
                          Toast.LENGTH_SHORT)
                .show();
          }
        });
  }

  private void openImagePicker() {
    try {
      Intent intent = new Intent(Intent.ACTION_PICK,
                                 MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
      imagePickerLauncher.launch(intent);
    } catch (Exception e) {
      Toast
          .makeText(this, "Failed to open image picker: " + e.getMessage(),
                    Toast.LENGTH_SHORT)
          .show();
    }
  }

  private void openCamera() {
    try {
      Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      if (intent.resolveActivity(getPackageManager()) != null) {
        cameraLauncher.launch(intent);
      } else {
        Toast.makeText(this, "Camera app not available", Toast.LENGTH_SHORT)
            .show();
      }
    } catch (Exception e) {
      Toast
          .makeText(this, "Failed to open camera: " + e.getMessage(),
                    Toast.LENGTH_SHORT)
          .show();
    }
  }

  private void displayImage(Uri imageUri) {
    try {
      Bitmap bitmap = loadBitmap(imageUri);
      displayBitmap(bitmap);
    } catch (Exception e) {
      Toast
          .makeText(this, "Failed to load image: " + e.getMessage(),
                    Toast.LENGTH_SHORT)
          .show();
    }
  }

  private void displayBitmap(Bitmap bitmap) {
    imagePreview.setImageBitmap(bitmap);
    imagePreview.setVisibility(View.VISIBLE);
    btnPredict.setEnabled(true);
  }

  private Bitmap loadBitmap(Uri imageUri) throws IOException {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      ImageDecoder.Source source =
          ImageDecoder.createSource(getContentResolver(), imageUri);
      return ImageDecoder.decodeBitmap(source);
    } else {
      return MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
    }
  }

  private void loadModelAsync() {
    new Thread(() -> {
      try {
        classifier = new TFLiteClassifier(this);
        isModelLoaded = true;
        runOnUiThread(() -> {
          Toast
              .makeText(this, "AI model loaded successfully!",
                        Toast.LENGTH_SHORT)
              .show();
        });
      } catch (Exception e) {
        runOnUiThread(() -> {
          Toast
              .makeText(this, "Failed to load AI model: " + e.getMessage(),
                        Toast.LENGTH_SHORT)
              .show();
        });
      }
    }).start();
  }

  private void predictWaste() {
    if (!isModelLoaded) {
      Toast.makeText(this, "AI model is still loading...", Toast.LENGTH_SHORT)
          .show();
      return;
    }

    if (currentBitmap == null && currentImageUri == null) {
      Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT)
          .show();
      return;
    }

    btnPredict.setEnabled(false);

    new Thread(() -> {
      try {
        Bitmap bitmap = currentBitmap;
        if (bitmap == null && currentImageUri != null) {
          bitmap = loadBitmap(currentImageUri);
        }

        if (bitmap == null) {
          throw new Exception("Failed to load image");
        }

        TFLiteClassifier.Result result = classifier.classify(bitmap);

        runOnUiThread(() -> {
          btnPredict.setEnabled(true);

          // Display prediction result
          String label = result.label;
          float confidence = result.confidence;

          String predictionText = "Predicted: " + label + " (" +
                                  String.format("%.1f%%", confidence * 100) +
                                  ")";
          txtPredicted.setText(predictionText);

          // Update tips based on prediction
          txtTips.setText(getTipsFor(label));

          // Show result card
          resultCard.setVisibility(View.VISIBLE);

          Toast.makeText(this, "Classification complete!", Toast.LENGTH_SHORT)
              .show();
        });

      } catch (Exception e) {
        runOnUiThread(() -> {
          btnPredict.setEnabled(true);
          Toast
              .makeText(this, "Prediction failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT)
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
}
