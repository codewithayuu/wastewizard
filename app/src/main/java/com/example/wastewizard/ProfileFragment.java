package com.example.wastewizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ProfileFragment extends Fragment {

    private GameManager gameManager;
    private TextView txtLevel, txtPoints, txtStreak, txtAccuracy;
    private TextView txtTotalScans, txtBestStreak, txtAchievementsUnlocked;
    private RecyclerView recyclerViewAchievements;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        gameManager = new GameManager(requireContext());
        initializeViews(view);
        setupAchievements();
        refreshData();
        
        return view;
    }
    
    private void initializeViews(View view) {
        // Main stats
        txtLevel = view.findViewById(R.id.txtLevel);
        txtPoints = view.findViewById(R.id.txtPoints);
        txtStreak = view.findViewById(R.id.txtStreak);
        txtAccuracy = view.findViewById(R.id.txtAccuracy);
        
        // Additional stats
        txtTotalScans = view.findViewById(R.id.txtTotalScans);
        txtBestStreak = view.findViewById(R.id.txtBestStreak);
        txtAchievementsUnlocked = view.findViewById(R.id.txtAchievementsUnlocked);
        
        // Achievements
        recyclerViewAchievements = view.findViewById(R.id.recyclerViewAchievements);
    }
    
    private void setupAchievements() {
        recyclerViewAchievements.setLayoutManager(new GridLayoutManager(getContext(), 2));
        
        List<GameManager.Achievement> achievements = gameManager.getAchievements();
        AchievementsAdapter adapter = new AchievementsAdapter(achievements);
        recyclerViewAchievements.setAdapter(adapter);
    }
    
    public void refreshData() {
        if (gameManager == null) return;
        
        // Update main stats
        txtLevel.setText("Level " + gameManager.getLevel());
        txtPoints.setText(String.valueOf(gameManager.getTotalPoints()));
        txtStreak.setText(String.valueOf(gameManager.getCurrentStreak()));
        txtAccuracy.setText(String.format("%.1f%%", gameManager.getAccuracy()));
        
        // Update additional stats
        txtTotalScans.setText(String.valueOf(gameManager.getTotalPredictions()));
        txtBestStreak.setText(String.valueOf(gameManager.getBestStreak()));
        txtAchievementsUnlocked.setText(String.valueOf(gameManager.getAchievements().size()));
        
        // Refresh achievements
        setupAchievements();
    }
    
    // Achievements Adapter
    public static class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.ViewHolder> {
        private List<GameManager.Achievement> achievements;
        
        public AchievementsAdapter(List<GameManager.Achievement> achievements) {
            this.achievements = achievements;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GameManager.Achievement achievement = achievements.get(position);
            
            holder.txtTitle.setText(achievement.title);
            holder.txtDescription.setText(achievement.description);
            
            if (achievement.unlocked) {
                holder.card.setStrokeColor(holder.itemView.getContext().getResources().getColor(R.color.success_color));
                holder.card.setStrokeWidth(2);
                holder.txtTitle.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.success_color));
                holder.icon.setText("🏆");
            } else {
                holder.card.setStrokeWidth(0);
                holder.txtTitle.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_secondary));
                holder.icon.setText("🔒");
                holder.card.setAlpha(0.6f);
            }
        }
        
        @Override
        public int getItemCount() {
            return achievements.size();
        }
        
        public static class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView card;
            TextView txtTitle, txtDescription, icon;
            
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                card = itemView.findViewById(R.id.cardAchievement);
                txtTitle = itemView.findViewById(R.id.txtAchievementTitle);
                txtDescription = itemView.findViewById(R.id.txtAchievementDescription);
                icon = itemView.findViewById(R.id.txtAchievementIcon);
            }
        }
    }
}
