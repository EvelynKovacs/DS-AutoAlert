package com.example.autoalert.viewmodel;

import java.util.Map;

public class SensorDataViewModel {

    private SensorViewModel sensorViewModel;

    // Constructor que recibe una instancia de SensorViewModel
    public SensorDataViewModel(SensorViewModel sensorViewModel) {
        this.sensorViewModel = sensorViewModel;
    }

    // Método para verificar la disponibilidad de los sensores y registrar listeners
    public void checkSensorAvailabilityAndShowData() {
        if (sensorViewModel.isGyroscopeAvailable()) {
            System.out.println("El giroscopio está disponible.");
        } else {
            System.out.println("El giroscopio no está disponible.");
        }

        if (sensorViewModel.isLinearAccelerationAvailable()) {
            System.out.println("El sensor de aceleración lineal está disponible.");
        } else {
            System.out.println("El sensor de aceleración lineal no está disponible.");
        }

        if (sensorViewModel.isAccelerometerAvailable()) {
            System.out.println("El acelerómetro está disponible.");
        } else {
            System.out.println("El acelerómetro no está disponible.");
        }

        // Registrar los listeners para los sensores disponibles
        sensorViewModel.registerSensorListeners();

        // Monitorear los datos de los sensores
        monitorSensorData();
    }

    // Método para mostrar los datos de los sensores disponibles por consola
    private void monitorSensorData() {
        // Observa los cambios en los valores de los sensores
        sensorViewModel.getSensorValues().observeForever(sensorValues -> {
            for (Map.Entry<String, String> entry : sensorValues.entrySet()) {
                System.out.println("Datos del sensor " + entry.getKey() + ": " + entry.getValue());
            }
        });
    }

    // Método para detener el monitoreo y liberar recursos
    public void stopMonitoring() {
        sensorViewModel.unregisterSensorListeners();
    }
}
