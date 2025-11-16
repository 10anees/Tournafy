package com.example.tournafy.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.tournafy.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            // Get the NavController
            NavController navController = navHostFragment.getNavController();
            
            // Find the BottomNavigationView
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_view);
            
            // Set up the BottomNavigationView with the NavController
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
    }
}