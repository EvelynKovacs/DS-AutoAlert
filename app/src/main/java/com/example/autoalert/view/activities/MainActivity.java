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
    private TextView myIpTextView;
    private BroadcastSender broadcastSender;
    private ResponseListener responseListener;
    private BroadcastReceiver broadcastReceiver;
    private Handler handler = new Handler(Looper.getMainLooper());
    private MessageSender messageSender;
    public List<String> ipList = new ArrayList<>(); // Lista de IPs obtenidas por broadcast
    private HashMap<String, String> ipMessageMap;
    private TextView ipMessageTextView;
    private Button btnYes;
    private Button btnNo;
    private TextView statusBtnTextView;
    private TextView responseTextView;

    private int cont = 0;
    //private int contPositivo = 0;
    //private int contNegativo = 0;




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
        ipMessageTextView = findViewById(R.id.ipMessageTextView);
        myIpTextView = findViewById(R.id.myIpTextView);
        btnYes = findViewById(R.id.btnYes);
        btnNo = findViewById(R.id.btnNo);
        statusBtnTextView = findViewById(R.id.statusBtnTextView);
        responseTextView = findViewById(R.id.responseTextView);
        Log.e("MainActivity", "Componentes inicializados.");


        // Inicializar los componentes
        Log.e("MainActivity", "Inicio de hilos");
        broadcastSender = new BroadcastSender();
        broadcastReceiver = new BroadcastReceiver(this);
        messageSender = new MessageSender();
        //responseListener = new ResponseListener(this);

        // Inicializamos el HashMap
        ipMessageMap = new HashMap<>();

        Log.e("MainActivity", "Inicio de Gestionador de Red.");
        // Inicializar el administrador del hotspot
        hotspotManager = new WifiHotspot(this, this);

        // Obtener y mostrar la IP del dispositivo
        Log.e("MainActivity", "Mostrando IP....");
        String deviceIpAddress = getDeviceIpAddress();
        ipTextView.setText("Mi IP: " + deviceIpAddress);

        String myDeviceIpAddress = getDeviceIpAddress();
        myIpTextView.setText("Mi IP: " + myDeviceIpAddress);

        // Iniciar la recepción de broadcasts y respuestas
        Log.e("MainActivity", "Escuchando mensajes de broadcast.");
        broadcastReceiver.startListening();
        //responseListener.listenForResponses();

        // Enviar mensaje de broadcast cuando se haga clic en el botón
        btnSendBroadcast.setOnClickListener(view -> {
            broadcastSender.sendBroadcast();
        });

        btnYes.setOnClickListener(view -> {
            setStatusTextViewOnYes();
        });

        btnNo.setOnClickListener(view -> {
            setStatusTextViewOnNo();
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
            String message = responseTextView.getText().toString();
            int port = 12345; // Puedes definir el puerto a utilizar
            Log.e("MainActivity", "Enviando mensajes.");

            // Supongamos que quieres enviar el mensaje a la primera IP de la lista
            if (!ipList.isEmpty()) {
                //String targetIp = ipList.get(0); // Usar la IP que quieras de la lista
                for(String targetIp : ipList) {
                    messageSender.sendMessage(targetIp, port, message);
                    Log.i("Envio de mensaje", "Mensaje enviado a: " + targetIp);
                    Toast.makeText(MainActivity.this, "Mensaje enviado a: " + targetIp, Toast.LENGTH_SHORT).show();

                }
            } else {
                Log.e("Envio de mensaje", "No hay IPs disponibles para enviar el mensaje.");
                Toast.makeText(MainActivity.this, "No hay IPs disponibles para enviar el mensaje", Toast.LENGTH_SHORT).show();
            }
        });


    }

    // Método para actualizar la lista de IPs en el TextView
    public void updateIpList(String ip) {
        Log.e("Actualizacion de lista", "Actualizando lista de IP's.");
        handler.post(() -> {
            StringBuilder ips = new StringBuilder("IPs recibidas:\n");
            if (!ipList.contains(ip)) {
                Log.e("Actualizacion de lista", "IP agregada: " + ip);
                ipList.add(ip);
                ips.append(ip).append("\n");
            }
            ipTextView.setText(ips.toString());
            Log.e("Actualizacion de lista", "Lista actualizada.");
        });

    }

    public void setStatusTextViewOnYes() {
        responseTextView.setText("SI");
    }


    public void setStatusTextViewOnNo() {
        responseTextView.setText("NO");
    }

    public void storeMessageFromIp(String ip, String message) {
        ipMessageMap.put(ip, message);
        // Mostrar el mensaje recibido en la interfaz
        // Actualizar la interfaz con el contenido del HashMap
        updateIpMessageView();
        runOnUiThread(() -> {
            Toast.makeText(this, "Mensaje recibido de " + ip + ": " + message, Toast.LENGTH_SHORT).show();
        });
    }

    // Método para actualizar el TextView con el contenido del HashMap
    private void updateIpMessageView() {
        Log.e("Actualizar IP-Mensaje", "Se actualiza lista de mensajes.");

        StringBuilder displayText = new StringBuilder("Mensajes recibidos:\n");
        for (String ip : ipMessageMap.keySet()) {
            displayText.append("IP: ").append(ip).append(" - Mensaje: ").append(ipMessageMap.get(ip)).append("\n");
        }

        // Actualizar el TextView en el hilo de la UI
        runOnUiThread(() -> ipMessageTextView.setText(displayText.toString()));
    }


    public void guardarVoto(String ip, String voto){
        Log.e("Guardado de votos", "Se inicia el guardado de votos.");
        String[] votoArray = voto.split(":");
        String resultadoVoto = votoArray[1];
        Log.e("Guardado de votos", "Se obtiene voto: " + resultadoVoto);

        ipMessageMap.put(ip, resultadoVoto);
        // Mostrar el mensaje recibido en la interfaz
        // Actualizar la interfaz con el contenido del HashMap
        updateIpMessageView();
        runOnUiThread(() -> {
            Toast.makeText(this, "Mensaje recibido de " + ip + ": " + resultadoVoto, Toast.LENGTH_SHORT).show();
        });
        Log.e("Guardado de votos", "El contador esta en " + cont);

        cont = cont + 1;
        if(ipList.size() == cont) {
            Log.e("Recoleccion de estados", "Se obtuvieron los estados de todos los dispositivos. Se inicia el conteo de votos.");
            iniciarConteo();
            cont = 0;
        }
    }

    public void iniciarConteo(){
        int contPositivo = 0;
        int contNegativo = 0;
        StringBuilder displayText = new StringBuilder("Mensajes recibidos:\n");
        Log.e("Conteo de votos", "Conteo de votos iniciado.");

        for (String ip : ipMessageMap.keySet()) {
            String voto = ipMessageMap.get(ip);
            if ("SI".equals(voto)) {
                contPositivo++;
                Log.e("Conteo de votos", "VOTO:SI");
            } else if ("NO".equals(voto)) {
                contNegativo++;
                Log.e("Conteo de votos", "VOTO:NO");
            }
        }

        // Mostrar resultado basado en la cantidad de votos
        if (contPositivo >= contNegativo) {
            Log.e("Conteo de votos", "Hay Accidente");
            displayText.append("ACCIDENTE").append("\n");
            ipMessageTextView.setText(displayText.toString());
        } else {
            Log.e("Conteo de votos", "No hubo accidente");

            ipMessageTextView.setText("NO HUBO ACCIDENTE");
        }
    }

    public void enviarEstado(){
        String message;
        Log.e("Envio de Estado", "Enviando estado");
        if(responseTextView.getText() == "SI"){
            message = "VOTO:SI";
            Log.e("Envio de Estado", "Enviando mensaje: VOTO:SI");

        } else {
            message = "VOTO:NO";
            Log.e("Envio de Estado", "Enviando mensaje: VOTO:NO");

        }

        int port = 12345; // Puedes definir el puerto a utilizar

        // Supongamos que quieres enviar el mensaje a la primera IP de la lista
        if (!ipList.isEmpty()) {
            //String targetIp = ipList.get(0); // Usar la IP que quieras de la lista
            for(String targetIp : ipList) {
                messageSender.sendMessage(targetIp, port, message);
                //Toast.makeText(MainActivity.this, "Mensaje enviado a: " + targetIp, Toast.LENGTH_SHORT).show();

            }
        }
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
        //ipTextView.setText("Mi IP: " + deviceIpAddress);

        myIpTextView.setText("Mi IP: " + deviceIpAddress);
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
