package com.example.wastewizard;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.view.View;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainAppActivity extends AppCompatActivity {

  private BottomNavigationView bottomNavigation;
  private FloatingActionButton fabScan;
  private androidx.appcompat.widget.Toolbar toolbar;
  private GameManager gameManager;

  // Fragment references
  private DashboardFragment dashboardFragment;
  private ScanFragment scanFragment;
  private LeaderboardFragment leaderboardFragment;
  private ProfileFragment profileFragment;
  private HistoryFragment historyFragment;

  // Activity result launchers
  private ActivityResultLauncher<Intent>
      imagePickerLauncher; // ACTION_PICK (legacy)
  private ActivityResultLauncher<String>
      getContentLauncher; // System picker (recommended)
  private ActivityResultLauncher<Intent>
      cameraLauncher; // Legacy camera thumbnail
  private ActivityResultLauncher<String> requestPermissionLauncher;
  private ActivityResultLauncher<Uri> takePictureLauncher;

  private Uri currentPhotoUri;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Apply dynamic colors on Android 12+ (before setContentView)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
            && AppThemeManager.isDynamicEnabled()) {
      com.google.android.material.color.DynamicColors.applyToActivityIfAvailable(this);
    }

    // Enable edge-to-edge
    WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

    try {
      setContentView(R.layout.activity_main_app);

      // Initialize game manager
      gameManager = new GameManager(this);

      // Initialize UI components
      initializeViews();
      setupFragments();
      setupBottomNavigation();
      setupActivityResultLaunchers();
      setupClickListeners();
      setupEdgeToEdgeInsets();

      // Load default fragment
      if (savedInstanceState == null) {
        loadFragment(dashboardFragment);
      }

      Toast.makeText(this, "Welcome to WasteWizard!", Toast.LENGTH_SHORT)
          .show();
    } catch (Exception e) {
      Toast
          .makeText(this, "Error initializing app: " + e.getMessage(),
                    Toast.LENGTH_LONG)
          .show();
      android.util.Log.e("MainAppActivity", "onCreate error", e);
    }
  }

  private void initializeViews() {
    try {
      toolbar = findViewById(R.id.toolbar);
      bottomNavigation = findViewById(R.id.bottomNavigation);
      fabScan = findViewById(R.id.fabScan);

      // Setup toolbar
      if (toolbar != null) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
          getSupportActionBar().setTitle("WasteWizard");
          getSupportActionBar().setSubtitle(AppThemeManager.getUsername());
        }
      }
    } catch (Exception e) {
      android.util.Log.e("MainAppActivity", "initializeViews error", e);
    }
  }

  private void setupFragments() {
    dashboardFragment = new DashboardFragment();
    scanFragment = new ScanFragment();
    leaderboardFragment = new LeaderboardFragment();
    profileFragment = new ProfileFragment();
    historyFragment = new HistoryFragment();
  }

  private void setupBottomNavigation() {
    bottomNavigation.setOnItemSelectedListener(item -> {
      int itemId = item.getItemId();

      if (itemId == R.id.nav_dashboard) {
        loadFragment(dashboardFragment);
        return true;
      } else if (itemId == R.id.nav_scan) {
        loadFragment(scanFragment);
        return true;
      } else if (itemId == R.id.nav_leaderboard) {
        loadFragment(leaderboardFragment);
        return true;
      } else if (itemId == R.id.nav_profile) {
        loadFragment(profileFragment);
        return true;
      }

      return false;
    });
  }

  private void setupClickListeners() {
    fabScan.setOnClickListener(v -> {
      // Animate FAB
      fabScan.animate()
          .scaleX(0.8f)
          .scaleY(0.8f)
          .setDuration(100)
          .withEndAction(
              ()
                  -> fabScan.animate().scaleX(1.0f).scaleY(1.0f).setDuration(
                      100));

      // Switch to scan fragment - let ScanFragment handle camera/gallery
      loadFragment(scanFragment);
    });
  }

  private void setupActivityResultLaunchers() {
    // Legacy gallery picker (ACTION_PICK)
    imagePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(), result -> {
          if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri imageUri = result.getData().getData();
            // ScanFragment handles its own image picking
          }
        });

    // Recommended: System picker (GET_CONTENT) — works even if no Gallery app
    // installed
    getContentLauncher = registerForActivityResult(
        new ActivityResultContracts.GetContent(), uri -> {
          if (uri != null) {
            // ScanFragment handles its own image picking
          } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT)
                .show();
          }
        });

    // Legacy camera launcher (thumbnail)
    cameraLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(), result -> {
          if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            try {
              Bitmap bitmap = (Bitmap)result.getData().getExtras().get("data");
              if (bitmap != null) {
                // ScanFragment handles its own image capture
                Toast
                    .makeText(this, "Image captured successfully!",
                              Toast.LENGTH_SHORT)
                    .show();
              } else {
                Toast
                    .makeText(this,
                              "Failed to capture image. Please try again.",
                              Toast.LENGTH_SHORT)
                    .show();
              }
            } catch (Exception e) {
              android.util.Log.e("MainAppActivity", "Camera result error", e);
              Toast
                  .makeText(this,
                            "Error processing captured image. Please use " +
                            "Gallery instead.",
                            Toast.LENGTH_SHORT)
                  .show();
              openImagePicker();
            }
          } else if (result.getResultCode() == RESULT_CANCELED) {
            Toast.makeText(this, "Camera cancelled", Toast.LENGTH_SHORT).show();
          } else {
            Toast
                .makeText(this, "Camera failed. Please use Gallery instead.",
                          Toast.LENGTH_SHORT)
                .show();
            openImagePicker();
          }
        });

    // TakePicture contract (full-resolution to Uri)
    takePictureLauncher = registerForActivityResult(
        new ActivityResultContracts.TakePicture(), isSuccess -> {
          if (Boolean.TRUE.equals(isSuccess) && currentPhotoUri != null) {
            // ScanFragment handles its own image capture
            Toast.makeText(this, "Image captured!", Toast.LENGTH_SHORT).show();
          } else {
            Toast.makeText(this, "Capture canceled", Toast.LENGTH_SHORT).show();
          }
        });

    // Permission request for CAMERA - Do not call into fragments
    requestPermissionLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(), isGranted -> {
          // Do not call into fragments. ScanFragment handles its own permission.
          if (!isGranted) {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
          }
        });
  }

  private void loadFragment(Fragment fragment) {
    try {
      FragmentManager fragmentManager = getSupportFragmentManager();
      FragmentTransaction transaction = fragmentManager.beginTransaction();
      transaction.replace(R.id.fragmentContainer, fragment);
      transaction.commitAllowingStateLoss();
    } catch (Exception e) {
      Toast
          .makeText(this, "Error loading screen: " + e.getMessage(),
                    Toast.LENGTH_SHORT)
          .show();
      android.util.Log.e("MainAppActivity", "loadFragment error", e);
    }
  }

  // Public methods for fragments to access
  public GameManager getGameManager() { return gameManager; }

  public ActivityResultLauncher<Intent> getImagePickerLauncher() {
    return imagePickerLauncher;
  }

  public ActivityResultLauncher<Intent> getCameraLauncher() {
    return cameraLauncher;
  }

  public ActivityResultLauncher<String> getRequestPermissionLauncher() {
    return requestPermissionLauncher;
  }

  public void requestPermission(String permission) {
    requestPermissionLauncher.launch(permission);
  }

  // Updated: robust image picker that falls back to system picker
  public void openImagePicker() {
    try {
      Intent intent = new Intent(Intent.ACTION_PICK,
                                 MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
      if (intent.resolveActivity(getPackageManager()) != null) {
        imagePickerLauncher.launch(intent);
      } else {
        // Fallback to system picker (works without any Gallery app)
        getContentLauncher.launch("image/*");
      }
    } catch (Exception e) {
      // Last resort: system picker
      try {
        getContentLauncher.launch("image/*");
      } catch (Exception e2) {
        Toast
            .makeText(this, "Gallery error: " + e2.getMessage(),
                      Toast.LENGTH_SHORT)
            .show();
        android.util.Log.e("MainAppActivity", "Gallery error", e2);
      }
    }
  }

  public void openCamera() {
    // Check permission
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
        PackageManager.PERMISSION_GRANTED) {
      requestPermissionLauncher.launch(Manifest.permission.CAMERA);
      return;
    }

    // Hardware check
    if (!getPackageManager().hasSystemFeature(
            PackageManager.FEATURE_CAMERA_ANY)) {
      Toast
          .makeText(this,
                    "This device doesn't have a camera. Please use Gallery.",
                    Toast.LENGTH_LONG)
          .show();
      openImagePicker();
      return;
    }

    // Create output Uri and launch camera via TakePicture contract
    currentPhotoUri = createImageUri();
    if (currentPhotoUri == null) {
      Toast
          .makeText(this, "Could not create image file. Try Gallery.",
                    Toast.LENGTH_LONG)
          .show();
      return;
    }

    takePictureLauncher.launch(currentPhotoUri);
  }

  private Uri createImageUri() {
    try {
      String name = "wastewizard_" + System.currentTimeMillis() + ".jpg";

      if (Build.VERSION.SDK_INT >= 29) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH,
                   "Pictures/WasteWizard");
        return getContentResolver().insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
      } else {
        java.io.File dir = new java.io.File(
            getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES),
            "WasteWizard");
        if (!dir.exists())
          dir.mkdirs();
        java.io.File file = new java.io.File(dir, name);
        return androidx.core.content.FileProvider.getUriForFile(
            this, getPackageName() + ".fileprovider", file);
      }
    } catch (Exception e) {
      android.util.Log.e("MainAppActivity", "createImageUri error", e);
      return null;
    }
  }

  public void loadFragmentFromFragment(Fragment fragment) {
    loadFragment(fragment);
  }

  public void refreshDashboard() {
    if (dashboardFragment != null)
      dashboardFragment.refreshData();
  }

  public void refreshLeaderboard() {
    if (leaderboardFragment != null)
      leaderboardFragment.refreshData();
  }

  public void refreshProfile() {
    if (profileFragment != null)
      profileFragment.refreshData();
  }

  public void refreshHistory() {
    if (historyFragment != null)
      historyFragment.refreshData();
  }

  @Override
  public boolean onCreateOptionsMenu(android.view.Menu menu) {
    getMenuInflater().inflate(R.menu.main_app_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == R.id.action_settings) {
      // Navigate to SettingsFragment
      getSupportFragmentManager()
              .beginTransaction()
              .replace(R.id.fragmentContainer, new SettingsFragment())
              .addToBackStack("settings")
              .commit();
      return true;
    } else if (item.getItemId() == R.id.action_about) {
      // Navigate to AboutFragment
      getSupportFragmentManager()
              .beginTransaction()
              .replace(R.id.fragmentContainer, new AboutFragment())
              .addToBackStack("about")
              .commit();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (getSupportActionBar() != null) {
      getSupportActionBar().setSubtitle(AppThemeManager.getUsername());
    }
  }

  private void setupEdgeToEdgeInsets() {
    // Simplified edge-to-edge implementation
    // The app will work fine without complex insets handling
  }
}