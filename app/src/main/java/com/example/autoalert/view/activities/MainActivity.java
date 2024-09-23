package com.example.autoalert.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.autoalert.R;
import com.example.autoalert.repository.AccelerometerQueueRepository;
import com.example.autoalert.utils.SmsUtils;
import com.example.autoalert.view.adapters.SensorAdapter;
import com.example.autoalert.viewmodel.SensorViewModel;
import com.example.autoalert.viewmodel.SpeedViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import  com.example.autoalert.utils.AccidentDetector;

public class MainActivity extends AppCompatActivity {
    private SensorViewModel sensorViewModel;
    private SensorAdapter sensorAdapter;
    private SpeedViewModel speedViewModel;
    private TextView tvSpeed, tvLocation, tvAddress;  // Nuevo TextView para la dirección
    private boolean isMessageSent = false;  // Bandera para controlar el envío del mensaje
    private AccidentDetector accidentDetector; // Instancia de AccidentDetector
    private AccelerometerQueueRepository accelerometerQueueRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSpeed = findViewById(R.id.tvSpeed);
        tvLocation = findViewById(R.id.tvLocation);
        tvAddress = findViewById(R.id.tvAddress);  // TextView para la dirección
        // Inicializa las vistas
        LinearLayout redAlertLayout = findViewById(R.id.red_alert_layout);
        TextView countdownTimer = findViewById(R.id.countdown_timer);
        // Crea y configura AccidentDetector
        accidentDetector = new AccidentDetector(redAlertLayout, countdownTimer);

        accelerometerQueueRepository = new AccelerometerQueueRepository(getApplicationContext());


        //SmsUtils.checkAndSendSms(this, new String[]{"2804405851", "2804611882", "2804992455"}, "Mensaje de emergencia");

        // SmsUtils.checkAndSendSms(this, new String[]{"2804559405", "2804611882", "2804382723"}, "Mensaje de emergencia");

        sensorViewModel = new ViewModelProvider(this).get(SensorViewModel.class);
        ListView listView = findViewById(R.id.listView1);

        // Inicializar el adaptador con datos vacíos al inicio
        sensorAdapter = new SensorAdapter(this, new ArrayList<>(), new HashMap<>());
        listView.setAdapter(sensorAdapter);

        // Observadores para actualizar la UI en base a los datos del ViewModel
        sensorViewModel.getSensorNames().observe(this, sensorNames -> {
            sensorAdapter.updateSensorNames(sensorNames);
        });

        sensorViewModel.getSensorValues().observe(this, sensorValues -> {
            sensorAdapter.updateSensorValues(sensorValues);
        });

        sensorViewModel.getMissingSensors().observe(this, missingSensors -> {
            if (!missingSensors.isEmpty()) {
                sensorViewModel.showSensorNotification();
            }
        });

        sensorViewModel.detectSensors();

        // Inicializar el ViewModel
        speedViewModel = new ViewModelProvider(this).get(SpeedViewModel.class);

        // Observar los cambios de velocidad y almacenar en el SensorViewModel
        speedViewModel.getSpeed().observe(this, speedKmh -> {
            tvSpeed.setText(String.format("Velocidad: %.2f km/h", speedKmh));
        });

        // Observar los cambios de ubicación (coordenadas)
        speedViewModel.getLocation().observe(this, location -> {
            tvLocation.setText(String.format("Ubicación: %.6f, %.6f", location.getLatitude(), location.getLongitude()));
        });

        // Observar los cambios de dirección
        speedViewModel.getAddress().observe(this, address -> {
            tvAddress.setText("Dirección: " + address);  // Actualizar el TextView de la dirección
            System.out.println("DIRECCION: " + address);
            String emergencyMessage = "Emergencia. Dirección: " + address;
            System.out.println(emergencyMessage);
            String sanitizedAddress = " Mensaje de Emergencia. La siguiente direccion podria no ser exacta, aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + address.replaceAll("[^a-zA-Z0-9\\s,.]", "");


//
            //SmsUtils.checkAndSendSms(this, new String[]{"2804559405", "2804611882", "2804382723"}, sanitizedAddress);

            if (!isMessageSent) {
                //String emergencyMessage = "Mensaje de emergencia. Dirección: " + address;

                //SmsUtils.checkAndSendSms(this, new String[]{ "2804559405"}, sanitizedAddress);
                SmsUtils.checkAndSendSms(this, new String[]{"2804559405", "2804611882", "2804382723"}, sanitizedAddress);

                isMessageSent = true;  // Marcar como enviado
            }
        });

        // Observar el estado de los permisos
        speedViewModel.getLocationPermissionState().observe(this, permissionGranted -> {
            if (!permissionGranted) {
                speedViewModel.requestLocationPermissions(this);
            }
        });

        // Iniciar la configuración de ubicación
        speedViewModel.checkLocationSettings(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorViewModel.clearSensorData();
        sensorViewModel.registerSensorListeners();
        speedViewModel.resumeLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorViewModel.unregisterSensorListeners();
        speedViewModel.pauseLocationUpdates();
    }

    // Manejar la respuesta de los permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        speedViewModel.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        speedViewModel.onActivityResult(requestCode, resultCode);
    }
}
