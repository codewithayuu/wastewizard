package com.example.wastewizard;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;
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
        android.util.Log.d("GM", "recordPrediction isCorrect=" + isCorrect);
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
    
    public void addScanHistory(String imagePath, String predictedLabel, float confidence, long timestamp, boolean isCorrect) {
        android.util.Log.d("GM", "addScanHistory correct=" + isCorrect + " label=" + predictedLabel + " confidence=" + confidence);
        String historyStr = prefs.getString(KEY_SCAN_HISTORY, "");
        java.util.List<ScanHistory> list = new java.util.ArrayList<>();
        if (historyStr != null && !historyStr.isEmpty() && !"[]".equals(historyStr)) {
            String[] entries = historyStr.split("\\|");
            for (String entry : entries) {
                String[] p = entry.split(",");
                try {
                    if (p.length >= 5) {
                        list.add(new ScanHistory(p[0], p[1], Float.parseFloat(p[2]), Long.parseLong(p[3]), Boolean.parseBoolean(p[4])));
                    } else if (p.length >= 4) {
                        list.add(new ScanHistory(p[0], p[1], Float.parseFloat(p[2]), Long.parseLong(p[3]), null));
                    }
                } catch (Exception ignored) {}
            }
        }

        list.add(new ScanHistory(imagePath, predictedLabel, confidence, timestamp, isCorrect));
        if (list.size() > 50) list = list.subList(list.size() - 50, list.size());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append("|");
            ScanHistory s = list.get(i);
            sb.append(s.imagePath).append(",")
              .append(s.predictedLabel).append(",")
              .append(String.format("%.6f", s.confidence)).append(",")
              .append(s.timestamp).append(",")
              .append(s.isCorrect == null ? "false" : s.isCorrect.toString());
        }
        prefs.edit().putString(KEY_SCAN_HISTORY, sb.toString()).apply();
    }

    @Deprecated
    public void addScanHistory(String imagePath, String predictedLabel, float confidence, long timestamp) {
        // Fallback: mark unknown as false to avoid inflating accuracy
        addScanHistory(imagePath, predictedLabel, confidence, timestamp, false);
    }
    
    public java.util.List<ScanHistory> getScanHistory() {
        java.util.List<ScanHistory> list = new java.util.ArrayList<>();
        String historyStr = prefs.getString(KEY_SCAN_HISTORY, "");
        if (historyStr != null && !historyStr.isEmpty() && !"[]".equals(historyStr)) {
            String[] entries = historyStr.split("\\|");
            for (String entry : entries) {
                String[] p = entry.split(",");
                try {
                    if (p.length >= 5) {
                        float conf = Float.parseFloat(p[2]);
                        android.util.Log.d("GM", "Retrieved confidence: " + conf + " for label: " + p[1]);
                        list.add(new ScanHistory(p[0], p[1], conf, Long.parseLong(p[3]), Boolean.parseBoolean(p[4])));
                    } else if (p.length >= 4) {
                        float conf = Float.parseFloat(p[2]);
                        android.util.Log.d("GM", "Retrieved confidence: " + conf + " for label: " + p[1]);
                        list.add(new ScanHistory(p[0], p[1], conf, Long.parseLong(p[3]), null));
                    }
                } catch (Exception ignored) {}
            }
        }
        java.util.Collections.reverse(list); // newest first
        return list;
    }
    
    // Clear scan history method
    public void clearScanHistory() {
        String historyStr = prefs.getString(KEY_SCAN_HISTORY, "");
        if (historyStr != null && !historyStr.isEmpty() && !"[]".equals(historyStr)) {
            String[] entries = historyStr.split("\\|");
            for (String entry : entries) {
                String[] parts = entry.split(",");
                if (parts.length >= 1) {
                    String path = parts[0];
                    try {
                        if (path != null && !path.isEmpty()) {
                            java.io.File f;
                            if (path.startsWith("file:")) {
                                android.net.Uri u = android.net.Uri.parse(path);
                                f = new java.io.File(u.getPath());
                            } else {
                                f = new java.io.File(path);
                            }
                            if (f.exists() && isAppOwnedFile(f)) {
                                // Best-effort delete
                                //noinspection ResultOfMethodCallIgnored
                                f.delete();
                            }
                        }
                    } catch (Exception ignored) { }
                }
            }
        }
        prefs.edit().remove(KEY_SCAN_HISTORY).apply();
    }

    private boolean isAppOwnedFile(java.io.File f) {
        try {
            String p = f.getCanonicalPath();
            String files = context.getFilesDir().getCanonicalPath();
            String cache = context.getCacheDir().getCanonicalPath();
            return p.startsWith(files) || p.startsWith(cache);
        } catch (java.io.IOException e) {
            return false;
        }
    }
    
    // Reset all game data (optional)
    public void resetAllData() {
        prefs.edit().clear().apply();
    }
    
    // Stats helpers (today, week, accuracy)
    public int getScanHistoryCount() {
        return getScanHistory().size();
    }

    public int getScanCountBetween(long startMs, long endMs) {
        int c = 0;
        for (ScanHistory s : getScanHistory()) {
            if (s.timestamp >= startMs && s.timestamp < endMs) c++;
        }
        return c;
    }

    public int getScanCountToday() {
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now();
        long start = now.toLocalDate().atStartOfDay(now.getZone()).toInstant().toEpochMilli();
        long end = now.plusDays(1).toLocalDate().atStartOfDay(now.getZone()).toInstant().toEpochMilli();
        return getScanCountBetween(start, end);
    }

    public int getScanCountThisWeek() {
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now();
        java.time.ZonedDateTime weekStart = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .toLocalDate().atStartOfDay(now.getZone());
        long start = weekStart.toInstant().toEpochMilli();
        long end = now.plusDays(1).toLocalDate().atStartOfDay(now.getZone()).toInstant().toEpochMilli();
        return getScanCountBetween(start, end);
    }

    public double getAccuracyFromHistory() {
        int total = 0, correct = 0;
        for (ScanHistory s : getScanHistory()) {
            if (s.isCorrect != null) {
                total++;
                if (s.isCorrect) correct++;
            }
        }
        if (total == 0) return 0.0;
        return (correct * 100.0) / total;
    }
    
    public static class ScanHistory {
        public String imagePath;
        public String predictedLabel;
        public float confidence;
        public long timestamp;
        public Boolean isCorrect; // null = unknown (old entries)
        
        public ScanHistory(String imagePath, String predictedLabel, float confidence, long timestamp, @Nullable Boolean isCorrect) {
            this.imagePath = imagePath;
            this.predictedLabel = predictedLabel;
            this.confidence = confidence;
            this.timestamp = timestamp;
            this.isCorrect = isCorrect;
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
