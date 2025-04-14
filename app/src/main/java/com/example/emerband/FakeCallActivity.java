package com.example.emerband;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.emerband.utils.EmergencyUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Locale;

public class FakeCallActivity extends AppCompatActivity {
    private TextView callDurationText;
    private Handler handler;
    private int seconds = 0;
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            seconds++;
            updateTimer();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_call);

        // Initialize views
        callDurationText = findViewById(R.id.callDurationText);
        FloatingActionButton endCallButton = findViewById(R.id.endCallButton);

        // Set up end call button
        endCallButton.setOnClickListener(v -> {
            EmergencyUtils.stopFakeCall();
            finish();
        });

        // Initialize handler for timer
        handler = new Handler(Looper.getMainLooper());
        startTimer();
    }

    private void startTimer() {
        handler.post(timerRunnable);
    }

    private void updateTimer() {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        String time = String.format(Locale.getDefault(), "%02d:%02d", minutes, secs);
        callDurationText.setText(time);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(timerRunnable);
        }
        EmergencyUtils.stopFakeCall();
    }
}