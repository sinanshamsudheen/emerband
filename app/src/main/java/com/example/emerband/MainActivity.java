package com.example.emerband;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import com.example.emerband.database.DatabaseHelper;
import com.example.emerband.models.Contact;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Handler;
import android.graphics.Color;

public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "EmerbandPrefs";
    private static final String KEY_USER_NAME = "userName";
    
    // Permission request code
    private static final int PERMISSION_REQUEST_CODE = 1;
    
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
    private ImageView ivBluetoothIcon;
    private TextView tvBluetoothStatus;
    private ImageView ivDebugExpand;
    private LinearLayout debugHeader;
    private LinearLayout debugContent;
    private FloatingActionButton fabEmergency;
    private MaterialCardView debugCard;
    private Button btnTestFakeCall;
    private Button btnTestAlert;
    private Button btnTestOffline;
    private Button btnTestEmergency;
    private Toolbar toolbar;

    // Additional UI Elements
    private Button btnEmergencyCall;
    private Button btnCyberCell;
    private Button btnLocation;
    private Button btnSettings;
    private Button btnEmergencyContacts;

    private boolean isAlertPlaying = false;
    private MediaPlayer sirenMediaPlayer;

    private static final int PERMISSION_SEND_SMS = 123;
    private static final int PERMISSION_CALL_PHONE = 2;
    private static final int PERMISSION_LOCATION = 125;
    private static final int PERMISSION_BLUETOOTH = 126;
    private static final int PERMISSION_CAMERA = 127;
    private static final int REQUEST_ENABLE_BT = 3;

    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Set up the toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        
        // Enable test mode for development
        TestingUtils.setTestMode(true);
        TestingUtils.setTestPhoneNumber("0000000000");
        
        try {
            initializeViews();
            checkAndRequestPermissions();
            setupClickListeners();
            showDebugSection();
            
            // Check if we're coming from an emergency notification
            handleIntent(getIntent());

            // Check Bluetooth status
            updateBluetoothStatus();
        } catch (Exception e) {
            String error = "Error initializing app: " + e.getMessage();
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error initializing app", e);
        }
    }
    
    private void initializeViews() {
        try {
            // Initialize status views
            ivBluetoothIcon = findViewById(R.id.iv_bluetooth_icon);
            tvBluetoothStatus = findViewById(R.id.tv_bluetooth_status);

            // Initialize emergency features buttons
            btnEmergencyContacts = findViewById(R.id.btnEmergencyContacts);
            btnSettings = findViewById(R.id.btnSettings);

            // Initialize debug tools views
            debugCard = findViewById(R.id.debugCard);
            debugHeader = findViewById(R.id.debugHeader);
            debugContent = findViewById(R.id.debugContent);
            ivDebugExpand = findViewById(R.id.ivDebugExpand);
            btnTestEmergency = findViewById(R.id.btnTestEmergency);
            btnEmergencyCall = findViewById(R.id.btnEmergencyCall);
            btnCyberCell = findViewById(R.id.btnCyberCell);
            btnLocation = findViewById(R.id.btnLocation);
            btnTestFakeCall = findViewById(R.id.btnTestFakeCall);
            btnTestAlert = findViewById(R.id.btnTestAlert);
            btnTestOffline = findViewById(R.id.btnTestOffline);

            // Remove FAB initialization since we removed it from layout
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize views: " + e.getMessage(), e);
        }
    }
    
    private void setupClickListeners() {
        try {
            // Debug Tools expand/collapse
            debugHeader.setOnClickListener(v -> {
                boolean isExpanded = debugContent.getVisibility() == View.VISIBLE;
                debugContent.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
                ivDebugExpand.animate()
                    .rotation(isExpanded ? 0 : 180)
                    .setDuration(200)
                    .start();
            });

            // Emergency Contacts button
            btnEmergencyContacts.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, EmergencyContactsActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Error opening Emergency Contacts: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });

            // Change Settings button to About button
            btnSettings.setText("About"); // Change button text to "About"
            btnSettings.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, SettingsActivity.class); // This is now the About activity
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Error opening About: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });

            // Debug buttons click listeners
            setupDebugButtonListeners();

        } catch (Exception e) {
            Toast.makeText(this, "Error setting up click listeners: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        }
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
            
            bluetoothAdapter = bluetoothManager.getAdapter();
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
            Intent intent = new Intent(this, LocationSettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_help) {
            Intent intent = new Intent(this, HelpSupportActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_about) {
            Intent intent = new Intent(this, SettingsActivity.class); // This should be the About activity
            startActivity(intent);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void startSiren() {
        try {
            if (sirenMediaPlayer == null) {
                sirenMediaPlayer = MediaPlayer.create(this, R.raw.siren);
                sirenMediaPlayer.setLooping(true);
            }
            sirenMediaPlayer.start();
        } catch (Exception e) {
            Log.e(TAG, "Error starting siren", e);
            Toast.makeText(this, "Error starting siren", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopSiren() {
        try {
            if (sirenMediaPlayer != null) {
                sirenMediaPlayer.stop();
                sirenMediaPlayer.release();
                sirenMediaPlayer = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping siren", e);
            Toast.makeText(this, "Error stopping siren", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSiren();
    }

    private void setupDebugButtonListeners() {
        // Test Emergency Button
        btnTestEmergency.setOnClickListener(v -> {
            try {
                // Check for SMS permission
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.SEND_SMS},
                            PERMISSION_SEND_SMS);
                    return;
                }

                // Send emergency SMS directly
                EmergencyUtils.sendEmergencyMessage(this, "");

            } catch (Exception e) {
                Toast.makeText(this, "Error sending emergency messages: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Emergency Call Button
        btnEmergencyCall.setOnClickListener(v -> {
            try {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:112"));
                startActivity(callIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Error making emergency call: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Cyber Cell Button
        btnCyberCell.setOnClickListener(v -> {
            try {
                EmergencyUtils.makeCyberCellCall(this);
            } catch (Exception e) {
                Toast.makeText(this, "Error calling cyber cell: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Location Button
        btnLocation.setOnClickListener(v -> {
            try {
                testLocation();
            } catch (Exception e) {
                Toast.makeText(this, "Error getting location: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Test Fake Call Button
        btnTestFakeCall.setOnClickListener(v -> {
            try {
                if (EmergencyUtils.isPlayingFakeCall()) {
                    EmergencyUtils.stopFakeCall();
                    Toast.makeText(this, "Fake call stopped", Toast.LENGTH_SHORT).show();
                } else {
                    EmergencyUtils.playFakeCall(this);
                    Intent intent = new Intent(this, FakeCallActivity.class);
                    startActivity(intent);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error with fake call: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Alert Button
        btnTestAlert.setOnClickListener(v -> {
            try {
                if (isAlertPlaying) {
                    stopSiren();
                    btnTestAlert.setText(R.string.test_alert);
                    isAlertPlaying = false;
                } else {
                    startSiren();
                    btnTestAlert.setText(R.string.stop_alert);
                    isAlertPlaying = true;
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error with alert: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Error toggling airplane mode: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Emergency FAB
        fabEmergency.setOnClickListener(v -> {
            try {
                handleEmergency();
            } catch (Exception e) {
                Toast.makeText(this, "Error handling emergency: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void testLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_LOCATION);
                return;
            }

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean highAccuracy = LocationSettingsActivity.isHighAccuracyEnabled(prefs);

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                Location lastKnownLocation = null;
                
                if (highAccuracy) {
                    // Try to get cached location first
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastKnownLocation == null) {
                        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }

                if (lastKnownLocation != null) {
                    String locationStr = "Latitude: " + lastKnownLocation.getLatitude() +
                            "\nLongitude: " + lastKnownLocation.getLongitude();
                    Toast.makeText(this, locationStr, Toast.LENGTH_LONG).show();
                } else {
                    // If no cached location or high accuracy is off, request location updates
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                            new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    String locationStr = "Latitude: " + location.getLatitude() +
                                            "\nLongitude: " + location.getLongitude();
                                    Toast.makeText(MainActivity.this, locationStr, Toast.LENGTH_LONG).show();
                                    locationManager.removeUpdates(this);
                                }

                                @Override
                                public void onStatusChanged(String provider, int status, Bundle extras) {}

                                @Override
                                public void onProviderEnabled(String provider) {}

                                @Override
                                public void onProviderDisabled(String provider) {
                                    Toast.makeText(MainActivity.this, "Please enable GPS", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error getting location", e);
        }
    }

    private void setupEmergencyButton() {
        // Emergency button functionality moved to Test Emergency button
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        // Check for required permissions
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        // Check for additional permissions for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            for (String permission : androidSPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(permission);
                }
            }
        }

        // Request permissions if needed
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    private void updateBluetoothStatus() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            ivBluetoothIcon.setImageResource(R.drawable.ic_bluetooth); // Set active Bluetooth icon
            tvBluetoothStatus.setText("Bluetooth is ON"); // Ensure this text is correct
        } else {
            ivBluetoothIcon.setImageResource(R.drawable.ic_bluetooth_disabled); // Set disabled Bluetooth icon
            tvBluetoothStatus.setText("Bluetooth is OFF");
        }
    }
} 