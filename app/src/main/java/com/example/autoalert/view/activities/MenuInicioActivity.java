package com.example.autoalert.view.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.example.autoalert.utils.FileUtils;
import com.example.autoalert.utils.LocationWorker;
import com.example.autoalert.utils.NetworkUtils;
import com.example.autoalert.view.fragments.PantallaBienvenidaFragment;
import com.example.autoalert.view.fragments.PasosASeguirFragment;
import com.example.autoalert.view.fragments.PrincipalFragment;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MenuInicioActivity extends AppCompatActivity implements PantallaBienvenidaFragment.OnCompleteListener {

    private static final String PREFS_NAME = "AppPreferences";
    private static final String FIRST_TIME_KEY = "isFirstTime";

    // COSAS CONEXIONES
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private BroadcastSender broadcastSender;
    private BroadcastReceiver broadcastReceiver;
    private MessageSender messageSender;
    private MessageReceiver messageReceiver;
    public Set<String> ipList = new HashSet<>();
    private HashMap<String, String> ipMessageMap;
    private BroadcastTimer broadcastTimer;
    private int cont = 0;
    private WifiManager wifiManager;
    private NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver(this);
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    public HashMap<String, String> ipAliasMap = new HashMap<>();
    private SistemaVotacion sistemaVotacion;
    private NetworkUtils networkUtils;
    private HashMap<String, String> ipTimestamp = new HashMap<>();

    private WifiHotspot hotspotManager;

    private FileUtils fileUtils;
// HASTA ACA

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        // Solicitar los permisos al iniciar la actividad
        checkPermissions();

        // Inicia el WorkManager para ejecutar LocationWorker cada 30 segundos
        startPeriodicLocationWorker();


        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean(FIRST_TIME_KEY, true);
        Log.d("MenuInicioActivity", "isFirstTime: " + isFirstTime);

        // COSAS DE CONEXIONES

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);

        fileUtils = new FileUtils(this);

        broadcastSender = new BroadcastSender(this);
        broadcastReceiver = new BroadcastReceiver(this);
        messageSender = new MessageSender();
        broadcastTimer = new BroadcastTimer(this);
        networkUtils = new NetworkUtils();
        sistemaVotacion = new SistemaVotacion(this);
        ipMessageMap = new HashMap<>();
        messageReceiver = new MessageReceiver(this);

        broadcastReceiver.startListening();
        broadcastTimer.startBroadcastTimer();
        messageReceiver.startListening();

        checkPermissions();

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);

        fileUtils.clearAppFilesContent();

        fileUtils.crearOReiniciarArchivo("lista-ip");
        fileUtils.crearOReiniciarArchivo("map-ip-message");
        fileUtils.crearOReiniciarArchivo("map-ip-voto");
        fileUtils.crearOReiniciarArchivo("map-ip-timestamp");
        fileUtils.crearOReiniciarArchivo("map-conf-red");
        fileUtils.crearOReiniciarArchivo("state");
        fileUtils.crearOReiniciarArchivo("conf-red");


        fileUtils.crearOReiniciarArchivo("map-ip-alias");
        fileUtils.addAndRefreshMap("conf-red", "creada", "false");



        // HASTA ACA



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


    // DESDE ACA EN ADELANTE TODO ES CONEXIONES, TODOOO HE DICHO!!

    public void enviarMensaje(){
        String message = fileUtils.readState();
        Set<String> ipListArchivo = fileUtils.leerListaIpsEnArchivo();
        if (!ipListArchivo.isEmpty()) {
            for(String targetIp : ipListArchivo) {
                messageSender.sendMessage(targetIp, message);
                Log.i("Envio de mensaje", "Mensaje enviado a: " + targetIp + " con " + message);
            }
            if(message.equals("SI")){
                enviarEstado();
            }
        } else {
            Log.e("Envio de mensaje", "No hay IPs disponibles para enviar el mensaje.");
            Toast.makeText(this, "No hay IPs disponibles para enviar el mensaje", Toast.LENGTH_SHORT).show();
        }
    }


    public void updateIpList(String ip) {
        Set<String> ipListArchivo = fileUtils.leerListaIpsEnArchivo();
        runOnUiThread(() -> {
            StringBuilder ips = new StringBuilder("IPs recibidas:\n");
            for (String savedIp : ipListArchivo) {
                ips.append(savedIp).append("\n");
                Log.i("Lista de IPs", "IP guardada: " + savedIp); // Log para cada IP
            }
        });
    }

    public void setStatusTextViewOnYes() {
        fileUtils.saveStateInFile("SI");
    }


    public void setStatusTextViewOnNo() {
        fileUtils.saveStateInFile("NO");
    }

    public void storeMessageFromIp(String ip, String message) {
        ipMessageMap.put(ip, message);
        updateIpMessageView();
        runOnUiThread(() -> {
            Toast.makeText(this, "Mensaje recibido de " + ip + ": " + message, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateIpMessageView() {
        StringBuilder displayText = new StringBuilder("Mensajes recibidos:\n");
        for (String ip : ipMessageMap.keySet()) {
            displayText.append("IP: ").append(ip).append(" - Mensaje: ").append(ipMessageMap.get(ip)).append("\n");
        }
    }


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
        hotspotManager.stopHotspot();
//        CreacionRedActivity creacionRedActivity = new CreacionRedActivity();
//        creacionRedActivity.stopWifiDirectHotspot();
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

    public void reiniciarContador(){
        cont = 0;
    }

    public void setResultadoText(String message){
        StringBuilder displayText = new StringBuilder("Mensajes recibidos:\n");
        displayText.append(message).append("\n");
    }

    public void actualizarIpTimeStamp(String senderIp, String timestamp) {
        fileUtils.addAndRefreshMap("map-ip-timestamp", senderIp, timestamp);
        ipTimestamp.put(senderIp, timestamp);
        for(Map.Entry<String, String> disp : ipTimestamp.entrySet()){
            Log.d("Actualizar Timestamp", "IP" + disp.getKey() + " y " + disp.getValue());
        }
    }

    public void verificarConexion() throws ParseException {
        HashMap<String, String> ipTimestampFromFile = fileUtils.readMapfromFile("map-ip-timestamp");
        Log.i("Verificacion Conexion", "Verificando conexion de dispositivos");
        if(ipTimestampFromFile.isEmpty()){
            Log.d("Verificacion Conexion", "Lista de ips vacia");
            return;
        }

        for(Map.Entry<String, String> dispositivo : ipTimestampFromFile.entrySet()){
            long diferenciaTiempo = calcularDiferenciaTiempo(dispositivo.getValue());
            Log.i("Verificacion Conexion", "Verificando conexion de: " + dispositivo.getKey());
            if(diferenciaTiempo > 10){
                Log.i("Verificacion Conexion", "El dispositivo " + dispositivo.getKey() + " está DESCONECTADO");
                fileUtils.addAndRefreshMap("map-ip-message", dispositivo.getKey(), "DESCONECTADO");
                ipMessageMap.put(dispositivo.getKey(), "DESCONECTADO");
                deleteIpFromListAndMap(dispositivo.getKey());
            } else {
                Log.i("Verificacion Conexion", "El dispositivo " + dispositivo.getKey() + " está CONECTADO");
                fileUtils.addAndRefreshMap("map-ip-message", dispositivo.getKey(), "CONECTADO");
                ipMessageMap.put(dispositivo.getKey(), "CONECTADO");
            }
        }
    }


    public long calcularDiferenciaTiempo(String tiempoRecibido) throws ParseException {
        long primerTimestampRecuperado = Long.parseLong(tiempoRecibido);
        long segundoTimestamp = Calendar.getInstance().getTimeInMillis();
        // Calcular la diferencia en milisegundos
        long diferenciaEnMilisegundos = segundoTimestamp - primerTimestampRecuperado;
        long diferenciaEnSegundos = diferenciaEnMilisegundos / 1000;

        return diferenciaEnSegundos;
    }

    public void agregarIpYActualizarArchivo(String nuevaIp) {
        fileUtils.agregarIpYActualizarArchivo(nuevaIp);
    }

    public void addAndRefreshMap(String filename, String ip, String message){
        fileUtils.addAndRefreshMap(filename, ip, message);
    }

    public void enviarEstado(){
        Set<String> listaIps = fileUtils.leerListaIpsEnArchivo();
        fileUtils.clearVotoFileContent();
        String estado = fileUtils.readState();
        String message;
        if(estado.equals("SI")){
            message = "VOTO:SI";
            Log.i("Envio de Estado", "Enviando mensaje: VOTO:SI");
        } else {
            message = "VOTO:NO";
            Log.i("Envio de Estado", "Enviando mensaje: VOTO:NO");

        }
        if(!listaIps.isEmpty()){
            for(String targetIp : listaIps) {
                messageSender.sendMessage(targetIp, message);
                Log.i("Envio de Estado", "Enviando mensaje a " + targetIp + " con: " + message);
            }
        }

        startVotacionTimer();
    }

    public void saveVote(String ip, String vote) {
        String[] votoArray = vote.split(":");
        String resultadoVoto = votoArray[1];
        Log.i("Guardado de votos", "Se obtiene voto: " + resultadoVoto + " de " + ip);
        storeMessageFromIp(ip, resultadoVoto);
        fileUtils.addAndRefreshMap("map-ip-message", ip, resultadoVoto);
        fileUtils.addAndRefreshMap("map-ip-voto", ip, resultadoVoto);
    }

    public Set<String> leerListaIpsEnArchivo(){
        return fileUtils.leerListaIpsEnArchivo();
    }

    public void saveStateInFile(String state) {
        fileUtils.saveStateInFile(state);
    }

    public void sendBroadcast(){
        this.broadcastSender.sendBroadcast();
    }

    public void deleteIpFromListAndMap(String ip){
        fileUtils.deleteIpFromListAndMap(ip);
    }

    public HashMap<String, String> readMapFromFile(String fileName){
        return fileUtils.readMapfromFile(fileName);
    }

    public void saveMapInFile(String filenName, HashMap<String, String> mapToSave){
        fileUtils.saveMapInFile(filenName, mapToSave);
    }

    public void startVotacionTimer() {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                HashMap<String, String> votos = fileUtils.readMapfromFile("map-ip-voto");
                String myOwnVote = fileUtils.readState();
                votos.put(networkUtils.getDeviceIpAddress(), myOwnVote);
                boolean veredicto = sistemaVotacion.iniciarConteo(votos);
                reiniciarContador();
                if(veredicto) {
                    Log.i("Votacion", "HAY ACCIDENTE");
                    setResultadoText("HAY ACCIDENTE");
                } else {
                    Log.i("Votacion", "NO HAY ACCIDENTE");
                    setResultadoText("NO HAY ACCIDENTE");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Hilo contador", "Error al al contar votos" + e.getMessage());
            }
        }).start();
    }

 // ESTO NO ES CONEXIONES, ES LO DEL ALIAS

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
                Log.e("MainActivity", "El archivo JSON no existe.");
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error al leer el archivo JSON: " + e.getMessage());
        }
        if (alias.isEmpty()){
            alias = "aliasGenerico";
        }
        return alias.trim(); // Devuelve el alias generado
    }
// ACA TERMINA ALIAS
}
