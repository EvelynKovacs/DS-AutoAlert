package com.example.autoalert.viewmodel;

import static android.content.ContentValues.TAG;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.autoalert.R;
import com.example.autoalert.repository.SensorQueueRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class SensorViewModel extends AndroidViewModel implements SensorEventListener {

    private static final String CHANNEL_ID = "sensor_notifications";
    private SensorManager sensorManager;
    private List<Sensor> sensorsToMonitor;
    private MutableLiveData<Map<String, String>> sensorValues;
    private MutableLiveData<List<String>> missingSensors;


    private MutableLiveData<List<Sensor>> sensorNames;


    // Booleanos para indicar la disponibilidad de los sensores
    private boolean isGyroscopeAvailable;
    private boolean isLinearAccelerationAvailable;
    private boolean isAccelerometerAvailable;

    private SensorQueueRepository sensorData;

    public SensorViewModel(@NonNull Application application) {
        super(application);
        sensorManager = (SensorManager) application.getSystemService(Context.SENSOR_SERVICE);
        sensorValues = new MutableLiveData<>(new HashMap<>());
        missingSensors = new MutableLiveData<>(new ArrayList<>());
        sensorNames = new MutableLiveData<>(new ArrayList<>());
        sensorsToMonitor = new ArrayList<>();

        // Inicializar los booleanos como falsos
        isGyroscopeAvailable = false;
        isLinearAccelerationAvailable = false;
        isAccelerometerAvailable = false;

        sensorData= new SensorQueueRepository();
    }

    // Métodos para obtener el estado de los sensores
    public boolean isGyroscopeAvailable() {
        return isGyroscopeAvailable;
    }

    public boolean isLinearAccelerationAvailable() {
        return isLinearAccelerationAvailable;
    }

    public boolean isAccelerometerAvailable() {
        return isAccelerometerAvailable;
    }

    public LiveData<Map<String, String>> getSensorValues() {
        return sensorValues;
    }

    public LiveData<List<String>> getMissingSensors() {
        return missingSensors;
    }

    public LiveData<List<Sensor>> getSensorNames() {
        return sensorNames;
    }

    public void detectSensors() {
        int[] sensorTypes = {
                Sensor.TYPE_GYROSCOPE,
                Sensor.TYPE_LINEAR_ACCELERATION,
                Sensor.TYPE_ACCELEROMETER
        };



        List<Sensor> allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        Map<String, String> initialValues = new HashMap<>();
        List<Sensor> foundSensors = new ArrayList<>();

        for (int type : sensorTypes) {
            boolean sensorFound = false;
            for (Sensor sensor : allSensors) {
                if (sensor.getType() == type) {
                    sensorsToMonitor.add(sensor);
                    foundSensors.add(sensor);
                    String sensorName = getSensorName(type);
                    initialValues.put(sensorName, "X: 0.0, Y: 0.0, Z: 0.0");
                    sensorFound = true;

                    // Actualizar los booleanos basados en el tipo de sensor encontrado
                    if (type == Sensor.TYPE_GYROSCOPE) {
                        isGyroscopeAvailable = true;
                        Log.d(TAG, "Gyroscopio está disponible."); // Mensaje de depuración
                    } else if (type == Sensor.TYPE_LINEAR_ACCELERATION) {
                        isLinearAccelerationAvailable = true;
                        Log.d(TAG, "Aceleración Lineal está disponible."); // Mensaje de depuración
                    } else if (type == Sensor.TYPE_ACCELEROMETER) {
                        isAccelerometerAvailable = true;
                        Log.d(TAG, "Acelerometro está disponible."); // Mensaje de depuración
                    }

                    break;
                }
            }


            if (!sensorFound) {
                missingSensors.getValue().add(getSensorName(type));
            }
        }

        sensorValues.setValue(initialValues);
        sensorNames.setValue(foundSensors);
    }

    public void storeAccelerometerData(String accelerometerData) {
        if (this.isAccelerometerAvailable()) {
            sensorData.addAccelerometerData(accelerometerData);
        }
    }

    public void storeGyroscopeData(String gyroscopeData) {
        if (this.isGyroscopeAvailable()) {
            sensorData.addGyroscopeData(gyroscopeData);
        }
    }

    public void storeSpeedData(String speedData) {
        sensorData.addSpeedData(speedData);
    }


    public void registerSensorListeners() {
        for (Sensor sensor : sensorsToMonitor) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void unregisterSensorListeners() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        String sensorName = getSensorName(event.sensor.getType());
        if (sensorName != null) {
            String values = String.format("X: %.2f, Y: %.2f, Z: %.2f", event.values[0], event.values[1], event.values[2]);
            sensorValues.getValue().put(sensorName, values);
            sensorValues.postValue(sensorValues.getValue());


            // Verificar si es acelerómetro, giroscopio o velocidad y almacenar los datos
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    storeAccelerometerData(values);
                    break;

                case Sensor.TYPE_GYROSCOPE:
                    storeGyroscopeData(values);
                    break;

            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No se necesita implementar para este caso
    }

    public void showSensorNotification() {
        String notificationMessage;

        if (missingSensors.getValue().isEmpty()) {
            return;
        } else if (missingSensors.getValue().size() == 3) {
            notificationMessage = "No se pueden usar las funciones de la aplicación debido a la falta de sensores.";
        } else {
            StringBuilder messageBuilder = new StringBuilder("Faltan los siguientes sensores: ");
            for (String sensor : missingSensors.getValue()) {
                messageBuilder.append(sensor).append(", ");
            }
            notificationMessage = messageBuilder.substring(0, messageBuilder.length() - 2);
        }

        NotificationManager notificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplication(), CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Estado de los Sensores")
                    .setContentText(notificationMessage)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Sensor Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Canal para notificaciones sobre sensores");
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(1, builder.build());
        }
    }
    public void clearSensorData() {
        sensorData.clearAllData();  // Limpiar los datos almacenados en la cola
    }

    private String getSensorName(int type) {
        switch (type) {
            case Sensor.TYPE_GYROSCOPE:
                return "Gyroscope";
            case Sensor.TYPE_LINEAR_ACCELERATION:
                return "Linear Acceleration";
            case Sensor.TYPE_ACCELEROMETER:
                return "Accelerometer";
            default:
                return null;
        }
    }

}
