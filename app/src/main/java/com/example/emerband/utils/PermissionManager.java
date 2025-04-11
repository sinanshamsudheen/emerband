package com.example.emerband.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralized permission manager for handling all runtime permissions
 * required by the application.
 */
public class PermissionManager {
    private static final String TAG = "PermissionManager";
    
    // Permission constants
    public static final String[] EMERGENCY_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE
    };
    
    public static final String[] ALERT_PERMISSIONS = {
            Manifest.permission.VIBRATE,
            Manifest.permission.CAMERA // For flashlight
    };
    
    public static final String[] BLE_PERMISSIONS = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    };
    
    public static final String[] BACKGROUND_PERMISSIONS = {
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
    };
    
    /**
     * Check if all permissions in the given array are granted
     * @param context Application context
     * @param permissions Array of permissions to check
     * @return true if all permissions are granted, false otherwise
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context == null || permissions == null) {
            return false;
        }
        
        for (String permission : permissions) {
            // Skip Bluetooth permissions on lower APIs
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && 
                    (permission.equals(Manifest.permission.BLUETOOTH_CONNECT) || 
                     permission.equals(Manifest.permission.BLUETOOTH_SCAN))) {
                continue;
            }
            
            if (ContextCompat.checkSelfPermission(context, permission) != 
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Request all emergency feature permissions
     * @param activity Activity to request permissions from
     * @return true if all permissions are already granted, false if request was made
     */
    public static boolean requestEmergencyPermissions(Activity activity) {
        if (hasPermissions(activity, EMERGENCY_PERMISSIONS)) {
            return true;
        }
        
        ActivityCompat.requestPermissions(activity, EMERGENCY_PERMISSIONS, 100);
        return false;
    }
    
    /**
     * Request all BLE-related permissions
     * @param activity Activity to request permissions from
     * @return true if all permissions are already granted, false if request was made
     */
    public static boolean requestBlePermissions(Activity activity) {
        List<String> permissionsToRequest = new ArrayList<>();
        
        for (String permission : BLE_PERMISSIONS) {
            // Skip Bluetooth permissions on lower APIs
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && 
                    (permission.equals(Manifest.permission.BLUETOOTH_CONNECT) || 
                     permission.equals(Manifest.permission.BLUETOOTH_SCAN))) {
                continue;
            }
            
            if (ContextCompat.checkSelfPermission(activity, permission) != 
                    PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(activity, 
                    permissionsToRequest.toArray(new String[0]), 101);
            return false;
        }
        
        return true;
    }
    
    /**
     * Request all alert-related permissions
     * @param activity Activity to request permissions from
     * @return true if all permissions are already granted, false if request was made
     */
    public static boolean requestAlertPermissions(Activity activity) {
        if (hasPermissions(activity, ALERT_PERMISSIONS)) {
            return true;
        }
        
        ActivityCompat.requestPermissions(activity, ALERT_PERMISSIONS, 102);
        return false;
    }
    
    /**
     * Request all background operation permissions
     * @param activity Activity to request permissions from
     * @return true if all permissions are already granted, false if request was made
     */
    public static boolean requestBackgroundPermissions(Activity activity) {
        if (hasPermissions(activity, BACKGROUND_PERMISSIONS)) {
            return true;
        }
        
        ActivityCompat.requestPermissions(activity, BACKGROUND_PERMISSIONS, 103);
        return false;
    }
    
    /**
     * Request location permissions needed for BLE scanning on Android 6.0+
     * @param activity Activity to request permissions from
     * @return true if permissions are already granted, false if request was made
     */
    public static boolean requestLocationPermissions(Activity activity) {
        String[] locationPermissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        
        if (hasPermissions(activity, locationPermissions)) {
            return true;
        }
        
        ActivityCompat.requestPermissions(activity, locationPermissions, 104);
        return false;
    }
    
    /**
     * Shows rationale dialog explaining why a permission is needed
     * @param activity Activity context
     * @param title Dialog title
     * @param message Dialog message
     * @param requestCode Request code for permission
     * @param permissions Permissions to request
     */
    public static void showPermissionRationale(Activity activity, String title, 
            String message, final int requestCode, final String... permissions) {
        
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Allow", (dialog, which) -> 
                        ActivityCompat.requestPermissions(activity, permissions, requestCode))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
    
    /**
     * Navigate to app settings when permission is permanently denied
     * @param activity Activity context
     */
    public static void openAppSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }
} 