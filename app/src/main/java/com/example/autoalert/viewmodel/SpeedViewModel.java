package com.example.autoalert.viewmodel;

import android.location.Geocoder;
import android.location.Address;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.example.autoalert.utils.DetectorAccidente;
import com.example.autoalert.utils.KalmanFilter;


import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

//import com.example.autoalert.repository.SensorQueueRepository;
import com.example.autoalert.model.entities.DatosMovimiento;
import com.example.autoalert.repository.AccelerationDataWriter;
import com.example.autoalert.repository.AccelerationQueueRepository;
import com.example.autoalert.repository.CoordinateQueue;
import com.example.autoalert.repository.DetectorAccidenteDataWriter;
import com.example.autoalert.repository.SpeedQueueRepository;
import com.example.autoalert.view.activities.MainActivity;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;

public class SpeedViewModel extends AndroidViewModel {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final int REQUEST_CHECK_SETTINGS = 1002;
    private static int MAX_SIZE_COORD=3;

    private MutableLiveData<Double> speedKmh = new MutableLiveData<>();
    private MutableLiveData<Location> location = new MutableLiveData<>();
    private MutableLiveData<Boolean> locationPermissionState = new MutableLiveData<>();
    private MutableLiveData<String> address = new MutableLiveData<>();

    private LocationManager locationManager;
    private LocationListener locationListener;
    private SpeedQueueRepository sensorData;
    private AccelerationQueueRepository accelerationQueueRepository;
    private long lastSpeedUpdate = 0;
    private boolean isFirstMeasurement = true;
    private float previousSpeed;
    private long previousTime;
    private double previousLatitude;
    private double previousLongitude;
    private boolean isFirstCoordinate = true;

    private final CoordinateQueue coordinateQueue = new CoordinateQueue();
    private DetectorAccidente accidente;










    private static final long UPDATE_INTERVAL_MS = 1000;
    public SpeedViewModel(@NonNull Application application) {
        super(application);
        locationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
        sensorData = new SpeedQueueRepository(application.getApplicationContext());
        accelerationQueueRepository = new AccelerationQueueRepository(application.getApplicationContext());

        accidente = new DetectorAccidente(getApplication().getApplicationContext());

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                updateLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(@NonNull String provider) {}

            @Override
            public void onProviderDisabled(@NonNull String provider) {}
        };

        checkLocationPermissions();
    }

    public LiveData<Double> getSpeed() {
        return speedKmh;
    }

    public LiveData<Location> getLocation() {
        return location;
    }

    public LiveData<Boolean> getLocationPermissionState() {
        return locationPermissionState;
    }

    public LiveData<String> getAddress() {return address;}

    private void updateLocation(Location location) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastSpeedUpdate >= UPDATE_INTERVAL_MS) {
            lastSpeedUpdate = currentTime;

            float currentSpeed = location.getSpeed();  // Velocidad en m/s
            DetectorAccidenteDataWriter.writeAccidentDataToFile(getApplication().getApplicationContext(),"Velocidad en M/S:"+ currentSpeed);
            if (isFirstMeasurement) {
                isFirstMeasurement = false;
                previousSpeed = currentSpeed;
                previousTime = currentTime;
            }
            else{
                float acceleration = (currentSpeed - previousSpeed) / (currentTime - previousTime);
                accelerationQueueRepository.addAccelerationData(acceleration);
                AccelerationDataWriter.writeAcceleration(getApplication().getApplicationContext(),"Aceleracion dada por VF="+ currentSpeed+", VI="+ previousSpeed+ "/ TF="+currentTime+", TI="+previousTime);
                previousSpeed = currentSpeed;
                previousTime = currentTime;
            }

            KalmanFilter kalmanFilter = new KalmanFilter(0.1, 0.1); // Configura los parámetros de ruido

// En tu bucle de actualización de ubicación
            //double measuredSpeed = location.getSpeed() * 3.6; // Velocidad en km/h



            double speedKmhValue = currentSpeed * 3.6;  // Convertir a km/h
            speedKmh.setValue(speedKmhValue);
            this.location.setValue(location);
            double smoothedSpeed = kalmanFilter.update(speedKmhValue);


            DetectorAccidenteDataWriter.writeAccidentDataToFile(getApplication().getApplicationContext(),"Velocidad en KM/H:"+ speedKmhValue );

            //DetectorAccidenteDataWriter.writeAccidentDataToFile(getApplication().getApplicationContext(),"Velocidad en KM/H CON KALMAN:"+ smoothedSpeed );

            sensorData.addSpeedData(speedKmhValue,UPDATE_INTERVAL_MS);  // Almacenar datos


            // Store coordinates and calculate direction changes
//            double currentLatitude = location.getLatitude();
//            double currentLongitude = location.getLongitude();
//
//            coordinateQueue.addCoordinates(currentLatitude, currentLongitude);

            accidente.registrarNuevoDato(new DatosMovimiento(location.getLatitude(),location.getLongitude(),currentSpeed,currentTime));

            // Solo actuar si ya tenemos 3 puntos
//            if (coordinateQueue.getCoordinateCount() == MAX_SIZE_COORD) {
//                AccidentDetector accidentDetector = new AccidentDetector(getApplication().getApplicationContext());
//
//                // Pasar las tres coordenadas para detectar un accidente
//                boolean accidentDetected = accidentDetector.detectAccident(
//                        coordinateQueue.getLongitude(0), coordinateQueue.getLatitude(0),  // Primer punto
//                        coordinateQueue.getLongitude(1), coordinateQueue.getLatitude(1),  // Segundo punto
//                        coordinateQueue.getLongitude(2), coordinateQueue.getLatitude(2)   // Tercer punto
//                );
//
//                if (accidentDetected) {
//                    // Manejar la detección de accidente
//                    System.out.println("Accident detected between coordinates!");
//                }
//            }

//            if (isFirstCoordinate) {
//                // First coordinate: just store it, don't calculate angle yet
//                previousLatitude = currentLatitude;
//                previousLongitude = currentLongitude;
//                isFirstCoordinate = false;
//            } else {
//                // Second coordinate and beyond: calculate angle and detect potential accidents
//                AccidentDetector accidentDetector = new AccidentDetector(getApplication().getApplicationContext());
//
//                boolean accidentDetected = accidentDetector.detectAccident(
//                        previousLongitude, previousLatitude, currentLongitude, currentLatitude);
//
//                if (accidentDetected) {
//                    // Handle accident detection, e.g., show alert or send a notification
//                    System.out.println("Accident detected between coordinates!");
//                }
//
//                // Update previous coordinates for the next comparison
//                previousLatitude = currentLatitude;
//                previousLongitude = currentLongitude;
//            }





























            // Obtener la dirección a partir de las coordenadas
            Geocoder geocoder = new Geocoder(getApplication(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    String addressString = addresses.get(0).getAddressLine(0);  // Dirección completa
                    address.setValue(addressString);  // Actualizar la dirección
                } else {
                    address.setValue("Dirección no encontrada");
                }
            } catch (IOException e) {
                e.printStackTrace();
                address.setValue("Error al obtener la dirección");
            }

        }
    }

    public void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionState.setValue(false);
        } else {
            locationPermissionState.setValue(true);
            resumeLocationUpdates();
        }
    }

    public void requestLocationPermissions(Context context) {
        ActivityCompat.requestPermissions((MainActivity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
    }

    public void checkLocationSettings(Context context) {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
                .setIntervalMillis(100)             // Sets the interval for location updates
                .setMinUpdateIntervalMillis(100/2)  // Sets the fastest allowed interval of location updates.
                .setWaitForAccurateLocation(false)              // Want Accurate location updates make it true or you get approximate updates
                .setMaxUpdateDelayMillis(100)                   // Sets the longest a location update may be delayed.
                .build();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(context).checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                resumeLocationUpdates();
            }
        });

        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                ResolvableApiException resolvable = (ResolvableApiException) e;
                try {
                    resolvable.startResolutionForResult((MainActivity) context, REQUEST_CHECK_SETTINGS);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void resumeLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL_MS, 0, locationListener);
        }
    }

    public void pauseLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(locationListener);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationSettings(getApplication().getApplicationContext());
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode) {
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == MainActivity.RESULT_OK) {
            checkLocationSettings(getApplication().getApplicationContext());
        }
    }
}
