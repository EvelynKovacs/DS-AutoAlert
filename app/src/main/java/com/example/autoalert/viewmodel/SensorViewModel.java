//package com.example.autoalert.viewmodel;
//
//import static android.content.ContentValues.TAG;
//
//import android.app.Application;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.content.Context;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//import androidx.core.app.NotificationCompat;
//import androidx.lifecycle.AndroidViewModel;
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//
//import com.example.autoalert.R;
//import com.example.autoalert.repository.AccelerometerQueueRepository;
////import com.example.autoalert.repository.SensorQueueRepository;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class SensorViewModel extends AndroidViewModel implements SensorEventListener {
//
//    private static final String CHANNEL_ID = "sensor_notifications";
//    private SensorManager sensorManager;
//    private List<Sensor> sensorsToMonitor;
//    private MutableLiveData<Map<String, String>> sensorValues;
//    private MutableLiveData<List<String>> missingSensors;
//
//
//    private MutableLiveData<List<Sensor>> sensorNames;
//    // Variable para almacenar el último tiempo de actualización del acelerómetro
//    private long lastAccelerometerUpdate = 0;
//    private long lastGyroscopeUpdate = 0;
//    private static final long UPDATE_INTERVAL_MS = 100;
//
//
//
//    // Booleanos para indicar la disponibilidad de los sensores
//    private boolean isGyroscopeAvailable;
//
//    private boolean isAccelerometerAvailable;
//
//    //private SensorQueueRepository sensorData;
//    private AccelerometerQueueRepository sensorValuesAccelerometer;
//
//
//    public SensorViewModel(@NonNull Application application) {
//        super(application);
//        sensorManager = (SensorManager) application.getSystemService(Context.SENSOR_SERVICE);
//        sensorValues = new MutableLiveData<>(new HashMap<>());
//        missingSensors = new MutableLiveData<>(new ArrayList<>());
//        sensorNames = new MutableLiveData<>(new ArrayList<>());
//        sensorsToMonitor = new ArrayList<>();
//
//        // Inicializar los booleanos como falsos
//        isGyroscopeAvailable = false;
//        isAccelerometerAvailable = false;
//        sensorValuesAccelerometer= new AccelerometerQueueRepository(application.getApplicationContext() );
//
//    }
//
//    // Métodos para obtener el estado de los sensores
//    public boolean isGyroscopeAvailable() {
//        return isGyroscopeAvailable;
//    }
//
//    public boolean isAccelerometerAvailable() {
//        return isAccelerometerAvailable;
//    }
//
//    public LiveData<Map<String, String>> getSensorValues() {
//        return sensorValues;
//    }
//
//    public LiveData<List<String>> getMissingSensors() {
//        return missingSensors;
//    }
//
//    public LiveData<List<Sensor>> getSensorNames() {
//        return sensorNames;
//    }
//
//    public void detectSensors() {
//        int[] sensorTypes = {
//                Sensor.TYPE_GYROSCOPE,
//                Sensor.TYPE_ACCELEROMETER,
//                Sensor.TYPE_GRAVITY,         // Agregar sensor de gravedad
//                Sensor.TYPE_ORIENTATION      // Agregar sensor de orientación (aunque está obsoleto, algunas versiones de Android aún lo soportan)
//
//        };
//
//
//
//        List<Sensor> allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
//        Map<String, String> initialValues = new HashMap<>();
//        List<Sensor> foundSensors = new ArrayList<>();
//
//        for (int type : sensorTypes) {
//            boolean sensorFound = false;
//            for (Sensor sensor : allSensors) {
//                if (sensor.getType() == type) {
//                    sensorsToMonitor.add(sensor);
//                    foundSensors.add(sensor);
//                    String sensorName = getSensorName(type);
//                    initialValues.put(sensorName, "X: 0.0, Y: 0.0, Z: 0.0");
//                    sensorFound = true;
//
//                    // Actualizar los booleanos basados en el tipo de sensor encontrado
//                    if (type == Sensor.TYPE_GYROSCOPE) {
//                        isGyroscopeAvailable = true;
//                        Log.d(TAG, "Gyroscopio está disponible."); // Mensaje de depuración
//                    } else if (type == Sensor.TYPE_ACCELEROMETER) {
//                        isAccelerometerAvailable = true;
//                        Log.d(TAG, "Acelerometro está disponible."); // Mensaje de depuración
//                    }
//
//                    break;
//                }
//            }
//
//
//            if (!sensorFound) {
//                missingSensors.getValue().add(getSensorName(type));
//            }
//        }
//
//        sensorValues.setValue(initialValues);
//        sensorNames.setValue(foundSensors);
//    }
//
//    public void storeAccelerometerData(String accelerometerData) {
//        if (this.isAccelerometerAvailable()) {
//            sensorValuesAccelerometer.addAccelerometerData(accelerometerData);
//            //SensorDataWriter.writeDataToFile(getApplication(),"Acelerómetro: " + accelerometerData); // Guardar en archivo
//        }
//    }
//
//
//    public void storeValuesAccelerometer(SensorEvent event) {
//        if (this.isAccelerometerAvailable()) {
//            sensorValuesAccelerometer.xValueAdd((double) event.values[0],UPDATE_INTERVAL_MS);
//            sensorValuesAccelerometer.yValueAdd((double)event.values[1],UPDATE_INTERVAL_MS);
//            sensorValuesAccelerometer.zValueAdd((double)event.values[2],UPDATE_INTERVAL_MS);
//
//            //SensorDataWriter.writeDataToFile(getApplication(),"Giroscopio: " + gyroscopeData);
//        }
//    }
//
//
//
//
//
//    public void registerSensorListeners() {
//        // Establecer un intervalo de 1 segundo (1000000 microsegundos)
//        int delay = 10000000; // 1 segundo en microsegundos
//
//        for (Sensor sensor : sensorsToMonitor) {
//            sensorManager.registerListener(this, sensor, delay);
//        }
//    }
//
//    public void unregisterSensorListeners() {
//        sensorManager.unregisterListener(this);
//    }
//
//
//   @Override
//   public void onSensorChanged(SensorEvent event) {
//       long currentTime = System.currentTimeMillis();  // Obtiene el tiempo actual en milisegundos
//
//       String sensorName = getSensorName(event.sensor.getType());
//       if (sensorName != null) {
//           String values = String.format("X: %.2f, Y: %.2f, Z: %.2f", event.values[0], event.values[1], event.values[2]);
//           sensorValues.getValue().put(sensorName, values);
//           sensorValues.postValue(sensorValues.getValue());
//
//           switch (event.sensor.getType()) {
//               case Sensor.TYPE_ACCELEROMETER:
//                   // Si ha pasado más de un segundo desde la última actualización
//                   if (currentTime - lastAccelerometerUpdate >=  UPDATE_INTERVAL_MS ){
//
//                       lastAccelerometerUpdate = currentTime;
//                       storeAccelerometerData(values);  // Procesa y almacena los datos del acelerómetro
//                       storeValuesAccelerometer(event);
//                   }
//                   break;
//
//
//           }
//       }
//   }
//
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        // No se necesita implementar para este caso
//    }
//
//    public void showSensorNotification() {
//        String notificationMessage;
//
//        if (missingSensors.getValue().isEmpty()) {
//            return;
//        } else if (missingSensors.getValue().size() == 3) {
//            notificationMessage = "No se pueden usar las funciones de la aplicación debido a la falta de sensores.";
//        } else {
//            StringBuilder messageBuilder = new StringBuilder("Faltan los siguientes sensores: ");
//            for (String sensor : missingSensors.getValue()) {
//                messageBuilder.append(sensor).append(", ");
//            }
//            notificationMessage = messageBuilder.substring(0, messageBuilder.length() - 2);
//        }
//
//        NotificationManager notificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
//        if (notificationManager != null) {
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplication(), CHANNEL_ID)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentTitle("Estado de los Sensores")
//                    .setContentText(notificationMessage)
//                    .setPriority(NotificationCompat.PRIORITY_HIGH)
//                    .setAutoCancel(true);
//
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                NotificationChannel channel = new NotificationChannel(
//                        CHANNEL_ID,
//                        "Sensor Notifications",
//                        NotificationManager.IMPORTANCE_HIGH
//                );
//                channel.setDescription("Canal para notificaciones sobre sensores");
//                notificationManager.createNotificationChannel(channel);
//            }
//
//            notificationManager.notify(1, builder.build());
//        }
//    }
//    public void clearSensorData() {
//        sensorValuesAccelerometer.clearAllData();  // Limpiar los datos almacenados en la cola
//    }
//
//    private String getSensorName(int type) {
//        switch (type) {
//            case Sensor.TYPE_GYROSCOPE:
//                return "Gyroscope";
//            case Sensor.TYPE_ACCELEROMETER:
//                return "Accelerometer";
//            default:
//                return null;
//        }
//    }
//
//}

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
import com.example.autoalert.repository.CsvAccelerometer;
import com.example.autoalert.repository.CsvGravity;
import com.example.autoalert.repository.CsvGyroscope;
import com.example.autoalert.repository.CsvMagnetometer;
import com.example.autoalert.repository.CsvOrientation;
import com.example.autoalert.repository.CsvRotation;

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
    private long lastAccelerometerUpdate = 0;
    private long lastGyroscopeUpdate = 0;
    private long lastGravityUpdate = 0;
    private long lastRotationVectorUpdate = 0;
    private long lastMagnetometerUpdate = 0;
    private long lastOrientationUpdate = 0;
    private static final long UPDATE_INTERVAL_MS = 100;

    private boolean isGyroscopeAvailable;
    private boolean isAccelerometerAvailable;
    private boolean isGravitySensorAvailable;
    private boolean isRotationVectorAvailable;
    private boolean isMagnetometerAvailable;
    private boolean isOrientationAvailable;

    private AccelerometerQueueRepository sensorValuesAccelerometer;

    private CsvGravity csvGravity;
    private CsvGyroscope csvGyroscope;
    private CsvAccelerometer csvAccelerometer;
    private CsvMagnetometer csvMagnetometer;
    private CsvRotation csvRotation;
    private CsvOrientation csvOrientation;

    public SensorViewModel(@NonNull Application application) {
        super(application);
        sensorManager = (SensorManager) application.getSystemService(Context.SENSOR_SERVICE);
        sensorValues = new MutableLiveData<>(new HashMap<>());
        missingSensors = new MutableLiveData<>(new ArrayList<>());
        sensorNames = new MutableLiveData<>(new ArrayList<>());
        sensorsToMonitor = new ArrayList<>();

        csvAccelerometer=new CsvAccelerometer(getApplication().getApplicationContext());
        csvGravity= new CsvGravity(getApplication().getApplicationContext());
        csvGyroscope= new CsvGyroscope(getApplication().getApplicationContext());
        csvMagnetometer= new CsvMagnetometer(getApplication().getApplicationContext());
        csvRotation= new CsvRotation(getApplication().getApplicationContext());
        csvOrientation= new CsvOrientation(getApplication().getApplicationContext());

        isGyroscopeAvailable = false;
        isAccelerometerAvailable = false;
        isGravitySensorAvailable = false;
        isRotationVectorAvailable = false;
        isMagnetometerAvailable = false;
        isOrientationAvailable = false;
        sensorValuesAccelerometer = new AccelerometerQueueRepository(application.getApplicationContext());
    }

    public boolean isGyroscopeAvailable() {
        return isGyroscopeAvailable;
    }

    public boolean isAccelerometerAvailable() {
        return isAccelerometerAvailable;
    }

    public boolean isGravitySensorAvailable() {
        return isGravitySensorAvailable;
    }

    public boolean isRotationVectorAvailable() {
        return isRotationVectorAvailable;
    }

    public boolean isMagnetometerAvailable() {
        return isMagnetometerAvailable;
    }
    public boolean isOrientationAvailable() {
        return isOrientationAvailable;
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
                Sensor.TYPE_ACCELEROMETER,
                Sensor.TYPE_GRAVITY,
                Sensor.TYPE_ROTATION_VECTOR,
                Sensor.TYPE_MAGNETIC_FIELD,
                Sensor.TYPE_ORIENTATION
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

                    // Actualizar los booleanos según el sensor encontrado
                    if (type == Sensor.TYPE_GYROSCOPE) {
                        isGyroscopeAvailable = true;
                        Log.d(TAG, "Giroscopio disponible.");
                    } else if (type == Sensor.TYPE_ACCELEROMETER) {
                        isAccelerometerAvailable = true;
                        Log.d(TAG, "Acelerómetro disponible.");
                    } else if (type == Sensor.TYPE_GRAVITY) {
                        isGravitySensorAvailable = true;
                        Log.d(TAG, "Sensor de Gravedad disponible.");
                    } else if (type == Sensor.TYPE_ROTATION_VECTOR) {
                        isRotationVectorAvailable = true;
                        Log.d(TAG, "Sensor de Vector de Rotación disponible.");
                    } else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
                        isMagnetometerAvailable = true;
                        Log.d(TAG, "Magnetómetro disponible.");
                    } else if (type == Sensor.TYPE_ORIENTATION) {
                        isOrientationAvailable = true;
                        Log.d(TAG, "Sensor de Orientacion disponible.");
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

    public void registerSensorListeners() {
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
        long currentTime = System.currentTimeMillis();
        String sensorName = getSensorName(event.sensor.getType());
        if (sensorName != null) {
            String values = String.format("X: %.2f, Y: %.2f, Z: %.2f", event.values[0], event.values[1], event.values[2]);
            sensorValues.getValue().put(sensorName, values);
            sensorValues.postValue(sensorValues.getValue());

            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    if (currentTime - lastAccelerometerUpdate >= UPDATE_INTERVAL_MS) {
                        lastAccelerometerUpdate = currentTime;
                        storeAccelerometerData(values);
                        storeValuesAccelerometer(event);
                    }
                    break;

                case Sensor.TYPE_GYROSCOPE:
                    if (currentTime - lastGyroscopeUpdate >= UPDATE_INTERVAL_MS) {
                        lastGyroscopeUpdate = currentTime;
                        storeValuesGyroscope(event);


                        // Almacenar los valores del giroscopio
                    }
                    break;

                case Sensor.TYPE_GRAVITY:
                    if (currentTime - lastGravityUpdate >= UPDATE_INTERVAL_MS) {
                        lastGravityUpdate = currentTime;
                        storeValuesGravity(event);
                        // Procesar los datos del sensor de gravedad
                        Log.d(TAG, "Datos del sensor de gravedad: " + values);
                    }
                    break;

                case Sensor.TYPE_ROTATION_VECTOR:
                    if (currentTime - lastRotationVectorUpdate >= UPDATE_INTERVAL_MS) {
                        lastRotationVectorUpdate = currentTime;
                        storeValuesRotationVector(event);

                        // Procesar los datos del sensor de rotación
                        Log.d(TAG, "Datos del sensor de vector de rotación: " + values);
                    }
                    break;

                case Sensor.TYPE_MAGNETIC_FIELD:
                    if (currentTime - lastMagnetometerUpdate >= UPDATE_INTERVAL_MS) {
                        lastMagnetometerUpdate = currentTime;
                        storeValuesMagnetometer(event);

                        // Procesar los datos del magnetómetro
                        Log.d(TAG, "Datos del magnetómetro: " + values);
                    }
                case Sensor.TYPE_ORIENTATION:
                    if (currentTime - lastMagnetometerUpdate >= UPDATE_INTERVAL_MS) {
                        lastOrientationUpdate = currentTime;
                        storeValuesOrientation(event);

                        // Procesar los datos del magnetómetro
                        Log.d(TAG, "Datos de orientacion: " + values);
                    }
                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No implementado
    }

    public void showSensorNotification() {
        String notificationMessage;

        if (missingSensors.getValue().isEmpty()) {
            return;
        } else if (missingSensors.getValue().size() == 6) {
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
                    .setContentText("Algunos sensores faltan")  // Mensaje corto que aparecerá cuando la notificación no esté expandida
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationMessage))  // Estilo de texto expandido
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
        sensorValuesAccelerometer.clearAllData();
    }

    private String getSensorName(int type) {
        switch (type) {
            case Sensor.TYPE_GYROSCOPE:
                return "Gyroscope";
            case Sensor.TYPE_ACCELEROMETER:
                return "Accelerometer";
            case Sensor.TYPE_GRAVITY:
                return "Gravity";
            case Sensor.TYPE_ROTATION_VECTOR:
                return "Rotation Vector";
            case Sensor.TYPE_MAGNETIC_FIELD:
                return "Magnetometer"; // Magnetómetro agregado
            case Sensor.TYPE_ORIENTATION:
                return "Orientation";
            default:
                return null;
        }
    }

    public void storeValuesAccelerometer(SensorEvent event) {
        if (this.isAccelerometerAvailable()) {
            sensorValuesAccelerometer.xValueAdd((double) event.values[0], UPDATE_INTERVAL_MS);
            sensorValuesAccelerometer.yValueAdd((double) event.values[1], UPDATE_INTERVAL_MS);
            sensorValuesAccelerometer.zValueAdd((double) event.values[2], UPDATE_INTERVAL_MS);
            csvAccelerometer.saveDataToCsv( event.values[0], event.values[1],event.values[2]);
            //SensorDataWriter.writeDataToFile(getApplication(),"Giroscopio: " + gyroscopeData);
        }
    }
    public void storeValuesGyroscope(SensorEvent event) {
        if (this.isGyroscopeAvailable()) {

            csvGyroscope.saveDataToCsv( event.values[0], event.values[1], event.values[2]);


            Log.d(TAG, "Almacenando valores del giroscopio: " + event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
        }
    }

    public void storeValuesGravity(SensorEvent event) {
        if (this.isGravitySensorAvailable()) {
            csvGravity.saveDataToCsv( event.values[0], event.values[1], event.values[2]);


            Log.d(TAG, "Almacenando valores del sensor de gravedad: " + event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
        }
    }

    public void storeValuesRotationVector(SensorEvent event) {
        if (this.isRotationVectorAvailable()) {
            csvRotation.saveDataToCsv( event.values[0], event.values[1], event.values[2]);


            Log.d(TAG, "Almacenando valores del sensor de rotación: " + event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
        }
    }

    public void storeValuesMagnetometer(SensorEvent event) {
        if (this.isMagnetometerAvailable()) {
            csvMagnetometer.saveDataToCsv( event.values[0], event.values[1], event.values[2]);


            Log.d(TAG, "Almacenando valores del magnetómetro: " + event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
        }
    }
    public void storeValuesOrientation(SensorEvent event) {
        if (this.isOrientationAvailable()) {
            csvOrientation.saveDataToCsv( event.values[0], event.values[1], event.values[2]);


            Log.d(TAG, "Almacenando valores de orientacion: " + event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
        }
    }


    private void storeAccelerometerData(String values) {
        Log.d(TAG, "Almacenando valores del acelerómetro: " + values);
    }
}
