package com.example.emerband;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SetupActivity extends AppCompatActivity {
    
    private EditText nameEditText;
    private Button continueButton;
    private TextView instructionTextView;
    
    private static final String PREFS_NAME = "EmerbandPrefs";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_FIRST_LAUNCH = "isFirstLaunch";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        
        // Initialize UI components
        nameEditText = findViewById(R.id.nameEditText);
        continueButton = findViewById(R.id.continueButton);
        instructionTextView = findViewById(R.id.instructionTextView);
        
        // Set up the continue button click listener
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSaveName();
            }
        });
    }
    
    private void validateAndSaveName() {
        String name = nameEditText.getText().toString().trim();
        
        // Validate input - check if name is empty
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter your name to continue", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Save the name in SharedPreferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_USER_NAME, name);
        editor.putBoolean(KEY_FIRST_LAUNCH, false); // Mark first launch as complete
        editor.apply();
        
        // Navigate to MainActivity
        Intent intent = new Intent(SetupActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Close this activity so user can't go back
    }
    
    /**
     * Static method to check if the setup screen should be shown
     * @param context Application context
     * @return true if this is the first launch, false otherwise
     */
    public static boolean shouldShowSetup(android.content.Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Default to true (show setup) if the preference doesn't exist yet
        return settings.getBoolean(KEY_FIRST_LAUNCH, true);
    }
} 