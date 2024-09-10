package com.example.autoalert.view.activities;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.autoalert.R;
import com.example.autoalert.view.adapters.SensorAdapter;
import com.example.autoalert.viewmodel.SensorViewModel;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private SensorViewModel sensorViewModel;
    private SensorAdapter sensorAdapter;


    // Para el gráfico
    private int pointsPlotted = 5;
    private GraphView graph;
    private LineGraphSeries<DataPoint> series;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        // Inicializar el gráfico y la serie aquí
        graph = findViewById(R.id.graph);
        series = new LineGraphSeries<>();
        graph.addSeries(series);

        // Configurar el gráfico para avanzar más lentamente en el eje X
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(50);  // Aumentar el valor máximo del eje X
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);*/


        sensorViewModel = new ViewModelProvider(this).get(SensorViewModel.class);
        ListView listView = findViewById(R.id.listView1);

        // Inicializar el adaptador con datos vacíos al inicio
        sensorAdapter = new SensorAdapter(this, new ArrayList<>(), new HashMap<>(), sensorViewModel.getMissingSensors().getValue());
        listView.setAdapter(sensorAdapter);

        // Observadores para actualizar la UI en base a los datos del ViewModel
        sensorViewModel.getSensorNames().observe(this, sensorNames -> {
            sensorAdapter.updateSensorNames(sensorNames);
        });

        sensorViewModel.getSensorValues().observe(this, sensorValues -> {
            sensorAdapter.updateSensorValues(sensorValues);
/* MEJORAR ESTO
            // Actualizar el gráfico en base a los valores de los sensores
            String sensorName = "Accelerometer";  // Por ejemplo, usar "Accelerometer" para obtener los valores de ese sensor
            if (sensorValues.containsKey(sensorName)) {
                String[] values = sensorValues.get(sensorName).split(", ");
                double xValue = Double.parseDouble(values[0].split(": ")[1]);
                pointsPlotted++;
                series.appendData(new DataPoint(pointsPlotted, xValue), true, 50);
            }*/
        });

        sensorViewModel.getMissingSensors().observe(this, missingSensors -> {
            sensorAdapter.notifyDataSetChanged(); // Actualizar la lista cuando cambien los sensores faltantes
            if (!missingSensors.isEmpty()) {
                sensorViewModel.showSensorNotification();
            }
        });

        sensorViewModel.detectSensors();

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorViewModel.registerSensorListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorViewModel.unregisterSensorListeners();
    }
}
