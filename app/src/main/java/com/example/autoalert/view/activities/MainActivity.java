package com.example.autoalert.view.activities;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.autoalert.R;
import com.example.autoalert.repository.SensorQueueRepository;
import com.example.autoalert.view.adapters.SensorAdapter;
import com.example.autoalert.viewmodel.SensorDataViewModel;
import com.example.autoalert.viewmodel.SensorViewModel;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private SensorViewModel sensorViewModel;
    private SensorAdapter sensorAdapter;
    private SensorDataViewModel sensorDataViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorViewModel = new ViewModelProvider(this).get(SensorViewModel.class);
        ListView listView = findViewById(R.id.listView1);

        // Inicializar SensorDataViewModel pasando la instancia de SensorViewModel
        sensorDataViewModel = new SensorDataViewModel(sensorViewModel);

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
        sensorDataViewModel.checkSensorAvailabilityAndShowData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorViewModel.clearSensorData();
        sensorViewModel.registerSensorListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorViewModel.unregisterSensorListeners();
    }
}
