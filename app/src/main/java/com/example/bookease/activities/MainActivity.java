package com.example.bookease.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.bookease.R;
import com.example.bookease.fragments.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);

        // Default fragment
        loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home)     loadFragment(new HomeFragment());
            else if (id == R.id.nav_booking)  loadFragment(new BookingFragment());
            else if (id == R.id.nav_history)  loadFragment(new HistoryFragment());
            else if (id == R.id.nav_settings) loadFragment(new SettingsFragment());
            return true;
        });
    }

    public void loadFragment(Fragment fragment) {
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (current != null && current.getClass().equals(fragment.getClass())) return;

        getSupportFragmentManager().beginTransaction()
                .setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    public void navigateTo(int navId) {
        bottomNav.setSelectedItemId(navId);
    }
}
