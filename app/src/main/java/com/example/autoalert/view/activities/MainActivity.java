package com.example.autoalert.view.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.autoalert.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity{

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private TextView ipTextView;
    private TextView myIpTextView;
    private BroadcastSender broadcastSender;
    private BroadcastReceiver broadcastReceiver;
    private MessageSender messageSender;
    private MessageReceiver messageReceiver;
    public Set<String> ipList = new HashSet<>();
    private HashMap<String, String> ipMessageMap;
    private TextView ipMessageTextView;
    private Button btnYes;
    private Button btnNo;
    private TextView responseTextView;
    private Button btnCreacionRed;
    private BroadcastTimer broadcastTimer;
    private TextView resultadoTextView;
    private int cont = 0;
    private WifiManager wifiManager;
    private NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private WifiP2pInfo wifiP2pInfo;
    //private EditText aliasEditText;
    public HashMap<String, String> ipAliasMap = new HashMap<>();
    private SistemaVotacion sistemaVotacion;
    private NetworkUtils networkUtils;
    private HashMap<String, String> ipTimestamp = new HashMap<>();

    private WifiHotspot hotspotManager;

    private ArchiveUtils archiveUtils;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "Inicio de componentes visuales.");

        Button btnSendMessages = findViewById(R.id.btnSendMessages);
        ipTextView = findViewById(R.id.ipTextView);
        Button btnSendBroadcast = findViewById(R.id.btnSendBroadcast);
        ipMessageTextView = findViewById(R.id.ipMessageTextView);
        myIpTextView = findViewById(R.id.myIpTextView);
        btnYes = findViewById(R.id.btnYes);
        btnNo = findViewById(R.id.btnNo);
        responseTextView = findViewById(R.id.responseTextView);
        btnCreacionRed = findViewById(R.id.creacionRedbutton);
        resultadoTextView = findViewById(R.id.resultadoTextView);
        //aliasEditText = findViewById(R.id.aliasEditText);
        Log.i("MainActivity", "Componentes inicializados.");

        // Inicializamos wifiManager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Registro del BroadcastReceiver dinámicamente
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);


        ///////////////////////
        archiveUtils = new ArchiveUtils(this);

        // Inicializar los componentes
        Log.i("MainActivity", "Inicio de hilos");
        broadcastSender = new BroadcastSender();
        broadcastReceiver = new BroadcastReceiver(this);
        messageSender = new MessageSender();
        broadcastTimer = new BroadcastTimer();
        networkUtils = new NetworkUtils();
        sistemaVotacion = new SistemaVotacion(this);
        ipMessageMap = new HashMap<>();
        messageReceiver = new MessageReceiver(this);
        Log.i("MainActivity", "Inicio de Gestionador de Red.");
        // Iniciar la recepción de broadcasts y respuestas
        Log.i("MainActivity", "Escuchando mensajes de broadcast.");
        broadcastReceiver.startListening();
        broadcastTimer.startBroadcastTimer();
        messageReceiver.startListening();

        // Verificar y solicitar permisos necesarios
        Log.i("MainActivity", "Verificando permisos....");
        checkPermissions();

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);

        archiveUtils.crearOReiniciarArchivoIps();
        archiveUtils.crearOReiniciarArchivo("lista-ip");
        archiveUtils.crearOReiniciarArchivo("lista-ip-message");

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

        btnSendMessages.setOnClickListener(view -> {
                Log.i("Envio de mensaje", "ESTOY POR ENVIAR UN MENSAJE. PERO UNO NOMAS");
                enviarMensaje();
                // Una vez que el mensaje se haya enviado, reiniciar el estado
            if (responseTextView.getText().equals("SI") && ipList.isEmpty()){
                sistemaVotacion.iniciarConteo();
            }
        });

        // Obtener y mostrar la IP del dispositivo
        Log.i("MainActivity", "Mostrando IP....");
        String deviceIpAddress = networkUtils.getDeviceIpAddress();
        ipTextView.setText("Lista de IPs" + deviceIpAddress);

        String myDeviceIpAddress = networkUtils.getDeviceIpAddress();
        myIpTextView.setText("Mi IP: " + myDeviceIpAddress);


    }


    public void irACrecionRed(View view){
        Intent i = new Intent(this, CreacionRedActivity.class);
        startActivity(i);
    }


    public void enviarMensaje(){
        String message = responseTextView.getText().toString();

        Log.i("Enviar Mensaje", "Enviando mensaje.");

        Set<String> ipListArchivo = archiveUtils.leerListaIpsEnArchivo();
        // Supongamos que quieres enviar el mensaje a la primera IP de la lista
        if (!ipListArchivo.isEmpty()) {
            //String targetIp = ipList.get(0); // Usar la IP que quieras de la lista
            for(String targetIp : ipListArchivo) {
                messageSender.sendMessage(targetIp, message);
                Log.i("Envio de mensaje", "Mensaje enviado a: " + targetIp + " con " + message);
                Toast.makeText(MainActivity.this, "Mensaje enviado a: " + targetIp, Toast.LENGTH_SHORT).show();

            }

            if(responseTextView.getText().equals("SI")){
                sistemaVotacion.enviarEstado();
            }
        } else {
            Log.e("Envio de mensaje", "No hay IPs disponibles para enviar el mensaje.");
            Toast.makeText(this, "No hay IPs disponibles para enviar el mensaje", Toast.LENGTH_SHORT).show();
        }
    }


    // Método para actualizar la lista de IPs en el TextView
    public void updateIpList(String ip) {
        Log.i("Actualizacion de lista", "Actualizando lista de IP's.");

//        // Comprobar si la IP ya está en la lista
//        if (!ipList.contains(ip)) {
//            Log.i("Actualizacion de lista", "IP agregada: " + ip);
//            ipList.add(ip);  // Agregar IP a la lista interna
//
//            // Actualizar el TextView con todas las IPs acumuladas
//            runOnUiThread(() -> {
//                StringBuilder ips = new StringBuilder("IPs recibidas:\n");
//                for (String savedIp : ipList) {
//                    ips.append(savedIp).append("\n");
//                    Log.i("Lista de IPs", "IP guardada: " + savedIp); // Log para cada IP
//
//                }
//                ipTextView.setText(ips.toString());
//                Log.i("Actualizacion de lista", "Lista actualizada en pantalla.");
//            });
//        }

        Set<String> ipListArchivo = archiveUtils.leerListaIpsEnArchivo();

        // Actualizar el TextView con todas las IPs acumuladas
        runOnUiThread(() -> {
            StringBuilder ips = new StringBuilder("IPs recibidas:\n");
            for (String savedIp : ipListArchivo) {
                ips.append(savedIp).append("\n");
                Log.i("Lista de IPs", "IP guardada: " + savedIp); // Log para cada IP

            }
            ipTextView.setText(ips.toString());
            Log.i("Actualizacion de lista", "Lista actualizada en pantalla.");
        });
    }


    public void setMyIpTextView(String newIp) {
        myIpTextView.setText(newIp);
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

    public HashMap<String, String> getIpMessageMap(){
        return ipMessageMap;
    }

    public void sumarContador(){
        cont++;
    }

    public int getContador(){
        return cont;
    }

    public void reiniciarContador(){
        cont = 0;
    }

    public String getResponseText(){
        return responseTextView.getText().toString();
    }

    public Set<String> getIpList(){
        return ipList;
    }

    public void setResultadoText(String message){
        StringBuilder displayText = new StringBuilder("Mensajes recibidos:\n");
        displayText.append(message).append("\n");
        resultadoTextView.setText(message);
    }

    public void actualizarIpTimeStamp(String senderIp, String timestamp) {
        ipTimestamp.put(senderIp, timestamp);
        Log.d("Actualizar Timestamp", "Se actualiza la ip con timestamp con ip: " + senderIp + " y " + timestamp);
        for(Map.Entry<String, String> disp : ipTimestamp.entrySet()){
            Log.d("Actualizar Timestamp", "IP" + disp.getKey() + " y " + disp.getValue());
        }
    }

    public void verificarConexion() throws ParseException {
        Log.i("Verificacion Conexion", "Verificando conexion de dispositivos");
        if(ipTimestamp.size() == 0){
            Log.d("Verificacion Conexion", "EH AMIGO ME RE FUI. TA VACIO ACA");
            return;
        }

        for(Map.Entry<String, String> dispositivo : ipTimestamp.entrySet()){
            long diferenciaTiempo = calcularDiferenciaTiempo(dispositivo.getValue());
            Log.i("Verificacion Conexion", "Verificando conexion de: " + dispositivo.getKey());
            Log.i("Verificacion Conexion", "La diferencia de tiempo es de: " + diferenciaTiempo);
            if(diferenciaTiempo > 3){
                Log.i("Verificacion Conexion", "El dispositivo " + dispositivo.getKey() + " está DESCONECTADO");
                ipMessageMap.put(dispositivo.getKey(), "DESCONECTADO");
            }
            //TERMINAR DE HACER ESTOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
            //Es el sistema de votacion con tempo
        }
    }


    public long calcularDiferenciaTiempo(String tiempoRecibido) throws ParseException {
        // Formateador para convertir la cadena a un objeto Date
        SimpleDateFormat formato = new SimpleDateFormat("HH:mm:ss");

        // Parsear el tiempo recibido a un objeto Date
        Date tiempoRecibidoDate = formato.parse(tiempoRecibido);

        // Obtener el tiempo actual del sistema
        Calendar ahora = Calendar.getInstance();
        long timestampActual = ahora.getTimeInMillis();

        // Convertir el tiempo recibido a milisegundos desde el comienzo del día
        Calendar tiempoRecibidoCal = Calendar.getInstance();
        tiempoRecibidoCal.setTime(tiempoRecibidoDate);
        long timestampRecibido = tiempoRecibidoCal.getTimeInMillis();

        // Calcular la diferencia en milisegundos y convertirla a segundos
        long diferenciaEnMilisegundos = timestampActual - timestampRecibido;
        long diferenciaEnSegundos = diferenciaEnMilisegundos / 1000;

        Log.i("Diferencia de Tiempo","Diferencia en segundos: " + diferenciaEnSegundos);

        return diferenciaEnSegundos;
    }

    public void agregarIpYActualizarArchivo(String nuevaIp) {
        archiveUtils.agregarIpYActualizarArchivo(nuevaIp);
    }

}
