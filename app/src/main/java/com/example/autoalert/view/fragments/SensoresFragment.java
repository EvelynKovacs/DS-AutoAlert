package com.example.autoalert.view.fragments;


import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
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


public class SensoresFragment extends Fragment {

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout para este fragment
        View view = inflater.inflate(R.layout.fragment_sensores, container, false);

        // Inicializar gráficos
         graphGyro = view.findViewById(R.id.graphGyro);
         graphAccel = view.findViewById(R.id.graphAcelerometer);
        sensorViewModel = new ViewModelProvider(this).get(SensorViewModel.class);
        ListView listView = view.findViewById(R.id.listView1);

        // Configuración de gráficos (similar a MainActivity)
         setupGraph();

        // Inicializar el adaptador con datos vacíos al inicio
        sensorAdapter = new SensorAdapter(getActivity(), new ArrayList<>(), new HashMap<>(), sensorViewModel.getMissingSensors().getValue());
        listView.setAdapter(sensorAdapter);

        // Observadores para actualizar la UI en base a los datos del ViewModel
        setupObservers();

        // Detectar sensores
        sensorViewModel.detectSensors();

        // Configurar el botón de la flecha para volver atrás
        ImageView backArrow = view.findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> {
            requireActivity().onBackPressed(); // Volver a la pantalla anterior
        });


        return view;
    }

     private void setupGraph() {
         // Gráfico del giroscopio
         seriesGyroX = new LineGraphSeries<>();
         seriesGyroY = new LineGraphSeries<>();
         seriesGyroZ = new LineGraphSeries<>();

         graphGyro.addSeries(seriesGyroX);
         graphGyro.addSeries(seriesGyroY);
         graphGyro.addSeries(seriesGyroZ);

    //     // Configuración de colores
         seriesGyroX.setColor(getResources().getColor(R.color.red, getActivity().getTheme())); // Eje X en rojo
         seriesGyroY.setColor(getResources().getColor(R.color.green, getActivity().getTheme())); // Eje Y en verde
         seriesGyroZ.setColor(getResources().getColor(R.color.blue, getActivity().getTheme())); // Eje Z en azul

         // Configurar el gráfico
         graphGyro.getViewport().setXAxisBoundsManual(false);
         graphGyro.getViewport().setMinX(0);
         graphGyro.getViewport().setMaxX(pointsPlotted);
         graphGyro.getViewport().setScalable(true);
         graphGyro.getViewport().setScrollable(true);

         // Gráfico del acelerómetro
         seriesAccelX = new LineGraphSeries<>();
         seriesAccelY = new LineGraphSeries<>();
         seriesAccelZ = new LineGraphSeries<>();

         graphAccel.addSeries(seriesAccelX);
         graphAccel.addSeries(seriesAccelY);
         graphAccel.addSeries(seriesAccelZ);

    //     // Configuración de colores para acelerómetro
         seriesAccelX.setColor(getResources().getColor(R.color.red, getActivity().getTheme()));
         seriesAccelY.setColor(getResources().getColor(R.color.green, getActivity().getTheme()));
         seriesAccelZ.setColor(getResources().getColor(R.color.blue, getActivity().getTheme()));

         // Configurar el gráfico del acelerómetro
         graphAccel.getViewport().setXAxisBoundsManual(false);
         graphAccel.getViewport().setMinX(0);
         graphAccel.getViewport().setMaxX(pointsPlotted);
         graphAccel.getViewport().setScalable(true);
         graphAccel.getViewport().setScrollable(true);
     }

    private void setupObservers() {
        // Observadores para actualizar la UI en base a los datos del ViewModel
        sensorViewModel.getSensorNames().observe(getViewLifecycleOwner(), sensorNames -> {
            sensorAdapter.updateSensorNames(sensorNames);
        });

        // Observador de los valores de los sensores
        sensorViewModel.getSensorValues().observe(getViewLifecycleOwner(), sensorValues -> {
            sensorAdapter.updateSensorValues(sensorValues);
            // Llamar a updateGraph para actualizar el gráfico con los nuevos valores del sensor
             updateGraph(sensorValues);
        });

        // Verificar la disponibilidad de los sensores
        sensorViewModel.getMissingSensors().observe(getViewLifecycleOwner(), missingSensors -> {
             if (sensorViewModel.isGyroscopeAvailable()) {
                 graphGyro.setVisibility(View.VISIBLE);
             } else {
                 graphGyro.setVisibility(View.GONE);
             }

             if (sensorViewModel.isAccelerometerAvailable()) {
                 graphAccel.setVisibility(View.VISIBLE);
             } else {
                 graphAccel.setVisibility(View.GONE);
             }

            // Mostrar advertencias si faltan sensores
            TextView warningTextView = requireView().findViewById(R.id.sensor_warning);
            if (!missingSensors.isEmpty()) {
                warningTextView.setVisibility(View.VISIBLE);
                warningTextView.setText("Advertencia: Faltan los siguientes sensores: " + missingSensors);
            } else {
                warningTextView.setVisibility(View.GONE);
            }

            sensorViewModel.showSensorNotification(); // Muestra la notificación si hay sensores faltantes.
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorViewModel.registerSensorListeners();
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorViewModel.unregisterSensorListeners();
    }

    // Actualizar el gráfico con datos para los ejes X, Y, Z
     private void updateGraph(Map<String, String> sensorValues) {
         if (sensorViewModel.isGyroscopeAvailable()) {
             String sensorName = "Giroscopio";
             if (sensorValues.containsKey(sensorName)) {
                 String[] values = sensorValues.get(sensorName).split(", ");
                 double xValue = Double.parseDouble(values[0].split(": ")[1]);
                 double yValue = Double.parseDouble(values[1].split(": ")[1]);
                 double zValue = Double.parseDouble(values[2].split(": ")[1]);

                 handler.postDelayed(() -> {
                     pointsPlotted++;
                     seriesGyroX.appendData(new DataPoint(pointsPlotted, xValue), true, 50);
                     seriesGyroY.appendData(new DataPoint(pointsPlotted, yValue), true, 50);
                     seriesGyroZ.appendData(new DataPoint(pointsPlotted, zValue), true, 50);
                 }, GRAPH_UPDATE_DELAY_MS);
             }
         }

         if (sensorViewModel.isAccelerometerAvailable()) {
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
}
