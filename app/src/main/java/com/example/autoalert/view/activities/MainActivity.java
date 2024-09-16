package com.example.autoalert.view.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements WifiHotspot.HotspotListener{

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private WifiHotspot hotspotManager;
    private TextView statusTextView;
    private Button toggleHotspotButton;
    private boolean isHotspotActive = false;
    private TextView ssidTextView;
    private TextView passwordTextView;
    private EditText ssidEditText;
    private WifiHotspot wifiHotspotManager;
    private EditText passwordEditText;

    private TextView ipTextView;
    private BroadcastSender broadcastSender;
    private ResponseListener responseListener;
    private BroadcastReceiver broadcastReceiver;

    private Handler handler = new Handler(Looper.getMainLooper());

    private MessageSender messageSender;

    public List<String> ipList = new ArrayList<>(); // Lista de IPs obtenidas por broadcast




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ssidTextView = findViewById(R.id.ssidTextView);
        passwordTextView = findViewById(R.id.passwordTextView);
        ssidEditText = findViewById(R.id.ssidEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button btnSendMessages = findViewById(R.id.btnSendMessages);

        ipTextView = findViewById(R.id.ipTextView);
        Button btnSendBroadcast = findViewById(R.id.btnSendBroadcast);

        // Inicializar los componentes
        broadcastSender = new BroadcastSender();

        broadcastReceiver = new BroadcastReceiver();



        messageSender = new MessageSender();
        responseListener = new ResponseListener(this);


        // Inicializar el administrador del hotspot
        hotspotManager = new WifiHotspot(this, this);

        // Enlazar las vistas de la interfaz de usuario
        statusTextView = findViewById(R.id.statusTextView);
        toggleHotspotButton = findViewById(R.id.toggleHotspotButton);

        // Verificar y solicitar permisos necesarios
        checkPermissions();

        MessageReceiver messageReceiver = new MessageReceiver();
        int listenPort = 12345; // Puerto donde escuchar los mensajes
        messageReceiver.startListening(listenPort);

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
                // Verificar si el sistema operativo es Android 10 o superior
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Activar el Hotspot (Wi-Fi Direct) con el SSID y la contraseña ingresados por el usuario
                    toggleHotspot(ssid, password);
                } else {
                    // Mostrar mensaje de error si la versión es inferior a Android 10
                    Toast.makeText(MainActivity.this, "Wi-Fi Direct Hotspot requiere Android 10 o superior", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Iniciar la recepción de broadcasts y respuestas
        broadcastReceiver.startListening();
        responseListener.listenForResponses();

        // Enviar mensaje de broadcast cuando se haga clic en el botón
        btnSendBroadcast.setOnClickListener(view -> {
            broadcastSender.sendBroadcast();
        });

        // Escuchar las respuestas de broadcast y actualizar la lista de IPs
        responseListener.listenForResponses();

        btnSendMessages.setOnClickListener(view -> {
            String message = "Este es un mensaje predefinido";
            int port = 12345; // Puedes definir el puerto a utilizar

            // Supongamos que quieres enviar el mensaje a la primera IP de la lista
            if (!ipList.isEmpty()) {
                String targetIp = ipList.get(0); // Usar la IP que quieras de la lista
                messageSender.sendMessage(targetIp, port, message);
                Toast.makeText(MainActivity.this, "Mensaje enviado a: " + targetIp, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "No hay IPs disponibles para enviar el mensaje", Toast.LENGTH_SHORT).show();
            }
        });


    }

    // Método para actualizar la lista de IPs en el TextView
    public void updateIpList(List<String> ipList) {
        handler.post(() -> {
            StringBuilder ips = new StringBuilder("IPs recibidas:\n");
            for (String ip : ipList) {
                ips.append(ip).append("\n");
            }
            ipTextView.setText(ips.toString());
        });
    }

    // Método para alternar el estado del Hotspot
    private void toggleHotspot(String ssid, String password) {
        if (!isHotspotActive) {
            // Iniciar el Hotspot con Wi-Fi Direct
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                hotspotManager.startWifiDirectHotspot(ssid, password);
            }
        } else {
            // Detener el Hotspot
            hotspotManager.stopHotspot();
        }

        isHotspotActive = !isHotspotActive; // Cambiar el estado
        String statusMessage = isHotspotActive ? "Hotspot activado" : "Hotspot desactivado";

        // Mostrar un mensaje de éxito o error
        Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show();

        // Actualizar la interfaz
        statusTextView.setText(statusMessage);
        toggleHotspotButton.setText(isHotspotActive ? "Desactivar Hotspot" : "Activar Hotspot");

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
