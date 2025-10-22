package com.example.wastewizard;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.wastewizard.databinding.FragmentScanBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ScanFragment extends Fragment {

    private FragmentScanBinding binding;

    // App-scoped context (safe for toasts, decoding, etc.)
    private Context appContext;

    // ML
    private TFLiteClassifier classifier;
    
    // GameManager for gamification
    private GameManager gameManager;

    // CameraX
    private ProcessCameraProvider cameraProvider;
    private Preview preview;
    private ImageCapture imageCapture;
    private androidx.camera.core.ImageAnalysis imageAnalysis;
    private boolean cameraRunning = false;
    
    // Real-time analysis
    private volatile long lastAnalyzedTime = 0L;
    private final java.util.concurrent.atomic.AtomicBoolean analyzing = new java.util.concurrent.atomic.AtomicBoolean(false);
    private final java.util.concurrent.atomic.AtomicInteger liveSeq = new java.util.concurrent.atomic.AtomicInteger(0);
    private static final long ANALYZE_INTERVAL_MS = 400; // ~2.5 FPS
    private static final float LIVE_MIN_CONFIDENCE = 0.60f; // ignore low-confidence noise

    // GameManager integration
    private TFLiteClassifier.Result lastLiveResult;

    // Executors
    private ExecutorService cameraExecutor;
    private ExecutorService inferenceExecutor;

    // Pickers & permission
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
    private ActivityResultLauncher<String> getContentLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    // Current image
    private Uri currentImageUri;
    private Bitmap currentBitmap;
    
    // Race condition guard
    private final AtomicInteger classifySeq = new AtomicInteger(0);

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appContext = context.getApplicationContext();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new com.google.android.material.transition.MaterialFadeThrough());
        setReturnTransition(new com.google.android.material.transition.MaterialFadeThrough());

        // Init GameManager
        gameManager = new GameManager(requireContext());

        // Init classifier
        try {
            classifier = new TFLiteClassifier(appContext);
      } catch (Exception e) {
            Toast.makeText(appContext, "Failed to load ML model", Toast.LENGTH_LONG).show();
        }
        
        // Init GameManager
        gameManager = new GameManager(appContext);

        cameraExecutor = Executors.newSingleThreadExecutor();
        inferenceExecutor = Executors.newSingleThreadExecutor();

        // Photo Picker (Android 13+)
        pickMediaLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) handleImageUri(uri);
                    }
                }
        );

        // GetContent fallback (<= Android 12)
        getContentLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) handleImageUri(uri);
                    }
                }
        );

        // Camera permission scoped to Fragment
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startCamera();
                    } else {
                        // Check if user selected "Don't ask again"
                        if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                            showCameraPermissionRationale();
                        } else {
                            Toast.makeText(appContext, "Camera permission denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentScanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Buttons
        binding.btnPickImage.setOnClickListener(v -> openPicker());
        binding.btnCapture.setOnClickListener(v -> onCameraButton());
        binding.btnPredict.setOnClickListener(v -> {
            if (currentBitmap != null) runClassification(currentBitmap);
            else toast("Please select or capture an image first");
        });

        // Live result chip click handler
        binding.chipLiveResult.setOnClickListener(v -> {
            if (imageCapture == null) return;
            // take a still photo for history + rock-solid classification
            java.io.File photoFile = new java.io.File(requireContext().getCacheDir(), "live_" + System.currentTimeMillis() + ".jpg");
            ImageCapture.OutputFileOptions opts = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
            showLoading(true);
            imageCapture.takePicture(opts, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
                @Override public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                    Uri uri = Uri.fromFile(photoFile);
                    try {
                        int input = classifier.getInputSize();
                        Bitmap bmp = decodeBitmap(requireContext().getContentResolver(), uri, input, input);
                        TFLiteClassifier.Result res = classifier.classify(bmp);
                        postToMain(() -> {
                            showLoading(false);
                            if (res != null) {
                                binding.resultCard.setVisibility(View.VISIBLE);
                                binding.txtPredicted.setText(String.format(java.util.Locale.getDefault(),
                                        "Predicted: %s (%.1f%%)", res.label, res.confidence * 100f));
                                askCorrectnessAndRecord(res, uri.toString());
                            }
                        });
                    } catch (Exception e) {
                        postToMain(() -> { showLoading(false); toast("Live confirm failed: " + e.getMessage()); });
                    }
                }
                @Override public void onError(@NonNull ImageCaptureException ex) {
                    postToMain(() -> { showLoading(false); toast("Capture failed: " + ex.getMessage()); });
                }
            });
        });

        // If device has no camera, disable capture
        boolean hasCamera = requireContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
        binding.btnCapture.setEnabled(hasCamera);
    }

    private void openPicker() {
        // Stop camera if running (so preview doesn't overlay)
        stopCameraIfRunning();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PickVisualMediaRequest req = new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build();
            pickMediaLauncher.launch(req);
        } else {
            getContentLauncher.launch("image/*");
        }
    }

    private void onCameraButton() {
        if (cameraRunning) {
            // Take a photo
            takePhoto();
        } else {
            // Start camera
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            } else {
                startCamera();
            }
        }
    }

    private void startCamera() {
        if (!isFragmentSafe()) return;

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindUseCases();
                cameraRunning = true;
                binding.cameraPreview.setVisibility(View.VISIBLE);
                binding.noImageLayout.setVisibility(View.GONE);
                binding.imagePreview.setVisibility(View.GONE);
                binding.btnCapture.setText("Snap");
                binding.btnPredict.setEnabled(false); // wait for capture
            } catch (Exception e) {
                toast("Camera error: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindUseCases() {
        if (!isFragmentSafe() || cameraProvider == null) return;

        cameraProvider.unbindAll();

        preview = new Preview.Builder()
                .setTargetResolution(new Size(1280, 720))
                .build();

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(requireView().getDisplay().getRotation())
                .build();

        CameraSelector selector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(binding.cameraPreview.getSurfaceProvider());

        if (AppThemeManager.isRealtimeEnabled()) {
            imageAnalysis = new androidx.camera.core.ImageAnalysis.Builder()
                    .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(new android.util.Size(640, 480)) // smaller is faster
                    .build();

            imageAnalysis.setAnalyzer(inferenceExecutor, image -> {
                long now = System.currentTimeMillis();
                if (now - lastAnalyzedTime < ANALYZE_INTERVAL_MS || !analyzing.compareAndSet(false, true)) {
                    image.close();
                    return;
                }
                lastAnalyzedTime = now;

                try {
                    if (classifier == null || !classifier.isModelReady()) {
                        image.close();
                        return;
                    }
                    int sz = classifier.getInputSize(); // 180
                    Bitmap input = yuvToRgbCenterCropped(image, sz); // this closes image
                    final int token = liveSeq.incrementAndGet();
                    TFLiteClassifier.Result result = classifier.classify(input);

                    postToMain(() -> {
                        if (!isFragmentSafe() || token != liveSeq.get()) return;
                        if (result != null && result.confidence >= LIVE_MIN_CONFIDENCE) {
                            binding.chipLiveResult.setText(
                                    String.format(java.util.Locale.getDefault(), "%s • %.0f%%",
                                            result.label, result.confidence * 100f));
                            binding.chipLiveResult.setVisibility(View.VISIBLE);
                        } else {
                            binding.chipLiveResult.setVisibility(View.GONE);
                        }
                    });
                } catch (Exception ignored) {
                } finally {
                    analyzing.set(false);
                }
            });

            cameraProvider.bindToLifecycle(this, selector, preview, imageCapture, imageAnalysis);
        } else {
            cameraProvider.bindToLifecycle(this, selector, preview, imageCapture);
        }
    }

    private void stopCameraIfRunning() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        cameraRunning = false;
        if (binding != null) {
            binding.cameraPreview.setVisibility(View.GONE);
            binding.btnCapture.setText("Camera");
        }
    }

    private void takePhoto() {
        if (!isFragmentSafe() || imageCapture == null) return;

        showLoading(true);

        File photoFile = new File(requireContext().getCacheDir(),
                "capture_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions opts =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                opts,
                cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        Uri uri = Uri.fromFile(photoFile);
                        postToMain(() -> {
                            stopCameraIfRunning();
                            handleImageUri(uri);
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        postToMain(() -> {
                            showLoading(false);
                            toast("Capture failed: " + exception.getMessage());
                        });
                    }
                }
        );
    }

    private void handleImageUri(@NonNull Uri uri) {
        if (!isFragmentSafe()) return;
        try {
            int input = (classifier != null) ? classifier.getInputSize() : 180;
            Bitmap bmp = decodeBitmap(requireContext().getContentResolver(), uri, input, input);
            currentImageUri = uri;
            currentBitmap = bmp;

            if (binding == null) return;
        binding.imagePreview.setImageBitmap(bmp);
        binding.imagePreview.setVisibility(View.VISIBLE);
        binding.cameraPreview.setVisibility(View.GONE);
        binding.noImageLayout.setVisibility(View.GONE);
        binding.btnPredict.setVisibility(View.GONE); // hide, we auto-run
        showLoading(false);

        if (AppThemeManager.isAutoClassifyEnabled()) {
            runClassification(bmp);
        }
        } catch (Exception e) {
            showLoading(false);
            toast("Failed to load image: " + e.getMessage());
        }
    }

    private void runClassification(@NonNull Bitmap source) {
        if (classifier == null || !classifier.isModelReady()) {
            toast("Model not ready");
            return;
        }
        
        final int token = classifySeq.incrementAndGet();
        showLoading(true);

        inferenceExecutor.execute(() -> {
            try {
                long startTime = System.currentTimeMillis();
                int sz = classifier.getInputSize();
                Bitmap resized = (source.getWidth() == sz && source.getHeight() == sz)
                        ? source
                        : Bitmap.createScaledBitmap(source, sz, sz, true);

                TFLiteClassifier.Result result = classifier.classify(resized);
                long inferenceTime = System.currentTimeMillis() - startTime;
                
                android.util.Log.d("ScanFragment", "Inference completed in " + inferenceTime + "ms");

                if (!isFragmentSafe() || binding == null) return;

                postToMain(() -> {
                    if (token != classifySeq.get()) return; // stale result, ignore
                    showLoading(false);
                    if (result != null) {
                        revealResultCard();
                        binding.txtPredicted.setText(
                                String.format(Locale.getDefault(),
                                        "Predicted: %s (%.1f%%)",
                                        result.label, result.confidence * 100f));
                        
                        // Set tips based on result.label
                        showTipsFor(result.label);
                        updateCardColors(result.label);
                        
                        // Ask user for correctness feedback and record
                        String imagePath = currentImageUri != null ? currentImageUri.toString() : "";
                        askCorrectnessAndRecord(result, imagePath);
                    } else {
                        toast("No result");
                    }
                });
      } catch (Exception e) {
                if (!isFragmentSafe()) return;
                postToMain(() -> {
                    if (token != classifySeq.get()) return; // stale result, ignore
                    showLoading(false);
                    toast("Classification failed: " + e.getMessage());
                });
            }
        });
    }

    private void showLoading(boolean loading) {
        if (binding == null) return;
        binding.progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnPickImage.setEnabled(!loading);
        binding.btnCapture.setEnabled(!loading);
        binding.btnPredict.setEnabled(!loading && currentBitmap != null);
    }

    private static Bitmap decodeBitmap(ContentResolver resolver, Uri uri, int targetW, int targetH) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.Source src = ImageDecoder.createSource(resolver, uri);
            return ImageDecoder.decodeBitmap(src, (decoder, info, src1) -> {
                decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                decoder.setTargetSize(targetW, targetH);
            });
        } else {
            // minSdk is 28, so this path won't be hit
            throw new IOException("Unsupported SDK for decode path");
        }
    }

    private boolean isFragmentSafe() {
        return isAdded() && getContext() != null && binding != null;
    }

    private void toast(String msg) {
        Toast.makeText(appContext, msg, Toast.LENGTH_SHORT).show();
    }
    
    // Safer UI update pattern - prevents crashes if fragment detaches
    private void postToMain(Runnable r) {
        if (binding == null) return;
        binding.getRoot().post(() -> {
            if (isFragmentSafe()) r.run();
        });
    }
    
    // Handle "Don't ask again" with Settings deep link
    private void showCameraPermissionRationale() {
        if (!isFragmentSafe()) return;
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Camera permission needed")
            .setMessage("Enable camera permission in Settings to take photos.")
            .setPositiveButton("Open Settings", (d, w) -> openAppSettings())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void openAppSettings() {
        try {
            Uri uri = Uri.parse("package:" + requireContext().getPackageName());
            startActivity(new android.content.Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri));
        } catch (Exception ignored) { }
    }

    // Enhanced tips display with better content
    private void showTipsFor(String label) {
        switch (label.toLowerCase()) {
            case "plastic":
                binding.txtTips.setText("Rinse bottles; remove caps and labels if required by your local rules.");
                binding.txtReduce.setText("Use refillable bottles; avoid single-use bags.");
                binding.txtRecycle.setText("Most PET/HDPE are recyclable. Check local codes 1 and 2.");
                break;
            case "paper":
                binding.txtTips.setText("Keep paper dry and clean for better recycling.");
                binding.txtReduce.setText("Go digital; opt out of junk mail.");
                binding.txtRecycle.setText("Remove plastic windows; flatten boxes.");
                break;
            case "glass":
                binding.txtTips.setText("Glass can be recycled infinitely. Separate by color for better recycling.");
                binding.txtReduce.setText("Reuse jars and bottles for storage.");
                binding.txtRecycle.setText("Remove caps; separate by color if possible.");
                break;
            case "metal":
                binding.txtTips.setText("Metal is highly recyclable. Aluminum and steel can be recycled indefinitely.");
                binding.txtReduce.setText("Choose metal containers when possible.");
                binding.txtRecycle.setText("Rinse cans; crush aluminum cans.");
                break;
            case "cardboard":
                binding.txtTips.setText("Cardboard can be recycled. Flatten boxes before recycling.");
                binding.txtReduce.setText("Flatten and reuse boxes.");
                binding.txtRecycle.setText("Remove tape; keep dry.");
                break;
            default:
                binding.txtTips.setText("General: reduce single-use, reuse, and follow local recycling rules.");
                binding.txtReduce.setText("Minimize consumption and waste.");
                binding.txtRecycle.setText("Follow local recycling guidelines.");
                break;
        }
    }
    
    // Show user feedback for classification correctness

    // Educational content methods (legacy - keeping for compatibility)
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
        if (binding == null) return;
    int color = getClassColor(label);
        binding.cardReduce.setStrokeColor(color);
        binding.cardRecycle.setStrokeColor(color);
  }

    private int getClassColor(String label) {
        // Use harmonized category colors that blend with dynamic theme
        return CategoryColors.accent(requireContext(), label);
    }

    private void revealResultCard() {
        binding.resultCard.setScaleX(0.98f);
        binding.resultCard.setScaleY(0.98f);
        binding.resultCard.setAlpha(0f);
        binding.resultCard.setVisibility(View.VISIBLE);
        binding.resultCard.animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(180).start();
    }

    private void askCorrectnessAndRecord(@NonNull TFLiteClassifier.Result result, @Nullable String imagePathOrUri) {
        if (!isFragmentSafe()) return;

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Was this correct?")
            .setMessage(String.format(java.util.Locale.getDefault(),
                    "%s • %.1f%%", result.label, result.confidence * 100f))
            .setPositiveButton("Yes", (d, w) -> {
                gameManager.recordPrediction(true);
                gameManager.addScanHistory(imagePathOrUri != null ? imagePathOrUri : "", result.label, result.confidence, System.currentTimeMillis(), true);
                if (gameManager.checkLevelUp()) {
                    com.google.android.material.snackbar.Snackbar
                        .make(binding.getRoot(), "Level up! 🎉", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                        .show();
                }
            })
            .setNegativeButton("No", (d, w) -> {
                gameManager.recordPrediction(false);
                gameManager.addScanHistory(imagePathOrUri != null ? imagePathOrUri : "", result.label, result.confidence, System.currentTimeMillis(), false);
            })
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopCameraIfRunning();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (classifier != null) classifier.close();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        if (inferenceExecutor != null) inferenceExecutor.shutdown();
    }

    private Bitmap yuvToRgbCenterCropped(@NonNull androidx.camera.core.ImageProxy image, int outSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        int crop = Math.min(width, height);
        int startX = (width - crop) / 2;
        int startY = (height - crop) / 2;

        // Read planes
        androidx.camera.core.ImageProxy.PlaneProxy[] planes = image.getPlanes();
        java.nio.ByteBuffer yBuf = planes[0].getBuffer();
        java.nio.ByteBuffer uBuf = planes[1].getBuffer();
        java.nio.ByteBuffer vBuf = planes[2].getBuffer();

        int yRowStride = planes[0].getRowStride();
        int yPixelStride = planes[0].getPixelStride();

        int uRowStride = planes[1].getRowStride();
        int uPixelStride = planes[1].getPixelStride();

        int vRowStride = planes[2].getRowStride();
        int vPixelStride = planes[2].getPixelStride();

        yBuf.rewind(); uBuf.rewind(); vBuf.rewind();

        int[] out = new int[outSize * outSize];

        // Map each output pixel to a source pixel in the center-cropped square (nearest neighbor)
        for (int oy = 0; oy < outSize; oy++) {
            int sy = startY + (oy * crop) / outSize;
            int uvY = sy / 2;

            for (int ox = 0; ox < outSize; ox++) {
                int sx = startX + (ox * crop) / outSize;

                int yIndex = sy * yRowStride + sx * yPixelStride;
                int yVal = yBuf.get(yIndex) & 0xFF;

                int uvX = sx / 2;

                int uIndex = uvY * uRowStride + uvX * uPixelStride;
                int vIndex = uvY * vRowStride + uvX * vPixelStride;

                int uVal = uBuf.get(uIndex) & 0xFF;
                int vVal = vBuf.get(vIndex) & 0xFF;

                int color = yuvToArgb(yVal, uVal, vVal);
                out[oy * outSize + ox] = color;
            }
        }

        image.close();

        Bitmap bmp = Bitmap.createBitmap(outSize, outSize, Bitmap.Config.ARGB_8888);
        bmp.setPixels(out, 0, outSize, 0, 0, outSize, outSize);

        // Apply rotation after sampling
        int rot = image.getImageInfo().getRotationDegrees();
        if (rot != 0) {
            android.graphics.Matrix m = new android.graphics.Matrix();
            m.postRotate(rot);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
        }
        return bmp;
    }

    private static int yuvToArgb(int y, int u, int v) {
        // BT.601 full range
        float yf = (float) y;
        float uf = (float) (u - 128);
        float vf = (float) (v - 128);

        int r = clamp((int) (yf + 1.402f * vf));
        int g = clamp((int) (yf - 0.344136f * uf - 0.714136f * vf));
        int b = clamp((int) (yf + 1.772f * uf));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int clamp(int v) {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return v;
    }
}