package com.example.autoalert.viewmodel;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Address;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.example.autoalert.utils.AddressFetcher;
import com.example.autoalert.utils.DetectorAccidente;
//import com.example.autoalert.utils.KalmanFilter;


import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

//import com.example.autoalert.repository.SensorQueueRepository;
import com.example.autoalert.model.entities.DatosMovimiento;
//import com.example.autoalert.repository.AccelerationDataWriter;
//import com.example.autoalert.repository.AccelerationQueueRepository;
//import com.example.autoalert.repository.CoordinateQueue;
//import com.example.autoalert.repository.DetectorAccidenteDataWriter;
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
    public static final int REQUEST_CHECK_SETTINGS = 1002;
    private static int MAX_SIZE_COORD = 3;
    private static int UMBRAL_MIN_VEL=5;

    private MutableLiveData<Double> speedKmh = new MutableLiveData<>();
    private MutableLiveData<Location> location = new MutableLiveData<>();
    private MutableLiveData<Boolean> locationPermissionState = new MutableLiveData<>();
    private MutableLiveData<String> address = new MutableLiveData<>();

    private LocationManager locationManager;
    private LocationListener locationListener;
    private SpeedQueueRepository sensorData;
    //private AccelerationQueueRepository accelerationQueueRepository;
    private long lastSpeedUpdate = 0;
    private boolean isFirstMeasurement = true;
    private float previousSpeed;
    private long previousTime;
    private double previousLatitude;
    private double previousLongitude;
    private boolean isFirstCoordinate = true;

    private boolean deteccionIniciada = false;


    //private final CoordinateQueue coordinateQueue = new CoordinateQueue();
    private DetectorAccidente accidente;
    private AddressFetcher addressFetcher;

    private final MutableLiveData<Boolean> locationEnabled = new MutableLiveData<>();


    private static final long UPDATE_INTERVAL_MS = 1000;

    public SpeedViewModel(@NonNull Application application) {
        super(application);
        locationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);

        sensorData = new SpeedQueueRepository(application.getApplicationContext());
        //accelerationQueueRepository = new AccelerationQueueRepository(application.getApplicationContext());

        accidente = new DetectorAccidente(getApplication().getApplicationContext());

        addressFetcher = new AddressFetcher(getApplication().getApplicationContext());


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                updateLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                promptEnableLocationSettings();

            }
        };

        //checkLocationPermissions();
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

    public LiveData<String> getAddress() {
        return address;
    }

    // Actualiza la ubicación en la interfaz
    private void updateLocation(Location location) {
        if (location != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastSpeedUpdate >= UPDATE_INTERVAL_MS) {
                lastSpeedUpdate = currentTime;
                processSpeedData(location);


                //float currentSpeed = location.getSpeed();  // Velocidad en m/s
                //DetectorAccidenteDataWriter.writeAccidentDataToFile(getApplication().getApplicationContext(), "Velocidad en M/S:" + currentSpeed);

                //speedAndAccelerationHandler.handleSpeedAndAcceleration(currentSpeed, currentTime, accelerationQueueRepository);

                //double speedKmhValue = currentSpeed * 3.6;  // Convertir a km/h
                //speedKmh.setValue(speedKmhValue);

                // Registrar datos de velocidad y aplicar filtro de Kalman
                //DetectorAccidenteDataWriter.writeAccidentDataToFile(getApplication().getApplicationContext(), "Velocidad en KM/H:" + speedKmhValue);
                //sensorData.addSpeedData(speedKmhValue, UPDATE_INTERVAL_MS);  // Almacenar datos

                // Registrar datos de movimiento
                //accidente.registrarNuevoDato(new DatosMovimiento(location.getLatitude(), location.getLongitude(), currentSpeed, currentTime));

                // Obtener dirección desde las coordenadas
                addressFetcher.fetchAddressFromLocation(location, address);

                // Actualizar la ubicación
                this.location.setValue(location);
            }


        }
    }
    private void processSpeedData(Location location) {
        float currentSpeed = location.getSpeed();  // Velocidad en m/s
        double speedKmhValue = currentSpeed * 3.6;  // Convertir a km/h
        speedKmh.setValue(speedKmhValue);
        sensorData.addSpeedData(speedKmhValue, UPDATE_INTERVAL_MS);  // Almacenar datos
        if (!deteccionIniciada && speedKmhValue > UMBRAL_MIN_VEL) {
            Log.i("MainActivity", "Velocidad mayor a 5km/h. Iniciando detección de accidentes.");
            Toast.makeText(getApplication().getApplicationContext(), "Velocidad mayor a 5 km/h. Iniciando detección de accidentes.", Toast.LENGTH_LONG).show();

            deteccionIniciada = true;  // Marcamos que ya hemos iniciado la detección
            accidente.registrarNuevoDato(new DatosMovimiento(location.getLatitude(), location.getLongitude(), speedKmhValue, System.currentTimeMillis()));
        }else if (deteccionIniciada){
            accidente.registrarNuevoDato(new DatosMovimiento(location.getLatitude(), location.getLongitude(), speedKmhValue, System.currentTimeMillis()));

        }else{
            Log.i("MainActivity", "Velocidad menor a 5km/h. Esperando para iniciar la detección.");
            Toast.makeText(getApplication().getApplicationContext(), "Velocidad menor a 5 km/h. Esperando para iniciar la detección.", Toast.LENGTH_LONG).show();


        }
//csvHelper.saveDataToCsv(speedKmhValue,location.getLatitude(),location.getLongitude(),0,false,false,false);
        // csvHelper.saveDataToCsv(speedKmhValue,location.getLatitude(),location.getLongitude(),address.getValue());

    }

//    private void updateLocation(Location location) {
//        long currentTime = System.currentTimeMillis();
//
//        if (currentTime - lastSpeedUpdate >= UPDATE_INTERVAL_MS) {
//            lastSpeedUpdate = currentTime;
//
//            float currentSpeed = location.getSpeed();  // Velocidad en m/s
//            DetectorAccidenteDataWriter.writeAccidentDataToFile(getApplication().getApplicationContext(),"Velocidad en M/S:"+ currentSpeed);
//            if (isFirstMeasurement) {
//                isFirstMeasurement = false;
//                previousSpeed = currentSpeed;
//                previousTime = currentTime;
//            }
//            else{
//                float acceleration = (currentSpeed - previousSpeed) / (currentTime - previousTime);
//                accelerationQueueRepository.addAccelerationData(acceleration);
//                AccelerationDataWriter.writeAcceleration(getApplication().getApplicationContext(),"Aceleracion dada por VF="+ currentSpeed+", VI="+ previousSpeed+ "/ TF="+currentTime+", TI="+previousTime);
//                previousSpeed = currentSpeed;
//                previousTime = currentTime;
//            }
//
//            KalmanFilter kalmanFilter = new KalmanFilter(0.1, 0.1); // Configura los parámetros de ruido
//
//
//            double speedKmhValue = currentSpeed * 3.6;  // Convertir a km/h
//            speedKmh.setValue(speedKmhValue);
//            this.location.setValue(location);
//            double smoothedSpeed = kalmanFilter.update(speedKmhValue);
//
//
//            DetectorAccidenteDataWriter.writeAccidentDataToFile(getApplication().getApplicationContext(),"Velocidad en KM/H:"+ speedKmhValue );
//
//            //DetectorAccidenteDataWriter.writeAccidentDataToFile(getApplication().getApplicationContext(),"Velocidad en KM/H CON KALMAN:"+ smoothedSpeed );
//
//            sensorData.addSpeedData(speedKmhValue,UPDATE_INTERVAL_MS);  // Almacenar datos
//
//
//            accidente.registrarNuevoDato(new DatosMovimiento(location.getLatitude(),location.getLongitude(),currentSpeed,currentTime));
//
//
//
//
//            // Obtener la dirección a partir de las coordenadas
//            Geocoder geocoder = new Geocoder(getApplication(), Locale.getDefault());
//            try {
//                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
//                if (addresses != null && !addresses.isEmpty()) {
//                    String addressString = addresses.get(0).getAddressLine(0);  // Dirección completa
//                    address.setValue(addressString);  // Actualizar la dirección
//                } else {
//                    address.setValue("Dirección no encontrada");
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                address.setValue("Error al obtener la dirección");
//            }
//
//        }
//    }

//    public void checkLocationPermissions() {
//        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            locationPermissionState.setValue(false);
//        } else {
//            locationPermissionState.setValue(true);
//            resumeLocationUpdates();
//        }
//    }

    // Verifica permisos de ubicación
    public void checkLocationPermissions() {
        Log.i("SpeedViewModel", "Checking location permissions");
        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationPermissionState.setValue(false);
        } else {
            locationPermissionState.setValue(true);
            checkLocationSettings(getApplication().getApplicationContext());
        }
    }

//    // Solicita permisos de ubicación
//    public void requestLocationPermissions(Context context) {
//        ActivityCompat.requestPermissions((MainActivity) context,
//                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                REQUEST_LOCATION_PERMISSION);
//
//    }

    public void checkLocationSettings(Context context) {
        Log.i("SpeedViewModel", "Permissions granted, checking location settings");

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS)
                .setIntervalMillis(UPDATE_INTERVAL_MS)
                .setMinUpdateIntervalMillis(UPDATE_INTERVAL_MS / 2)
                .build();



        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);


        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(context)
                .checkLocationSettings(builder.build());

        //resumeLocationUpdates();


        task.addOnSuccessListener(locationSettingsResponse -> {
            Log.i("SpeedViewModel", "Location settings are satisfied.");
            resumeLocationUpdates(); // Llama a resumeLocationUpdates solo si la ubicación está activada
        });
        //task.addOnSuccessListener(locationSettingsResponse -> resumeLocationUpdates());


        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                ResolvableApiException resolvable = (ResolvableApiException) e;
                try {
                    resolvable.startResolutionForResult((MainActivity) context, REQUEST_CHECK_SETTINGS);
                    Log.i("SpeedViewModel", "?????");

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
//    public void checkLocationSettings(Context context) {
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
//            return;
//        }
//
//        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
//        if (isGpsEnabled || isNetworkEnabled) {
//            resumeLocationUpdates();
//        } else {
//            showEnableLocationDialog(context);
//        }
//    }
//
//    private void showEnableLocationDialog(Context context) {
//        new AlertDialog.Builder(context)
//                .setTitle("Activar Ubicación")
//                .setMessage("Por favor, activa la ubicación y la precisión alta en la configuración.")
//                .setPositiveButton("Ir a Configuración", (dialog, which) -> {
//                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                    context.startActivity(intent);
//                })
//                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
//                .setOnDismissListener(dialog -> {
//                    // Aquí puedes verificar nuevamente si la ubicación está habilitada
//                    checkLocationSettings(context);
//                })
//                .show();
//    }


    // Inicia las actualizaciones de ubicación
    public void resumeLocationUpdates() {
        Log.i("SpeedViewModel", "Location settings are OK, resuming location updates");

        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL_MS, 0, locationListener);
        }
    }

    //    // Pausa las actualizaciones de ubicación
//    public void pauseLocationUpdates() {
//        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            locationManager.removeUpdates(locationListener);
//        }
//    }
    // Muestra diálogo para habilitar los ajustes de ubicación si están deshabilitados
    private void promptEnableLocationSettings() {
        checkLocationSettings(getApplication().getApplicationContext());
    }
//    // Maneja el resultado de la solicitud de permisos
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.length > 0
//                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            checkLocationSettings(getApplication().getApplicationContext());
//        }
//    }

    // Maneja el resultado de la solicitud de activación de ubicación
//    public void onActivityResult(int requestCode, int resultCode) {
//        if (requestCode == REQUEST_CHECK_SETTINGS) {
//            if (resultCode == MainActivity.RESULT_OK) {
//                checkLocationSettings(getApplication().getApplicationContext());
//                // La ubicación ahora está habilitada, comienza a recibir actualizaciones
//            } else {
//                // La ubicación no se activó, puedes manejar esto como desees
//                Log.e("LocationSettingsError", "El usuario no habilitó la ubicación.");
//            }
//        }

//    public void onActivityResult(int requestCode, int resultCode) {
//        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == MainActivity.RESULT_OK) {
//            resumeLocationUpdates();
//            //checkLocationSettings(getApplication().getApplicationContext());
//        }
//
//    }
}