package com.example.autoalert.view.activities;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.autoalert.R;
import com.example.autoalert.view.fragments.SimulacionFragment;

public class BackgroundAccidentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuración para mostrar la actividad encima de la pantalla de bloqueo
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_background_accident);

        // Cargar el TimerFragment
        if (savedInstanceState == null) {
            SimulacionFragment simulacionFragment = new SimulacionFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, simulacionFragment);
            transaction.commit();
        }
    }
}

