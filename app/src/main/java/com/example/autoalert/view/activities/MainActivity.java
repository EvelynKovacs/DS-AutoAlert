package com.example.autoalert.view.activities;

import static com.example.autoalert.viewmodel.SpeedViewModel.REQUEST_CHECK_SETTINGS;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.example.autoalert.R;
import com.example.autoalert.repository.AccelerometerQueueRepository;
import com.example.autoalert.repository.CsvAccFrontal;
import com.example.autoalert.repository.CsvAccLateral;
import com.example.autoalert.repository.CsvAccTrasero;
import com.example.autoalert.repository.CsvAccelerometer;
import com.example.autoalert.repository.CsvGravity;
import com.example.autoalert.repository.CsvGyroscope;
import com.example.autoalert.repository.CsvMagnetometer;
import com.example.autoalert.repository.CsvOrientation;
import com.example.autoalert.repository.CsvRotation;
import com.example.autoalert.utils.CreateZipFile;
import com.example.autoalert.utils.SmsUtils;
import com.example.autoalert.view.adapters.SensorAdapter;
import com.example.autoalert.viewmodel.SensorViewModel;
import com.example.autoalert.viewmodel.SpeedViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import android.content.pm.PackageManager;
import android.widget.Toast;

import  com.example.autoalert.utils.AccidentDetector;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private SensorViewModel sensorViewModel;
    private SensorAdapter sensorAdapter;
    private SpeedViewModel speedViewModel;
    private TextView tvSpeed, tvLocation, tvAddress;  // Nuevo TextView para la dirección
    private boolean isMessageSent = false;  // Bandera para controlar el envío del mensaje
    private AccidentDetector accidentDetector; // Instancia de AccidentDetector
    private AccelerometerQueueRepository accelerometerQueueRepository;

    private Button btnStopAndDownload;
    private CsvAccLateral csvHelper; // Declarar CsvHelper
    private String zipFilePath; // Variable global para almacenar la ruta del archivo ZIP
    private CsvAccFrontal csvAccFrontal;
    private CsvAccTrasero csvAccTrasero;
    private CsvOrientation csvOrientation;
    private CsvRotation csvRotation;
    private CsvGyroscope csvGyroscope;
    private CsvAccelerometer csvAccelerometer;
    private CsvMagnetometer csvMagnetometer;
    private CsvGravity csvGravity;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        tvSpeed = findViewById(R.id.tvSpeed);
        tvLocation = findViewById(R.id.tvLocation);
        tvAddress = findViewById(R.id.tvAddress);  // TextView para la dirección
        // Inicializa las vistas
        //LinearLayout redAlertLayout = findViewById(R.id.red_alert_layout);
        //TextView countdownTimer = findViewById(R.id.countdown_timer);
        // Crea y configura AccidentDetector
        accidentDetector = new AccidentDetector(getApplicationContext());
        csvHelper = new CsvAccLateral(this);
        csvAccFrontal= new CsvAccFrontal(this);
        csvAccTrasero= new CsvAccTrasero(this);
        csvGravity= new CsvGravity(this);
        csvAccelerometer= new CsvAccelerometer(this);
        csvGyroscope= new CsvGyroscope(this);
        csvMagnetometer = new CsvMagnetometer(this);
        csvOrientation = new CsvOrientation(this);
        csvRotation = new CsvRotation(this);
        //csvHelper.createCsvFile(this);


        accelerometerQueueRepository = new AccelerometerQueueRepository(getApplicationContext());


        //SmsUtils.checkAndSendSms(this, new String[]{"2804405851", "2804611882", "2804992455"}, "Mensaje de emergencia");

        //SmsUtils.checkAndSendSms(this, new String[]{"2804559405", "2804611882", "2804382723"}, "Mensaje de emergencia");

        sensorViewModel = new ViewModelProvider(this).get(SensorViewModel.class);
        //ListView listView = findViewById(R.id.listView1);

        // Inicializar el adaptador con datos vacíos al inicio
        //sensorAdapter = new SensorAdapter(this, new ArrayList<>(), new HashMap<>());
        //listView.setAdapter(sensorAdapter);

        // Observadores para actualizar la UI en base a los datos del ViewModel
//        sensorViewModel.getSensorNames().observe(this, sensorNames -> {
//            sensorAdapter.updateSensorNames(sensorNames);
//        });

//        sensorViewModel.getSensorValues().observe(this, sensorValues -> {
//            sensorAdapter.updateSensorValues(sensorValues);
//        });

        sensorViewModel.getMissingSensors().observe(this, missingSensors -> {
            if (!missingSensors.isEmpty()) {
                sensorViewModel.showSensorNotification();
            }
        });

        sensorViewModel.detectSensors();

        // Inicializar el ViewModel
        speedViewModel = new ViewModelProvider(this).get(SpeedViewModel.class);

        // Observar los cambios de velocidad y almacenar en el SensorViewModel
        speedViewModel.getSpeed().observe(this, speedKmh -> {
            tvSpeed.setText(String.format("Velocidad: %.2f km/h", speedKmh));
        });

        // Observar los cambios de ubicación (coordenadas)
        speedViewModel.getLocation().observe(this, location -> {
            tvLocation.setText(String.format("Ubicación: %.6f, %.6f", location.getLatitude(), location.getLongitude()));
        });

        // Observar los cambios de dirección
        speedViewModel.getAddress().observe(this, address -> {
            tvAddress.setText("Dirección: " + address);  // Actualizar el TextView de la dirección
            System.out.println("DIRECCION: " + address);
            String emergencyMessage = "Emergencia. Dirección: " + address;
            System.out.println(emergencyMessage);
            String sanitizedAddress = " Mensaje de Emergencia. La siguiente direccion podria no ser exacta. " + address.replaceAll("[^a-zA-Z0-9\\s,.]", "");


//
            //SmsUtils.checkAndSendSms(this, new String[]{"2804559405", "2804611882", "2804382723"}, sanitizedAddress);

            if (!isMessageSent) {
                //String emergencyMessage = "Mensaje de emergencia. Dirección: " + address;

                //SmsUtils.checkAndSendSms(this, new String[]{"2804992455", "2804611882", "2804405851"}, sanitizedAddress);

                isMessageSent = true;  // Marcar como enviado
            }
        });

        // Observar el estado de los permisos
//        speedViewModel.getLocationPermissionState().observe(this, permissionGranted -> {
//            if (!permissionGranted) {
//                speedViewModel.requestLocationPermissions(this);
//            }
//        });

        checkPermissions();

        btnStopAndDownload = findViewById(R.id.button_stop_download);
        btnStopAndDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAndDownload();
            }
        });


        // Iniciar la configuración de ubicación
        //speedViewModel.checkLocationSettings(this);


    }

    // Método para verificar permisos de ubicación en tiempo de ejecución
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }, 1);
            }
        }
    }

    // Callback para manejar la respuesta del usuario a la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speedViewModel.checkLocationSettings(this); // Inicia la verificación de la configuración de ubicación

                // Permisos concedidos, puedes iniciar el hotspot
                Log.i("PermissionSuccesfull", "Permiso de ubicación habilitados. Se puede iniciar el hotspot.");
            } else {
                // Permisos no concedidos, manejar el caso según tu lógica
                Log.e("PermissionError", "Permiso de ubicación denegado. No se puede iniciar el hotspot.");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorViewModel.clearSensorData();
        sensorViewModel.registerSensorListeners();
        //speedViewModel.resumeLocationUpdates();
        speedViewModel.checkLocationSettings(this); // Verifica la configuración de ubicación cada vez que se reanuda la actividad

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorViewModel.unregisterSensorListeners();
        //speedViewModel.pauseLocationUpdates();
    }


    // Manejar la respuesta de los permisos
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        speedViewModel.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CHECK_SETTINGS) {
//            if (resultCode == RESULT_OK) {
//                Log.i("MainActivity", "Location settings enabled by user.");
//                // Aquí puedes llamar a resumeLocationUpdates
//                speedViewModel.resumeLocationUpdates();
//            } else {
//                Log.e("MainActivity", "Location settings were not enabled.");
//            }
//        }
//    }

    // Métodos para detener la grabación y descargar el CSV
//    private void stopAndDownload() {
//        // Detener la grabación de datos
//        speedViewModel.pauseLocationUpdates(); // Detener las actualizaciones de ubicación
//        //csvHelper.createCsvFile(this); // Este método creará un nuevo archivo, borrando el anterior.
//
//        // Lógica para descargar el archivo CSV
//        String csvFilePath = csvHelper.getCsvFilePath(); // Usar la instancia de CsvHelper
//        downloadCsv(csvFilePath);
//    }
//
//
//private void downloadCsv(String filePath) {
//    if (filePath != null) {
//        File file = new File(filePath);
//        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", file);
//
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setDataAndType(uri, "text/csv");
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        startActivity(intent);
//    } else {
//        Toast.makeText(this, "No se pudo encontrar el archivo CSV.", Toast.LENGTH_SHORT).show();
//    }
    private void stopAndDownload() {
        // Detener la grabación de datos
        speedViewModel.pauseLocationUpdates();

        // Aquí supongo que ya tienes una lista de archivos CSV creados
        ArrayList<String> csvFilePaths = new ArrayList<>();
        csvFilePaths.add(csvHelper.getCsvFilePath());  // Agregar las rutas de los archivos CSV
        csvFilePaths.add(csvAccFrontal.getCsvFilePath()); // Añadir otros archivos CSV si los tienes
        csvFilePaths.add(csvAccTrasero.getCsvFilePath());
        csvFilePaths.add(csvGyroscope.getCsvFilePath());
        csvFilePaths.add(csvAccelerometer.getCsvFilePath());
        csvFilePaths.add(csvOrientation.getCsvFilePath());
        csvFilePaths.add(csvRotation.getCsvFilePath());
        csvFilePaths.add(csvMagnetometer.getCsvFilePath());
        csvFilePaths.add(csvGravity.getCsvFilePath());

        // Definir la ruta del archivo ZIP donde se almacenarán los CSVs
         zipFilePath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/datos.zip";

        // Crear el archivo ZIP
        try {
            CreateZipFile zipCreator = new CreateZipFile();
            zipCreator.createZipFile(csvFilePaths, zipFilePath);

            // Una vez creado el ZIP, ofrecerlo para descargar
            //downloadZip(zipFilePath);
            showShareOrSaveOptions(zipFilePath);


        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al crear el archivo ZIP", Toast.LENGTH_SHORT).show();
        }
    }

    private void showShareOrSaveOptions(String zipFilePath) {
        if (zipFilePath != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("¿Qué desea hacer con el archivo ZIP?");
            builder.setItems(new CharSequence[]{"Compartir", "Guardar en dispositivo"},
                    (dialog, which) -> {
                        if (which == 0) {
                            // Opción de compartir
                            shareZip(zipFilePath);
                        } else {
                            // Opción de guardar en el dispositivo
                            saveZipToDevice(zipFilePath);
                        }
                    });
            builder.show();
        } else {
            Toast.makeText(this, "No se pudo encontrar el archivo ZIP.", Toast.LENGTH_SHORT).show();
        }
    }


    private void shareZip(String zipFilePath) {
        if (zipFilePath != null) {
            File zipFile = new File(zipFilePath);
            Uri zipUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", zipFile);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/zip");
            intent.putExtra(Intent.EXTRA_STREAM, zipUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Mostrar el selector de aplicaciones para compartir el archivo
            startActivity(Intent.createChooser(intent, "Compartir archivo ZIP"));
        } else {
            Toast.makeText(this, "No se pudo encontrar el archivo ZIP.", Toast.LENGTH_SHORT).show();
        }
    }

    private static final int CREATE_FILE_REQUEST_CODE = 1;

    private void saveZipToDevice(String zipFilePath) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_TITLE, "archivo.zip");  // Nombre predeterminado del archivo
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Verificación de la configuración de ubicación
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Log.i("MainActivity", "Location settings enabled by user.");
                // Llamar a resumeLocationUpdates cuando la ubicación está habilitada
                speedViewModel.resumeLocationUpdates();
            } else {
                Log.e("MainActivity", "Location settings were not enabled.");
            }
        }

        // Verificación para guardar el archivo ZIP
        else if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                try {
                    // Copiar el archivo ZIP a la ubicación seleccionada por el usuario
                    copyZipToUri(uri, new File(zipFilePath));  // zipFilePath debe ser accesible
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al guardar el archivo.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void copyZipToUri(Uri uri, File zipFile) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(zipFile);
             OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream != null) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }
        }
    }



//    private void downloadZip(String zipFilePath) {
//        if (zipFilePath != null) {
//            File zipFile = new File(zipFilePath);
//            Uri zipUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", zipFile);
//
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setDataAndType(zipUri, "application/zip");
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            startActivity(intent);
//        } else {
//            Toast.makeText(this, "No se pudo encontrar el archivo ZIP.", Toast.LENGTH_SHORT).show();
//        }
//    }











}
