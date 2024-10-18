package com.example.autoalert.view.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.autoalert.R;
import com.example.autoalert.view.activities.RedActivity;
import com.github.anastr.speedviewlib.ImageLinearGauge;
import com.github.anastr.speedviewlib.ImageSpeedometer;
import com.github.anastr.speedviewlib.PointerSpeedometer;
import com.github.anastr.speedviewlib.SpeedView;
import com.google.android.material.button.MaterialButton;

public class PrincipalFragment extends Fragment {

    private SimulacionFragment simulacionFragment;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private SpeedView speedView; // Declare SpeedView variable

    public PrincipalFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_principal, container, false);

        // Initialize SpeedView after inflating the layout
        speedView = view.findViewById(R.id.speedView); // Initialize SpeedView here

        // Initialize permission launcher
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
            Boolean writeStorageGranted = result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            Boolean readStorageGranted = result.get(Manifest.permission.READ_EXTERNAL_STORAGE);

            if (fineLocationGranted != null && !fineLocationGranted ||
                    writeStorageGranted != null && !writeStorageGranted ||
                    readStorageGranted != null && !readStorageGranted) {
                // Handle permission denial
            } else {
                // Start simulation if permissions are granted
                startSimulation();
            }
        });

        MaterialButton btnEnvioMensaje = view.findViewById(R.id.sendMessageButton);
        ImageButton btnUsuario = view.findViewById(R.id.editProfileButton);
        ImageButton btnRed = view.findViewById(R.id.createNetworkButton);

        // Create instance of SimulacionFragment
        simulacionFragment = new SimulacionFragment();

        btnEnvioMensaje.setOnClickListener(v -> {
            if (hasPermissions()) {
                startSimulation();
            } else {
                requestPermissions();
            }
        });

        btnUsuario.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fcv_main_container, new VerUsuarioFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnRed.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RedActivity.class);
            startActivity(intent);
        });

        // Simulate speed updates every 2 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateSpeedometer(); // Simulate speed as 50 km/h
                new Handler().postDelayed(this, 2000); // Repeat every 2 seconds
            }
        }, 2000);

        return view;
    }

    private void startSimulation() {
        // Start transaction to switch to SimulacionFragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fcv_main_container, simulacionFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private boolean hasPermissions() {
        return (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissions() {
        requestPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        });
    }

    private void updateSpeedometer() {
        if (speedView != null) {
            speedView.setMaxSpeed(300);
            speedView.speedTo(50); // Move to 50 Km/h
        }
    }
}
