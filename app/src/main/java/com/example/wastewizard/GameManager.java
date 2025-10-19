package com.example.wastewizard;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private static final String PREFS_NAME = "WasteWizardGame";
    private static final String KEY_TOTAL_POINTS = "total_points";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_CORRECT_PREDICTIONS = "correct_predictions";
    private static final String KEY_TOTAL_PREDICTIONS = "total_predictions";
    private static final String KEY_STREAK = "current_streak";
    private static final String KEY_BEST_STREAK = "best_streak";
    
    private SharedPreferences prefs;
    private Context context;
    
    public GameManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    // Points and Level System
    public void addPoints(int points) {
        int currentPoints = getTotalPoints();
        int newPoints = currentPoints + points;
        prefs.edit().putInt(KEY_TOTAL_POINTS, newPoints).apply();
    }
    
    public int getTotalPoints() {
        return prefs.getInt(KEY_TOTAL_POINTS, 0);
    }
    
    public int getLevel() {
        return prefs.getInt(KEY_LEVEL, 1);
    }
    
    public int getPointsToNextLevel() {
        int currentLevel = getLevel();
        int pointsNeeded = currentLevel * 100; // 100 points per level
        int currentPoints = getTotalPoints();
        return Math.max(0, pointsNeeded - currentPoints);
    }
    
    public boolean checkLevelUp() {
        int currentLevel = getLevel();
        int pointsForNextLevel = (currentLevel + 1) * 100;
        int currentPoints = getTotalPoints();
        
        if (currentPoints >= pointsForNextLevel) {
            prefs.edit().putInt(KEY_LEVEL, currentLevel + 1).apply();
            return true;
        }
        return false;
    }
    
    // Statistics
    public void recordPrediction(boolean isCorrect) {
        int total = getTotalPredictions() + 1;
        int correct = getCorrectPredictions();
        if (isCorrect) {
            correct++;
            addStreak();
            addPoints(calculatePoints());
        } else {
            resetStreak();
        }
        
        prefs.edit()
            .putInt(KEY_TOTAL_PREDICTIONS, total)
            .putInt(KEY_CORRECT_PREDICTIONS, correct)
            .apply();
    }
    
    public int getTotalPredictions() {
        return prefs.getInt(KEY_TOTAL_PREDICTIONS, 0);
    }
    
    public int getCorrectPredictions() {
        return prefs.getInt(KEY_CORRECT_PREDICTIONS, 0);
    }
    
    public double getAccuracy() {
        int total = getTotalPredictions();
        if (total == 0) return 0.0;
        return (double) getCorrectPredictions() / total * 100;
    }
    
    // Streak System
    public void addStreak() {
        int currentStreak = getCurrentStreak() + 1;
        int bestStreak = getBestStreak();
        
        prefs.edit().putInt(KEY_STREAK, currentStreak).apply();
        
        if (currentStreak > bestStreak) {
            prefs.edit().putInt(KEY_BEST_STREAK, currentStreak).apply();
        }
    }
    
    public void resetStreak() {
        prefs.edit().putInt(KEY_STREAK, 0).apply();
    }
    
    public int getCurrentStreak() {
        return prefs.getInt(KEY_STREAK, 0);
    }
    
    public int getBestStreak() {
        return prefs.getInt(KEY_BEST_STREAK, 0);
    }
    
    // Additional methods for compatibility
    public int getStreak() {
        return getCurrentStreak();
    }
    
    public void incrementStreak() {
        addStreak();
    }
    
    // Scan History Management
    private static final String KEY_SCAN_HISTORY = "scan_history";
    
    public void addScanHistory(String imagePath, String predictedLabel, float confidence, long timestamp) {
        String historyJson = prefs.getString(KEY_SCAN_HISTORY, "[]");
        try {
            java.util.List<ScanHistory> history = new java.util.ArrayList<>();
            
            // Parse existing history (simplified JSON-like format)
            if (!historyJson.equals("[]")) {
                String[] entries = historyJson.split("\\|");
                for (String entry : entries) {
                    String[] parts = entry.split(",");
                    if (parts.length >= 4) {
                        history.add(new ScanHistory(parts[0], parts[1], Float.parseFloat(parts[2]), Long.parseLong(parts[3])));
                    }
                }
            }
            
            // Add new scan
            history.add(new ScanHistory(imagePath, predictedLabel, confidence, timestamp));
            
            // Keep only last 50 scans
            if (history.size() > 50) {
                history = history.subList(history.size() - 50, history.size());
            }
            
            // Save back to preferences
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < history.size(); i++) {
                if (i > 0) sb.append("|");
                ScanHistory scan = history.get(i);
                sb.append(scan.imagePath).append(",")
                  .append(scan.predictedLabel).append(",")
                  .append(scan.confidence).append(",")
                  .append(scan.timestamp);
            }
            
            prefs.edit().putString(KEY_SCAN_HISTORY, sb.toString()).apply();
            
        } catch (Exception e) {
            android.util.Log.e("GameManager", "Error saving scan history", e);
        }
    }
    
    public java.util.List<ScanHistory> getScanHistory() {
        java.util.List<ScanHistory> history = new java.util.ArrayList<>();
        String historyJson = prefs.getString(KEY_SCAN_HISTORY, "[]");
        
        try {
            if (!historyJson.equals("[]")) {
                String[] entries = historyJson.split("\\|");
                for (String entry : entries) {
                    String[] parts = entry.split(",");
                    if (parts.length >= 4) {
                        history.add(new ScanHistory(parts[0], parts[1], Float.parseFloat(parts[2]), Long.parseLong(parts[3])));
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("GameManager", "Error loading scan history", e);
        }
        
        // Return in reverse order (newest first)
        java.util.Collections.reverse(history);
        return history;
    }
    
    public static class ScanHistory {
        public String imagePath;
        public String predictedLabel;
        public float confidence;
        public long timestamp;
        
        public ScanHistory(String imagePath, String predictedLabel, float confidence, long timestamp) {
            this.imagePath = imagePath;
            this.predictedLabel = predictedLabel;
            this.confidence = confidence;
            this.timestamp = timestamp;
        }
        
        public String getFormattedTime() {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault());
            return sdf.format(new java.util.Date(timestamp));
        }
    }
    
    // Achievement System
    public List<Achievement> getAchievements() {
        List<Achievement> achievements = new ArrayList<>();
        
        // Accuracy achievements
        if (getAccuracy() >= 90) {
            achievements.add(new Achievement("Accuracy Master", "Achieve 90% accuracy", true));
        }
        if (getAccuracy() >= 95) {
            achievements.add(new Achievement("Perfect Shot", "Achieve 95% accuracy", true));
        }
        
        // Streak achievements
        if (getBestStreak() >= 5) {
            achievements.add(new Achievement("Hot Streak", "Get 5 predictions in a row", true));
        }
        if (getBestStreak() >= 10) {
            achievements.add(new Achievement("Unstoppable", "Get 10 predictions in a row", true));
        }
        
        // Level achievements
        if (getLevel() >= 5) {
            achievements.add(new Achievement("Rising Star", "Reach level 5", true));
        }
        if (getLevel() >= 10) {
            achievements.add(new Achievement("Waste Warrior", "Reach level 10", true));
        }
        
        // Prediction count achievements
        if (getTotalPredictions() >= 50) {
            achievements.add(new Achievement("Dedicated Learner", "Make 50 predictions", true));
        }
        if (getTotalPredictions() >= 100) {
            achievements.add(new Achievement("Expert Classifier", "Make 100 predictions", true));
        }
        
        return achievements;
    }
    
    private int calculatePoints() {
        int basePoints = 10;
        int streakBonus = getCurrentStreak() * 2;
        int accuracyBonus = (int) (getAccuracy() / 10);
        return basePoints + streakBonus + accuracyBonus;
    }
    
    public static class Achievement {
        public String title;
        public String description;
        public boolean unlocked;
        
        public Achievement(String title, String description, boolean unlocked) {
            this.title = title;
            this.description = description;
            this.unlocked = unlocked;
        }
    }
}
