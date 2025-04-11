package com.example.emerband.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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
    
    /**
     * Simulate a BLE signal using a direct method call to the appropriate handler
     * @param context Application context
     * @param signal The signal to simulate ('E', 'F', 'C', 'A')
     */
    public static void simulateBleSignal(Context context, char signal) {
        Log.d(TAG, "Simulating BLE signal: " + signal);
        
        switch (signal) {
            case 'E':
                // Simulate Emergency signal
                OfflineModeManager.getInstance(context).handleEmergencyEvent("Test emergency");
                break;
                
            case 'F':
                // Simulate Fake Call signal
                Intent fakeCallIntent = new Intent(context, FakeCallActivity.class);
                fakeCallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(fakeCallIntent);
                break;
                
            case 'C':
                // Simulate Cyber Cell alert
                OfflineModeManager.getInstance(context).handleCyberCellEvent();
                break;
                
            case 'A':
                // Simulate Alert signal
                Intent alertIntent = new Intent(context, AlertActivity.class);
                alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(alertIntent);
                break;
                
            default:
                Log.e(TAG, "Unknown test signal: " + signal);
        }
    }
    
    /**
     * Simulate going into offline mode
     * @param context Application context
     * @param durationMs How long to remain in simulated offline mode (ms)
     */
    public static void simulateOfflineMode(Context context, long durationMs) {
        // This is just a demonstration method - in a real implementation,
        // you would create a proper network callback or override connectivity check
        Log.d(TAG, "Simulating offline mode for " + durationMs + "ms");
        
        // Store an event while "offline"
        OfflineModeManager.getInstance(context).handleEmergencyEvent("Test offline emergency");
        
        // After the specified duration, simulate coming back online and process events
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Log.d(TAG, "Simulating return to online mode");
            // In a real implementation, this would be triggered by actual connectivity changes
        }, durationMs);
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
    
    /**
     * Test recovery from app force-close
     * @param context Application context
     */
    public static void testRecoveryFromForceClose(Context context) {
        // Stop and restart all components to simulate recovery
        stopBleService(context);
        
        // Disable boot receiver temporarily to simulate a force-close state
        ComponentName bootReceiver = new ComponentName(context, "com.example.emerband.BootCompletedReceiver");
        context.getPackageManager().setComponentEnabledSetting(
                bootReceiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        
        // After a short delay, re-enable everything
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Re-enable boot receiver
            context.getPackageManager().setComponentEnabledSetting(
                    bootReceiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            
            // Restart services
            startBleService(context);
            
            Log.d(TAG, "Simulated recovery from force-close complete");
        }, 2000);
    }
} 