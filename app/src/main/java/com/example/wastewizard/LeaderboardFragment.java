package com.example.wastewizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardFragment extends Fragment {

    private GameManager gameManager;
    private RecyclerView recyclerViewLeaderboard;
    private TextView txtYourRank, txtYourPoints, txtTotalPlayers;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        
        gameManager = new GameManager(requireContext());
        initializeViews(view);
        setupLeaderboard();
        refreshData();
        
        return view;
    }
    
    private void initializeViews(View view) {
        recyclerViewLeaderboard = view.findViewById(R.id.recyclerViewLeaderboard);
        txtYourRank = view.findViewById(R.id.txtYourRank);
        txtYourPoints = view.findViewById(R.id.txtYourPoints);
        txtTotalPlayers = view.findViewById(R.id.txtTotalPlayers);
    }
    
    private void setupLeaderboard() {
        recyclerViewLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Create mock leaderboard data (in real app, this would be stored locally)
        List<LeaderboardEntry> entries = createMockLeaderboard();
        
        LeaderboardAdapter adapter = new LeaderboardAdapter(entries);
        recyclerViewLeaderboard.setAdapter(adapter);
    }
    
    private List<LeaderboardEntry> createMockLeaderboard() {
        List<LeaderboardEntry> entries = new ArrayList<>();
        
        // Add current user at their position
        entries.add(new LeaderboardEntry("You", gameManager.getTotalPoints(), gameManager.getLevel(), true));
        
        // Add mock players
        entries.add(new LeaderboardEntry("EcoWarrior", 1250, 13, false));
        entries.add(new LeaderboardEntry("GreenHero", 1180, 12, false));
        entries.add(new LeaderboardEntry("RecycleMaster", 1100, 11, false));
        entries.add(new LeaderboardEntry("WasteHunter", 980, 10, false));
        entries.add(new LeaderboardEntry("EcoFighter", 920, 9, false));
        entries.add(new LeaderboardEntry("GreenGuardian", 850, 9, false));
        entries.add(new LeaderboardEntry("RecyclePro", 780, 8, false));
        entries.add(new LeaderboardEntry("EcoChampion", 720, 8, false));
        entries.add(new LeaderboardEntry("WasteWizard", 650, 7, false));
        
        // Sort by points (descending)
        entries.sort((a, b) -> Integer.compare(b.points, a.points));
        
        // Assign ranks
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).rank = i + 1;
        }
        
        return entries;
    }
    
    public void refreshData() {
        if (gameManager == null) return;
        
        txtYourPoints.setText(String.valueOf(gameManager.getTotalPoints()));
        txtTotalPlayers.setText("10"); // Mock total players
        
        // Calculate user's rank
        int userRank = calculateUserRank();
        txtYourRank.setText("#" + userRank);
    }
    
    private int calculateUserRank() {
        int userPoints = gameManager.getTotalPoints();
        // Mock calculation - in real app, this would be based on stored data
        if (userPoints >= 1200) return 1;
        if (userPoints >= 1000) return 2;
        if (userPoints >= 800) return 3;
        if (userPoints >= 600) return 4;
        if (userPoints >= 400) return 5;
        return 6;
    }
    
    // Leaderboard Entry class
    public static class LeaderboardEntry {
        public String name;
        public int points;
        public int level;
        public boolean isCurrentUser;
        public int rank;
        
        public LeaderboardEntry(String name, int points, int level, boolean isCurrentUser) {
            this.name = name;
            this.points = points;
            this.level = level;
            this.isCurrentUser = isCurrentUser;
        }
    }
    
    // Leaderboard Adapter
    public static class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
        private List<LeaderboardEntry> entries;
        
        public LeaderboardAdapter(List<LeaderboardEntry> entries) {
            this.entries = entries;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LeaderboardEntry entry = entries.get(position);
            
            holder.txtRank.setText("#" + entry.rank);
            holder.txtName.setText(entry.name);
            holder.txtPoints.setText(String.valueOf(entry.points));
            holder.txtLevel.setText("Level " + entry.level);
            
            // Get Material colors for dynamic theming
            int colorPrimary = MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorPrimary);
            int onSurface = MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorOnSurface);
            int onSurfaceVariant = MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorOnSurfaceVariant);
            
            // Highlight current user using theme colors (dynamic-safe)
            if (entry.isCurrentUser) {
                holder.card.setStrokeColor(colorPrimary);
                holder.card.setStrokeWidth(2);
                holder.txtName.setTextColor(colorPrimary);
            } else {
                holder.card.setStrokeWidth(0);
                holder.txtName.setTextColor(onSurface);
            }
            holder.txtLevel.setTextColor(onSurfaceVariant);
            
            // Rank color using theme accents instead of fixed colors
            int rankColor = (entry.rank == 1)
                    ? MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorSecondary)
                    : (entry.rank <= 3
                        ? MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorTertiary)
                        : onSurfaceVariant);
            holder.txtRank.setTextColor(rankColor);
        }
        
        @Override
        public int getItemCount() {
            return entries.size();
        }
        
        public static class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView card;
            TextView txtRank, txtName, txtPoints, txtLevel;
            
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                card = itemView.findViewById(R.id.cardLeaderboardEntry);
                txtRank = itemView.findViewById(R.id.txtRank);
                txtName = itemView.findViewById(R.id.txtName);
                txtPoints = itemView.findViewById(R.id.txtPoints);
                txtLevel = itemView.findViewById(R.id.txtLevel);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }
}
