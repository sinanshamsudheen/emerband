package com.example.emerband;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * BroadcastReceiver that starts the BLEBackgroundService when the device boots up.
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootCompletedReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed, starting BLE service");
            
            // Start the BLEBackgroundService
            Intent serviceIntent = new Intent(context, BLEBackgroundService.class);
            
            // On Android 8.0 (Oreo) and above, we need to start the service as a foreground service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
} 