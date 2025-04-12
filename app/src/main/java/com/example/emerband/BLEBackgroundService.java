package com.example.emerband;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.PowerManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.emerband.offline.OfflineModeManager;
import com.example.emerband.utils.ConnectivityUtils;
import com.example.emerband.utils.ResourceManager;

public class BLEBackgroundService extends Service {
    
    private static final String TAG = "BLEBackgroundService";
    
    // Notification channel and ID
    private static final String CHANNEL_ID = "BleServiceChannel";
    private static final int NOTIFICATION_ID = 1001;
    
    // BLE scanning parameters
    private static final long SCAN_PERIOD = 10000; // 10 seconds
    private static final String TARGET_DEVICE_NAME = "EmergencyWatch";
    private static final String TARGET_SERVICE_UUID = "0000180D-0000-1000-8000-00805f9b34fb"; // Heart Rate Service UUID as example
    
    // Emergency codes from watch
    private static final char EMERGENCY_CODE = 'E';  // Emergency signal
    private static final char FALL_CODE = 'F';      // Fall detected
    private static final char CYBER_CELL_CODE = 'C'; // Cyber cell alert (previously cancel code)
    private static final char ALERT_CODE = 'A';     // General alert
    
    // Code for cancellation (if needed in the future)
    private static final char CANCEL_CODE = 'X';    // New cancel code (not used currently)
    
    // SharedPreferences constants
    private static final String PREFS_NAME = "EmerbandPrefs";
    private static final String KEY_USER_NAME = "userName";
    
    // Bluetooth related objects
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;
    
    // Handler for delayed tasks and timeouts
    private Handler handler;
    
    // Wake lock to keep CPU running while service is active
    private PowerManager.WakeLock wakeLock;
    
    // OfflineModeManager instance
    private OfflineModeManager offlineModeManager;
    
    // Scan callback
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            
            String deviceName = null;
            try {
                if (ActivityCompat.checkSelfPermission(BLEBackgroundService.this, 
                        Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    deviceName = device.getName();
                }
            } catch (SecurityException e) {
                Log.e(TAG, "Bluetooth permission not granted", e);
                return;
            }
            
            Log.d(TAG, "Found device: " + (deviceName != null ? deviceName : "Unknown"));
            
            if (deviceName != null && deviceName.equals(TARGET_DEVICE_NAME)) {
                // Stop scanning when we find our target device
                stopScanning();
                connectToDevice(device);
            }
        }
        
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "Scan failed with error: " + errorCode);
        }
    };
    
    // GATT callback
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                // Discover services after successful connection
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                // Start scanning again when disconnected
                startScanning();
            }
        }
        
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Services discovered.");
                // Enable notification for relevant characteristics
                enableNotifications(gatt);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
        
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                char value = (char) data[0];
                Log.d(TAG, "Received value: " + value);
                processReceivedValue(value);
            }
        }
    };
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "BLE Service created");
        
        handler = new Handler(Looper.getMainLooper());
        
        // Initialize Bluetooth adapter and scanner
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter != null) {
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            }
        }
        
        // Initialize OfflineModeManager
        offlineModeManager = OfflineModeManager.getInstance(this);
        
        // Create a wake lock to keep the CPU running
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Emerband:BLEWakeLock");
            wakeLock.acquire();
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        
        // Start as a foreground service with a notification
        startForeground(NOTIFICATION_ID, createNotification());
        
        // Register connectivity receiver for offline mode
        offlineModeManager.registerConnectivityReceiver();
        
        // Start scanning for BLE devices
        startScanning();
        
        // If the service gets killed, restart it
        return START_STICKY;
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // We don't provide binding
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "Service destroyed");
        
        // Unregister connectivity receiver
        offlineModeManager.unregisterConnectivityReceiver();
        
        // Stop scanning
        stopScanning();
        
        // Disconnect GATT
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
        
        // Release wake lock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        
        super.onDestroy();
    }
    
    /**
     * Create a notification for the foreground service
     */
    private Notification createNotification() {
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "BLE Service Channel",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Channel for BLE Background Service");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
        
        // Create pending intent for notification click
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE);
        
        // Build and return the notification
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Emerband Active")
                .setContentText("Monitoring for emergency signals")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .build();
    }
    
    /**
     * Start scanning for BLE devices
     */
    private void startScanning() {
        if (bluetoothAdapter == null || bluetoothLeScanner == null) {
            Log.e(TAG, "Bluetooth not initialized");
            return;
        }
        
        // Create scan filters to only look for our target device
        List<ScanFilter> filters = new ArrayList<>();
        
        // Add filter for device name
        ScanFilter nameFilter = new ScanFilter.Builder()
                .setDeviceName(TARGET_DEVICE_NAME)
                .build();
        filters.add(nameFilter);
        
        // Add filter for service UUID if needed
        try {
            ParcelUuid serviceUuid = ParcelUuid.fromString(TARGET_SERVICE_UUID);
            ScanFilter uuidFilter = new ScanFilter.Builder()
                    .setServiceUuid(serviceUuid)
                    .build();
            filters.add(uuidFilter);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid UUID format", e);
        }
        
        // Configure scan settings for balanced power usage
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER) // Use LOW_POWER for better battery life
                .build();
        
        // Start the scan with our filters and settings
        try {
            bluetoothLeScanner.startScan(filters, settings, leScanCallback);
            Log.d(TAG, "Started BLE scan");
            
            // Stop scanning after SCAN_PERIOD to conserve battery
            handler.postDelayed(this::stopScanning, SCAN_PERIOD);
        } catch (Exception e) {
            Log.e(TAG, "Error starting BLE scan", e);
        }
    }
    
    /**
     * Stop the ongoing BLE scan
     */
    private void stopScanning() {
        if (bluetoothAdapter != null && bluetoothLeScanner != null) {
            try {
                bluetoothLeScanner.stopScan(leScanCallback);
                Log.d(TAG, "Stopped BLE scan");
            } catch (Exception e) {
                Log.e(TAG, "Error stopping BLE scan", e);
            }
        }
    }
    
    /**
     * Connect to a BLE device
     */
    private void connectToDevice(BluetoothDevice device) {
        Log.d(TAG, "Connecting to device: " + device.getName());
        
        // Close any existing connection
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
        
        // Connect to the device
        bluetoothGatt = device.connectGatt(this, true, gattCallback);
    }
    
    /**
     * Enable notifications for relevant characteristics
     */
    private void enableNotifications(BluetoothGatt gatt) {
        // Typically, you would look for specific services and characteristics here
        // For demonstration, we'll just log the available services
        for (BluetoothGattService service : gatt.getServices()) {
            Log.d(TAG, "Found service: " + service.getUuid());
            
            // For each service, find the notification characteristic
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                // Set up notification for all characteristics that support it
                // In a real app, you would check for specific characteristics
                if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    // Enable notification
                    boolean success = gatt.setCharacteristicNotification(characteristic, true);
                    Log.d(TAG, "Set notification for " + characteristic.getUuid() + ": " + success);
                }
            }
        }
    }
    
    /**
     * Process received value from the BLE device
     */
    private void processReceivedValue(char value) {
        // Main processing of received values based on the code
        switch (value) {
            case EMERGENCY_CODE:
                handleEmergency();
                break;
            case FALL_CODE:
                // For Fall Detection - Note: The 'F' signal is now used for both Fake Call and Fall Detection
                // We'll check for additional context to differentiate between the two
                if (isAdditionalContextForFallDetection()) {
                    handleFallDetection();
                } else {
                    // If no additional context indicating fall detection, treat as fake call
                    handleFakeCall();
                }
                break;
            case CYBER_CELL_CODE:
                handleCyberCellAlert();
                break;
            case ALERT_CODE:
                handleGeneralAlert();
                break;
            case CANCEL_CODE:
                handleCancelAlert();
                break;
            default:
                Log.d(TAG, "Unknown code received: " + value);
                break;
        }
    }
    
    /**
     * Check for additional context to determine if this is a fall detection
     * This is a placeholder - in a real implementation, you would check for additional
     * data from the BLE device to differentiate between fake call and fall detection
     */
    private boolean isAdditionalContextForFallDetection() {
        // In a real implementation, this would analyze additional data
        // For example, the BLE device could send "F:FALL" for fall detection
        // and "F:CALL" for fake call
        
        // For now, assuming all 'F' signals are for fake calls
        return false;
    }
    
    /**
     * Handle fake call request ('F' without fall detection context)
     */
    private void handleFakeCall() {
        Log.d(TAG, "FAKE CALL signal received!");
        
        // Launch the fake call activity
        Intent fakeCallIntent = new Intent(this, FakeCallActivity.class);
        fakeCallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(fakeCallIntent);
    }
    
    /**
     * Handle emergency signal ('E')
     */
    private void handleEmergency() {
        Log.d(TAG, "EMERGENCY signal received!");
        
        // Get GPS data from characteristic (if available)
        String gpsData = getGPSData();
        
        // Additional data for emergency context
        String additionalData = "";
        if (ResourceManager.getUserName(this) != null) {
            additionalData = "User: " + ResourceManager.getUserName(this);
        }
        
        // Use offline mode manager to handle the emergency
        // This will automatically handle online/offline scenarios
        offlineModeManager.handleEmergencyEvent(additionalData);
    }
    
    /**
     * Get GPS data from the connected device
     */
    private String getGPSData() {
        if (bluetoothGatt != null) {
            // Use the utility class to read GPS data from the GATT connection
            return BLEGattUtils.readGPSData(bluetoothGatt);
        }
        return null;
    }
    
    /**
     * Handle fall detection signal ('F')
     */
    private void handleFallDetection() {
        Log.d(TAG, "FALL DETECTION signal received!");
        
        // Show notification
        showEmergencyNotification("Fall Detected", "A fall has been detected! Are you OK?");
        
        // Get GPS data
        String gpsData = getGPSData();
        
        // Check connectivity and handle appropriately
        if (ConnectivityUtils.isInternetAvailable(this)) {
            // Send fall alert SMS directly
            sendEmergencySMS("ALERT: A fall has been detected. I may need assistance.");
        } else {
            // Store as offline emergency event
            offlineModeManager.handleEmergencyEvent("Fall detection");
            showToast("Fall alert stored offline. Will be sent when connectivity returns.");
        }
        
        // Launch emergency activity
        launchEmergencyActivity();
    }
    
    /**
     * Handle cyber cell alert signal ('C')
     */
    private void handleCyberCellAlert() {
        Log.d(TAG, "CYBER CELL ALERT signal received!");
        
        // Use offline mode manager to handle the cyber cell alert
        // This will automatically handle online/offline scenarios
        offlineModeManager.handleCyberCellEvent();
    }
    
    /**
     * Handle cancel alert signal ('C')
     */
    private void handleCancelAlert() {
        Log.d(TAG, "CANCEL ALERT signal received!");
        
        // Cancel any ongoing alerts
        NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID + 1);  // Cancel emergency notification
        }
        
        // Notify that alert was cancelled
        showToast("Alert cancelled");
    }
    
    /**
     * Handle general alert signal ('A')
     */
    private void handleGeneralAlert() {
        Log.d(TAG, "GENERAL ALERT signal received!");
        
        // Launch the full-screen alert activity
        Intent alertIntent = new Intent(this, AlertActivity.class);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(alertIntent);
    }
    
    /**
     * Display a toast message on the UI thread
     */
    private void showToast(final String message) {
        handler.post(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show());
    }
    
    /**
     * Show emergency notification
     */
    private void showEmergencyNotification(String title, String message) {
        // Create emergency notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID + "_emergency",
                    "Emergency Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("High priority alerts for emergencies");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
        
        // Create intent for notification click
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE);
        
        // Build emergency notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID + "_emergency")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVibrate(ResourceManager.EMERGENCY_VIBRATION_PATTERN)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        
        // Show the notification
        NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID + 1, notification);
        }
    }
    
    /**
     * Send emergency SMS to emergency contacts
     */
    private void sendEmergencySMS(String message) {
        // TODO: Implement getting emergency contacts from preferences
        // For now, we'll just log that we would send an SMS
        
        // Get user name from SharedPreferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userName = settings.getString(KEY_USER_NAME, "User");
        
        // Add user name to message
        String fullMessage = message + " - Sent by " + userName;
        
        Log.d(TAG, "Would send SMS: " + fullMessage);
        
        // Uncomment below code to actually send SMS
        // Note: SMS_SEND permission is required
        /*
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, fullMessage, null, null);
        */
    }
    
    /**
     * Launch the emergency activity
     */
    private void launchEmergencyActivity() {
        // Create intent to launch emergency activity
        Intent emergencyIntent = new Intent(this, MainActivity.class); // Replace with EmergencyActivity if available
        emergencyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emergencyIntent.putExtra("EMERGENCY", true);
        startActivity(emergencyIntent);
    }
} 