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

public class EmergencyUtils {
    private static final String EMERGENCY_NUMBER = "112";
    private static final String CYBER_CELL_NUMBER = "1930";
    private static MediaPlayer mediaPlayer;

    public static void makeEmergencyCall(Context context) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + EMERGENCY_NUMBER));
        if (checkCallPermission(context)) {
            context.startActivity(intent);
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
        SharedPreferences prefs = context.getSharedPreferences("EmerbandPrefs", Context.MODE_PRIVATE);
        String emergencyContacts = prefs.getString("emergency_contacts", "");
        String[] contacts = emergencyContacts.split(",");
        
        SmsManager smsManager = SmsManager.getDefault();
        String message = "EMERGENCY! I need help! My current location is: " + location;
        
        for (String contact : contacts) {
            if (!contact.isEmpty()) {
                smsManager.sendTextMessage(contact, null, message, null, null);
            }
        }
    }

    public static void playFakeCall(Context context) {
        stopCurrentAudio();
        mediaPlayer = MediaPlayer.create(context, Uri.parse("android.resource://" + context.getPackageName() + "/raw/ringtone"));
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // Show fake call UI
        Intent intent = new Intent(context, FakeCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
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

    public interface LocationCallback {
        void onLocationReceived(Location location);
    }
} 