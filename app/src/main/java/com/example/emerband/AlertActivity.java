package com.example.emerband;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity that displays a full-screen flashing red alert with siren sound.
 * Activated when the 'A' signal is received from the BLE device.
 */
public class AlertActivity extends AppCompatActivity {

    private static final String TAG = "AlertActivity";
    private static final String PREFS_NAME = "EmerbandPrefs";
    private static final String KEY_LAST_ALERT_TIME = "lastAlertTime";
    
    private static final long BLINK_DELAY_MS = 500; // Time between blinks (500ms)
    private static final long VIBRATION_PATTERN[] = {0, 500, 500, 500, 500}; // Vibration pattern
    private static final int LIGHT_RED = Color.rgb(255, 0, 0); // Bright red
    private static final int DARK_RED = Color.rgb(150, 0, 0); // Darker red for contrast
    
    private FrameLayout alertBackground;
    private Button stopAlertButton;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private Handler handler;
    private boolean isRedBackground = true;
    private Runnable blinkRunnable;
    
    // Flashlight variables
    private CameraManager cameraManager;
    private String cameraId;
    private boolean hasFlash = false;
    private boolean isFlashOn = false;
    private Runnable flashlightRunnable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        
        // Make sure the screen turns on if the device is locked
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        
        // Make the activity fullscreen
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        
        // Initialize views
        alertBackground = findViewById(R.id.alertBackground);
        stopAlertButton = findViewById(R.id.stopAlertButton);
        
        // Store the alert timestamp
        saveAlertTimestamp();
        
        // Set up the button click listener
        stopAlertButton.setOnClickListener(v -> stopAlert());
        
        // Initialize handler for background blinking
        handler = new Handler(Looper.getMainLooper());
        
        // Check if device has flashlight
        hasFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        
        // Initialize flashlight
        if (hasFlash) {
            initializeFlashlight();
        }
        
        // Start the alert
        startAlert();
    }
    
    /**
     * Initialize flashlight access
     */
    private void initializeFlashlight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                if (cameraManager != null) {
                    cameraId = cameraManager.getCameraIdList()[0]; // Usually the back camera
                }
            } catch (CameraAccessException e) {
                Log.e(TAG, "Failed to access camera: " + e.getMessage());
                hasFlash = false; // Disable flash if we can't access it
            }
        } else {
            hasFlash = false; // Disable flash on older devices
        }
    }
    
    /**
     * Start the alert with audio, visual, and haptic feedback
     */
    private void startAlert() {
        // Maximize volume
        maximizeVolume();
        
        // Start siren sound
        playAlertSound();
        
        // Start vibration
        startVibration();
        
        // Start background blinking
        startBackgroundBlinking();
        
        // Start flashlight blinking if available
        if (hasFlash) {
            startFlashlightBlinking();
        }
    }
    
    /**
     * Stop the alert and close the activity
     */
    private void stopAlert() {
        // Stop blinking
        handler.removeCallbacks(blinkRunnable);
        
        // Stop flashlight blinking
        if (hasFlash) {
            handler.removeCallbacks(flashlightRunnable);
            setFlashlight(false); // Ensure flashlight is off
        }
        
        // Stop media player
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        
        // Stop vibration
        if (vibrator != null) {
            vibrator.cancel();
        }
        
        // Close the activity
        finish();
    }
    
    /**
     * Set the device volume to maximum
     */
    private void maximizeVolume() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                    0 // Don't show the UI
            );
            
            // Also maximize alarm volume
            audioManager.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                    0
            );
        }
    }
    
    /**
     * Play the alert siren sound
     */
    private void playAlertSound() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.siren);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        } catch (Exception e) {
            Log.e(TAG, "Failed to play alert sound: " + e.getMessage());
        }
    }
    
    /**
     * Start device vibration
     */
    private void startVibration() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, 0)); // repeat indefinitely
            } else {
                vibrator.vibrate(VIBRATION_PATTERN, 0); // repeat indefinitely
            }
        }
    }
    
    /**
     * Start the background color blinking effect
     */
    private void startBackgroundBlinking() {
        blinkRunnable = new Runnable() {
            @Override
            public void run() {
                // Toggle between light red and dark red
                if (isRedBackground) {
                    alertBackground.setBackgroundColor(DARK_RED);
                } else {
                    alertBackground.setBackgroundColor(LIGHT_RED);
                }
                isRedBackground = !isRedBackground;
                
                // Schedule the next color change
                handler.postDelayed(this, BLINK_DELAY_MS);
            }
        };
        
        // Start the blinking immediately
        handler.post(blinkRunnable);
    }
    
    /**
     * Start the flashlight blinking
     */
    private void startFlashlightBlinking() {
        flashlightRunnable = new Runnable() {
            @Override
            public void run() {
                // Toggle flashlight state
                isFlashOn = !isFlashOn;
                setFlashlight(isFlashOn);
                
                // Schedule the next toggle
                handler.postDelayed(this, BLINK_DELAY_MS);
            }
        };
        
        // Start the flashlight blinking immediately
        handler.post(flashlightRunnable);
    }
    
    /**
     * Set the flashlight state
     */
    private void setFlashlight(boolean on) {
        if (!hasFlash || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        
        try {
            if (cameraManager != null) {
                cameraManager.setTorchMode(cameraId, on);
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to access camera flashlight: " + e.getMessage());
        }
    }
    
    /**
     * Save the alert timestamp to SharedPreferences
     */
    private void saveAlertTimestamp() {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        settings.edit().putString(KEY_LAST_ALERT_TIME, timestamp).apply();
    }
    
    @Override
    protected void onDestroy() {
        stopAlert();
        super.onDestroy();
    }
} 