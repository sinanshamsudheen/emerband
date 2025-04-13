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
    
    private static boolean testMode = false;
    private static String testPhoneNumber = "0000000000";
    
    /**
     * Simulate a BLE signal from the smartwatch
     * @param context Application context
     * @param signal Type of signal to simulate ('E', 'F', 'C', 'A')
     */
    public static void simulateBleSignal(Context context, char signal) {
        String message;
        switch (signal) {
            case 'E':
                message = "Emergency Alert Triggered";
                simulateEmergency(context);
                break;
            case 'F':
                message = "Fake Call Triggered";
                simulateFakeCall(context);
                break;
            case 'C':
                message = "Cyber Cell Alert Triggered";
                simulateCyberCell(context);
                break;
            case 'A':
                message = "Alert Mode Triggered";
                simulateAlert(context);
                break;
            default:
                message = "Unknown signal: " + signal;
                break;
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    
    private static void simulateEmergency(Context context) {
        // TODO: Implement emergency simulation
        // For now, just show a toast
        Toast.makeText(context, "Simulating emergency: Sending SOS signals", Toast.LENGTH_LONG).show();
        
        // Simulate delay
        mainHandler.postDelayed(() -> {
            Toast.makeText(context, "Emergency contacts notified", Toast.LENGTH_SHORT).show();
        }, 2000);
    }

    private static void simulateFakeCall(Context context) {
        // TODO: Implement fake call simulation
        Toast.makeText(context, "Simulating incoming call...", Toast.LENGTH_SHORT).show();
        
        mainHandler.postDelayed(() -> {
            Toast.makeText(context, "Incoming call from Emergency Contact", Toast.LENGTH_LONG).show();
        }, 1500);
    }

    private static void simulateCyberCell(Context context) {
        // TODO: Implement cyber cell alert simulation
        Toast.makeText(context, "Contacting cyber cell...", Toast.LENGTH_SHORT).show();
        
        mainHandler.postDelayed(() -> {
            Toast.makeText(context, "Cyber cell alert sent", Toast.LENGTH_SHORT).show();
        }, 1000);
    }

    private static void simulateAlert(Context context) {
        // TODO: Implement alert mode simulation
        Toast.makeText(context, "Activating alert mode...", Toast.LENGTH_SHORT).show();
        
        mainHandler.postDelayed(() -> {
            Toast.makeText(context, "Alert mode active: Siren and flashlight enabled", Toast.LENGTH_LONG).show();
        }, 1000);
    }
    
    /**
     * Simulate offline mode for testing
     * @param context Application context
     * @param durationMillis Duration of offline mode in milliseconds
     */
    public static void simulateOfflineMode(Context context, long durationMillis) {
        if (!isOfflineMode) {
            isOfflineMode = true;
            Toast.makeText(context, "Entering offline mode", Toast.LENGTH_SHORT).show();

            mainHandler.postDelayed(() -> {
                isOfflineMode = false;
                Toast.makeText(context, "Back online - Processing stored events", Toast.LENGTH_SHORT).show();
            }, durationMillis);
        }
    }
    
    /**
     * Test recovery from force close
     * @param context Application context
     */
    public static void testRecoveryFromForceClose(Context context) {
        Toast.makeText(context, "Simulating app crash...", Toast.LENGTH_SHORT).show();
        
        mainHandler.postDelayed(() -> {
            Toast.makeText(context, "App recovered from crash", Toast.LENGTH_SHORT).show();
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

    public static boolean isTestMode() {
        return testMode;
    }
    
    public static void setTestMode(boolean enabled) {
        testMode = enabled;
    }
    
    public static String getTestPhoneNumber() {
        return testPhoneNumber;
    }
    
    public static void setTestPhoneNumber(String number) {
        testPhoneNumber = number;
    }
} 