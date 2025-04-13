package com.example.emerband;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FakeCallActivity extends AppCompatActivity {
    private ImageView callerImageView;
    private TextView callerNameTextView;
    private TextView callTypeTextView;
    private ImageButton btnAnswer;
    private ImageButton btnReject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_call);

        // Initialize views
        callerImageView = findViewById(R.id.callerImageView);
        callerNameTextView = findViewById(R.id.callerNameTextView);
        callTypeTextView = findViewById(R.id.callTypeTextView);
        btnAnswer = findViewById(R.id.btnAnswer);
        btnReject = findViewById(R.id.btnReject);

        // Set up click listeners
        btnAnswer.setOnClickListener(v -> handleAnswerCall());
        btnReject.setOnClickListener(v -> handleRejectCall());

        // Set up initial UI
        setupCallUI();
    }

    private void setupCallUI() {
        callerNameTextView.setText(getString(R.string.emergency_caller));
        callTypeTextView.setText(getString(R.string.incoming_mobile_call));
        callerImageView.setImageResource(R.drawable.ic_person);
    }

    private void handleAnswerCall() {
        // Add call answer logic here
        finish();
    }

    private void handleRejectCall() {
        // Add call reject logic here
        finish();
    }
}