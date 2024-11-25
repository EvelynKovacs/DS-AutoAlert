    package com.example.autoalert.viewmodel;


    import android.content.res.AssetManager;
    import android.location.Geocoder;
    import android.location.Address;

    import java.io.BufferedReader;
    import java.io.File;
    import java.io.FileReader;
    import java.io.FileWriter;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.InputStreamReader;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Date;
    import java.util.List;
    import java.util.Locale;
    import java.util.concurrent.ExecutorService;
    import java.util.concurrent.Executors;

    import com.example.autoalert.utils.AddressFetcher;
    import com.example.autoalert.utils.DetectorAccidente;


    import android.Manifest;
    import android.app.Application;
    import android.content.Context;
    import android.content.pm.PackageManager;
    import android.location.Location;
    import android.location.LocationListener;
    import android.location.LocationManager;
    import android.os.Bundle;
    import android.os.Handler;
    import android.os.Looper;
    import android.util.Log;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.core.app.ActivityCompat;
    import androidx.lifecycle.AndroidViewModel;
    import androidx.lifecycle.LiveData;
    import androidx.lifecycle.MutableLiveData;

    //import com.example.autoalert.repository.SensorQueueRepository;
    import com.example.autoalert.model.entities.DatosMovimiento;
    import com.example.autoalert.repository.SpeedQueueRepository;
    import com.example.autoalert.view.activities.MainActivity;
    import com.google.android.gms.common.api.ResolvableApiException;
    import com.google.android.gms.location.LocationRequest;
    import com.google.android.gms.location.LocationServices;
    import com.google.android.gms.location.LocationSettingsRequest;
    import com.google.android.gms.location.LocationSettingsResponse;
    import com.google.android.gms.location.Priority;
    import com.google.android.gms.tasks.Task;

    import org.json.JSONArray;
    import org.json.JSONObject;


    public class SpeedViewModel extends AndroidViewModel {

        private static final String TAG = "UbicacionGuardar";
        private ExecutorService executorService;

        private static final int REQUEST_LOCATION_PERMISSION = 1001;
        public static final int REQUEST_CHECK_SETTINGS = 1002;
        private static int MAX_SIZE_COORD = 3;
        private static int UMBRAL_MIN_VEL=-1;

        private MutableLiveData<Double> speedKmh = new MutableLiveData<>(0.0);
        private MutableLiveData<Location> location = new MutableLiveData<>();
        private MutableLiveData<Boolean> locationPermissionState = new MutableLiveData<>();
        private MutableLiveData<String> address = new MutableLiveData<>();

        private LocationManager locationManager;
        private LocationListener locationListener;
        private SpeedQueueRepository sensorData;
        private long lastSpeedUpdate = 0;
        private boolean isFirstMeasurement = true;
        private float previousSpeed;
        private long previousTime;
        private double previousLatitude;
        private double previousLongitude;
        private boolean isFirstCoordinate = true;

        private boolean deteccionIniciada = false;


        private DetectorAccidente accidente;
        private AddressFetcher addressFetcher;

        private final MutableLiveData<Boolean> locationEnabled = new MutableLiveData<>();


        private static final long UPDATE_INTERVAL_MS = 100;

        private Location locationDeArchivo;
        private MutableLiveData<Location> locationLiveData = new MutableLiveData<>();
        private List<Location> locationList = new ArrayList<>();
        private int currentIndex = 0;


        public SpeedViewModel(@NonNull Application application) {
            super(application);
            locationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);

            sensorData = new SpeedQueueRepository(application.getApplicationContext());

            accidente = new DetectorAccidente(getApplication().getApplicationContext());

            addressFetcher = new AddressFetcher(getApplication().getApplicationContext());

            executorService = Executors.newSingleThreadExecutor();

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {

                    //updateLocation(location);
//                    Log.d(TAG, "Datos del currentIndex: "+currentIndex+" Y el tamaño de la lista es: "+locationList.size());
//                    // Verificar si el índice es válido para la lista locationList
//                    if (currentIndex < locationList.size()) {
//                        // Obtener la ubicación correspondiente en la lista locationList
//                        Location newLocation = locationList.get(currentIndex);
//
//                        // Llamar al método para actualizar la ubicación
//                        updateLocation(newLocation);
//
//                        Log.d(TAG, "Leyendo dato nro: "+currentIndex + "con " + newLocation.getLatitude() + " " + newLocation.getLongitude()  + " " + newLocation.getSpeed());
//                        // Incrementar el índice para la siguiente ubicación
//                        currentIndex++;
//
//                        // Si el índice supera el tamaño de la lista, detener la actualización (opcional)
//                        if (currentIndex >= locationList.size()) {
//                            Log.d(TAG, "Se han procesado todas las ubicaciones.");
//                            currentIndex = 0;
//                        }
//                    }
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

            //executorService = Executors.newSingleThreadExecutor();
            //loadLocationsFromCsv(); // Carga las ubicaciones desde el archivo CSV
        //    startLocationUpdates(); // Inicia la simulación de actualizaciones

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
        public void updateLocation(Location location) {
            if (location != null) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastSpeedUpdate >= UPDATE_INTERVAL_MS) {
                    lastSpeedUpdate = currentTime;
                    processSpeedData(location);



                    // Obtener dirección desde las coordenadas
                    addressFetcher.fetchAddressFromLocation(location, address);

                    // Actualizar la ubicación
                    this.location.postValue(location);
                    saveLocation(location);  // Guarda la ubicación en el archivo JSON
                }


            }
        }

        private void processSpeedData(Location location) {
            float currentSpeed = location.getSpeed();  // Velocidad en m/s
            double speedKmhValue = currentSpeed; /* 3.6;*/  // Convertir a km/h
            speedKmh.postValue(speedKmhValue);
            Log.i("SpeedObserver", "Velocidad actual ACA: " + speedKmh.getValue());

            // sensorData.addSpeedData(speedKmhValue, UPDATE_INTERVAL_MS);  // Almacenar datos
            if (!deteccionIniciada && speedKmhValue > UMBRAL_MIN_VEL) {
                Log.i("MainActivity", "Velocidad mayor a 5km/h. Iniciando detección de accidentes.");
                //Toast.makeText(getApplication().getApplicationContext(), "Velocidad mayor a 5 km/h. Iniciando detección de accidentes.", Toast.LENGTH_LONG).show();

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
                resumeLocationUpdates();
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

        // PARA TENER LA ULTIMA UBICACION
        private void saveLocation(Location location) {
            File archivoUltimaUbicacion = new File(getApplication().getApplicationContext().getFilesDir(), "ultima_ubicacion.json");
            File archivoTodasUbicaciones = new File(getApplication().getApplicationContext().getFilesDir(), "ubicaciones_periodicas.json");

            try {
                String lugar = "Desconocido";
                try {
                    Geocoder geocoder = new Geocoder(getApplication().getApplicationContext(), Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        lugar = address.getAddressLine(0);
                    }
                } catch (IOException geocoderException) {
                    Log.e(TAG, "Error al obtener la dirección. Guardando solo coordenadas.", geocoderException);
                    lugar = "Desconocido (sin conexión)";
                }

                String horaActual = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                JSONObject nuevaUbicacion = new JSONObject();
                nuevaUbicacion.put("latitud", location.getLatitude());
                nuevaUbicacion.put("longitud", location.getLongitude());
                nuevaUbicacion.put("lugar", lugar);
                nuevaUbicacion.put("hora", horaActual);

                try (FileWriter fileWriterUltimaUbicacion = new FileWriter(archivoUltimaUbicacion, false)) {
                    fileWriterUltimaUbicacion.write(nuevaUbicacion.toString());
                    Log.d(TAG, "Última ubicación guardada en archivo JSON.");
                }

                JSONArray jsonArray = new JSONArray();
                if (archivoTodasUbicaciones.exists()) {
                    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(archivoTodasUbicaciones))) {
                        StringBuilder jsonStringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            jsonStringBuilder.append(line);
                        }
                        if (!jsonStringBuilder.toString().isEmpty()) {
                            jsonArray = new JSONArray(jsonStringBuilder.toString());
                        }
                    }
                }

                jsonArray.put(nuevaUbicacion);

                try (FileWriter fileWriterTodasUbicaciones = new FileWriter(archivoTodasUbicaciones, false)) {
                    fileWriterTodasUbicaciones.write(jsonArray.toString());
                    Log.d(TAG, "Ubicación guardada en archivo JSON de todas las ubicaciones.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al guardar ubicación en JSON.", e);
            }
        }

            public void loadLocationsFromCsv(String fileName) {
               // String archivo = tipo.equals("accidente") ? "datos_accidente.csv" : "datos_sin_accidente.csv";
                AssetManager assetManager = getApplication().getAssets(); // Obtener el AssetManager

                try {
                    // Ruta del archivo
//                    File csvFile = new File(getApplication().getFilesDir(), fileName);
//                    BufferedReader reader = new BufferedReader(new FileReader(csvFile));
                    String archivo = fileName.equals("frontal_accidente") ? "frontal_accidente" :
                            fileName.equals("frontal_normal") ? "frontal_normal" :
                                    fileName.equals("lateral_accidente") ? "lateral_accidente" :
                                                fileName.equals("lateral_normal") ? "lateral_normal" :
                                                        fileName.equals("trasero_accidente") ? "trasero_accidente" :
                                                                fileName.equals("trasero_normal") ? "trasero_normal" :
                                                                        "Desconocido";

                    // Abrir el archivo desde assets
                    InputStream inputStream = assetManager.open(archivo);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));


                    String line;
                    boolean isFirstLine = true; // Ignorar la cabecera
                    while ((line = reader.readLine()) != null) {
                        if (isFirstLine) {
                            isFirstLine = false;
                            continue; // Ignorar la primera línea que es la cabecera
                        }

                        // Divide la línea en columnas usando tabuladores
                        String[] columns = line.split("\t");
                        if (columns.length >= 3) {
                            // La primera columna es la hora (la puedes ignorar si no la necesitas)
                            String velocidad = columns[0];  // Hora
                            double latitude = Double.parseDouble(columns[1]);  // Latitud
                            double longitude = Double.parseDouble(columns[2]);  // Longitud

                            // Crear un objeto Location
                            Location location = new Location("csv");
                            location.setLatitude(latitude);
                            location.setLongitude(longitude);
                            location.setSpeed((Float.parseFloat(velocidad)));
                            //speedKmh.setValue((Double.parseDouble(velocidad)));

                            Log.d(TAG, "Location guardado con estos datos: " +location.getSpeed()+ " " +location.getLatitude() + "  "+location.getLongitude()+" ");
                            // Agregar la ubicación a la lista
                            locationList.add(location);

                        }
                    }

                    reader.close();
                    Log.d(TAG, "Archivo cargado exitosamente con " + locationList.size() + " ubicaciones.");
                    startProcessingLocations();


                } catch (Exception e) {
                    Log.e(TAG, "Error al cargar el archivo Archivo", e);
                }
            }
//        private void startProcessingLocations() {
//            executorService.execute(() -> {
//                try {
//                    while (true) {
//                        // Verificar que la lista no esté vacía y el índice sea válido
//                        if (!locationList.isEmpty() && currentIndex < locationList.size()) {
//                            Log.d(TAG, "Datos del currentIndex: " + currentIndex + " Y el tamaño de la lista es: " + locationList.size());
//
//                            // Obtener la ubicación actual de la lista
//                            Location newLocation = locationList.get(currentIndex);
//
//                            // Llamar al método para actualizar la ubicación
//                            updateLocation(newLocation);
//
//                            Log.d(TAG, "Leyendo dato nro: " + currentIndex + " con " + newLocation.getLatitude() + " " + newLocation.getLongitude() + " " + newLocation.getSpeed());
//
//                            // Incrementar el índice para la siguiente ubicación
//                            currentIndex++;
//
//                            // Si el índice supera el tamaño de la lista, reiniciamos el índice
//                            if (currentIndex >= locationList.size()) {
//                                Log.d(TAG, "Se han procesado todas las ubicaciones.");
//                                currentIndex = 0; // Reiniciamos el índice para comenzar de nuevo
//                            }
//
//                            // Esperar un intervalo antes de la próxima actualización
//                            Thread.sleep(UPDATE_INTERVAL_MS); // Pausa antes de la siguiente actualización
//                        } else {
//                            // Si la lista está vacía o el índice es mayor que el tamaño, reiniciar el índice
//                            currentIndex = 0;
//                        }
//                    }
//                } catch (InterruptedException e) {
//                    Log.e(TAG, "Error en el hilo de actualización", e);
//                }
//            });
//        }


        private void startProcessingLocations() {
            executorService.execute(() -> {
                try {
                    while (currentIndex < locationList.size()) {
                        // Obtener la ubicación del archivo
                        Location currentLocation = locationList.get(currentIndex);
                        Log.d(TAG, "Leyendo ubicación desde el archivo: " + currentIndex + " con "
                                + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());

                        updateLocation(currentLocation);
                        //locationLiveData.postValue(currentLocation);  // Usa postValue() aquí en lugar de setValue()

                        //speedKmh.postValue(Double.parseDouble(String.valueOf(currentLocation.getSpeed())));  // Convierte el valor a String

                        // Actualizar la UI o LiveData
                       // locationLiveData.postValue(currentLocation);

                        // Incrementar el índice para la siguiente ubicación
                        currentIndex++;

                        // Esperar un intervalo antes de procesar la siguiente ubicación
                        Thread.sleep(UPDATE_INTERVAL_MS);
                    }
                    // Mostrar el Toast cuando termine la lectura
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(getApplication().getApplicationContext(), "Lectura del archivo completada", Toast.LENGTH_SHORT).show()
                    );

                } catch (InterruptedException e) {
                    Log.e(TAG, "Error en el hilo de lectura de archivo", e);
                }
            });
        }


        private void startLocationUpdates() {
            executorService.execute(() -> {
                while (true) {
                    try {
                        if (!locationList.isEmpty() && currentIndex < locationList.size()) {
                            // Obtener la ubicación actual de la lista
                            Location currentLocation = locationList.get(currentIndex);

                            // Actualizar LiveData
                            locationLiveData.postValue(currentLocation);



                            // Incrementar el índice para la siguiente ubicación
                            currentIndex++;

                            // Esperar un segundo antes de la próxima actualización
                            Thread.sleep(UPDATE_INTERVAL_MS);
                        } else {
                            currentIndex = 0; // Reinicia el índice para repetir
                        }
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Error en el hilo de actualización", e);
                        break;
                    }
                }
            });
        }
    }