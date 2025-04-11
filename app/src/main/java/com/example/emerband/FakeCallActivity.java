package com.example.emerband;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

/**
 * Activity that simulates a realistic incoming call screen.
 * This serves as a discreet escape tool from uncomfortable or risky situations.
 */
public class FakeCallActivity extends AppCompatActivity {

    private static final long[] VIBRATION_PATTERN = {0, 1000, 1000};
    
    private TextView callerNameTextView;
    private TextView callerNumberTextView;
    private Button answerButton;
    private Button declineButton;
    private Chronometer callTimer;
    private ImageView callerImageView;
    
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private AudioManager audioManager;
    
    private boolean isCallAnswered = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_call);
        
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
        initializeViews();
        
        // Set up caller information
        setupCallerInfo();
        
        // Start ringtone and vibration
        startRinging();
        
        // Set up button listeners
        setupButtonListeners();
    }
    
    private void initializeViews() {
        callerNameTextView = findViewById(R.id.callerNameTextView);
        callerNumberTextView = findViewById(R.id.callerNumberTextView);
        answerButton = findViewById(R.id.answerButton);
        declineButton = findViewById(R.id.declineButton);
        callTimer = findViewById(R.id.callTimer);
        callerImageView = findViewById(R.id.callerImageView);
    }
    
    private void setupCallerInfo() {
        // TODO: In the future, allow customization of caller info
        callerNameTextView.setText("Mom");
        callerNumberTextView.setText("+1 555-123-4567");
        callerImageView.setImageResource(R.drawable.default_caller_image);
    }
    
    private void setupButtonListeners() {
        answerButton.setOnClickListener(v -> answerCall());
        declineButton.setOnClickListener(v -> endCall());
    }
    
    private void startRinging() {
        // Initialize media player with ringtone
        mediaPlayer = MediaPlayer.create(this, R.raw.fake_ringtone);
        mediaPlayer.setLooping(true);
        
        // Get audio manager and adjust volume
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume * 3 / 4, 0);
        
        // Set audio attributes for media player
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            mediaPlayer.setAudioAttributes(attributes);
        } else {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
        }
        
        // Start playing ringtone
        mediaPlayer.start();
        
        // Start vibration
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, 0));
            } else {
                vibrator.vibrate(VIBRATION_PATTERN, 0);
            }
        }
    }
    
    private void answerCall() {
        isCallAnswered = true;
        
        // Stop ringtone and vibration
        stopRinging();
        
        // Update UI to show "on call" state
        answerButton.setVisibility(View.GONE);
        declineButton.setText("End Call");
        
        // Start call timer
        callTimer.setVisibility(View.VISIBLE);
        callTimer.start();
    }
    
    private void endCall() {
        // Stop ringtone and vibration if not already stopped
        stopRinging();
        
        // Finish activity
        finish();
    }
    
    private void stopRinging() {
        // Stop media player
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        
        // Stop vibration
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
    
    @Override
    protected void onDestroy() {
        stopRinging();
        super.onDestroy();
    }
} 