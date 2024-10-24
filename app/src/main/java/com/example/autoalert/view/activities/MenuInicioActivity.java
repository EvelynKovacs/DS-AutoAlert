package com.example.autoalert.view.activities;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;


import com.example.autoalert.R;
import com.example.autoalert.utils.LocationWorker;
import com.example.autoalert.view.fragments.PantallaBienvenidaFragment;
import com.example.autoalert.view.fragments.PasosASeguirFragment;
import com.example.autoalert.view.fragments.PrincipalFragment;

import java.util.concurrent.TimeUnit;

public class MenuInicioActivity extends AppCompatActivity implements PantallaBienvenidaFragment.OnCompleteListener {

    private static final String PREFS_NAME = "AppPreferences";
    private static final String FIRST_TIME_KEY = "isFirstTime";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        // Solicitar los permisos al iniciar la actividad
        checkPermissions();


        // Inicia el WorkManager para ejecutar LocationWorker cada 30 segundos
        startPeriodicLocationWorker();

        // Inicia el WorkManager para ejecutar LocationWorker cada 3 minutos
        //startLocationWorker();

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean(FIRST_TIME_KEY, true);
        Log.d("MenuInicioActivity", "isFirstTime: " + isFirstTime);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setReorderingAllowed(true);

            if (isFirstTime) {
                PantallaBienvenidaFragment bienvenidaFragment = new PantallaBienvenidaFragment();
                bienvenidaFragment.setOnCompleteListener(this);
                transaction.add(R.id.fcv_main_container, bienvenidaFragment);
            } else {
                transaction.add(R.id.fcv_main_container, new PrincipalFragment());
            }

            transaction.commit();
        }
    }

    // Método para verificar permisos en tiempo de ejecución
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_WIFI_STATE,
                                Manifest.permission.CHANGE_WIFI_STATE,
                                Manifest.permission.ACCESS_NETWORK_STATE,
                                Manifest.permission.CHANGE_NETWORK_STATE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        }, 1);
            }
        }
    }

//    private void startLocationWorker() {
//        // Crear las restricciones necesarias (sin requerir red)
//        Constraints constraints = new Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // No requiere red para funcionar
//                .build();
//
//        // Crear una solicitud de trabajo único
//        OneTimeWorkRequest locationWorkRequest = new OneTimeWorkRequest.Builder(LocationWorker.class)
//                .setConstraints(constraints)
//                .build();
//
//        // Encolar el trabajo
//        WorkManager.getInstance(this).enqueue(locationWorkRequest);
//
//        // Mostrar un Toast para confirmar la ejecución
//        Toast.makeText(this, "Ubicación guardada. Se volverá a guardar en 30 segundos.", Toast.LENGTH_SHORT).show();
//        Log.d("GuardaLocation","Se guardo en 30 vuelvo");
//    }

    private void startPeriodicLocationWorker() {
        // Crear las restricciones necesarias (sin requerir red)
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // No requiere red para funcionar
                .build();

        // Crear una solicitud de trabajo periódico (cada 30 segundos)
        PeriodicWorkRequest locationWorkRequest = new PeriodicWorkRequest.Builder(LocationWorker.class, 30, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .build();

        // Encolar el trabajo periódico con una política de reemplazo en caso de que ya exista uno
        WorkManager workManager = WorkManager.getInstance(this);
        workManager.enqueueUniquePeriodicWork("LocationWorker",
                ExistingPeriodicWorkPolicy.REPLACE,
                locationWorkRequest);

        // Mostrar un Toast para confirmar la ejecución
        Toast.makeText(this, "Trabajo periódico programado para cada 30 segundos.", Toast.LENGTH_SHORT).show();
        Log.d("PeriodicLocationWorker", "Trabajo periódico programado para ejecutarse cada 30 segundos.");
    }




    @Override
    public void onComplete() {
        markFirstTimeCompleted();
    }

    public void markFirstTimeCompleted() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(FIRST_TIME_KEY, false);
        editor.apply();
    }
}
