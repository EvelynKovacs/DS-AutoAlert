package com.example.autoalert.view.fragments;


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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.autoalert.R;
import com.example.autoalert.view.activities.BroadcastReceiver;
import com.example.autoalert.view.activities.BroadcastSender;
import com.example.autoalert.view.activities.BroadcastTimer;
import com.example.autoalert.view.activities.CreacionRedActivity;
import com.example.autoalert.view.activities.FileUtils;
import com.example.autoalert.view.activities.MessageReceiver;
import com.example.autoalert.view.activities.MessageSender;
import com.example.autoalert.view.activities.NetworkChangeReceiver;
import com.example.autoalert.view.activities.NetworkUtils;
import com.example.autoalert.view.activities.SistemaVotacion;
import com.example.autoalert.view.activities.WifiHotspot;
import com.example.autoalert.view.activities.MainActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConexionFragment extends Fragment {

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
    //private NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
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
    MainActivity mainActivity;

    private WifiHotspot hotspotManager;

    private FileUtils fileUtils;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mainActivity = (MainActivity) requireActivity();

        Button btnSendMessages = rootView.findViewById(R.id.btnSendMessages);
        ipTextView = rootView.findViewById(R.id.ipTextView);
        Button btnSendBroadcast = rootView.findViewById(R.id.btnSendBroadcast);
        ipMessageTextView = rootView.findViewById(R.id.ipMessageTextView);
        myIpTextView = rootView.findViewById(R.id.myIpTextView);
        btnYes = rootView.findViewById(R.id.btnYes);
        btnNo = rootView.findViewById(R.id.btnNo);
        responseTextView = rootView.findViewById(R.id.responseTextView);
        btnCreacionRed = rootView.findViewById(R.id.creacionRedbutton);
        resultadoTextView = rootView.findViewById(R.id.resultadoTextView);
        //aliasEditText = findViewById(R.id.aliasEditText);

        networkUtils = new NetworkUtils();

//        // Inicializamos wifiManager
//        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

//        // Registro del BroadcastReceiver dinámicamente
//        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//        registerReceiver(networkChangeReceiver, filter);


//        ///////////////////////
//        fileUtils = new FileUtils(this);
//
//        // Inicializar los componentes
//        Log.i("MainActivity", "Inicio de hilos");
//        broadcastSender = new BroadcastSender();
//        broadcastReceiver = new BroadcastReceiver(this);
//        messageSender = new MessageSender();
//        broadcastTimer = new BroadcastTimer(this);
//        networkUtils = new NetworkUtils();
//        sistemaVotacion = new SistemaVotacion(this);
//        ipMessageMap = new HashMap<>();
//        messageReceiver = new MessageReceiver(this);
//        Log.i("MainActivity", "Inicio de Gestionador de Red.");
//        // Iniciar la recepción de broadcasts y respuestas
//        Log.i("MainActivity", "Escuchando mensajes de broadcast.");
//        broadcastReceiver.startListening();
//        broadcastTimer.startBroadcastTimer();
//        messageReceiver.startListening();
//
//        // Verificar y solicitar permisos necesarios
//        Log.i("MainActivity", "Verificando permisos....");
//        checkPermissions();
//
//        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
//        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
//
//        //fileUtils.crearOReiniciarArchivoIps();
//        fileUtils.crearOReiniciarArchivo("lista-ip");
//        fileUtils.crearOReiniciarArchivo("map-ip-message");
//        fileUtils.crearOReiniciarArchivo("map-ip-voto");
//        fileUtils.crearOReiniciarArchivo("map-ip-timestamp");
//        fileUtils.crearOReiniciarArchivo("map-conf-red");
//        fileUtils.crearOReiniciarArchivo("state");

        // Enviar mensaje de broadcast cuando se haga clic en el botón
        btnSendBroadcast.setOnClickListener(view -> {
            this.mainActivity.sendBroadcast();
        });

        btnYes.setOnClickListener(view -> {
            setStatusTextViewOnYes();
        });

        btnNo.setOnClickListener(view -> {
            setStatusTextViewOnNo();
        });

        btnCreacionRed.setOnClickListener(view -> {
            //irACrecionRed(view);
        });

        btnSendMessages.setOnClickListener(view -> {
            this.mainActivity.enviarMensaje();
//            enviarMensaje();
//            // Una vez que el mensaje se haya enviado, reiniciar el estado
//            if (responseTextView.getText().equals("SI") && ipList.isEmpty()){
//                StringBuilder displayText = new StringBuilder("Mensajes recibidos:\n");
//                displayText.append("Resultado Votación: HUBO ACCIDENTE").append("\n");
//                setResultadoText("Resultado Votación: HAY ACCIDENTE");
//                //sistemaVotacion.iniciarConteo();
//            }
        });

        //Obtener y mostrar la IP del dispositivo
        String deviceIpAddress = networkUtils.getDeviceIpAddress();
        ipTextView.setText("Lista de IPs" + deviceIpAddress);

        String myDeviceIpAddress = networkUtils.getDeviceIpAddress();
        myIpTextView.setText("Mi IP: " + myDeviceIpAddress);


        return rootView;

    }




//    public void irACrecionRed(View view){
//        Intent i = new Intent(this, CreacionRedActivity.class);
//        startActivity(i);
//    }


    public void enviarMensaje(){
        this.mainActivity.enviarMensaje();
    }


    // Método para actualizar la lista de IPs en el TextView
    public void updateIpList(String ip) {

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

        Set<String> ipListArchivo = this.mainActivity.leerListaIpsEnArchivo();

        // Actualizar el TextView con todas las IPs acumuladas
        mainActivity.runOnUiThread(() -> {
            StringBuilder ips = new StringBuilder("IPs recibidas:\n");
            for (String savedIp : ipListArchivo) {
                ips.append(savedIp).append("\n");
                Log.i("Lista de IPs", "IP guardada: " + savedIp); // Log para cada IP

            }
            ipTextView.setText(ips.toString());
        });
    }


    public void setMyIpTextView(String newIp) {
        myIpTextView.setText(newIp);
    }


    public void setStatusTextViewOnYes() {
        mainActivity.saveStateInFile("SI");
        responseTextView.setText("SI");
    }


    public void setStatusTextViewOnNo() {
        mainActivity.saveStateInFile("NO");
        responseTextView.setText("NO");
    }

    public void storeMessageFromIp(String ip, String message) {
        mainActivity.addAndRefreshMap("map-ip-message", ip, message);
        // Mostrar el mensaje recibido en la interfaz
        // Actualizar la interfaz con el contenido del HashMap
        updateIpMessageView();
        this.mainActivity.runOnUiThread(() -> {
            Toast.makeText(this.mainActivity, "Mensaje recibido de " + ip + ": " + message, Toast.LENGTH_SHORT).show();
        });
    }

    // Método para actualizar el TextView con el contenido del HashMap
    private void updateIpMessageView() {
        StringBuilder displayText = new StringBuilder("Mensajes recibidos:\n");
        for (String ip : ipMessageMap.keySet()) {
            displayText.append("IP: ").append(ip).append(" - Mensaje: ").append(ipMessageMap.get(ip)).append("\n");
        }
        // Actualizar el TextView en el hilo de la UI
        this.mainActivity.runOnUiThread(() -> ipMessageTextView.setText(displayText.toString()));
    }



//    // Método para verificar permisos en tiempo de ejecución
//    private void checkPermissions() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
//                    ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
//                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
//                    ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
//                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//                ActivityCompat.requestPermissions(this,
//                        new String[]{
//                                Manifest.permission.ACCESS_WIFI_STATE,
//                                Manifest.permission.CHANGE_WIFI_STATE,
//                                Manifest.permission.ACCESS_NETWORK_STATE,
//                                Manifest.permission.CHANGE_NETWORK_STATE,
//                                Manifest.permission.ACCESS_FINE_LOCATION,
//                                Manifest.permission.ACCESS_COARSE_LOCATION
//                        }, 1);
//            }
//        }
//    }

//    // Callback para manejar la respuesta del usuario a la solicitud de permisos
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permisos concedidos, puedes iniciar el hotspot
//                Log.i("PermissionSuccesfull", "Permiso de ubicación habilitados. Se puede iniciar el hotspot.");
//            } else {
//                // Permisos no concedidos, manejar el caso según tu lógica
//                Log.e("PermissionError", "Permiso de ubicación denegado. No se puede iniciar el hotspot.");
//            }
//        }
//    }

    @Override
    public void onDestroy() {
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
    public void onPause() {
        super.onPause();
        //unregisterReceiver(networkChangeReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        //registerReceiver(networkChangeReceiver, filter);
    }

    public HashMap<String, String> getIpMessageMap(){
        return ipMessageMap;
    }

//    public void sumarContador(){
//        cont++;
//    }
//
//    public int getContador(){
//        return cont;
//    }
//
//    public void reiniciarContador(){
//        cont = 0;
//    }
//
//    public String getResponseText(){
//        return responseTextView.getText().toString();
//    }
//
//    public Set<String> getIpList(){
//        return ipList;
//    }

    public void setResultadoText(String message){
        StringBuilder displayText = new StringBuilder("Mensajes recibidos:\n");
        displayText.append(message).append("\n");
        resultadoTextView.setText(message);
    }

//    public void actualizarIpTimeStamp(String senderIp, String timestamp) {
//        fileUtils.addAndRefreshMap("map-ip-timestamp", senderIp, timestamp);
//        ipTimestamp.put(senderIp, timestamp);
//        Log.d("Actualizar Timestamp", "Se actualiza la ip con timestamp con ip: " + senderIp + " y " + timestamp);
//        for(Map.Entry<String, String> disp : ipTimestamp.entrySet()){
//            Log.d("Actualizar Timestamp", "IP" + disp.getKey() + " y " + disp.getValue());
//        }
//    }
//
//    public void verificarConexion() throws ParseException {
//        HashMap<String, String> ipTimestampFromFile = fileUtils.readMapfromFile("map-ip-timestamp");
//        Log.i("Verificacion Conexion", "Verificando conexion de dispositivos");
//        if(ipTimestampFromFile.isEmpty()){
//            Log.d("Verificacion Conexion", "EH AMIGO ME RE FUI. TA VACIO ACA");
//            return;
//        }
//
//        for(Map.Entry<String, String> dispositivo : ipTimestampFromFile.entrySet()){
//            long diferenciaTiempo = calcularDiferenciaTiempo(dispositivo.getValue());
//            Log.i("Verificacion Conexion", "Verificando conexion de: " + dispositivo.getKey());
//            Log.i("Verificacion Conexion", "La diferencia de tiempo es de: " + diferenciaTiempo);
//            if(diferenciaTiempo > 4){
//                Log.i("Verificacion Conexion", "El dispositivo " + dispositivo.getKey() + " está DESCONECTADO");
//                fileUtils.addAndRefreshMap("map-ip-message", dispositivo.getKey(), "DESCONECTADO");
//                ipMessageMap.put(dispositivo.getKey(), "DESCONECTADO");
//            }
//        }
//    }
//
//
//    public long calcularDiferenciaTiempo(String tiempoRecibido) throws ParseException {
//
//        long primerTimestampRecuperado = Long.parseLong(tiempoRecibido);
//        Log.i("Diferencia de Tiempo", "Primer tiempo de String a Long: " + primerTimestampRecuperado);
//        long segundoTimestamp = Calendar.getInstance().getTimeInMillis();
//        Log.i("Diferencia de Tiempo", "Segundo tiempo en milisegundos: " + segundoTimestamp);
//        // Calcular la diferencia en milisegundos
//        long diferenciaEnMilisegundos = segundoTimestamp - primerTimestampRecuperado;
//        long diferenciaEnSegundos = diferenciaEnMilisegundos / 1000;
//        Log.i("Diferencia de Tiempo", "Diferencia en segundos: " + diferenciaEnSegundos);
//
//        return diferenciaEnSegundos;
//    }
//
//    public void agregarIpYActualizarArchivo(String nuevaIp) {
//        fileUtils.agregarIpYActualizarArchivo(nuevaIp);
//    }
//
//    public void addAndRefreshMap(String filename, String ip, String message){
//        fileUtils.addAndRefreshMap(filename, ip, message);
//    }
//
//    public void enviarEstado(){
//        Set<String> listaIps = fileUtils.leerListaIpsEnArchivo();
//        String estado = fileUtils.readState();
//        String message;
//        Log.i("Envio de Estado", "Enviando estado");
//        if(estado.equals("SI")){
//            message = "VOTO:SI";
//            Log.i("Envio de Estado", "Enviando mensaje: VOTO:SI");
//
//        } else {
//            message = "VOTO:NO";
//            Log.i("Envio de Estado", "Enviando mensaje: VOTO:NO");
//
//        }
//        if(!listaIps.isEmpty()){
//            //String targetIp = ipList.get(0); // Usar la IP que quieras de la lista
//            for(String targetIp : listaIps) {
//                messageSender.sendMessage(targetIp, message);
//                Log.i("Envio de Estado", "Enviando mensaje a " + targetIp + " con: " + message);
//            }
//        }
//    }
//
//    public void saveVote(String ip, String vote) {
//        Log.i("Guardado de votos", "Se inicia el guardado de votos.");
//        String[] votoArray = vote.split(":");
//        String resultadoVoto = votoArray[1];
//        Log.i("Guardado de votos", "Se obtiene voto: " + resultadoVoto);
//
//        storeMessageFromIp(ip, resultadoVoto);
//        fileUtils.addAndRefreshMap("map-ip-message", ip, resultadoVoto);
//
//        // Mostrar el mensaje recibido en la interfaz
//        // Actualizar la interfaz con el contenido del HashMap
//
//        sumarContador();
//        Log.i("Guardado de votos", "El contador esta en " + getContador());
//
//        if(fileUtils.leerListaIpsEnArchivo().size() == getContador()){
//            Log.i("Recoleccion de estados", "Se obtuvieron los estados de todos los dispositivos. Se inicia el conteo de votos.");
//            HashMap<String, String> votos = fileUtils.readMapfromFile("map-ip-voto");
//            String myOwnVote = fileUtils.readState();
//            votos.put(networkUtils.getDeviceIpAddress(), myOwnVote);
//            boolean veredicto = sistemaVotacion.iniciarConteo(votos);
//            reiniciarContador();
//
//            if(veredicto) {
//                setResultadoText("HAY ACCIDENTE");
//            } else {
//                setResultadoText("NO HAY AACIDENTE");
//            }
//        }
//
//    }
}

