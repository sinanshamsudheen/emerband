package com.example.emerband;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("About");
        }

        // Set the app description
        TextView aboutDescription = findViewById(R.id.aboutDescription);
        aboutDescription.setText(getAppDescription());
    }

    private String getAppDescription() {
        return "Emerband is a comprehensive Android emergency response application designed to work with a BLE-connected Arduino smartwatch. " +
               "It provides various emergency response capabilities, including sending alerts, making fake calls, contacting cyber cell helplines, " +
               "and triggering high-visibility alerts—all triggered via BLE signals from the smartwatch.\n\n" +
               "Key Features:\n" +
               "- Emergency alerts with GPS coordinates\n" +
               "- Fake call simulation for discreet escape\n" +
               "- Cyber cell alert for digital threats\n" +
               "- Multi-sensory alert system for emergencies\n" +
               "- Offline mode for storing emergency events\n\n" +
               "Developed by [Your Name].";
    }
} 