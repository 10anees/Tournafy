package com.example.tournafy.ui.fragments.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tournafy.R;
import com.example.tournafy.domain.models.base.HostedEntity; // [cite: 55]
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {

    private List<HostedEntity> entityList;

    public HomeAdapter(List<HostedEntity> entityList) {
        this.entityList = entityList;
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_hosted_entity, parent, false);
        return new HomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        HostedEntity entity = entityList.get(position);
        holder.bind(entity);
    }

    @Override
    public int getItemCount() {
        return entityList.size();
    }

    // Method to update data (will be used by ViewModel)
    public void updateData(List<HostedEntity> newList) {
        this.entityList.clear();
        this.entityList.addAll(newList);
        notifyDataSetChanged();
    }

    // ViewHolder Class
    static class HomeViewHolder extends RecyclerView.ViewHolder {
        TextView textEntityName;
        TextView textEntityType;
        TextView textEntityStatus;

        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            textEntityName = itemView.findViewById(R.id.text_entity_name);
            textEntityType = itemView.findViewById(R.id.text_entity_type);
            textEntityStatus = itemView.findViewById(R.id.text_entity_status);
        }

        public void bind(HostedEntity entity) {
            // We use the base HostedEntity's fields [cite: 202]
            textEntityName.setText(entity.getName());
            textEntityStatus.setText(entity.getStatus());
            
            // Combine type and online status
            String type = entity.getEntityType(); // e.g., "Match", "Tournament"
            String onlineStatus = entity.isOnline() ? "Online" : "Offline";
            textEntityType.setText(String.format("%s / %s", type, onlineStatus));
        }
    }
}