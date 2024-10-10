package com.example.autoalert.view.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
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

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity{

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
    private BroadcastReceiver broadcastReceiver;
    private Handler handler = new Handler(Looper.getMainLooper());
    private MessageSender messageSender;
    //public List<String> ipList = new ArrayList<>(); // Lista de IPs obtenidas por broadcast
    public Set<String> ipList = new HashSet<>();
    private HashMap<String, String> ipMessageMap;
    private TextView ipMessageTextView;
    private Button btnYes;
    private Button btnNo;
    private TextView statusBtnTextView;
    private TextView responseTextView;
    private TextView estadoRedTextView;
    private Button btnCreacionRed;
    private BroadcastTimer broadcastTimer;
    private TextView resultadoTextView;
    private int cont = 0;

    private boolean isSendingMessage = false;
    private WifiManager wifiManager;

    private NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();

    private BroadcastReceiver networkReceiver;

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private WifiP2pInfo wifiP2pInfo;

    //private EditText aliasEditText;

    // HashMap para almacenar la asociación de IPs y alias
    public HashMap<String, String> ipAliasMap = new HashMap<>();



    @SuppressLint("MissingInflatedId")
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
        estadoRedTextView = findViewById(R.id.redStatusTextView);
        btnCreacionRed = findViewById(R.id.creacionRedbutton);
        resultadoTextView = findViewById(R.id.resultadoTextView);
        // Inicializa el alias
        //aliasEditText = findViewById(R.id.aliasEditText);
        Log.i("MainActivity", "Componentes inicializados.");

        // Inicializamos wifiManager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Registro del BroadcastReceiver dinámicamente
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);


        // Inicializar los componentes
        Log.i("MainActivity", "Inicio de hilos");
        broadcastSender = new BroadcastSender();
        broadcastReceiver = new BroadcastReceiver(this);
        messageSender = new MessageSender();
        broadcastTimer = new BroadcastTimer();

        // Inicializamos el HashMap
        ipMessageMap = new HashMap<>();

        Log.i("MainActivity", "Inicio de Gestionador de Red.");

        // Obtener y mostrar la IP del dispositivo
        Log.i("MainActivity", "Mostrando IP....");
        String deviceIpAddress = getDeviceIpAddress();
        ipTextView.setText("Lista de IPs" + deviceIpAddress);

        String myDeviceIpAddress = getDeviceIpAddress();
        myIpTextView.setText("Mi IP: " + myDeviceIpAddress);

        // Iniciar la recepción de broadcasts y respuestas
        Log.i("MainActivity", "Escuchando mensajes de broadcast.");
        broadcastReceiver.startListening();

        broadcastTimer.startBroadcastTimer();
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

        btnCreacionRed.setOnClickListener(view -> {
            irACrecionRed(view);
        });

        // Verificar y solicitar permisos necesarios
        Log.i("MainActivity", "Verificando permisos....");
        checkPermissions();

        MessageReceiver messageReceiver = new MessageReceiver(this);
        messageReceiver.startListening();

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);

       /* btnSendMessages.setOnClickListener(view -> {
            if (isConnectedToWifi() || isWifiDirectGroupOwner()){
                if (isSendingMessage) {
                    // Si ya se está enviando un mensaje, no hacemos nada
                    return;
                }
                Log.i("Envio de mensaje", "ESTOY POR ENVIAR UN MENSAJE. PERO UNO NOMAS");
                isSendingMessage = false;
                enviarMensaje();
                isSendingMessage = true;  // Bloquear nuevos envíos


                // Una vez que el mensaje se haya enviado, reiniciar el estado
            } else if (responseTextView.getText().equals("SI")){
                iniciarConteo();
            }
        });

        */

        btnSendMessages.setOnClickListener(view -> {
                Log.i("Envio de mensaje", "ESTOY POR ENVIAR UN MENSAJE. PERO UNO NOMAS");
                enviarMensaje();
                // Una vez que el mensaje se haya enviado, reiniciar el estado
            if (responseTextView.getText().equals("SI") && ipList.isEmpty()){
                iniciarConteo();
            }
        });



    }

    private boolean isWifiDirectGroupOwner() {
        // Suponiendo que tienes acceso al objeto WifiP2pInfo para verificar si este dispositivo es el propietario del grupo
        if (wifiP2pInfo != null) {
            return wifiP2pInfo.isGroupOwner;
        }
        return false;
    }

//    // Guardar la IP y el alias recibido
//    public void storeAliasFromIp(String ip, String alias) {
//        ipAliasMap.put(ip, alias);
//        Log.d("MainActivity", "Guardado: IP " + ip + " con alias " + alias);
//    }
//
//    // Método para obtener el alias
//    public String getAlias() {
//        return aliasEditText.getText().toString().trim(); // Devuelve el alias como un String
//    }

    public void irACrecionRed(View view){
        Intent i = new Intent(this, CreacionRedActivity.class);
        startActivity(i);
    }

    public void eliminarIp(String ip){
        ipList.remove(ip);
        ipMessageMap.remove(ip);
        updateIpMessageView();
    }

    public void enviarMensaje(){
        String message = responseTextView.getText().toString();

        Log.i("Enviar Mensaje", "Enviando mensaje.");

        // Supongamos que quieres enviar el mensaje a la primera IP de la lista
        if (!ipList.isEmpty()) {
            //String targetIp = ipList.get(0); // Usar la IP que quieras de la lista
            for(String targetIp : ipList) {
                messageSender.sendMessage(targetIp, message);
                Log.i("Envio de mensaje", "Mensaje enviado a: " + targetIp + " con " + message);
                Toast.makeText(MainActivity.this, "Mensaje enviado a: " + targetIp, Toast.LENGTH_SHORT).show();

            }

            if(responseTextView.getText().equals("SI")){
                enviarEstado();
            }
        } else {
            Log.e("Envio de mensaje", "No hay IPs disponibles para enviar el mensaje.");
            Toast.makeText(this, "No hay IPs disponibles para enviar el mensaje", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    // Método para actualizar la lista de IPs en el TextView
    public void updateIpList(String ip) {
        Log.i("Actualizacion de lista", "Actualizando lista de IP's.");
        handler.post(() -> {
            StringBuilder ips = new StringBuilder("IPs recibidas:\n");
            if (!ipList.contains(ip)) {
                Log.i("Actualizacion de lista", "IP agregada: " + ip);
                ipList.add(ip);
                ips.append(ip).append("\n");
            }
            //ipTextView.setText(ips.toString());
            runOnUiThread(() -> ipTextView.setText(ips.toString()));
            Log.i("Actualizacion de lista", "Lista actualizada.");
        });

    }
     */

    // Método para actualizar la lista de IPs en el TextView
    public void updateIpList(String ip) {
        Log.i("Actualizacion de lista", "Actualizando lista de IP's.");

        // Comprobar si la IP ya está en la lista
        if (!ipList.contains(ip)) {
            Log.i("Actualizacion de lista", "IP agregada: " + ip);
            ipList.add(ip);  // Agregar IP a la lista interna

            // Actualizar el TextView con todas las IPs acumuladas
            runOnUiThread(() -> {
                StringBuilder ips = new StringBuilder("IPs recibidas:\n");
                for (String savedIp : ipList) {
                    ips.append(savedIp).append("\n");
                    Log.i("Lista de IPs", "IP guardada: " + savedIp); // Log para cada IP

                }
                ipTextView.setText(ips.toString());
                Log.i("Actualizacion de lista", "Lista actualizada en pantalla.");
            });
        }
    }


    public void setMyIpTextView(String newIp) {
        myIpTextView.setText(newIp);
    }

    public Button getBtnCreacionRed(){
        return btnCreacionRed;
    }

    public void limpiarListasIp(){
        Log.i("Estado de Red", "Limpiando listas.");
        StringBuilder displayText = new StringBuilder("Mensajes recibidos:\n");
        // Actualizar el TextView en el hilo de la UI
        runOnUiThread(() -> ipMessageTextView.setText(displayText.toString()));
        StringBuilder displayTextips = new StringBuilder("IP's recibidas:\n");
        // Actualizar el TextView en el hilo de la UI
        runOnUiThread(() -> ipMessageTextView.setText(displayTextips.toString()));
        //ipList.clear();
        ipMessageMap.clear();
        Log.i("Estado de Red", "Listas limpiadas");
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
        Log.i("Actualizar IP-Mensaje", "Se actualiza lista de mensajes.");

        StringBuilder displayText = new StringBuilder("Mensajes recibidos:\n");
        for (String ip : ipMessageMap.keySet()) {
            displayText.append("IP: ").append(ip).append(" - Mensaje: ").append(ipMessageMap.get(ip)).append("\n");
        }

        // Actualizar el TextView en el hilo de la UI
        runOnUiThread(() -> ipMessageTextView.setText(displayText.toString()));
    }


    public void guardarVoto(String ip, String voto){
        Log.i("Guardado de votos", "Se inicia el guardado de votos.");
        String[] votoArray = voto.split(":");
        String resultadoVoto = votoArray[1];
        Log.i("Guardado de votos", "Se obtiene voto: " + resultadoVoto);

        ipMessageMap.put(ip, resultadoVoto);
        // Mostrar el mensaje recibido en la interfaz
        // Actualizar la interfaz con el contenido del HashMap
        updateIpMessageView();
        runOnUiThread(() -> {
            Toast.makeText(this, "Mensaje recibido de " + ip + ": " + resultadoVoto, Toast.LENGTH_SHORT).show();
        });
        cont = cont + 1;
        Log.i("Guardado de votos", "El contador esta en " + cont);

        if(ipList.size() == cont) {
            Log.i("Recoleccion de estados", "Se obtuvieron los estados de todos los dispositivos. Se inicia el conteo de votos.");
            iniciarConteo();
            cont = 0;
        }
    }

    public void iniciarConteo(){
        int contPositivo = 0;
        int contNegativo = 0;
        StringBuilder displayText = new StringBuilder("Mensajes recibidos:\n");
        Log.i("Conteo de votos", "Conteo de votos iniciado.");

        for (String ip : ipMessageMap.keySet()) {
            String voto = ipMessageMap.get(ip);
            if ("SI".equals(voto)) {
                contPositivo++;
                Log.i("Conteo de votos", "VOTO:SI");
            } else if ("NO".equals(voto)) {
                contNegativo++;
                Log.i("Conteo de votos", "VOTO:NO");
            }
        }

        Log.i("Conteo de votos", "Añadiendo voto del propio dispositivo.");
        if(responseTextView.getText().equals("SI")){
            Log.i("Conteo de votos", "Se añade un VOTO:SI");
            contPositivo++;
        } else {
            Log.i("Conteo de votos", "Se añade un VOTO:NO");
            contNegativo++;
        }

        // Mostrar resultado basado en la cantidad de votos
        if (contPositivo >= contNegativo) {
            Log.i("Conteo de votos", "Hay Accidente");
            displayText.append("Resultado Votación: HAY ACCIDENTE").append("\n");
            resultadoTextView.setText("Resultado Votación: HAY ACCIDENTE");
        } else {
            Log.i("Conteo de votos", "No hubo accidente");
            displayText.append("Resultado Votación: NO HUBO ACCIDENTE").append("\n");
            resultadoTextView.setText("Resultado Votación: NO HUBO ACCIDENTE");
        }
    }

    public void enviarEstado(){
        String message;
        Log.i("Envio de Estado", "Enviando estado");
        if(responseTextView.getText().equals("SI")){
            message = "VOTO:SI";
            Log.i("Envio de Estado", "Enviando mensaje: VOTO:SI");

        } else {
            message = "VOTO:NO";
            Log.i("Envio de Estado", "Enviando mensaje: VOTO:NO");

        }



        // Supongamos que quieres enviar el mensaje a la primera IP de la lista
        if (!ipList.isEmpty()) {
            //String targetIp = ipList.get(0); // Usar la IP que quieras de la lista
            for(String targetIp : ipList) {
                messageSender.sendMessage(targetIp, message);
                Log.i("Envio de Estado", "Enviando mensaje a " + targetIp + " con: VOTO:NO");
            }
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
                Log.i("PermissionSuccesfull", "Permiso de ubicación habilitados. Se puede iniciar el hotspot.");
            } else {
                // Permisos no concedidos, manejar el caso según tu lógica
                Log.e("PermissionError", "Permiso de ubicación denegado. No se puede iniciar el hotspot.");
            }
        }
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


    public boolean isConnectedToWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return networkInfo != null && networkInfo.isConnected();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Detener el hotspot al cerrar la aplicación
        hotspotManager.stopHotspot();

        CreacionRedActivity creacionRedActivity = new CreacionRedActivity();
        creacionRedActivity.stopWifiDirectHotspot();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeReceiver, filter);
    }

}
