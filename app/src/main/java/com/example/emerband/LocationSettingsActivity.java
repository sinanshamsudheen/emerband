package com.example.emerband;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class LocationSettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "EmerbandPrefs";
    private static final String KEY_HIGH_ACCURACY = "high_accuracy_mode";

    private SwitchMaterial switchHighAccuracy;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_settings);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.menu_location);
        }

        // Initialize preferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Setup high accuracy switch
        switchHighAccuracy = findViewById(R.id.switchHighAccuracy);
        switchHighAccuracy.setChecked(prefs.getBoolean(KEY_HIGH_ACCURACY, false));
        switchHighAccuracy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_HIGH_ACCURACY, isChecked).apply();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public static boolean isHighAccuracyEnabled(SharedPreferences prefs) {
        return prefs.getBoolean(KEY_HIGH_ACCURACY, false);
    }
} 