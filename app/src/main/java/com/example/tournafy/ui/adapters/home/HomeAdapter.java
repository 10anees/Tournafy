package com.example.tournafy.ui.adapters.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.base.HostedEntity;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.series.Series;
import com.example.tournafy.domain.models.tournament.Tournament;
import com.google.android.material.button.MaterialButton;

public class HomeAdapter extends ListAdapter<HostedEntity, HomeAdapter.EntityViewHolder> {

    private final OnEntityClickListener listener;

    public interface OnEntityClickListener {
        void onEntityClick(HostedEntity entity);
        void onEntityLongClick(HostedEntity entity);
        void onDeleteClick(HostedEntity entity);
        void onShareClick(HostedEntity entity);
    }

    public HomeAdapter(OnEntityClickListener listener) {
        super(new DiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public EntityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_hosted_entity, parent, false);
        return new EntityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntityViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class EntityViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus, tvType;
        ImageView ivIcon;
        MaterialButton btnShare;
        MaterialButton btnDelete;

        public EntityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEntityName);
            tvStatus = itemView.findViewById(R.id.tvEntityStatus);
            tvType = itemView.findViewById(R.id.tvEntityType);
            ivIcon = itemView.findViewById(R.id.ivEntityIcon);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(HostedEntity entity, OnEntityClickListener listener) {
            tvName.setText(entity.getName());
            tvStatus.setText(entity.getStatus());

            if (entity instanceof Match) {
                tvType.setText("Match");
                ivIcon.setImageResource(R.drawable.ic_matches);
            } else if (entity instanceof Tournament) {
                tvType.setText("Tournament");
                ivIcon.setImageResource(R.drawable.ic_trophy);
            } else if (entity instanceof Series) {
                tvType.setText("Series");
                ivIcon.setImageResource(R.drawable.ic_series);
            }

            itemView.setOnClickListener(v -> listener.onEntityClick(entity));
            itemView.setOnLongClickListener(v -> {
                listener.onEntityLongClick(entity);
                return true;
            });
            
            // Handle share button click
            btnShare.setOnClickListener(v -> listener.onShareClick(entity));
            
            // Handle delete button click
            btnDelete.setOnClickListener(v -> listener.onDeleteClick(entity));
        }
    }

    static class DiffCallback extends DiffUtil.ItemCallback<HostedEntity> {
        @Override
        public boolean areItemsTheSame(@NonNull HostedEntity oldItem, @NonNull HostedEntity newItem) {
            if (oldItem.getEntityId() == null || newItem.getEntityId() == null) return false;
            return oldItem.getEntityId().equals(newItem.getEntityId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull HostedEntity oldItem, @NonNull HostedEntity newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getStatus().equals(newItem.getStatus());
        }
    }
}