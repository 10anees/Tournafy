package com.example.tournafy.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.search.SearchResult;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying search results.
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {
    
    private List<SearchResult> results = new ArrayList<>();
    private OnResultClickListener listener;
    
    public interface OnResultClickListener {
        void onResultClick(SearchResult result);
    }
    
    public SearchResultAdapter(OnResultClickListener listener) {
        this.listener = listener;
    }
    
    public void setResults(List<SearchResult> results) {
        this.results = results != null ? results : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new SearchResultViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
        SearchResult result = results.get(position);
        holder.bind(result, listener);
    }
    
    @Override
    public int getItemCount() {
        return results.size();
    }
    
    static class SearchResultViewHolder extends RecyclerView.ViewHolder {
        
        private final ImageView ivTypeIcon;
        private final TextView tvTitle;
        private final TextView tvSubtitle;
        private final TextView tvInfo;
        private final Chip chipStatus;
        
        public SearchResultViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTypeIcon = itemView.findViewById(R.id.ivTypeIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }
        
        public void bind(SearchResult result, OnResultClickListener listener) {
            tvTitle.setText(result.getTitle());
            
            // Set subtitle with code
            String subtitle = result.getSubtitle();
            if (result.getCode() != null) {
                subtitle += " • Code: " + result.getCode().toUpperCase();
            }
            tvSubtitle.setText(subtitle);
            
            // Set info if available
            if (result.getInfo() != null && !result.getInfo().isEmpty()) {
                tvInfo.setText(result.getInfo());
                tvInfo.setVisibility(View.VISIBLE);
            } else {
                tvInfo.setVisibility(View.GONE);
            }
            
            // Set icon based on type
            switch (result.getType()) {
                case SearchResult.TYPE_MATCH:
                    ivTypeIcon.setImageResource(getSportIcon(result.getSportId()));
                    break;
                case SearchResult.TYPE_TOURNAMENT:
                    ivTypeIcon.setImageResource(R.drawable.ic_trophy);
                    break;
                case SearchResult.TYPE_SERIES:
                    ivTypeIcon.setImageResource(R.drawable.ic_series);
                    break;
            }
            
            // Set status chip
            String status = result.getStatus();
            if (status != null && !status.isEmpty()) {
                chipStatus.setText(status);
                chipStatus.setVisibility(View.VISIBLE);
                
                // Set chip color based on status
                if ("LIVE".equals(status)) {
                    chipStatus.setChipBackgroundColorResource(R.color.live_red);
                } else if ("COMPLETED".equals(status)) {
                    chipStatus.setChipBackgroundColorResource(R.color.success_green);
                } else {
                    chipStatus.setChipBackgroundColorResource(R.color.md_theme_dark_primaryContainer);
                }
            } else {
                chipStatus.setVisibility(View.GONE);
            }
            
            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onResultClick(result);
                }
            });
        }
        
        private int getSportIcon(String sportId) {
            if (sportId == null) {
                return R.drawable.ic_sports;
            }
            switch (sportId.toUpperCase()) {
                case "CRICKET":
                    return R.drawable.ic_cricket;
                case "FOOTBALL":
                    return R.drawable.ic_football;
                default:
                    return R.drawable.ic_sports;
            }
        }
    }
}
