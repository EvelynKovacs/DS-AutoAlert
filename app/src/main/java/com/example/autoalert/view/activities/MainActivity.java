package com.example.autoalert.view.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private SensorViewModel sensorViewModel;
    private SensorAdapter sensorAdapter;


    // Para el gráfico
    private int pointsPlotted = 5;
    private GraphView graphGyro, graphAccel;
    private LineGraphSeries<DataPoint> seriesGyroX, seriesGyroY, seriesGyroZ;
    private LineGraphSeries<DataPoint> seriesAccelX, seriesAccelY, seriesAccelZ;
    private Handler handler = new Handler(Looper.getMainLooper()); // Para retrasar las actualizaciones


    private static final int GRAPH_UPDATE_DELAY_MS = 500; // Retraso en milisegundos (500 ms = 0.5 segundos)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar gráficos
         graphGyro = findViewById(R.id.graphGyro);
         graphAccel = findViewById(R.id.graphAcelerometer);


        // Configurar gráficos (colores, series, etc.) como antes...
        // Gráfico del giroscopio
        seriesGyroX = new LineGraphSeries<>();
        seriesGyroY = new LineGraphSeries<>();
        seriesGyroZ = new LineGraphSeries<>();

        graphGyro.addSeries(seriesGyroX);
        graphGyro.addSeries(seriesGyroY);
        graphGyro.addSeries(seriesGyroZ);

        // Establecer diferentes colores para cada eje
        seriesGyroX.setColor(getResources().getColor(R.color.red, getTheme()));// Eje X en rojo
        seriesGyroY.setColor(getResources().getColor(R.color.green, getTheme())); // Eje Y en verde
        seriesGyroZ.setColor(getResources().getColor(R.color.blue, getTheme())); // Eje Z en azul



        // Añadir las series al gráfico del giroscopio
        graphGyro.addSeries(seriesGyroX);
        graphGyro.addSeries(seriesGyroY);
        graphGyro.addSeries(seriesGyroZ);

        // Configurar el gráfico
        graphGyro.getViewport().setXAxisBoundsManual(false); // Ajusta los límites automáticamente
        graphGyro.getViewport().setMinX(0);
        graphGyro.getViewport().setMaxX(pointsPlotted);
        graphGyro.getViewport().setScalable(true);  // Permitir zoom
        graphGyro.getViewport().setScrollable(true);  // Permitir desplazamiento


        // Gráfico del acelerómetro
         seriesAccelX = new LineGraphSeries<>();
         seriesAccelY = new LineGraphSeries<>();
         seriesAccelZ = new LineGraphSeries<>();

        graphAccel.addSeries(seriesAccelX);
        graphAccel.addSeries(seriesAccelY);
        graphAccel.addSeries(seriesAccelZ);

        // Establecer diferentes colores para cada eje
        seriesAccelX.setColor(getResources().getColor(R.color.red, getTheme())); // Eje X en rojo
        seriesAccelY.setColor(getResources().getColor(R.color.green, getTheme())); // Eje Y en verde
        seriesAccelZ.setColor(getResources().getColor(R.color.blue, getTheme())); // Eje Z en azul


        // Configurar el gráfico del acelerómetro
        graphAccel.getViewport().setXAxisBoundsManual(false); // Ajusta los límites automáticamente
        graphAccel.getViewport().setMinX(0);
        graphAccel.getViewport().setMaxX(pointsPlotted);
        graphAccel.getViewport().setScalable(true);  // Permitir zoom
        graphAccel.getViewport().setScrollable(true);  // Permitir desplazamiento

        sensorViewModel = new ViewModelProvider(this).get(SensorViewModel.class);
        ListView listView = findViewById(R.id.listView1);

        // Inicializar el adaptador con datos vacíos al inicio
        sensorAdapter = new SensorAdapter(this, new ArrayList<>(), new HashMap<>(), sensorViewModel.getMissingSensors().getValue());
        listView.setAdapter(sensorAdapter);

        // Observadores para actualizar la UI en base a los datos del ViewModel
        sensorViewModel.getSensorNames().observe(this, sensorNames -> {
            sensorAdapter.updateSensorNames(sensorNames);
        });

        // Observador de los valores de los sensores
        sensorViewModel.getSensorValues().observe(this, sensorValues -> {
            sensorAdapter.updateSensorValues(sensorValues);
            // Llamar a updateGraph para actualizar el gráfico con los nuevos valores del sensor
            updateGraph(sensorValues);

        });

        TextView warningTextView = findViewById(R.id.sensor_warning);

        sensorViewModel.getMissingSensors().observe(this, missingSensors -> {
            sensorAdapter.notifyDataSetChanged(); // Actualiza la lista de sensores.
            if (!missingSensors.isEmpty()) {
                warningTextView.setVisibility(View.VISIBLE);
                warningTextView.setText("Advertencia: Faltan los siguientes sensores: " + missingSensors);
            } else {
                warningTextView.setVisibility(View.GONE);
            }
            sensorViewModel.showSensorNotification(); // Muestra la notificación si hay sensores faltantes.
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

    // Actualizar el gráfico con datos para los ejes X, Y, Z
    private void updateGraph(Map<String, String> sensorValues) {
        String sensorName = "Giroscopio"; // Cambiar por el nombre del sensor de giroscopio
        if (sensorValues.containsKey(sensorName)) {
            String[] values = sensorValues.get(sensorName).split(", ");
            double xValue = Double.parseDouble(values[0].split(": ")[1]);
            double yValue = Double.parseDouble(values[1].split(": ")[1]);
            double zValue = Double.parseDouble(values[2].split(": ")[1]);

            // Usar el handler para retrasar la actualización
            handler.postDelayed(() -> {
                pointsPlotted++;
                seriesGyroX.appendData(new DataPoint(pointsPlotted, xValue), true, 50); // Graficar el eje X
                seriesGyroY.appendData(new DataPoint(pointsPlotted, yValue), true, 50); // Graficar el eje Y
                seriesGyroZ.appendData(new DataPoint(pointsPlotted, zValue), true, 50); // Graficar el eje Z
            }, GRAPH_UPDATE_DELAY_MS); // Retrasar la actualización
        }
        // Actualizar gráfico del acelerómetro
        String accelSensor = "Acelerómetro";
        if (sensorValues.containsKey(accelSensor)) {
            String[] values = sensorValues.get(accelSensor).split(", ");
            double xValue = Double.parseDouble(values[0].split(": ")[1]);
            double yValue = Double.parseDouble(values[1].split(": ")[1]);
            double zValue = Double.parseDouble(values[2].split(": ")[1]);

            handler.postDelayed(() -> {
                pointsPlotted++;
                seriesAccelX.appendData(new DataPoint(pointsPlotted, xValue), true, 50);
                seriesAccelY.appendData(new DataPoint(pointsPlotted, yValue), true, 50);
                seriesAccelZ.appendData(new DataPoint(pointsPlotted, zValue), true, 50);
            }, GRAPH_UPDATE_DELAY_MS);
        }
    }
}
