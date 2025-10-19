package com.example.wastewizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

public class DashboardFragment extends Fragment {

    private GameManager gameManager;
    private TextView txtLevel, txtPoints, txtStreak, txtAccuracy, txtTotalScans;
    private TextView txtTodayScans, txtWeeklyGoal, txtAchievements;
    private TextView txtThisWeek, txtLastWeek, txtMonthly;
    private MaterialCardView cardStats, cardToday, cardAchievements, cardQuickActions;
    private RecyclerView recyclerViewQuickActions;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
            
            gameManager = new GameManager(requireContext());
            initializeViews(view);
            setupQuickActions();
            refreshData();
            
            return view;
        } catch (Exception e) {
            android.util.Log.e("DashboardFragment", "onCreateView error", e);
            return inflater.inflate(R.layout.fragment_dashboard, container, false);
        }
    }
    
    private void initializeViews(View view) {
        // Stats cards
        txtLevel = view.findViewById(R.id.txtLevel);
        txtPoints = view.findViewById(R.id.txtPoints);
        txtStreak = view.findViewById(R.id.txtStreak);
        txtAccuracy = view.findViewById(R.id.txtAccuracy);
        txtTotalScans = view.findViewById(R.id.txtTotalScans);
        
        // Today's stats
        txtTodayScans = view.findViewById(R.id.txtTodayScans);
        txtWeeklyGoal = view.findViewById(R.id.txtWeeklyGoal);
        txtAchievements = view.findViewById(R.id.txtAchievements);
        
        // Weekly progress stats
        txtThisWeek = view.findViewById(R.id.txtThisWeek);
        txtLastWeek = view.findViewById(R.id.txtLastWeek);
        txtMonthly = view.findViewById(R.id.txtMonthly);
        
        // Cards
        cardStats = view.findViewById(R.id.cardStats);
        cardToday = view.findViewById(R.id.cardToday);
        cardAchievements = view.findViewById(R.id.cardAchievements);
        cardQuickActions = view.findViewById(R.id.cardQuickActions);
        
        // Quick actions recycler view
        recyclerViewQuickActions = view.findViewById(R.id.recyclerViewQuickActions);
    }
    
    private void setupQuickActions() {
        recyclerViewQuickActions.setLayoutManager(new GridLayoutManager(getContext(), 2));
        
        // Quick actions data
        QuickAction[] quickActions = {
            new QuickAction("Scan Waste", R.drawable.ic_scan, "Classify waste items"),
            new QuickAction("View History", R.drawable.ic_history, "See past scans"),
            new QuickAction("Achievements", R.drawable.ic_achievement, "Check progress"),
            new QuickAction("Tips & Guide", R.drawable.ic_tips, "Learn recycling")
        };
        
        QuickActionsAdapter adapter = new QuickActionsAdapter(quickActions, this::onQuickActionClick);
        recyclerViewQuickActions.setAdapter(adapter);
    }
    
    private void onQuickActionClick(QuickAction action) {
        MainAppActivity mainActivity = (MainAppActivity) getActivity();
        if (mainActivity == null) return;
        
        switch (action.title) {
            case "Scan Waste":
                // Switch to scan fragment and open camera
                mainActivity.loadFragmentFromFragment(new ScanFragment());
                mainActivity.openCamera();
                break;
            case "View History":
                // Switch to history fragment to show scan history
                mainActivity.loadFragmentFromFragment(new HistoryFragment());
                break;
            case "Achievements":
                // Switch to profile to show achievements
                mainActivity.loadFragmentFromFragment(new ProfileFragment());
                break;
            case "Tips & Guide":
                // Show tips dialog or switch to a tips fragment
                showTipsDialog();
                break;
            default:
                Toast.makeText(getContext(), action.title + " clicked!", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    
    private void showTipsDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Recycling Tips")
            .setMessage("• Plastic: Look for recycling codes #1-7\n• Glass: Remove caps and rinse\n• Paper: Keep dry and remove staples\n• Metal: Rinse aluminum and steel cans\n• Cardboard: Remove tape and keep dry")
            .setPositiveButton("OK", null)
            .show();
    }
    
    public void refreshData() {
        if (gameManager == null) return;
        
        // Update main stats
        txtLevel.setText("Level " + gameManager.getLevel());
        txtPoints.setText(String.valueOf(gameManager.getTotalPoints()));
        txtStreak.setText(String.valueOf(gameManager.getCurrentStreak()));
        txtAccuracy.setText(String.format("%.1f%%", gameManager.getAccuracy()));
        txtTotalScans.setText(String.valueOf(gameManager.getTotalPredictions()));
        
        // Update today's stats (mock data for now)
        txtTodayScans.setText("5"); // Today's scans
        txtWeeklyGoal.setText("12/20"); // Weekly goal progress
        txtAchievements.setText(String.valueOf(gameManager.getAchievements().size()));
        
        // Update weekly progress stats
        txtThisWeek.setText("12"); // This week's scans
        txtLastWeek.setText("8"); // Last week's scans
        txtMonthly.setText("45"); // Monthly scans
        
        // Animate cards
        animateCards();
    }
    
    private void animateCards() {
        cardStats.animate().scaleX(1.02f).scaleY(1.02f).setDuration(200).withEndAction(() -> {
            cardStats.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200);
        });
        
        cardToday.animate().alpha(0.8f).setDuration(300).withEndAction(() -> {
            cardToday.animate().alpha(1.0f).setDuration(300);
        });
    }
    
    // Quick Action class
    public static class QuickAction {
        public String title;
        public int iconRes;
        public String description;
        
        public QuickAction(String title, int iconRes, String description) {
            this.title = title;
            this.iconRes = iconRes;
            this.description = description;
        }
    }
    
    // Quick Actions Adapter
    public static class QuickActionsAdapter extends RecyclerView.Adapter<QuickActionsAdapter.ViewHolder> {
        private QuickAction[] actions;
        private OnQuickActionClickListener listener;
        
        public interface OnQuickActionClickListener {
            void onQuickActionClick(QuickAction action);
        }
        
        public QuickActionsAdapter(QuickAction[] actions, OnQuickActionClickListener listener) {
            this.actions = actions;
            this.listener = listener;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quick_action, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            QuickAction action = actions[position];
            holder.title.setText(action.title);
            holder.description.setText(action.description);
            holder.icon.setImageResource(action.iconRes);
            
            holder.card.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuickActionClick(action);
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return actions.length;
        }
        
        public static class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView card;
            TextView title, description;
            android.widget.ImageView icon;
            
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                card = itemView.findViewById(R.id.cardQuickAction);
                title = itemView.findViewById(R.id.txtQuickActionTitle);
                description = itemView.findViewById(R.id.txtQuickActionDescription);
                icon = itemView.findViewById(R.id.imgQuickActionIcon);
            }
        }
    }
}
