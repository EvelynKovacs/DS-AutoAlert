package com.example.autoalert.view.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.autoalert.R;

public class MainActivity extends AppCompatActivity {

    private TextView speedText;
    private ImageButton  createNetworkButton, viewUsersButton, editProfileButton;
    private Button sendMessageButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        speedText = findViewById(R.id.speedText);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        createNetworkButton = findViewById(R.id.createNetworkButton);
        viewUsersButton = findViewById(R.id.viewUsersButton);
        editProfileButton = findViewById(R.id.editProfileButton);

        // Set speed or any other logic here
        speedText.setText("Velocidad\n60 km/h");  // Aquí se añade el salto de línea

        // Set button click listeners if needed
        sendMessageButton.setOnClickListener(v -> {
            // Handle send message action
        });

        createNetworkButton.setOnClickListener(v -> {
            // Handle create network action
        });

        viewUsersButton.setOnClickListener(v -> {
            // Handle view users action
        });

        editProfileButton.setOnClickListener(v -> {
            // Handle edit profile action
        });
    }
}