package com.example.emerband;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "EmerbandPrefs";
    private static final String KEY_USER_NAME = "userName";
    
    // Permission request code
    private static final int PERMISSION_REQUEST_CODE = 123;
    
    // Required permissions
    private final String[] requiredPermissions = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.CAMERA
    };
    
    // Additional permissions for Android 12+
    private final String[] androidSPermissions = {
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Check if we're coming from an emergency notification
        handleIntent(getIntent());
        
        // Example of retrieving the user's name from SharedPreferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userName = settings.getString(KEY_USER_NAME, "User"); // Default to "User" if not found
        
        // Example of displaying the user's name
        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        if (welcomeTextView != null) {
            welcomeTextView.setText("Welcome, " + userName + "!");
        }
        
        /*
         * TODO: Future Enhancements for Fake Call Feature:
         * 1. Add UI elements to allow the user to customize fake caller name and number
         * 2. Allow the user to select a custom ringtone for fake calls
         * 3. Implement a scheduler to trigger fake calls at specific times
         * 4. Add settings to configure call duration before auto-hangup
         * 5. Support response gestures (swipe to answer/decline) to mimic native call UI
         * 6. Allow the user to record and play a voice during the call for added realism
         */
        
        // Check and request necessary permissions
        checkAndRequestPermissions();
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Handle the intent in case we're coming from a notification
        handleIntent(intent);
    }
    
    /**
     * Handle intent, particularly from emergency notifications
     */
    private void handleIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra("EMERGENCY", false)) {
            // This is an emergency intent, handle appropriately
            showEmergencyUI();
        }
    }
    
    /**
     * Show emergency UI (placeholder for now)
     */
    private void showEmergencyUI() {
        // In a real app, this would display an emergency UI with options
        Toast.makeText(this, "EMERGENCY ALERT RECEIVED", Toast.LENGTH_LONG).show();
        
        // Example - change background color or show an alert dialog
    }
    
    /**
     * Check and request required permissions
     */
    private void checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        
        // Check all required permissions
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        // Check Android 12 specific permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            for (String permission : androidSPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
        }
        
        // Request permissions if needed
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        } else {
            // All permissions are granted, start the BLE service
            startBLEService();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            
            if (allPermissionsGranted) {
                // All permissions granted, start the BLE service
                startBLEService();
            } else {
                // Some permissions denied
                Toast.makeText(this, "Some required permissions were denied. The app may not function properly.", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    /**
     * Start the BLE background service
     */
    private void startBLEService() {
        // Check if Bluetooth is available and enabled
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth is not available on this device", Toast.LENGTH_LONG).show();
                return;
            }
            
            if (!bluetoothAdapter.isEnabled()) {
                // Bluetooth is not enabled, prompt user to enable it
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(enableBtIntent);
                    return;
                } else {
                    Log.e(TAG, "Bluetooth connect permission not granted");
                    Toast.makeText(this, "Bluetooth permissions not granted", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
        
        // Start the BLE service
        Intent serviceIntent = new Intent(this, BLEBackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        
        Log.d(TAG, "BLE service started");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_cyber_cell_settings) {
            // Launch the cyber cell settings activity
            Intent settingsIntent = new Intent(this, CyberCellSettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
} 