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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
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
    private EditText passwordEditText;
    private TextView ipTextView;
    private BroadcastSender broadcastSender;
    private ResponseListener responseListener;
    private BroadcastReceiver broadcastReceiver;
    private Handler handler = new Handler(Looper.getMainLooper());
    private MessageSender messageSender;
    public List<String> ipList = new ArrayList<>(); // Lista de IPs obtenidas por broadcast
    private HashMap<String, String> ipMessageMap;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "Inicio de componentes visuales.");

        ssidTextView = findViewById(R.id.ssidTextView);
        passwordTextView = findViewById(R.id.passwordTextView);
        ssidEditText = findViewById(R.id.ssidEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button btnSendMessages = findViewById(R.id.btnSendMessages);
        ipTextView = findViewById(R.id.ipTextView);
        Button btnSendBroadcast = findViewById(R.id.btnSendBroadcast);
        statusTextView = findViewById(R.id.statusTextView);
        toggleHotspotButton = findViewById(R.id.toggleHotspotButton);
        Log.e("MainActivity", "Componentes inicializados.");


        // Inicializar los componentes
        Log.e("MainActivity", "Inicio de hilos");
        broadcastSender = new BroadcastSender();
        broadcastReceiver = new BroadcastReceiver(this);
        messageSender = new MessageSender();
        responseListener = new ResponseListener(this);

        // Inicializamos el HashMap
        ipMessageMap = new HashMap<>();

        Log.e("MainActivity", "Inicio de Gestionador de Red.");
        // Inicializar el administrador del hotspot
        hotspotManager = new WifiHotspot(this, this);

        // Obtener y mostrar la IP del dispositivo
        Log.e("MainActivity", "Mostrando IP....");
        String deviceIpAddress = getDeviceIpAddress();
        ipTextView.setText("Mi IP: " + deviceIpAddress);

        // Iniciar la recepción de broadcasts y respuestas
        Log.e("MainActivity", "Escuchando mensajes de broadcast.");
        broadcastReceiver.startListening();
        //responseListener.listenForResponses();

        // Enviar mensaje de broadcast cuando se haga clic en el botón
        btnSendBroadcast.setOnClickListener(view -> {
            broadcastSender.sendBroadcast();
        });

        // Verificar y solicitar permisos necesarios
        Log.e("MainActivity", "Verificando permisos....");
        checkPermissions();

        MessageReceiver messageReceiver = new MessageReceiver(this);
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

            // Validar la longitud de la contraseña
            if (password.length() < 8 || password.length() > 63) {
                Toast.makeText(MainActivity.this, "La contraseña debe tener entre 8 y 63 caracteres", Toast.LENGTH_LONG).show();
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
        Log.e("Actualizacion de lista", "Actualizando lista de IP's.");
        handler.post(() -> {
            StringBuilder ips = new StringBuilder("IPs recibidas:\n");
            for (String ip : ipList) {
                ips.append(ip).append("\n");
            }
            ipTextView.setText(ips.toString());
            Log.e("Actualizacion de lista", "Lista actualizada.");
        });

    }

    public void storeMessageFromIp(String ip, String message) {
        ipMessageMap.put(ip, message);
        // Actualizar la lista de IPs también, si es necesario
        if (!ipList.contains(ip)) {
            ipList.add(ip);
            updateIpList(ipList); // Actualizar la UI con la nueva lista de IPs
        }

        // Mostrar el mensaje recibido en la interfaz
        runOnUiThread(() -> {
            Toast.makeText(this, "Mensaje recibido de " + ip + ": " + message, Toast.LENGTH_SHORT).show();
        });
    }

    // Método para alternar el estado del Hotspot
    private void toggleHotspot(String ssid, String password) {
        Log.e("Red Hotspot", "Cambiando estado de Hotspot.");
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

        // Obtener y mostrar la IP del dispositivo al crear el hotspot
        String deviceIpAddress = getDeviceIpAddress();
        ipTextView.setText("Mi IP: " + deviceIpAddress);
    }

    private String getDeviceIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "IP no disponible";
    }



}
