package com.example.autoalert.utils;
import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class LocationWorker extends Worker {
    private static final String TAG = "LocationWorker";
    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
//    private Integer cont;

    public LocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permiso de ubicación no otorgado.");
            return Result.failure();
        }

        // Create location request
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30000) // 30 seconds
                .setMinUpdateIntervalMillis(10000)  // Minimum interval between location updates (10 seconds)
                .setWaitForAccurateLocation(true)
                .build();


        // Create location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    saveLocation(locationResult.getLastLocation());
                    Log.d(TAG, "Ubicación guardada: " + locationResult.getLastLocation().toString());
                } else {
                    Log.d(TAG, "No se encontró una ubicación.");
                }
            }
        };

//        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
//            @Override
//            public void onSuccess(Location location) {
//                if (location != null) {
//                    saveLocation(location);
//                    Log.d(TAG, "Ubicación guardada: " + location.toString());
//                } else {
//                    Log.d(TAG, "No se encontró una ubicación.");
//                }
//            }
//        });

        // Request location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        return Result.success(); // or Result.retry() if you want to reschedule
        // Reprogramar el trabajo después de 30 segundos
        //    scheduleNextLocationWork();

        //  return Result.success();
    }

//    private void scheduleNextLocationWork() {
//        OneTimeWorkRequest locationWorkRequest = new OneTimeWorkRequest.Builder(LocationWorker.class)
//                .setInitialDelay(30, TimeUnit.SECONDS) // Esperar 30 segundos antes de ejecutar de nuevo
//                .build();
//
//        WorkManager.getInstance(context).enqueue(locationWorkRequest);
//        Log.d(TAG, "Tarea programada para ejecutarse nuevamente en 30 segundos.");
//    }

    private void saveLocation(Location location) {
        File archivoUltimaUbicacion = new File(context.getFilesDir(), "ultima_ubicacion.json"); // Archivo con la última ubicación
        File archivoTodasUbicaciones = new File(context.getFilesDir(), "ubicaciones_periodicas.json"); // Archivo con todas las ubicaciones

        try {
            // Obtener el nombre del lugar (dirección)
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String lugar = "Desconocido"; // Valor predeterminado en caso de no obtener una dirección
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                lugar = address.getAddressLine(0); // Obtener la dirección completa
            }

            // Obtener la hora actual
            String horaActual = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            // Crear el objeto JSON con la nueva ubicación
            JSONObject nuevaUbicacion = new JSONObject();
            nuevaUbicacion.put("latitud", location.getLatitude());
            nuevaUbicacion.put("longitud", location.getLongitude());
            nuevaUbicacion.put("lugar", lugar);
            nuevaUbicacion.put("hora", horaActual);

            // 1. Guardar solo la última ubicación en un archivo separado
            FileWriter fileWriterUltimaUbicacion = new FileWriter(archivoUltimaUbicacion, false); // Sobrescribir archivo con la última ubicación
            fileWriterUltimaUbicacion.write(nuevaUbicacion.toString());
            fileWriterUltimaUbicacion.close();
            Log.d(TAG, "Última ubicación guardada en archivo JSON.");

            // 2. Guardar todas las ubicaciones en otro archivo
            // Leer el archivo de todas las ubicaciones (si existe)
            JSONArray jsonArray = new JSONArray();
            if (archivoTodasUbicaciones.exists()) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(archivoTodasUbicaciones));
                StringBuilder jsonStringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    jsonStringBuilder.append(line);
                }
                bufferedReader.close();

                // Parsear el JSON existente
                String jsonString = jsonStringBuilder.toString();
                if (!jsonString.isEmpty()) {
                    jsonArray = new JSONArray(jsonString);
                }
            }

            // Añadir la nueva ubicación al array de todas las ubicaciones
            jsonArray.put(nuevaUbicacion);

            // Guardar el array actualizado en el archivo de todas las ubicaciones
            FileWriter fileWriterTodasUbicaciones = new FileWriter(archivoTodasUbicaciones, false); // Sobrescribir con el nuevo array
            fileWriterTodasUbicaciones.write(jsonArray.toString());
            fileWriterTodasUbicaciones.close();
            Log.d(TAG, "Ubicación guardada en archivo JSON de todas las ubicaciones.");

        } catch (Exception e) {
            Log.e(TAG, "Error al guardar ubicación en JSON.", e);
        }
    }



    @Override
    public void onStopped() {
        super.onStopped();
        // Stop location updates when the worker is stopped
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}
