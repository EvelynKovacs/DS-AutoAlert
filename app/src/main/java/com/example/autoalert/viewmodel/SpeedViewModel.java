package com.example.autoalert.viewmodel;

import android.location.Geocoder;
import android.location.Address;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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
import com.example.autoalert.repository.SpeedQueueRepository;
import com.example.autoalert.view.activities.MainActivity;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.Task;

public class SpeedViewModel extends AndroidViewModel {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final int REQUEST_CHECK_SETTINGS = 1002;

    private MutableLiveData<Double> speedKmh = new MutableLiveData<>();
    private MutableLiveData<Location> location = new MutableLiveData<>();
    private MutableLiveData<Boolean> locationPermissionState = new MutableLiveData<>();
    private MutableLiveData<String> address = new MutableLiveData<>();

    private LocationManager locationManager;
    private LocationListener locationListener;
    private SpeedQueueRepository sensorData;
    private long lastSpeedUpdate = 0;

    private static final long UPDATE_INTERVAL_MS = 100;  // 100 ms para 10 datos por segundo

    public SpeedViewModel(@NonNull Application application) {
        super(application);
        locationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
        sensorData = new SpeedQueueRepository(application.getApplicationContext());

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

            float speed = location.getSpeed();  // Velocidad en m/s
            double speedKmhValue = speed * 3.6;  // Convertir a km/h
            speedKmh.setValue(speedKmhValue);
            this.location.setValue(location);

            sensorData.addSpeedData(speedKmhValue);  // Almacenar datos


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
        LocationRequest locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
