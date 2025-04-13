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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.example.emerband.utils.EmergencyUtils;
import com.example.emerband.utils.TestingUtils;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.Button;
import android.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "EmerbandPrefs";
    private static final String KEY_USER_NAME = "userName";
    
    // Permission request code
    private static final int PERMISSION_REQUEST_CODE = 123;
    
    // Required permissions for Android < 12
    private final String[] requiredPermissions = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS
    };
    
    // Additional permissions for Android 12+
    private final String[] androidSPermissions = {
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    };

    // UI Elements
    private ImageView ivBluetoothStatus;
    private TextView tvBluetoothStatus;
    private ImageView ivBatteryStatus;
    private MaterialCardView debugCard;
    private FloatingActionButton fabEmergency;
    private Button btnTestEmergency;
    private Button btnTestFakeCall;
    private Button btnTestAlert;
    private Button btnTestOffline;
    private Toolbar toolbar;

    // Additional UI Elements
    private Button btnEmergencyCall;
    private Button btnCyberCell;
    private Button btnLocation;
    private Button btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Enable test mode for development
        TestingUtils.setTestMode(true);
        TestingUtils.setTestPhoneNumber("0000000000");
        
        try {
            initializeViews();
            setupToolbar();
            checkPermissions();
            setupClickListeners();
            showDebugSection();
            
            // Check if we're coming from an emergency notification
            handleIntent(getIntent());
        } catch (Exception e) {
            String error = "Error initializing app: " + e.getMessage();
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error initializing app", e);
        }
    }
    
    private void initializeViews() {
        try {
            ivBluetoothStatus = findViewById(R.id.ivBluetoothStatus);
            tvBluetoothStatus = findViewById(R.id.tvBluetoothStatus);
            ivBatteryStatus = findViewById(R.id.ivBatteryStatus);
            debugCard = findViewById(R.id.debugCard);
            fabEmergency = findViewById(R.id.fabEmergency);
            
            // Initialize emergency feature buttons
            btnEmergencyCall = findViewById(R.id.btnEmergencyCall);
            btnCyberCell = findViewById(R.id.btnCyberCell);
            btnLocation = findViewById(R.id.btnLocation);
            btnSettings = findViewById(R.id.btnSettings);
            
            // Initialize debug buttons
            btnTestEmergency = findViewById(R.id.btnTestEmergency);
            btnTestFakeCall = findViewById(R.id.btnTestFakeCall);
            btnTestAlert = findViewById(R.id.btnTestAlert);
            btnTestOffline = findViewById(R.id.btnTestOffline);
            toolbar = findViewById(R.id.toolbar);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize views: " + e.getMessage(), e);
        }
    }
    
    private void setupToolbar() {
        try {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.app_name);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up toolbar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void checkPermissions() {
        try {
            boolean allPermissionsGranted = true;
            
            // Check basic permissions
            for (String permission : requiredPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            
            // Check Android 12+ specific permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                for (String permission : androidSPermissions) {
                    if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false;
                        break;
                    }
                }
            }
            
            if (!allPermissionsGranted) {
                // Request all necessary permissions
                String[] permissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        ? combineArrays(requiredPermissions, androidSPermissions)
                        : requiredPermissions;
                        
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            } else {
                startBLEService();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error checking permissions: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error checking permissions", e);
        }
    }
    
    private String[] combineArrays(String[] arr1, String[] arr2) {
        String[] result = new String[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                startBLEService();
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                        "Required permissions not granted. Some features may not work.",
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }
    
    private void setupClickListeners() {
        // Emergency Call Button
        btnEmergencyCall.setOnClickListener(v -> {
            try {
                EmergencyUtils.makeEmergencyCall(this);
            } catch (Exception e) {
                Toast.makeText(this, "Error making emergency call: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error making emergency call", e);
            }
        });

        // Cyber Cell Button
        btnCyberCell.setOnClickListener(v -> {
            try {
                EmergencyUtils.makeCyberCellCall(this);
            } catch (Exception e) {
                Toast.makeText(this, "Error calling cyber cell: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error calling cyber cell", e);
            }
        });

        // Location Button
        btnLocation.setOnClickListener(v -> {
            try {
                EmergencyUtils.getCurrentLocation(this, location -> {
                    String locationStr = "Latitude: " + location.getLatitude() + 
                                      "\nLongitude: " + location.getLongitude();
                    new AlertDialog.Builder(this)
                        .setTitle("Current Location")
                        .setMessage(locationStr)
                        .setPositiveButton("OK", null)
                        .show();
                });
            } catch (Exception e) {
                Toast.makeText(this, "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error getting location", e);
            }
        });

        // Test Emergency Button
        btnTestEmergency.setOnClickListener(v -> {
            try {
                EmergencyUtils.getCurrentLocation(this, location -> {
                    String locationStr = "http://maps.google.com/?q=" + 
                                      location.getLatitude() + "," + 
                                      location.getLongitude();
                    EmergencyUtils.sendEmergencyMessage(this, locationStr);
                });
            } catch (Exception e) {
                Toast.makeText(this, "Error sending emergency message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error sending emergency message", e);
            }
        });

        // Fake Call Button
        btnTestFakeCall.setOnClickListener(v -> {
            try {
                EmergencyUtils.playFakeCall(this);
            } catch (Exception e) {
                Toast.makeText(this, "Error initiating fake call: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error initiating fake call", e);
            }
        });

        // Alert Button
        btnTestAlert.setOnClickListener(v -> {
            try {
                EmergencyUtils.playSiren(this);
            } catch (Exception e) {
                Toast.makeText(this, "Error playing siren: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error playing siren", e);
            }
        });

        // Offline Mode Button
        btnTestOffline.setOnClickListener(v -> {
            try {
                new AlertDialog.Builder(this)
                    .setTitle("Enable Airplane Mode")
                    .setMessage("Please enable airplane mode to test offline functionality")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        EmergencyUtils.toggleAirplaneMode(this);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            } catch (Exception e) {
                Toast.makeText(this, "Error toggling airplane mode: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error toggling airplane mode", e);
            }
        });

        // Emergency FAB
        fabEmergency.setOnClickListener(v -> {
            try {
                handleEmergency();
            } catch (Exception e) {
                Toast.makeText(this, "Error handling emergency: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error handling emergency", e);
            }
        });
    }
    
    private void showDebugSection() {
        try {
            if (debugCard != null) {
                debugCard.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error showing debug section: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error showing debug section", e);
        }
    }
    
    private void handleEmergency() {
        try {
            // Get current location
            EmergencyUtils.getCurrentLocation(this, location -> {
                try {
                    // Create Google Maps link with location
                    String locationStr = "http://maps.google.com/?q=" + 
                                      location.getLatitude() + "," + 
                                      location.getLongitude();
                    
                    // Send emergency message with location
                    EmergencyUtils.sendEmergencyMessage(this, locationStr);
                    
                    // Make emergency call
                    EmergencyUtils.makeEmergencyCall(this);
                    
                    // Show confirmation toast
                    Toast.makeText(this, "Emergency protocols activated", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error executing emergency protocols: " + e.getMessage(), 
                                 Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error in emergency protocol execution", e);
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error handling emergency: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error handling emergency", e);
        }
    }
    
    private void openEmergencyContacts() {
        try {
            Intent intent = new Intent(this, EmergencyContactsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening emergency contacts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error opening emergency contacts", e);
        }
    }
    
    private void openCyberCell() {
        try {
            Intent intent = new Intent(this, CyberCellActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening cyber cell: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error opening cyber cell", e);
        }
    }
    
    private void openLocationSettings() {
        try {
            Intent intent = new Intent(this, LocationSettingsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening location settings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error opening location settings", e);
        }
    }
    
    private void openSettings() {
        try {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening settings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error opening settings", e);
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }
    
    private void handleIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra("EMERGENCY", false)) {
            showEmergencyUI();
        }
    }
    
    private void showEmergencyUI() {
        // Show emergency dialog
        new EmergencyDialogFragment().show(getSupportFragmentManager(), "emergency_dialog");
    }
    
    private void startBLEService() {
        try {
            // Check if Bluetooth is available
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Toast.makeText(this, "Bluetooth is not available on this device", Toast.LENGTH_LONG).show();
                return;
            }
            
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth is not available on this device", Toast.LENGTH_LONG).show();
                return;
            }
            
            // Request to enable Bluetooth if it's not enabled
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(enableBtIntent);
                    return;
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
        } catch (Exception e) {
            Toast.makeText(this, "Error starting BLE service: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error starting BLE service", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.menu_contacts) {
            openEmergencyContacts();
            return true;
        } else if (id == R.id.menu_location) {
            openLocationSettings();
            return true;
        } else if (id == R.id.menu_help) {
            openCyberCell();
            return true;
        } else if (id == R.id.menu_settings) {
            openSettings();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
} 