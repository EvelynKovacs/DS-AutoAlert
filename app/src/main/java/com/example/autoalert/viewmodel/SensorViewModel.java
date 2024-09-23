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
import com.example.autoalert.repository.AccelerometerQueueRepository;
import com.example.autoalert.repository.GyroscopeQueueRepository;
//import com.example.autoalert.repository.SensorQueueRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorViewModel extends AndroidViewModel implements SensorEventListener {

    private static final String CHANNEL_ID = "sensor_notifications";
    private SensorManager sensorManager;
    private List<Sensor> sensorsToMonitor;
    private MutableLiveData<Map<String, String>> sensorValues;
    private MutableLiveData<List<String>> missingSensors;


    private MutableLiveData<List<Sensor>> sensorNames;
    // Variable para almacenar el último tiempo de actualización del acelerómetro
    private long lastAccelerometerUpdate = 0;
    private long lastGyroscopeUpdate = 0;
    private static final long UPDATE_INTERVAL_MS = 100;



    // Booleanos para indicar la disponibilidad de los sensores
    private boolean isGyroscopeAvailable;

    private boolean isAccelerometerAvailable;

    //private SensorQueueRepository sensorData;
    private AccelerometerQueueRepository sensorValuesAccelerometer;
    private GyroscopeQueueRepository sensorValuesGyroscope;


    public SensorViewModel(@NonNull Application application) {
        super(application);
        sensorManager = (SensorManager) application.getSystemService(Context.SENSOR_SERVICE);
        sensorValues = new MutableLiveData<>(new HashMap<>());
        missingSensors = new MutableLiveData<>(new ArrayList<>());
        sensorNames = new MutableLiveData<>(new ArrayList<>());
        sensorsToMonitor = new ArrayList<>();

        // Inicializar los booleanos como falsos
        isGyroscopeAvailable = false;
        isAccelerometerAvailable = false;
        sensorValuesAccelerometer= new AccelerometerQueueRepository(application.getApplicationContext() );

        sensorValuesGyroscope= new GyroscopeQueueRepository(application.getApplicationContext());
    }

    // Métodos para obtener el estado de los sensores
    public boolean isGyroscopeAvailable() {
        return isGyroscopeAvailable;
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
            sensorValuesAccelerometer.addAccelerometerData(accelerometerData);
            //SensorDataWriter.writeDataToFile(getApplication(),"Acelerómetro: " + accelerometerData); // Guardar en archivo
        }
    }

    public void storeGyroscopeData(String gyroscopeData) {
        if (this.isGyroscopeAvailable()) {
            sensorValuesGyroscope.addGyroscopeData(gyroscopeData);
            //SensorDataWriter.writeDataToFile(getApplication(),"Giroscopio: " + gyroscopeData);
        }
    }

    public void storeValuesAccelerometer(SensorEvent event) {
        if (this.isAccelerometerAvailable()) {
            sensorValuesAccelerometer.xValueAdd((double) event.values[0],UPDATE_INTERVAL_MS);
            sensorValuesAccelerometer.yValueAdd((double)event.values[1],UPDATE_INTERVAL_MS);
            sensorValuesAccelerometer.zValueAdd((double)event.values[2],UPDATE_INTERVAL_MS);

            //SensorDataWriter.writeDataToFile(getApplication(),"Giroscopio: " + gyroscopeData);
        }
    }

    public void storeValuesGyroscope(SensorEvent event) {
        if (this.isAccelerometerAvailable()) {
            sensorValuesGyroscope.xValueAdd((double) event.values[0],UPDATE_INTERVAL_MS);
            sensorValuesGyroscope.yValueAdd((double)event.values[1],UPDATE_INTERVAL_MS);
            sensorValuesGyroscope.zValueAdd((double)event.values[2],UPDATE_INTERVAL_MS);

            //SensorDataWriter.writeDataToFile(getApplication(),"Giroscopio: " + gyroscopeData);
        }
    }



    public void registerSensorListeners() {
        // Establecer un intervalo de 1 segundo (1000000 microsegundos)
        int delay = 10000000; // 1 segundo en microsegundos

        for (Sensor sensor : sensorsToMonitor) {
            sensorManager.registerListener(this, sensor, delay);
        }
    }

    public void unregisterSensorListeners() {
        sensorManager.unregisterListener(this);
    }


   @Override
   public void onSensorChanged(SensorEvent event) {
       long currentTime = System.currentTimeMillis();  // Obtiene el tiempo actual en milisegundos

       String sensorName = getSensorName(event.sensor.getType());
       if (sensorName != null) {
           String values = String.format("X: %.2f, Y: %.2f, Z: %.2f", event.values[0], event.values[1], event.values[2]);
           sensorValues.getValue().put(sensorName, values);
           sensorValues.postValue(sensorValues.getValue());

           switch (event.sensor.getType()) {
               case Sensor.TYPE_ACCELEROMETER:
                   // Si ha pasado más de un segundo desde la última actualización
                   if (currentTime - lastAccelerometerUpdate >=  UPDATE_INTERVAL_MS ){

                       lastAccelerometerUpdate = currentTime;
                       storeAccelerometerData(values);  // Procesa y almacena los datos del acelerómetro
                       storeValuesAccelerometer(event);
                   }
                   break;

               case Sensor.TYPE_GYROSCOPE:
                   // Si ha pasado más de un segundo desde la última actualización
                   if (currentTime - lastGyroscopeUpdate >=UPDATE_INTERVAL_MS ) {
                       lastGyroscopeUpdate = currentTime;
                       storeGyroscopeData(values);  // Procesa y almacena los datos del giroscopio
                       storeValuesGyroscope(event);

                   }
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
        sensorValuesAccelerometer.clearAllData();  // Limpiar los datos almacenados en la cola
    }

    private String getSensorName(int type) {
        switch (type) {
            case Sensor.TYPE_GYROSCOPE:
                return "Gyroscope";
            case Sensor.TYPE_ACCELEROMETER:
                return "Accelerometer";
            default:
                return null;
        }
    }

}
