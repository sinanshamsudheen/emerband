package com.example.emerband.utils;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import com.example.emerband.FakeCallActivity;
import com.example.emerband.R;

import android.location.LocationManager;
import android.media.AudioManager;
import android.telephony.SmsManager;
import android.content.SharedPreferences;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import com.example.emerband.models.Contact;
import com.example.emerband.database.DatabaseHelper;

public class EmergencyUtils {
    private static final String TAG = "EmergencyUtils";
    private static final String EMERGENCY_NUMBER = "112";
    private static final String CYBER_CELL_NUMBER = "1930";
    private static MediaPlayer mediaPlayer;
    private static MediaPlayer fakeCallPlayer;
    private static boolean isPlayingFakeCall = false;
    private static final String EMERGENCY_MESSAGE = "EMERGENCY: I need immediate assistance! This is an automated emergency alert from EmerBand.";

    public static void makeEmergencyCall(Context context) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        String number = TestingUtils.isTestMode() ? TestingUtils.getTestPhoneNumber() : EMERGENCY_NUMBER;
        intent.setData(Uri.parse("tel:" + number));
        if (checkCallPermission(context)) {
            if (TestingUtils.isTestMode()) {
                Toast.makeText(context, "TEST MODE: Would call " + number, Toast.LENGTH_LONG).show();
            } else {
                context.startActivity(intent);
            }
        }
    }

    public static void makeCyberCellCall(Context context) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + CYBER_CELL_NUMBER));
        if (checkCallPermission(context)) {
            context.startActivity(intent);
        }
    }

    public static void getCurrentLocation(Context context, LocationCallback callback) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (checkLocationPermission(context)) {
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    callback.onLocationReceived(location);
                    locationManager.removeUpdates(this);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}
            };

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0,
                locationListener
            );
        }
    }

    public static void sendEmergencyMessage(Context context, String location) {
        // Get contacts from database
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        List<Contact> contacts = databaseHelper.getAllContacts();
        
        if (contacts.isEmpty()) {
            Toast.makeText(context, "No emergency contacts found. Please add contacts in settings.", Toast.LENGTH_LONG).show();
            return;
        }
        
        String message = "EMERGENCY! I need help!";
        if (!location.isEmpty()) {
            message += " My current location is: " + location;
        }
        
        // Send SMS to all contacts
        SmsManager smsManager = SmsManager.getDefault();
        int successCount = 0;
        
        for (Contact contact : contacts) {
            try {
                smsManager.sendTextMessage(contact.getPhone(), null, message, null, null);
                Log.d(TAG, "SMS sent to: " + contact.getName() + " (" + contact.getPhone() + ")");
                successCount++;
            } catch (Exception e) {
                Log.e(TAG, "Failed to send SMS to " + contact.getName(), e);
                Toast.makeText(context, "Failed to send SMS to " + contact.getName(), Toast.LENGTH_SHORT).show();
            }
        }
        
        // Show confirmation message
        Toast.makeText(context, "Emergency triggered! Message sent to " + successCount + " contact(s)", Toast.LENGTH_LONG).show();
    }

    public static void playFakeCall(Context context) {
        try {
            if (fakeCallPlayer == null) {
                fakeCallPlayer = MediaPlayer.create(context, R.raw.ringtone);
                fakeCallPlayer.setOnCompletionListener(mp -> stopFakeCall());
            }
            fakeCallPlayer.start();
            isPlayingFakeCall = true;
        } catch (Exception e) {
            Log.e(TAG, "Error playing fake call", e);
            throw new RuntimeException("Failed to play fake call", e);
        }
    }

    public static void stopFakeCall() {
        try {
            if (fakeCallPlayer != null) {
                fakeCallPlayer.stop();
                fakeCallPlayer.release();
                fakeCallPlayer = null;
            }
            isPlayingFakeCall = false;
        } catch (Exception e) {
            Log.e(TAG, "Error stopping fake call", e);
            throw new RuntimeException("Failed to stop fake call", e);
        }
    }

    public static boolean isPlayingFakeCall() {
        return isPlayingFakeCall;
    }

    public static void playSiren(Context context) {
        stopCurrentAudio();
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        
        mediaPlayer = MediaPlayer.create(context, Uri.parse("android.resource://" + context.getPackageName() + "/raw/siren"));
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    public static void toggleAirplaneMode(Context context) {
        Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void stopCurrentAudio() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private static boolean checkCallPermission(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Call permission required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private static boolean checkLocationPermission(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static void sendEmergencySMS(Context context, List<Contact> emergencyContacts) {
        SmsManager smsManager = SmsManager.getDefault();
        for (Contact contact : emergencyContacts) {
            try {
                smsManager.sendTextMessage(contact.getPhone(), null, EMERGENCY_MESSAGE, null, null);
                Log.d(TAG, "SMS sent to " + contact.getName());
            } catch (Exception e) {
                Log.e(TAG, "Failed to send SMS to " + contact.getName(), e);
                Toast.makeText(context, "Failed to send SMS to " + contact.getName(), Toast.LENGTH_SHORT).show();
            }
        }
        Toast.makeText(context, "Emergency alerts sent to all contacts", Toast.LENGTH_SHORT).show();
    }

    public interface LocationCallback {
        void onLocationReceived(Location location);
    }
} 