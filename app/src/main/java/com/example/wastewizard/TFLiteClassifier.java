package com.example.wastewizard;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.TensorFlowLite;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class TFLiteClassifier {

    private static final String MODEL_NAME = "garbage_model.tflite";
    private static final String LABELS_NAME = "labels.txt";

    private static final int IMAGE_SIZE = 180; // Matches model_v217 input size
    private static final int CHANNELS = 3;

    private final Interpreter tflite;
    private final List<String> labels;

    public TFLiteClassifier(Context context) throws IOException {
        // Log TensorFlow Lite runtime version - CRITICAL for debugging
        try {
            String tfliteVersion = TensorFlowLite.runtimeVersion();
            android.util.Log.i("TFLite", "Runtime: " + tfliteVersion);
            android.util.Log.i("TFLiteClassifier", "TensorFlow Lite Runtime Version: " + tfliteVersion);
            
            // Verify we're on 2.16.1 or higher
            if (tfliteVersion.startsWith("2.16")) {
                android.util.Log.i("TFLite", "✅ Runtime version supports FullyConnected v12");
            } else {
                android.util.Log.w("TFLite", "⚠️ Runtime version may not support FullyConnected v12: " + tfliteVersion);
            }
        } catch (Exception e) {
            android.util.Log.w("TFLiteClassifier", "Could not get TFLite version: " + e.getMessage());
        }
        
        MappedByteBuffer model = loadModelFile(context);
        Interpreter.Options options = new Interpreter.Options();
        
        // Use optimized configuration
        options.setNumThreads(4);
        options.setUseXNNPACK(true);
        
        android.util.Log.d("TFLiteClassifier", "Creating interpreter with optimized configuration");
        
        try {
            tflite = new Interpreter(model, options);
            android.util.Log.d("TFLiteClassifier", "Interpreter created successfully");
        } catch (Exception e) {
            android.util.Log.e("TFLiteClassifier", "Failed to create interpreter: " + e.getMessage(), e);
            throw new IOException("Failed to create TensorFlow Lite interpreter: " + e.getMessage(), e);
        }
        
        labels = loadLabels(context);
        
        // Validate model and labels
        if (labels.isEmpty()) {
            throw new IOException("No labels found in " + LABELS_NAME);
        }
        
        // Validate model input/output details
        try {
            int[] inputShape = tflite.getInputTensor(0).shape();
            int[] outputShape = tflite.getOutputTensor(0).shape();
            
            android.util.Log.d("TFLiteClassifier", "Input shape: " + java.util.Arrays.toString(inputShape));
            android.util.Log.d("TFLiteClassifier", "Output shape: " + java.util.Arrays.toString(outputShape));
            
            if (inputShape.length != 4 || inputShape[1] != IMAGE_SIZE || inputShape[2] != IMAGE_SIZE || inputShape[3] != CHANNELS) {
                android.util.Log.w("TFLiteClassifier", "Input shape mismatch. Expected: [1, 180, 180, 3], Got: " + java.util.Arrays.toString(inputShape));
            }
            
            // Log TensorFlow Lite version info
            android.util.Log.d("TFLiteClassifier", "TensorFlow Lite interpreter created successfully");
            
        } catch (Exception e) {
            android.util.Log.e("TFLiteClassifier", "Error validating model: " + e.getMessage());
        }
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fd = context.getAssets().openFd(MODEL_NAME);
        FileInputStream inputStream = new FileInputStream(fd.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fd.getStartOffset();
        long declaredLength = fd.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabels(Context context) throws IOException {
        List<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(context.getAssets().open(LABELS_NAME)))) {
            String line;
            while ((line = br.readLine()) != null) {
                String l = line.trim();
                if (!l.isEmpty()) list.add(l);
            }
        }
        return list;
    }

    public static class Result {
        public final String label;
        public final float confidence; // raw model score
        public final int index;

        public Result(String label, float confidence, int index) {
            this.label = label;
            this.confidence = confidence;
            this.index = index;
        }
    }

    public Result classify(Bitmap bitmap) {
        if (bitmap == null) {
            throw new IllegalArgumentException("Bitmap cannot be null");
        }
        
        try {
            android.util.Log.d("TFLiteClassifier", "Input bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight() + ", config: " + bitmap.getConfig());
            
            // Convert to RGB and resize to 180x180 (exactly like Python: .convert("RGB").resize((180, 180)))
            Bitmap rgbBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            Bitmap resized = Bitmap.createScaledBitmap(rgbBitmap, IMAGE_SIZE, IMAGE_SIZE, true);
            
            android.util.Log.d("TFLiteClassifier", "Resized bitmap: " + resized.getWidth() + "x" + resized.getHeight());

            // Prepare input: float32 0..255 (exactly like Python: dtype=np.float32)
            ByteBuffer inputBuffer = ByteBuffer.allocateDirect(1 * IMAGE_SIZE * IMAGE_SIZE * CHANNELS * 4);
            inputBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[IMAGE_SIZE * IMAGE_SIZE];
            resized.getPixels(intValues, 0, IMAGE_SIZE, 0, 0, IMAGE_SIZE, IMAGE_SIZE);

            // Convert bitmap to float array - RAW pixel values (0-255)
            // The model was trained with raw pixel values, NOT normalized!
            int pixel = 0;
            for (int i = 0; i < IMAGE_SIZE; i++) {
                for (int j = 0; j < IMAGE_SIZE; j++) {
                    int val = intValues[pixel++];
                    // Extract RGB values as RAW float32 (0-255 range)
                    float r = ((val >> 16) & 0xFF);
                    float g = ((val >> 8) & 0xFF);
                    float b = (val & 0xFF);
                    
                    inputBuffer.putFloat(r); // Red (0-255)
                    inputBuffer.putFloat(g);  // Green (0-255)
                    inputBuffer.putFloat(b);         // Blue (0-255)
                    
                    // Log first pixel for debugging
                    if (pixel == 1) {
                        android.util.Log.d("TFLiteClassifier", "First pixel RGB: [" + r + ", " + g + ", " + b + "]");
                    }
                }
            }

            // Output buffer (matching Python output shape)
            int numClasses = labels.size();
            float[][] output = new float[1][numClasses];

            // Run inference (matching Python: interpreter.invoke())
            tflite.run(inputBuffer, output);

            // Apply softmax to convert logits to probabilities (0-1 range)
            float[] scores = output[0];
            android.util.Log.d("TFLiteClassifier", "Raw scores: " + java.util.Arrays.toString(scores));
            
            // Find max value for numerical stability
            float maxScore = scores[0];
            for (int i = 1; i < scores.length; i++) {
                if (scores[i] > maxScore) {
                    maxScore = scores[i];
                }
            }
            
            // Apply softmax: exp(x - max) / sum(exp(x - max))
            float sum = 0.0f;
            float[] softmaxScores = new float[scores.length];
            for (int i = 0; i < scores.length; i++) {
                softmaxScores[i] = (float) Math.exp(scores[i] - maxScore);
                sum += softmaxScores[i];
            }
            
            // Normalize to get probabilities
            for (int i = 0; i < softmaxScores.length; i++) {
                softmaxScores[i] = softmaxScores[i] / sum;
            }
            
            android.util.Log.d("TFLiteClassifier", "Softmax scores: " + java.util.Arrays.toString(softmaxScores));
            
            // Find argmax
            int maxIdx = 0;
            float maxVal = softmaxScores[0];
            for (int i = 1; i < softmaxScores.length; i++) {
                if (softmaxScores[i] > maxVal) {
                    maxVal = softmaxScores[i];
                    maxIdx = i;
                }
            }

            // Get predicted class name (matching Python: class_names[predicted_class])
            String predictedLabel = (maxIdx >= 0 && maxIdx < labels.size()) ? labels.get(maxIdx) : "unknown";
            
            android.util.Log.d("TFLiteClassifier", "Prediction: " + predictedLabel + " (confidence: " + maxVal + ", index: " + maxIdx + ")");
            
            return new Result(predictedLabel, maxVal, maxIdx);
            
        } catch (Exception e) {
            android.util.Log.e("TFLiteClassifier", "Error during classification: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Classification failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get the number of classes the model can classify
     */
    public int getNumClasses() {
        return labels.size();
    }
    
    /**
     * Get all available labels
     */
    public List<String> getLabels() {
        return new ArrayList<>(labels);
    }
    
    /**
     * Check if the model is loaded and ready
     */
    public boolean isModelReady() {
        return tflite != null && !labels.isEmpty();
    }
    
    /**
     * Get model input size
     */
    public int getInputSize() {
        return IMAGE_SIZE;
    }
    
    /**
     * Test the model with a simple input to verify it's working
     */
    public boolean testModel() {
        try {
            // Create a simple test bitmap (all white)
            Bitmap testBitmap = Bitmap.createBitmap(IMAGE_SIZE, IMAGE_SIZE, Bitmap.Config.ARGB_8888);
            testBitmap.eraseColor(0xFFFFFFFF); // White bitmap
            
            // Try to classify it
            Result result = classify(testBitmap);
            
            android.util.Log.d("TFLiteClassifier", "Model test successful. Result: " + result.label);
            return true;
            
        } catch (Exception e) {
            android.util.Log.e("TFLiteClassifier", "Model test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Close the model and free resources
     */
    public void close() {
        try { 
            if (tflite != null) {
                tflite.close(); 
            }
        } catch (Exception ignored) {}
    }
}
