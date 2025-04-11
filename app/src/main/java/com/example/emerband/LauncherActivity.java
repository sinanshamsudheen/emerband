package com.example.emerband;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Launcher activity that decides whether to show the SetupActivity (for first-time users)
 * or MainActivity (for returning users).
 */
public class LauncherActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "EmerbandPrefs";
    private static final String KEY_FIRST_LAUNCH = "isFirstLaunch";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if this is the first launch of the app
        if (isFirstLaunch()) {
            // First launch, show SetupActivity
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
        } else {
            // Not first launch, skip to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        
        // Close this activity so it doesn't remain in the back stack
        finish();
    }
    
    /**
     * Checks if this is the first launch of the application
     * @return true if this is the first launch, false otherwise
     */
    private boolean isFirstLaunch() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // If KEY_FIRST_LAUNCH doesn't exist yet, it's the first launch
        // Default to true (first launch) if the preference doesn't exist
        boolean isFirstLaunch = settings.getBoolean(KEY_FIRST_LAUNCH, true);
        
        return isFirstLaunch;
    }
} 