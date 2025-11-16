package com.example.tournafy.ui.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tournafy.R;
import com.example.tournafy.domain.models.base.HostedEntity; 
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private HomeAdapter adapter;
    private FloatingActionButton fabHostOffline;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view_hosted_entities);
        fabHostOffline = view.findViewById(R.id.fab_host_offline);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup FAB click listener
        // This FAB is for hosting offline 
        fabHostOffline.setOnClickListener(v -> {
            // Navigate to the HostNewMatchFragment
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_hostNewMatchFragment);
        });

        // Load offline data (placeholder)
        loadOfflineEntities();
    }

    private void setupRecyclerView() {
        adapter = new HomeAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadOfflineEntities() {
        // This is placeholder data.
        // In later phases, this will come from the FirestoreRepository.
        List<HostedEntity> placeholderList = new ArrayList<>();
        
        // Note: We use the base HostedEntity, but we'd need to create placeholder
        // subclasses or a simplified constructor if it's abstract.
        // For this phase, we'll assume we can create simple objects or just use strings.
        // Let's create a placeholder list for the adapter.
        
        // (This part will be updated in Phase 6+ with ViewModels)
        
        // Example:
        // HostedEntity match1 = new HostedEntity(); 
        // match1.setName("My First Offline Match");
        // placeholderList.add(match1);
        
        // adapter.updateData(placeholderList);
    }
}