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
import com.example.emerband.utils.TestingUtils;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;

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
    private ExtendedFloatingActionButton fabEmergency;
    private MaterialButton btnTestEmergency;
    private MaterialButton btnTestFakeCall;
    private MaterialButton btnTestCyberCell;
    private MaterialButton btnTestAlert;
    private MaterialButton btnTestOffline;
    private MaterialButton btnTestCrash;
    private Toolbar toolbar;

    // Additional UI Elements
    private MaterialButton btnEmergencyCall;
    private MaterialButton btnCyberCell;
    private MaterialButton btnLocation;
    private MaterialButton btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            btnTestCyberCell = findViewById(R.id.btnTestCyberCell);
            btnTestAlert = findViewById(R.id.btnTestAlert);
            btnTestOffline = findViewById(R.id.btnTestOffline);
            btnTestCrash = findViewById(R.id.btnTestCrash);
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
        View.OnClickListener errorHandler = v -> {
            try {
                handleClick(v.getId());
            } catch (Exception e) {
                Toast.makeText(this, "Error handling click: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error handling click", e);
            }
        };

        // Set up emergency feature button click listeners
        btnEmergencyCall.setOnClickListener(errorHandler);
        btnCyberCell.setOnClickListener(errorHandler);
        btnLocation.setOnClickListener(errorHandler);
        btnSettings.setOnClickListener(errorHandler);

        // Set up debug button click listeners
        btnTestEmergency.setOnClickListener(errorHandler);
        btnTestFakeCall.setOnClickListener(errorHandler);
        btnTestCyberCell.setOnClickListener(errorHandler);
        btnTestAlert.setOnClickListener(errorHandler);
        btnTestOffline.setOnClickListener(errorHandler);
        btnTestCrash.setOnClickListener(errorHandler);
        fabEmergency.setOnClickListener(errorHandler);
    }
    
    private void handleClick(int viewId) {
        if (viewId == R.id.btnTestEmergency || viewId == R.id.fabEmergency) {
            TestingUtils.simulateBleSignal(this, 'E');
        } else if (viewId == R.id.btnTestFakeCall) {
            TestingUtils.simulateBleSignal(this, 'F');
        } else if (viewId == R.id.btnTestCyberCell) {
            TestingUtils.simulateBleSignal(this, 'C');
        } else if (viewId == R.id.btnTestAlert) {
            TestingUtils.simulateBleSignal(this, 'A');
        } else if (viewId == R.id.btnTestOffline) {
            TestingUtils.simulateOfflineMode(this, 10000);
        } else if (viewId == R.id.btnTestCrash) {
            TestingUtils.testRecoveryFromForceClose(this);
        } else if (viewId == R.id.btnEmergencyCall) {
            openEmergencyContacts();
        } else if (viewId == R.id.btnCyberCell) {
            openHelp();
        } else if (viewId == R.id.btnLocation) {
            openLocationSettings();
        } else if (viewId == R.id.btnSettings) {
            openSettings();
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
            EmergencyDialogFragment dialog = new EmergencyDialogFragment();
            dialog.show(getSupportFragmentManager(), "emergency_dialog");
        } catch (Exception e) {
            Toast.makeText(this, "Error handling emergency: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private void openSettings() {
        Toast.makeText(this, "Opening Settings...", Toast.LENGTH_SHORT).show();
        // TODO: Implement settings activity
    }
    
    private void openEmergencyContacts() {
        Toast.makeText(this, "Opening Emergency Contacts...", Toast.LENGTH_SHORT).show();
        // TODO: Implement emergency contacts activity
    }
    
    private void openLocationSettings() {
        Toast.makeText(this, "Opening Location Settings...", Toast.LENGTH_SHORT).show();
        // TODO: Implement location settings activity
    }
    
    private void openHelp() {
        Toast.makeText(this, "Opening Help & Support...", Toast.LENGTH_SHORT).show();
        // TODO: Implement help activity
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
            openHelp();
            return true;
        } else if (id == R.id.menu_settings) {
            openSettings();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
} 