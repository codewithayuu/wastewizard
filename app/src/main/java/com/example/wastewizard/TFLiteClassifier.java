package com.example.wastewizard;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.TensorFlowLite;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod;
import org.tensorflow.lite.support.image.ImageProcessor;

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
    private List<String> labels;
    private final int inputSize = 180;
    private org.tensorflow.lite.DataType inType, outType;
    private boolean inputIsQuant = false;
    private org.tensorflow.lite.support.image.ImageProcessor procFloat;
    private org.tensorflow.lite.support.image.ImageProcessor procUint8;
    private org.tensorflow.lite.support.image.TensorImage tensorImage;
    private float[][] outFloat;
    private byte[][] outByte;
    private float outScale = 1f; 
    private int outZero = 0;

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
        
        // Setup unified preprocessing pipeline
        setupPipelines();
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

    private void setupPipelines() {
        inType = tflite.getInputTensor(0).dataType();
        outType = tflite.getOutputTensor(0).dataType();
        inputIsQuant = (inType == org.tensorflow.lite.DataType.UINT8);

        int numClasses = tflite.getOutputTensor(0).shape()[1];
        // Make sure labels size matches numClasses (no auto-drop of headers)
        labels = sanitizeLabels(labels, numClasses);

        if (inputIsQuant) {
            procUint8 = new org.tensorflow.lite.support.image.ImageProcessor.Builder()
                    .add(new org.tensorflow.lite.support.image.ops.ResizeOp(inputSize, inputSize,
                            org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod.BILINEAR))
                    .build();
            tensorImage = new org.tensorflow.lite.support.image.TensorImage(org.tensorflow.lite.DataType.UINT8);
        } else {
            // FLOAT32 with RAW 0..255 input → no NormalizeOp
            procFloat = new org.tensorflow.lite.support.image.ImageProcessor.Builder()
                    .add(new org.tensorflow.lite.support.image.ops.ResizeOp(inputSize, inputSize,
                            org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod.BILINEAR))
                    .build();
            tensorImage = new org.tensorflow.lite.support.image.TensorImage(org.tensorflow.lite.DataType.FLOAT32);
        }

        if (outType == org.tensorflow.lite.DataType.FLOAT32) {
            outFloat = new float[1][numClasses];
        } else {
            outByte = new byte[1][numClasses];
            org.tensorflow.lite.Tensor tOut = tflite.getOutputTensor(0);
            org.tensorflow.lite.Tensor.QuantizationParams q = tOut.quantizationParams();
            outScale = q.getScale();
            outZero = q.getZeroPoint();
        }

        android.util.Log.d("TFLite", "input=" + inType + " output=" + outType + " classes=" + numClasses);
    }

    private static java.util.List<String> sanitizeLabels(java.util.List<String> raw, int classes) {
        java.util.List<String> clean = new java.util.ArrayList<>();
        if (raw != null) {
            for (String s : raw) {
                if (s != null) {
                    String t = s.trim();
                    if (!t.isEmpty()) clean.add(t);
                }
            }
        }
        if (clean.size() == classes) return clean;
        // If mismatch, fall back to known order or generic
        android.util.Log.w("TFLite", "labels size " + clean.size() + " != classes " + classes + "; using known order or generic");
        java.util.List<String> fallback = java.util.Arrays.asList("cardboard","glass","metal","paper","plastic");
        if (classes == 5) return fallback;
        clean = new java.util.ArrayList<>();
        for (int i = 0; i < classes; i++) clean.add("class_" + i);
        return clean;
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

    // New API: always returns normalized 0..1 probs
    public float[] inferProbs(android.graphics.Bitmap src) {
        if (tflite == null) throw new IllegalStateException("Interpreter not ready");

        android.graphics.Bitmap argb = (src.getConfig() == android.graphics.Bitmap.Config.ARGB_8888)
                ? src : src.copy(android.graphics.Bitmap.Config.ARGB_8888, false);
        android.graphics.Bitmap cropped = centerCrop(argb);

        tensorImage.load(cropped);
        org.tensorflow.lite.support.image.TensorImage processed =
                inputIsQuant ? procUint8.process(tensorImage) : procFloat.process(tensorImage);

        float[] probs;
        if (outFloat != null) {
            tflite.run(processed.getBuffer(), outFloat);
            probs = outFloat[0].clone();
            // If they already look like probs (sum≈1), softmax keeps them same
            softmax(probs);
        } else {
            tflite.run(processed.getBuffer(), outByte);
            probs = dequantize(outByte[0], outScale, outZero);
            softmax(probs); // normalize logits
        }
        return probs;
    }

    public Result classify(android.graphics.Bitmap src) {
        float[] probs = inferProbs(src);
        int idx = 0; float best = -1f;
        for (int i = 0; i < probs.length; i++) if (probs[i] > best) { best = probs[i]; idx = i; }
        String label = (labels != null && idx < labels.size()) ? labels.get(idx) : ("class_" + idx);
        return new Result(label, best, idx);
    }

    private static android.graphics.Bitmap centerCrop(android.graphics.Bitmap src) {
        int w = src.getWidth(), h = src.getHeight();
        int size = Math.min(w, h);
        int x = (w - size) / 2, y = (h - size) / 2;
        return android.graphics.Bitmap.createBitmap(src, x, y, size, size);
    }

    private Result top1(float[] v) {
        int idx = 0; float best = -Float.MAX_VALUE;
        for (int i = 0; i < v.length; i++) if (v[i] > best) { best = v[i]; idx = i; }
        String label = (labels != null && idx < labels.size()) ? labels.get(idx) : ("class_" + idx);
        return new Result(label, best, idx);
    }

    private static float[] dequantize(byte[] q, float s, int zp) {
        float[] f = new float[q.length];
        for (int i = 0; i < q.length; i++) f[i] = ((q[i] & 0xFF) - zp) * s;
        return f;
    }

    private static void softmax(float[] v) {
        float max = Float.NEGATIVE_INFINITY;
        for (float x : v) if (x > max) max = x;
        float sum = 0f;
        for (int i = 0; i < v.length; i++) {
            v[i] = (float) Math.exp(v[i] - max);
            sum += v[i];
        }
        if (sum == 0f) return;
        for (int i = 0; i < v.length; i++) {
            v[i] /= sum;
            if (v[i] < 0f) v[i] = 0f;
            if (v[i] > 1f) v[i] = 1f;
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
     * Get label at specific index
     */
    public String getLabelAt(int index) {
        if (labels != null && index >= 0 && index < labels.size()) {
            return labels.get(index);
        }
        return "class_" + index;
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
        return inputSize; 
    }
    
    /**
     * Close the interpreter and free resources
     */
    public void close() { 
        if (tflite != null) tflite.close(); 
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
}
