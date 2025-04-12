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

    // UI Elements
    private ImageView ivBluetoothStatus;
    private TextView tvBluetoothStatus;
    private ImageView ivBatteryStatus;
    private MaterialCardView debugCard;
    private ExtendedFloatingActionButton fabEmergency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize UI elements
        initializeViews();
        setupToolbar();
        setupClickListeners();
        showDebugSection();
        
        // Check if we're coming from an emergency notification
        handleIntent(getIntent());
        
        // Check and request necessary permissions
        checkAndRequestPermissions();
    }
    
    private void initializeViews() {
        ivBluetoothStatus = findViewById(R.id.ivBluetoothStatus);
        tvBluetoothStatus = findViewById(R.id.tvBluetoothStatus);
        ivBatteryStatus = findViewById(R.id.ivBatteryStatus);
        debugCard = findViewById(R.id.debugCard);
        fabEmergency = findViewById(R.id.fabEmergency);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    
    private void setupClickListeners() {
        // Emergency FAB
        fabEmergency.setOnClickListener(v -> handleEmergency());

        // Menu buttons
        findViewById(R.id.btnEmergencyCall).setOnClickListener(v -> openEmergencyContacts());
        findViewById(R.id.btnCyberCell).setOnClickListener(v -> openHelp());
        findViewById(R.id.btnLocation).setOnClickListener(v -> openLocationSettings());
        findViewById(R.id.btnSettings).setOnClickListener(v -> openSettings());

        // Debug buttons
        findViewById(R.id.btnTestEmergency).setOnClickListener(v -> 
            TestingUtils.simulateBleSignal(this, 'E'));
        findViewById(R.id.btnTestFakeCall).setOnClickListener(v -> 
            TestingUtils.testFakeCall(this, "1234567890", "Test emergency call"));
        findViewById(R.id.btnTestCyberCell).setOnClickListener(v -> 
            TestingUtils.testCyberCell(this));
        findViewById(R.id.btnTestAlert).setOnClickListener(v -> 
            TestingUtils.testAlert(this));
        findViewById(R.id.btnTestOffline).setOnClickListener(v -> 
            TestingUtils.testOffline(this));
        findViewById(R.id.btnTestCrash).setOnClickListener(v -> 
            TestingUtils.testCrash(this));
    }
    
    private void showDebugSection() {
        debugCard.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
    }
    
    private void handleEmergency() {
        EmergencyDialogFragment dialog = new EmergencyDialogFragment();
        dialog.show(getSupportFragmentManager(), "emergency_dialog");
    }
    
    private void openSettings() {
        Toast.makeText(this, "Settings will be implemented soon", Toast.LENGTH_SHORT).show();
    }
    
    private void openEmergencyContacts() {
        Toast.makeText(this, "Emergency Contacts will be implemented soon", Toast.LENGTH_SHORT).show();
    }
    
    private void openLocationSettings() {
        Toast.makeText(this, "Location Settings will be implemented soon", Toast.LENGTH_SHORT).show();
    }
    
    private void openHelp() {
        Toast.makeText(this, "Help & Support will be implemented soon", Toast.LENGTH_SHORT).show();
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
    
    private void checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            for (String permission : androidSPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
        }
        
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        } else {
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
                startBLEService();
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                        "Some required permissions were denied. The app may not function properly.",
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }
    
    private void startBLEService() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Bluetooth is not available on this device",
                        Snackbar.LENGTH_LONG).show();
                return;
            }
            
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(enableBtIntent);
                    return;
                }
            }
        }
        
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