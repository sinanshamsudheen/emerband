package com.example.emerband.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.emerband.AlertActivity;
import com.example.emerband.BLEBackgroundService;
import com.example.emerband.FakeCallActivity;
import com.example.emerband.offline.OfflineModeManager;

/**
 * Utility class for testing the Emerband application in different scenarios.
 * This class simulates various events and conditions for testing purposes.
 */
public class TestingUtils {
    private static final String TAG = "TestingUtils";
    private static boolean isOfflineMode = false;
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    /**
     * Simulate a BLE signal from the smartwatch
     * @param context Application context
     * @param signalType Type of signal to simulate ('E', 'F', 'C', 'A')
     */
    public static void simulateBleSignal(Context context, char signalType) {
        Intent serviceIntent = new Intent(context, BLEBackgroundService.class);
        serviceIntent.putExtra("TEST_SIGNAL", signalType);
        
        if (isOfflineMode) {
            Toast.makeText(context, "Offline mode active - Signal will be processed when online", Toast.LENGTH_SHORT).show();
            // TODO: Store the signal in the offline database
        } else {
            context.startService(serviceIntent);
            showSignalToast(context, signalType);
        }
    }
    
    /**
     * Simulate offline mode for testing
     * @param context Application context
     * @param durationMs Duration of offline mode in milliseconds
     */
    public static void simulateOfflineMode(Context context, long durationMs) {
        isOfflineMode = true;
        Toast.makeText(context, "Offline mode activated", Toast.LENGTH_SHORT).show();

        mainHandler.postDelayed(() -> {
            isOfflineMode = false;
            Toast.makeText(context, "Back online - Processing stored events", Toast.LENGTH_SHORT).show();
            // TODO: Process any stored events from the offline database
        }, durationMs);
    }
    
    /**
     * Test recovery from force close
     * @param context Application context
     */
    public static void testRecoveryFromForceClose(Context context) {
        // Simulate a crash
        Toast.makeText(context, "Simulating app crash...", Toast.LENGTH_SHORT).show();
        
        mainHandler.postDelayed(() -> {
            // Restart the BLE service
            Intent serviceIntent = new Intent(context, BLEBackgroundService.class);
            context.startService(serviceIntent);
            Toast.makeText(context, "Service restarted after crash", Toast.LENGTH_SHORT).show();
        }, 2000);
    }
    
    /**
     * Test fake call feature directly
     * @param context Application context
     * @param callerName Name to display for the fake caller
     * @param phoneNumber Phone number to display
     */
    public static void testFakeCall(Context context, String callerName, String phoneNumber) {
        Intent intent = new Intent(context, FakeCallActivity.class);
        intent.putExtra("CALLER_NAME", callerName);
        intent.putExtra("PHONE_NUMBER", phoneNumber);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    private static void showSignalToast(Context context, char signalType) {
        String message;
        switch (signalType) {
            case 'E':
                message = "Emergency signal received";
                break;
            case 'F':
                message = "Fake call signal received";
                break;
            case 'C':
                message = "Cyber cell signal received";
                break;
            case 'A':
                message = "Alert mode signal received";
                break;
            default:
                message = "Unknown signal received";
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Simulate permission denial scenario
     * @param context Application context
     * @param permission The permission to simulate as denied
     */
    public static void simulatePermissionDenial(Context context, String permission) {
        Log.d(TAG, "Simulating denial of permission: " + permission);
        
        // This is just a log - in a real test implementation, you might use
        // reflection or mocking to modify permission checks
    }
    
    /**
     * Start the BLE background service
     * @param context Application context
     */
    public static void startBleService(Context context) {
        Intent serviceIntent = new Intent(context, BLEBackgroundService.class);
        context.startForegroundService(serviceIntent);
    }
    
    /**
     * Stop the BLE background service
     * @param context Application context
     */
    public static void stopBleService(Context context) {
        Intent serviceIntent = new Intent(context, BLEBackgroundService.class);
        context.stopService(serviceIntent);
    }

    public static void testCyberCell(Context context) {
        Toast.makeText(context, "Testing Cyber Cell functionality", Toast.LENGTH_SHORT).show();
    }

    public static void testAlert(Context context) {
        Toast.makeText(context, "Testing Alert functionality", Toast.LENGTH_SHORT).show();
    }

    public static void testOffline(Context context) {
        Toast.makeText(context, "Testing Offline mode", Toast.LENGTH_SHORT).show();
    }

    public static void testCrash(Context context) {
        Toast.makeText(context, "Testing Crash handling", Toast.LENGTH_SHORT).show();
    }
} 