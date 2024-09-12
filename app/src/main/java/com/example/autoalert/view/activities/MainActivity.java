package com.example.autoalert.view.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.autoalert.R;

public class MainActivity extends AppCompatActivity implements WifiHotspot.HotspotListener{

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private WifiHotspot hotspotManager;
    private TextView statusTextView;
    private Button toggleHotspotButton;
    private boolean isHotspotActive = false;
    private TextView ssidTextView;
    private TextView passwordTextView;
    private EditText ssidEditText;
    private EditText passwordEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ssidTextView = findViewById(R.id.ssidTextView);
        passwordTextView = findViewById(R.id.passwordTextView);
        ssidEditText = findViewById(R.id.ssidEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        // Inicializar el administrador del hotspot
        hotspotManager = new WifiHotspot(this, this);

        // Enlazar las vistas de la interfaz de usuario
        statusTextView = findViewById(R.id.statusTextView);
        toggleHotspotButton = findViewById(R.id.toggleHotspotButton);

        // Verificar y solicitar permisos necesarios
        checkPermissions();


        // Configuramos el botón para activar el hotspot
        toggleHotspotButton.setOnClickListener(view -> {
            String ssid = ssidEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            // Validar que el usuario haya ingresado el SSID y la contraseña
            if (ssid.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Ingrese un SSID y una contraseña", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!ssid.isEmpty() && !password.isEmpty()) {
                // Activar el Hotspot con el SSID y la contraseña ingresados por el usuario
                toggleHotspot(ssid, password);
            }
        });
    }

    // Método para alternar el estado del Hotspot
    private void toggleHotspot(String ssid, String password) {
        boolean success = hotspotManager.setWifiApEnabled(ssid, password, !isHotspotActive);

        if (success) {
            isHotspotActive = !isHotspotActive; // Cambiar el estado
            String statusMessage = isHotspotActive ? "Hotspot activado sin contraseña" : "Hotspot desactivado";

            // Mostrar un mensaje de éxito
            Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show();

            // Actualizar la interfaz
            statusTextView.setText(statusMessage);
            toggleHotspotButton.setText(isHotspotActive ? "Desactivar Hotspot" : "Activar Hotspot");


        } else {
            // Mostrar un mensaje de error
            Toast.makeText(this, "Error al cambiar el estado del Hotspot", Toast.LENGTH_SHORT).show();

            // Actualizar la interfaz con el error
            statusTextView.setText("Error al cambiar el estado del Hotspot");
        }
    }

    // Método para verificar permisos en tiempo de ejecución
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_WIFI_STATE,
                                Manifest.permission.CHANGE_WIFI_STATE,
                                Manifest.permission.ACCESS_NETWORK_STATE,
                                Manifest.permission.CHANGE_NETWORK_STATE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
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
                // Permisos concedidos, puedes iniciar el hotspot
                Log.e("PermissionSuccesfull", "Permiso de ubicación habilitados. Se puede iniciar el hotspot.");
            } else {
                // Permisos no concedidos, manejar el caso según tu lógica
                Log.e("PermissionError", "Permiso de ubicación denegado. No se puede iniciar el hotspot.");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onHotspotStarted(String ssid, String password) {
        // Mostrar SSID y contraseña en los TextViews
        ssidTextView.setText("SSID: " + ssid);
        passwordTextView.setText("Contraseña: " + password);
    }
}
