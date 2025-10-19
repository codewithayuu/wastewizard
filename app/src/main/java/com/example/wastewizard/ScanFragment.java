package com.example.wastewizard;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.io.IOException;

public class ScanFragment extends Fragment {

  private MainAppActivity mainActivity;
  private GameManager gameManager;
  private TFLiteClassifier classifier;

  // UI Components
  private ImageView imagePreview;
  private MaterialButton btnPickImage, btnCapture, btnPredict;
  private MaterialCardView resultCard;
  private TextView txtPredicted, txtTips, txtReduce, txtRecycle;
  private MaterialCardView cardReduce, cardRecycle;
  private CircularProgressIndicator progressIndicator;
  private View noImageLayout;

  private Uri currentImageUri;
  private Bitmap currentBitmap;
  private boolean isModelLoaded = false;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    try {
      View view = inflater.inflate(R.layout.fragment_scan, container, false);

      mainActivity = (MainAppActivity)requireActivity();
      gameManager = mainActivity.getGameManager();

      initializeViews(view);
      setupClickListeners();

      // Initially disable predict button until model loads
      btnPredict.setEnabled(false);

      loadModelAsync();

      return view;
    } catch (Exception e) {
      android.util.Log.e("ScanFragment", "onCreateView error", e);
      return inflater.inflate(R.layout.fragment_scan, container, false);
    }
  }

  private void initializeViews(View view) {
    imagePreview = view.findViewById(R.id.imagePreview);
    btnPickImage = view.findViewById(R.id.btnPickImage);
    btnCapture = view.findViewById(R.id.btnCapture);
    btnPredict = view.findViewById(R.id.btnPredict);
    resultCard = view.findViewById(R.id.resultCard);
    txtPredicted = view.findViewById(R.id.txtPredicted);
    txtTips = view.findViewById(R.id.txtTips);
    txtReduce = view.findViewById(R.id.txtReduce);
    txtRecycle = view.findViewById(R.id.txtRecycle);
    cardReduce = view.findViewById(R.id.cardReduce);
    cardRecycle = view.findViewById(R.id.cardRecycle);
    progressIndicator = view.findViewById(R.id.progressIndicator);
    noImageLayout = view.findViewById(R.id.noImageLayout);
  }

  private void setupClickListeners() {
    // Use system picker; no storage permission needed
    btnPickImage.setOnClickListener(v -> {
      try {
        mainActivity.openImagePicker();
      } catch (Exception e) {
        Toast
            .makeText(requireContext(),
                      "Failed to open gallery: " + e.getMessage(),
                      Toast.LENGTH_SHORT)
            .show();
      }
    });

    btnCapture.setOnClickListener(v -> {
      try {
        if (ContextCompat.checkSelfPermission(requireContext(),
                                              Manifest.permission.CAMERA) !=
            PackageManager.PERMISSION_GRANTED) {
          mainActivity.requestPermission(Manifest.permission.CAMERA);
          return;
        }

        // Show loading feedback
        btnCapture.setText("Opening Camera...");
        btnCapture.setEnabled(false);

        // Reset button after a short delay
        btnCapture.postDelayed(() -> {
          btnCapture.setText("Camera");
          btnCapture.setEnabled(true);
        }, 2000);

        mainActivity.openCamera();
      } catch (Exception e) {
        btnCapture.setText("Camera");
        btnCapture.setEnabled(true);
        Toast
            .makeText(requireContext(),
                      "Failed to open camera: " + e.getMessage(),
                      Toast.LENGTH_SHORT)
            .show();
      }
    });

    btnPredict.setOnClickListener(v -> predictWaste());
  }

  private void loadModelAsync() {
    progressIndicator.setVisibility(View.VISIBLE);

    new Thread(() -> {
      try {
        classifier = new TFLiteClassifier(requireContext());
        isModelLoaded = true;

        requireActivity().runOnUiThread(() -> {
          progressIndicator.setVisibility(View.GONE);
          btnPredict.setEnabled(true);
          Toast
              .makeText(requireContext(), "AI model loaded successfully!",
                        Toast.LENGTH_SHORT)
              .show();
        });

      } catch (Exception e) {
        isModelLoaded = false;
        requireActivity().runOnUiThread(() -> {
          progressIndicator.setVisibility(View.GONE);
          btnPredict.setEnabled(false);
          Toast
              .makeText(requireContext(),
                        "Failed to load AI model. Please restart the app.",
                        Toast.LENGTH_LONG)
              .show();
          android.util.Log.e("ScanFragment", "Model loading failed", e);
        });
      }
    }).start();
  }

  public void setImageUri(Uri imageUri) {
    currentImageUri = imageUri;
    currentBitmap = null; // Clear cached bitmap
    displayImage(imageUri);
  }

  public void setBitmap(Bitmap bitmap) {
    currentBitmap = bitmap;
    currentImageUri = null; // Clear cached URI
    displayBitmap(bitmap);
  }

  private void displayImage(Uri imageUri) {
    try {
      Bitmap bitmap = loadBitmap(imageUri);
      displayBitmap(bitmap);
    } catch (Exception e) {
      Toast
          .makeText(requireContext(), "Failed to load image: " + e.getMessage(),
                    Toast.LENGTH_SHORT)
          .show();
    }
  }

  private void displayBitmap(Bitmap bitmap) {
    imagePreview.setImageBitmap(bitmap);
    imagePreview.setVisibility(View.VISIBLE);
    noImageLayout.setVisibility(View.GONE); // Hide "No image selected" layout
    btnPredict.setEnabled(true);

    // Hide previous results when new image is loaded
    resultCard.setVisibility(View.GONE);

    // Animate image appearance
    imagePreview.animate().alpha(0f).setDuration(0).withEndAction(
        () -> { imagePreview.animate().alpha(1f).setDuration(300); });
  }

  private void resetImagePreview() {
    imagePreview.setVisibility(View.GONE);
    noImageLayout.setVisibility(View.VISIBLE);
    currentBitmap = null;
    currentImageUri = null;
    btnPredict.setEnabled(false);
    resultCard.setVisibility(View.GONE);
  }

  private Bitmap loadBitmap(Uri imageUri) throws IOException {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      ImageDecoder.Source source = ImageDecoder.createSource(
          requireContext().getContentResolver(), imageUri);
      return ImageDecoder.decodeBitmap(source);
    } else {
      return BitmapFactory.decodeStream(
          requireContext().getContentResolver().openInputStream(imageUri));
    }
  }

  public void onPermissionGranted() {
    Toast.makeText(requireContext(), "Permission granted!", Toast.LENGTH_SHORT)
        .show();
  }

  private void predictWaste() {
    if (!isModelLoaded) {
      Toast
          .makeText(requireContext(), "AI model is still loading...",
                    Toast.LENGTH_SHORT)
          .show();
      return;
    }

    if (currentBitmap == null && currentImageUri == null) {
      Toast
          .makeText(requireContext(), "Please select an image first",
                    Toast.LENGTH_SHORT)
          .show();
      return;
    }

    btnPredict.setEnabled(false);
    progressIndicator.setVisibility(View.VISIBLE);

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

        requireActivity().runOnUiThread(() -> {
          progressIndicator.setVisibility(View.GONE);
          btnPredict.setEnabled(true);

          // Display prediction result
          displayPredictionResult(result);

          // Record prediction for gamification
          boolean isCorrect = true; // For demo purposes
          gameManager.recordPrediction(isCorrect);

          // Save scan to history
          String imagePath = currentImageUri != null
                                 ? currentImageUri.toString()
                                 : "camera_capture";
          gameManager.addScanHistory(imagePath, result.label, result.confidence,
                                     System.currentTimeMillis());

          // Check for level up
          if (gameManager.checkLevelUp()) {
            showLevelUpAnimation();
          }

          // Refresh dashboard and history
          mainActivity.refreshDashboard();
          mainActivity.refreshHistory();
        });

      } catch (Exception e) {
        requireActivity().runOnUiThread(() -> {
          progressIndicator.setVisibility(View.GONE);
          btnPredict.setEnabled(true);
          Toast
              .makeText(requireContext(),
                        "Prediction failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT)
              .show();
        });
      }
    }).start();
  }

  private void displayPredictionResult(TFLiteClassifier.Result result) {
    String label = result.label;
    float confidence = result.confidence;

    String predictionText = "Predicted: " + label + " (" +
                            String.format("%.1f%%", confidence * 100) + ")";
    txtPredicted.setText(predictionText);

    txtTips.setText(getTipsFor(label));
    txtReduce.setText(getReduceTips(label));
    txtRecycle.setText(getRecycleTips(label));

    updateCardColors(label);

    resultCard.setVisibility(View.VISIBLE);
    resultCard.animate().alpha(0f).setDuration(0).withEndAction(
        () -> { resultCard.animate().alpha(1f).setDuration(300); });
  }

  private void showLevelUpAnimation() {
    Toast.makeText(requireContext(), "🎉 Level Up! 🎉", Toast.LENGTH_LONG)
        .show();
  }

  private String getTipsFor(String label) {
    switch (label.toLowerCase()) {
    case "plastic":
      return "Reduce: use reusable Plastic Items; avoid single-use.\nRecycle: "
          + "rinse, dry; check codes #1, #2.";
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
      return ContextCompat.getColor(requireContext(), R.color.cat_plastic);
    case "glass":
      return ContextCompat.getColor(requireContext(), R.color.cat_glass);
    case "paper":
      return ContextCompat.getColor(requireContext(), R.color.cat_paper);
    case "metal":
      return ContextCompat.getColor(requireContext(), R.color.cat_metal);
    case "cardboard":
      return ContextCompat.getColor(requireContext(), R.color.cat_cardboard);
    default:
      return ContextCompat.getColor(requireContext(),
                                    com.google.android.material.R.color
                                        .m3_ref_palette_dynamic_secondary50);
    }
  }
}