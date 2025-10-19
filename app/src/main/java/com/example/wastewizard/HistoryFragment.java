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
            holder.txtConfidence.setText(String.format("%.1f%%", scan.confidence * 100));
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
            
            // Set color based on predicted label
            int color = getClassColor(holder.itemView.getContext(), scan.predictedLabel);
            holder.card.setStrokeColor(color);
        }
        
        @Override
        public int getItemCount() {
            return history.size();
        }
        
        private int getClassColor(android.content.Context context, String label) {
            switch (label.toLowerCase()) {
                case "plastic": return context.getResources().getColor(R.color.cat_plastic);
                case "glass": return context.getResources().getColor(R.color.cat_glass);
                case "paper": return context.getResources().getColor(R.color.cat_paper);
                case "metal": return context.getResources().getColor(R.color.cat_metal);
                case "cardboard": return context.getResources().getColor(R.color.cat_cardboard);
                default: return context.getResources().getColor(R.color.text_secondary);
            }
        }
        
        public static class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView card;
            ImageView imgThumbnail;
            TextView txtLabel, txtConfidence, txtTime;
            
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                card = itemView.findViewById(R.id.cardScanHistory);
                imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
                txtLabel = itemView.findViewById(R.id.txtLabel);
                txtConfidence = itemView.findViewById(R.id.txtConfidence);
                txtTime = itemView.findViewById(R.id.txtTime);
            }
        }
    }
}
