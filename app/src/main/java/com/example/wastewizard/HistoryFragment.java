package com.example.wastewizard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

import java.io.File;
import java.util.List;

public class HistoryFragment extends Fragment {

    private GameManager gameManager;
    private RecyclerView recyclerViewHistory;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        
        gameManager = new GameManager(requireContext());
        initializeViews(view);
        setupHistory();
        
        return view;
    }
    
    private void initializeViews(View view) {
        recyclerViewHistory = view.findViewById(R.id.recyclerViewHistory);
    }
    
    private void setupHistory() {
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        
        List<GameManager.ScanHistory> history = gameManager.getScanHistory();
        HistoryAdapter adapter = new HistoryAdapter(history);
        recyclerViewHistory.setAdapter(adapter);
    }
    
    public void refreshData() {
        if (gameManager != null) {
            setupHistory();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        refreshData(); // rebuilds the adapter from prefs
    }
    
    // History Adapter
    public static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<GameManager.ScanHistory> history;
        
        public HistoryAdapter(List<GameManager.ScanHistory> history) {
            this.history = history;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scan_history, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GameManager.ScanHistory scan = history.get(position);
            
            holder.txtLabel.setText(scan.predictedLabel);
            holder.txtConfidence.setText(String.format("%.1f%%", scan.confidence * 100f));
            holder.txtConfidenceBadge.setText(String.format("%.0f%%", scan.confidence * 100f));
            holder.txtTime.setText(scan.getFormattedTime());
            
            // Load image if available
            if (scan.imagePath != null && !scan.imagePath.isEmpty()) {
                try {
                    File imageFile = new File(scan.imagePath);
                    if (imageFile.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                        if (bitmap != null) {
                            holder.imgThumbnail.setImageBitmap(bitmap);
                            holder.imgThumbnail.setVisibility(View.VISIBLE);
                        } else {
                            holder.imgThumbnail.setVisibility(View.GONE);
                        }
                    } else {
                        holder.imgThumbnail.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    holder.imgThumbnail.setVisibility(View.GONE);
                }
            } else {
                holder.imgThumbnail.setVisibility(View.GONE);
            }
            
            // Set harmonized category color for stroke
            int stroke = CategoryColors.accent(holder.itemView.getContext(), scan.predictedLabel);
            holder.card.setStrokeColor(stroke);
            holder.card.setStrokeWidth(2);
            
            // Primary/secondary text via theme
            int onSurface = MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorOnSurface);
            int onSurfaceVariant = MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorOnSurfaceVariant);
            holder.txtLabel.setTextColor(onSurface);
            holder.txtConfidence.setTextColor(onSurfaceVariant);
            holder.txtTime.setTextColor(onSurfaceVariant);
        }
        
        @Override
        public int getItemCount() {
            return history.size();
        }
        
        private int getClassColor(android.content.Context context, String label) {
            // Use harmonized category colors that blend with dynamic theme
            return CategoryColors.accent(context, label);
        }
        
        public static class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView card;
            ImageView imgThumbnail;
            TextView txtLabel, txtConfidence, txtTime, txtConfidenceBadge;
            
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                card = itemView.findViewById(R.id.cardScanHistory);
                imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
                txtLabel = itemView.findViewById(R.id.txtLabel);
                txtConfidence = itemView.findViewById(R.id.txtConfidence);
                txtTime = itemView.findViewById(R.id.txtTime);
                txtConfidenceBadge = itemView.findViewById(R.id.txtConfidenceBadge);
            }
        }
    }
}
