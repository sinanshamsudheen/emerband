package com.example.emerband.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Centralized resource manager for handling audio, media, 
 * and other shared resources in the application.
 */
public class ResourceManager {
    private static final String TAG = "ResourceManager";
    
    // Vibration patterns
    public static final long[] EMERGENCY_VIBRATION_PATTERN = {0, 500, 500, 500, 500};
    public static final long[] CALL_VIBRATION_PATTERN = {0, 1000, 1000, 1000};
    public static final long[] ALERT_VIBRATION_PATTERN = {0, 300, 300, 300, 300, 500, 500};
    
    // SharedPreferences constants
    public static final String PREFS_NAME = "EmerbandPrefs";
    public static final String KEY_USER_NAME = "userName";
    public static final String KEY_EMERGENCY_CONTACTS = "emergencyContacts";
    public static final String KEY_SETUP_COMPLETED = "setupCompleted";
    
    // MediaPlayer instances for different sounds
    private static MediaPlayer emergencySirenPlayer;
    private static MediaPlayer alertSoundPlayer;
    private static Ringtone currentRingtone;
    
    // Flashlight state
    private static boolean isFlashlightOn = false;
    private static String cameraId = null;
    
    /**
     * Get the user's name from SharedPreferences
     * @param context Application context
     * @return The user's name or null if not set
     */
    public static String getUserName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_NAME, null);
    }
    
    /**
     * Check if setup has been completed
     * @param context Application context
     * @return true if setup is complete, false otherwise
     */
    public static boolean isSetupCompleted(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_SETUP_COMPLETED, false);
    }
    
    /**
     * Set setup completed status
     * @param context Application context
     * @param completed Setup completion status
     */
    public static void setSetupCompleted(Context context, boolean completed) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_SETUP_COMPLETED, completed).apply();
    }
    
    /**
     * Get emergency contacts from SharedPreferences
     * @param context Application context
     * @return List of emergency contact numbers
     */
    public static List<String> getEmergencyContacts(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String contactsString = prefs.getString(KEY_EMERGENCY_CONTACTS, "");
        
        List<String> contacts = new ArrayList<>();
        if (!contactsString.isEmpty()) {
            String[] contactArray = contactsString.split(",");
            for (String contact : contactArray) {
                if (!contact.trim().isEmpty()) {
                    contacts.add(contact.trim());
                }
            }
        }
        
        return contacts;
    }
    
    /**
     * Save emergency contacts to SharedPreferences
     * @param context Application context
     * @param contacts List of emergency contact numbers
     */
    public static void saveEmergencyContacts(Context context, List<String> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        for (String contact : contacts) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(contact);
        }
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_EMERGENCY_CONTACTS, sb.toString()).apply();
    }
    
    /**
     * Play the emergency siren sound at maximum volume
     * @param context Application context
     */
    public static void playEmergencySiren(Context context) {
        try {
            // Set volume to maximum
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                        0);
            }
            
            // Release any existing player
            if (emergencySirenPlayer != null) {
                emergencySirenPlayer.release();
            }
            
            // Create and prepare new player
            Uri sirenUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            emergencySirenPlayer = new MediaPlayer();
            emergencySirenPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            emergencySirenPlayer.setDataSource(context, sirenUri);
            emergencySirenPlayer.setLooping(true);
            emergencySirenPlayer.prepare();
            emergencySirenPlayer.start();
        } catch (Exception e) {
            Log.e(TAG, "Error playing emergency siren", e);
        }
    }
    
    /**
     * Stop the emergency siren sound
     */
    public static void stopEmergencySiren() {
        if (emergencySirenPlayer != null && emergencySirenPlayer.isPlaying()) {
            emergencySirenPlayer.stop();
            emergencySirenPlayer.release();
            emergencySirenPlayer = null;
        }
    }
    
    /**
     * Play the default ringtone for fake call
     * @param context Application context
     */
    public static void playRingtone(Context context) {
        try {
            Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            currentRingtone = RingtoneManager.getRingtone(context, defaultRingtoneUri);
            
            if (currentRingtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    currentRingtone.setLooping(true);
                }
                currentRingtone.play();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing ringtone", e);
        }
    }
    
    /**
     * Stop the currently playing ringtone
     */
    public static void stopRingtone() {
        if (currentRingtone != null && currentRingtone.isPlaying()) {
            currentRingtone.stop();
            currentRingtone = null;
        }
    }
    
    /**
     * Vibrate the device with the given pattern
     * @param context Application context
     * @param pattern Vibration pattern
     * @param repeat Index at which to repeat, or -1 for no repeat
     */
    public static void vibrate(Context context, long[] pattern, int repeat) {
        Vibrator vibrator;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = 
                    (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            if (vibratorManager != null) {
                vibrator = vibratorManager.getDefaultVibrator();
            } else {
                return;
            }
        } else {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
        
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat));
        } else {
            vibrator.vibrate(pattern, repeat);
        }
    }
    
    /**
     * Stop any ongoing vibration
     * @param context Application context
     */
    public static void stopVibration(Context context) {
        Vibrator vibrator;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = 
                    (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            if (vibratorManager != null) {
                vibrator = vibratorManager.getDefaultVibrator();
            } else {
                return;
            }
        } else {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
        
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
    
    /**
     * Toggle the device flashlight
     * @param context Application context
     * @return true if flashlight was toggled successfully, false otherwise
     */
    public static boolean toggleFlashlight(Context context) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        
        if (cameraManager == null) {
            return false;
        }
        
        try {
            if (cameraId == null) {
                cameraId = cameraManager.getCameraIdList()[0]; // Usually the back camera
            }
            
            isFlashlightOn = !isFlashlightOn;
            cameraManager.setTorchMode(cameraId, isFlashlightOn);
            return true;
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error accessing camera for flashlight", e);
            return false;
        }
    }
    
    /**
     * Turn on the device flashlight
     * @param context Application context
     * @return true if flashlight was turned on successfully, false otherwise
     */
    public static boolean turnOnFlashlight(Context context) {
        if (isFlashlightOn) {
            return true; // Already on
        }
        
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        
        if (cameraManager == null) {
            return false;
        }
        
        try {
            if (cameraId == null) {
                cameraId = cameraManager.getCameraIdList()[0];
            }
            
            cameraManager.setTorchMode(cameraId, true);
            isFlashlightOn = true;
            return true;
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error turning on flashlight", e);
            return false;
        }
    }
    
    /**
     * Turn off the device flashlight
     * @param context Application context
     */
    public static void turnOffFlashlight(Context context) {
        if (!isFlashlightOn) {
            return; // Already off
        }
        
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        
        if (cameraManager == null || cameraId == null) {
            return;
        }
        
        try {
            cameraManager.setTorchMode(cameraId, false);
            isFlashlightOn = false;
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error turning off flashlight", e);
        }
    }
    
    /**
     * Start flashing the flashlight at regular intervals
     * @param context Application context
     * @param intervalMs Interval between flashes in milliseconds
     * @return Handler used for flashing (can be used to stop)
     */
    public static Handler startFlashingLight(Context context, long intervalMs) {
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable flashRunnable = new Runnable() {
            @Override
            public void run() {
                toggleFlashlight(context);
                handler.postDelayed(this, intervalMs);
            }
        };
        
        handler.post(flashRunnable);
        return handler;
    }
    
    /**
     * Stop flashing the flashlight
     * @param handler Handler returned by startFlashingLight
     * @param context Application context
     */
    public static void stopFlashingLight(Handler handler, Context context) {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        
        // Ensure flashlight is turned off
        turnOffFlashlight(context);
    }
} 