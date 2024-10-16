package com.example.autoalert.view.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import androidx.fragment.app.FragmentTransaction;

import com.example.autoalert.R;
import com.example.autoalert.view.fragments.PasosASeguirFragment;
import com.example.autoalert.view.fragments.PrincipalFragment;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements PasosASeguirFragment.OnCompleteListener{

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

    private SistemaVotación sistemaVotacion;
    private NetworkUtils networkUtils;

    private HashMap<String, String> ipTimestamp = new HashMap<>();


    private static final String PREFS_NAME = "AppPreferences";
    private static final String FIRST_TIME_KEY = "isFirstTime";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean(FIRST_TIME_KEY, true);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setReorderingAllowed(true);

            if (isFirstTime) {
                PasosASeguirFragment pasosFragment = new PasosASeguirFragment();
                pasosFragment.setOnCompleteListener(this);
                transaction.add(R.id.fcv_main_container, pasosFragment);
            } else {
                transaction.add(R.id.fcv_main_container, new PrincipalFragment());
            }

            transaction.commit();
        }

        Log.i("MainActivity", "Componentes inicializados.");

        // Inicializamos wifiManager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Registro del BroadcastReceiver dinámicamente
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);


        // Inicializar los componentes
        Log.i("MainActivity", "Inicio de hilos");
        broadcastSender = new BroadcastSender();
        broadcastSender.setMainActivity(this);

        broadcastReceiver = new BroadcastReceiver(this);
        messageSender = new MessageSender();
        broadcastTimer = new BroadcastTimer();

        networkUtils = new NetworkUtils();
        sistemaVotacion = new SistemaVotación(this);

        // Inicializamos el HashMap
        ipMessageMap = new HashMap<>();

        Log.i("MainActivity", "Inicio de Gestionador de Red.");
        Log.d("MainActivity", "Alias en MainActivity: " + getAlias());



        // Iniciar la recepción de broadcasts y respuestas
        Log.i("MainActivity", "Escuchando mensajes de broadcast.");
        broadcastReceiver.startListening();

        broadcastTimer.startBroadcastTimer();


        // Verificar y solicitar permisos necesarios
        Log.i("MainActivity", "Verificando permisos....");
        checkPermissions();

        MessageReceiver messageReceiver = new MessageReceiver(this);
        messageReceiver.startListening();

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);



    }



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

// COSAS DEL SISTEMA DE VOTACION
//    public void guardarVoto(String ip, String voto){
//        Log.i("Guardado de votos", "Se inicia el guardado de votos.");
//        String[] votoArray = voto.split(":");
//        String resultadoVoto = votoArray[1];
//        Log.i("Guardado de votos", "Se obtiene voto: " + resultadoVoto);
//
//        ipMessageMap.put(ip, resultadoVoto);
//        // Mostrar el mensaje recibido en la interfaz
//        // Actualizar la interfaz con el contenido del HashMap
//        updateIpMessageView();
//        runOnUiThread(() -> {
//            Toast.makeText(this, "Mensaje recibido de " + ip + ": " + resultadoVoto, Toast.LENGTH_SHORT).show();
//        });
//        cont = cont + 1;
//        Log.i("Guardado de votos", "El contador esta en " + cont);
//
//        if(ipList.size() == cont) {
//            Log.i("Recoleccion de estados", "Se obtuvieron los estados de todos los dispositivos. Se inicia el conteo de votos.");
//            iniciarConteo();
//            cont = 0;
//        }
//    }
//
//    public void iniciarConteo(){
//        int contPositivo = 0;
//        int contNegativo = 0;
//        StringBuilder displayText = new StringBuilder("Mensajes recibidos:\n");
//        Log.i("Conteo de votos", "Conteo de votos iniciado.");
//
//        for (String ip : ipMessageMap.keySet()) {
//            String voto = ipMessageMap.get(ip);
//            if ("SI".equals(voto)) {
//                contPositivo++;
//                Log.i("Conteo de votos", "VOTO:SI");
//            } else if ("NO".equals(voto)) {
//                contNegativo++;
//                Log.i("Conteo de votos", "VOTO:NO");
//            }
//        }
//
//        Log.i("Conteo de votos", "Añadiendo voto del propio dispositivo.");
//        if(responseTextView.getText().equals("SI")){
//            Log.i("Conteo de votos", "Se añade un VOTO:SI");
//            contPositivo++;
//        } else {
//            Log.i("Conteo de votos", "Se añade un VOTO:NO");
//            contNegativo++;
//        }
//
//        // Mostrar resultado basado en la cantidad de votos
//        if (contPositivo >= contNegativo) {
//            Log.i("Conteo de votos", "Hay Accidente");
//            displayText.append("Resultado Votación: HAY ACCIDENTE").append("\n");
//            resultadoTextView.setText("Resultado Votación: HAY ACCIDENTE");
//        } else {
//            Log.i("Conteo de votos", "No hubo accidente");
//            displayText.append("Resultado Votación: NO HUBO ACCIDENTE").append("\n");
//            resultadoTextView.setText("Resultado Votación: NO HUBO ACCIDENTE");
//        }
//    }
//
//    public void enviarEstado(){
//        String message;
//        Log.i("Envio de Estado", "Enviando estado");
//        if(responseTextView.getText().equals("SI")){
//            message = "VOTO:SI";
//            Log.i("Envio de Estado", "Enviando mensaje: VOTO:SI");
//
//        } else {
//            message = "VOTO:NO";
//            Log.i("Envio de Estado", "Enviando mensaje: VOTO:NO");
//
//        }
//
//
//
//        // Supongamos que quieres enviar el mensaje a la primera IP de la lista
//        if (!ipList.isEmpty()) {
//            //String targetIp = ipList.get(0); // Usar la IP que quieras de la lista
//            for(String targetIp : ipList) {
//                messageSender.sendMessage(targetIp, message);
//                Log.i("Envio de Estado", "Enviando mensaje a " + targetIp + " con: VOTO:NO");
//            }
//        }
//    }

    //ACA FINALIZA COSAS DEL SISTEMA DE VOTACION

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

    //ESTO SACARLO Y DEJARLO EN UNA CLASE DE NETWORK UTILS
//    private String getDeviceIpAddress() {
//        try {
//            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
//                NetworkInterface intf = en.nextElement();
//                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
//                    InetAddress inetAddress = enumIpAddr.nextElement();
//                    if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
//                        return inetAddress.getHostAddress();
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return "IP no disponible";
//    }




    @Override
    protected void onDestroy() {
        super.onDestroy();



        // Detener el hotspot al cerrar la aplicación
        if (hotspotManager != null) {
            hotspotManager.stopHotspot();
        } else {
            Log.e("MainActivity", "WifiHotspot object is null. Cannot stop hotspot.");
        }

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



    public Set<String> getIpList(){
        return ipList;
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

    // Guardar la IP y el alias recibido
    public void storeAliasFromIp(String ip, String alias) {
        ipAliasMap.put(ip, alias);
        Log.d("RedActivity", "Guardado: IP " + ip + " con alias " + alias);
    }

    // Método para obtener el alias de una IP
    public String getAliasFromIp(String ip) {
        return ipAliasMap.get(ip);
    }

    // Método para obtener el alias del archivo JSON
    public String getAlias() {
        String alias = "";
        try {
            // Ruta del archivo JSON
            File file = new File(getFilesDir(), "user_data.json"); // Cambia la ruta si es necesario

            if (file.exists()) {
                // Lee el archivo JSON
                FileReader fileReader = new FileReader(file);

                // Usa Gson para parsear el archivo JSON
                Gson gson = new Gson();
                JsonObject userData = gson.fromJson(fileReader, JsonObject.class);

                // Extrae el nombre y apellido
                String nombreUsuario = userData.get("nombreUsuario").getAsString();
                String apellidoUsuario = userData.get("apellidoUsuario").getAsString();

                // Genera el alias
                alias = nombreUsuario + apellidoUsuario;

                fileReader.close();
            } else {
                Log.e("RedActivity", "El archivo JSON no existe.");
            }
        } catch (Exception e) {
            Log.e("RedActivity", "Error al leer el archivo JSON: " + e.getMessage());
        }

        return alias.trim(); // Devuelve el alias generado
    }

    @Override
    public void onFirstTimeCompleted() {
        // Logic to mark first time as completed
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(FIRST_TIME_KEY, false);
        editor.apply();
    }

}
