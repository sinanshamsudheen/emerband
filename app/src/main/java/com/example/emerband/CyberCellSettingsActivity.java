package com.example.emerband;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity for configuring cyber cell settings.
 */
public class CyberCellSettingsActivity extends AppCompatActivity {

    private EditText cyberCellNumberEditText;
    private EditText cyberAlertMessageEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cyber_cell_settings);

        // Set up the action bar with a back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Cyber Cell Settings");
        }

        // Initialize views
        cyberCellNumberEditText = findViewById(R.id.cyberCellNumberEditText);
        cyberAlertMessageEditText = findViewById(R.id.cyberAlertMessageEditText);
        saveButton = findViewById(R.id.saveButton);

        // Load existing settings
        loadSettings();

        // Set up save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }

    /**
     * Load existing settings from SharedPreferences
     */
    private void loadSettings() {
        String cyberCellNumber = CyberCellHandler.getCyberCellNumber(this);
        String cyberAlertMessage = CyberCellHandler.getCyberAlertMessage(this);

        cyberCellNumberEditText.setText(cyberCellNumber);
        cyberAlertMessageEditText.setText(cyberAlertMessage);
    }

    /**
     * Save settings to SharedPreferences
     */
    private void saveSettings() {
        String cyberCellNumber = cyberCellNumberEditText.getText().toString().trim();
        String cyberAlertMessage = cyberAlertMessageEditText.getText().toString().trim();

        // Validate inputs
        if (cyberCellNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a cyber cell number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cyberAlertMessage.isEmpty()) {
            Toast.makeText(this, "Please enter an alert message", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save settings
        CyberCellHandler.saveCyberCellNumber(this, cyberCellNumber);
        CyberCellHandler.saveCyberAlertMessage(this, cyberAlertMessage);

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button in the action bar
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 