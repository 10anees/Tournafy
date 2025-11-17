package com.example.tournafy.ui.fragments.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.tournafy.R;
import com.example.tournafy.ui.viewmodels.AuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    private AuthViewModel authViewModel;
    
    // UI Views
    private ShapeableImageView ivProfilePic;
    private TextView tvUserName, tvUserEmail, tvCurrentTheme;
    private MaterialButton btnProfile, btnLogout;
    private LinearLayout llTheme;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        initViews(view);
        setupObservers();
        setupClickListeners();
        updateThemeText();
    }

    private void initViews(View view) {
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvCurrentTheme = view.findViewById(R.id.tvCurrentTheme);
        btnProfile = view.findViewById(R.id.btnProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        llTheme = view.findViewById(R.id.llTheme);
    }

    private void setupObservers() {
        authViewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                // --- Logged In State ---
                tvUserName.setText(user.getName() != null ? user.getName() : "User");
                tvUserEmail.setText(user.getEmail());
                btnProfile.setVisibility(View.GONE); // Hide "Sign In" button
                btnLogout.setVisibility(View.VISIBLE); // Show "Log Out" button
                
                // Load Profile Pic
                if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                    Glide.with(this)
                         .load(user.getProfilePicture())
                         .placeholder(R.drawable.ic_launcher_foreground) // Ensure you have a placeholder
                         .into(ivProfilePic);
                }
            } else {
                // --- Guest State ---
                tvUserName.setText("Guest User");
                tvUserEmail.setText("Sign in to sync data");
                ivProfilePic.setImageResource(R.drawable.ic_launcher_foreground);
                btnProfile.setVisibility(View.VISIBLE); // Show "Sign In" button
                btnProfile.setText("Sign In");
                btnLogout.setVisibility(View.GONE); // Hide "Log Out" button
            }
        });
    }

    private void setupClickListeners() {
        // 1. Login / Profile Button
        btnProfile.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            // Navigate to AuthActivity or HostOnlineFragment which handles auth
            navController.navigate(R.id.action_settings_to_hostOnline); 
        });

        // 2. Logout Button
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    authViewModel.logout();
                    Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        // 3. Theme Selector
        llTheme.setOnClickListener(v -> showThemeDialog());
    }

    private void showThemeDialog() {
        String[] themes = {"Light", "Dark", "System Default"};
        int checkedItem = getCurrentThemeIndex();

        new AlertDialog.Builder(requireContext())
            .setTitle("Choose Theme")
            .setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                switch (which) {
                    case 0:
                        setAppTheme(AppCompatDelegate.MODE_NIGHT_NO, "Light");
                        break;
                    case 1:
                        setAppTheme(AppCompatDelegate.MODE_NIGHT_YES, "Dark");
                        break;
                    case 2:
                        setAppTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, "System Default");
                        break;
                }
                dialog.dismiss();
            })
            .show();
    }

    private void setAppTheme(int mode, String themeName) {
        AppCompatDelegate.setDefaultNightMode(mode);
        // Save to SharedPreferences for persistence across app restarts
        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        prefs.edit().putInt("theme_mode", mode).apply();
        updateThemeText();
    }

    private int getCurrentThemeIndex() {
        int mode = AppCompatDelegate.getDefaultNightMode();
        if (mode == AppCompatDelegate.MODE_NIGHT_NO) return 0;
        if (mode == AppCompatDelegate.MODE_NIGHT_YES) return 1;
        return 2; // System Default
    }

    private void updateThemeText() {
        int mode = AppCompatDelegate.getDefaultNightMode();
        if (mode == AppCompatDelegate.MODE_NIGHT_NO) tvCurrentTheme.setText("Light");
        else if (mode == AppCompatDelegate.MODE_NIGHT_YES) tvCurrentTheme.setText("Dark");
        else tvCurrentTheme.setText("System Default");
    }
}