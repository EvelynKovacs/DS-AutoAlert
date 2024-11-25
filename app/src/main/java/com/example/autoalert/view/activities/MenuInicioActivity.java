package com.example.autoalert.view.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;


import com.example.autoalert.R;
import com.example.autoalert.data.AccidentDetectionService;
import com.example.autoalert.utils.FileUtils;
import com.example.autoalert.utils.NetworkUtils;
import com.example.autoalert.view.fragments.PantallaBienvenidaFragment;
import com.example.autoalert.view.fragments.PrincipalFragment;
import com.example.autoalert.view.fragments.SimulacionFragment;
import com.example.autoalert.viewmodel.AccidentViewModel;
import com.example.autoalert.viewmodel.SpeedViewModel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.widget.TextView;



public class MenuInicioActivity extends AppCompatActivity implements PantallaBienvenidaFragment.OnCompleteListener {

    private boolean isNetworkReceiverRegistered = false;
    private boolean isAppInForeground = false;
    private AccidentDetectionService accidentDetectionService;
    private boolean isBound = false;
    private SimulacionFragment simulacionFragment = new SimulacionFragment();
    // Variable para controlar si la notificación ya se mostró en esta sesión
    private boolean isUserNotificationShown = false;

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


    private AccidentViewModel accidentViewModel;
    private SpeedViewModel speedViewModel;
    private TextView tvAddress;  // Nuevo TextView para la dirección
    private boolean isMessageSent = false;  // Bandera para controlar el envío del mensaje
    private boolean accidenteDetectado=false;
    private ActivityResultLauncher<Intent> checkSettingsLauncher;

    private boolean isCalledFromService = false;  // Esta bandera se activará si el servicio llama a la actividad



    private static final int NOTIFICATION_ID = 1; // Puedes usar cualquier número único


    private WifiHotspot hotspotManager;

    private FileUtils fileUtils;


  /*
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AccidentDetectionService.LocalBinder binder = (AccidentDetectionService.LocalBinder) service;
            accidentDetectionService = binder.getService();
            isBound = true;

            // Observar cambios en el estado del accidente
            if (accidentDetectionService != null) {
                accidentDetectionService.getAccidenteDetectadoLiveData().observe(MenuInicioActivity.this, accidentDetected -> {
                    Log.d("MenuInicioActivity", "Cambio en LiveData detectado: " + accidentDetected);
                    if (accidentDetected) {
                        cambiarFragmentoPorAccidente();
                    }
                });
            } else {
                Log.e("MenuInicioActivity", "AccidentDetectionService es null después de la vinculación");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };
*/



// HASTA ACA

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        //tvAddress = findViewById(R.id.tvAddress);  // TextView para la dirección

        // Inicia el servicio de detección de accidentes
        startAccidentDetectionService();

        // Iniciar y vincular el servicio
        Intent serviceIntent = new Intent(this, AccidentDetectionService.class);
        //bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        // Solicitar los permisos al iniciar la actividad
        checkPermissions();

        // Solicitar permiso SYSTEM_ALERT_WINDOW
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(intent);
        }


        // Inicia el WorkManager para ejecutar LocationWorker cada 30 segundos
//        startPeriodicLocationWorker();
        simulacionFragment = new SimulacionFragment();


        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean(FIRST_TIME_KEY, true);
        Log.d("MenuInicioActivity", "isFirstTime: " + isFirstTime);

        // Inicializa el launcher para manejar el resultado de REQUEST_CHECK_SETTINGS
        checkSettingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.i("MainActivity", "Location settings enabled by user.");
                        // Llama a resumeLocationUpdates aquí
                        speedViewModel.resumeLocationUpdates();
                    } else {
                        Log.e("MainActivity", "Location settings were not enabled.");
                    }
                }
        );

        // COSAS DE CONEXIONES

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);
        isNetworkReceiverRegistered = true;

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

        fileUtils.crearOReiniciarArchivo("lista-contactos");

        // HASTA ACA
        //PARTE EVE


        // Inicializar el ViewModel
        speedViewModel = new ViewModelProvider(this).get(SpeedViewModel.class);


        // Observar los cambios de dirección
//        speedViewModel.getAddress().observe(this, address -> {
//            tvAddress.setText("Dirección: " + address);  // Actualizar el TextView de la dirección
//            System.out.println("DIRECCION: " + address);
//            String emergencyMessage = "Emergencia. Dirección: " + address;
//            System.out.println(emergencyMessage);
//            String sanitizedAddress = " Mensaje de Emergencia. La siguiente direccion podria no ser exacta. " + address.replaceAll("[^a-zA-Z0-9\\s,.]", "");
//
//
//            //
//            //SmsUtils.checkAndSendSms(this, new String[]{"2804559405", "2804611882", "2804382723"}, sanitizedAddress);
//
//            if (!isMessageSent) {
//                //String emergencyMessage = "Mensaje de emergencia. Dirección: " + address;
//
//                //SmsUtils.checkAndSendSms(this, new String[]{"2804992455", "2804611882", "2804405851"}, sanitizedAddress);
//
//                isMessageSent = true;  // Marcar como enviado
//            }
//        });

        // Obtén el AccidentViewModel con un contexto de actividad para que sea compartido
        //accidentViewModel = new ViewModelProvider(this).get(AccidentViewModel.class);


        // Pasar el ViewModel al NotificadorAccidente
        //NotificadorAccidente notificador = NotificadorAccidente.getInstancia();
        //notificador.setAccidentViewModel(accidentViewModel);

        // Observar los cambios en el estado del accidente
        /*accidentViewModel.getAccidenteDetectado().observe(this, accidentDetected -> {
            if (!accidenteDetectado) {
                fileUtils.saveStateInFile("SI");
                enviarMensaje();
            }
        });*/

        //TERMINA EVE


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

    private void cambiarFragmentoPorAccidente() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fcv_main_container, simulacionFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        Log.i("MenuInicioActivity", "Cambiando al fragmento de simulación por accidente");
    }

    // Método para verificar permisos en tiempo de ejecución
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_WIFI_STATE,
                                Manifest.permission.CHANGE_WIFI_STATE,
                                Manifest.permission.ACCESS_NETWORK_STATE,
                                Manifest.permission.CHANGE_NETWORK_STATE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.SEND_SMS,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.READ_PHONE_NUMBERS,
                                Manifest.permission.READ_SMS
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

//    private void startPeriodicLocationWorker() {
//        // Crear las restricciones necesarias (sin requerir red)
//        Constraints constraints = new Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // No requiere red para funcionar
//                .build();
//
//        // Crear una solicitud de trabajo periódico (cada 30 segundos)
//        PeriodicWorkRequest locationWorkRequest = new PeriodicWorkRequest.Builder(LocationWorker.class, 30, TimeUnit.SECONDS)
//                .setConstraints(constraints)
//                .build();
//
//        // Encolar el trabajo periódico con una política de reemplazo en caso de que ya exista uno
//        WorkManager workManager = WorkManager.getInstance(this);
//        workManager.enqueueUniquePeriodicWork("LocationWorker",
//                ExistingPeriodicWorkPolicy.REPLACE,
//                locationWorkRequest);
//
//        // Mostrar un Toast para confirmar la ejecución
////        Toast.makeText(this, "Trabajo periódico programado para cada 30 segundos.", Toast.LENGTH_SHORT).show();
////        Log.d("PeriodicLocationWorker", "Trabajo periódico programado para ejecutarse cada 30 segundos.");
//    }




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
/*
    public void enviarMensaje(){
            String message = fileUtils.readState();
            Set<String> ipListArchivo = fileUtils.leerListaIpsEnArchivo();
            if (!ipListArchivo.isEmpty()) {
                for (String targetIp : ipListArchivo) {
                    messageSender.sendMessage(targetIp, message);
                    Log.i("Envio de mensaje", "Mensaje enviado a: " + targetIp + " con " + message);
                }
                if (message.equals("SI")) {
                    accidenteDetectado=true;
                    enviarEstado();

                }
            } else {
                Log.e("Envio de mensaje", "HUBO ACCIDENTE pero No hay IPs disponibles para enviar el mensaje.");
                Toast.makeText(this, "HUBO ACCIDENTE pero No hay IPs disponibles para enviar el mensaje", Toast.LENGTH_SHORT).show();
                accidenteDetectado=true;

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fcv_main_container, simulacionFragment);
                transaction.addToBackStack(null);
                transaction.commit();



            }


        // Restablecer accidenteDetectado a false para permitir futuras detecciones
        //accidenteDetectado = false;

    }
*/

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
/*
    public void setStatusTextViewOnYes() {
        fileUtils.saveStateInFile("SI");
    }


    public void setStatusTextViewOnNo() {
        fileUtils.saveStateInFile("NO");
    }*/

    public void storeMessageFromIp(String ip, String message) {
        ipMessageMap.put(ip, message);
        updateIpMessageView();
//        runOnUiThread(() -> {
//            Toast.makeText(this, "Mensaje recibido de " + ip + ": " + message, Toast.LENGTH_SHORT).show();
//        });
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
                speedViewModel.checkLocationSettings(this); // Inicia la verificación de la configuración de ubicación

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
        if (isNetworkReceiverRegistered) {
            unregisterReceiver(networkChangeReceiver);
            isNetworkReceiverRegistered = false;
        }

        if (isBound) {
            //unbindService(serviceConnection);
            isBound = false;
        }

        if (hotspotManager != null) {
            hotspotManager.stopHotspot();
        }

        // Unregister the network callback (if applicable)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause","Entro al onPause");
        if (isNetworkReceiverRegistered) {
            unregisterReceiver(networkChangeReceiver);
            isNetworkReceiverRegistered = false;
        }
        if (isCalledFromService==true && isAppInForeground==false) {
            //isCalledFromService = false; // Resetea la bandera
            // Realiza la acción necesaria al regresar desde el servicio
        }

        isAppInForeground = true; // La app esta en segundo plano
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("EnResume","Entro al onResume");
        if (!isNetworkReceiverRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(networkChangeReceiver, filter);
            isNetworkReceiverRegistered = true;
        }
        Log.d("MenuInicioActivity", "En onResume el valor de isAppInForeground es: "+isAppInForeground);
        if (isCalledFromService==true && isAppInForeground==true) {
            Log.d("MenuInicioActivity", "Llamada desde el servicio mientras la app está en 2° plano");
            //isCalledFromService = false; // Resetea la bandera
            // Realiza la acción necesaria al regresar desde el servicio
        }
        if (isCalledFromService==true && isAppInForeground==false) {
            Log.d("MenuInicioActivity", "Llamada desde el servicio mientras la app está en 1° plano");
            //isCalledFromService = false; // Resetea la bandera
            // Realiza la acción necesaria al regresar desde el servicio
        }
        if (isCalledFromService==false && isAppInForeground==false) {
            Log.d("MenuInicioActivity", "Llamada no fue desde el servicio y la app está en 1° plano");
            //isCalledFromService = false; // Resetea la bandera
            // Realiza la acción necesaria al regresar desde el servicio
        }
    }

    public boolean isAppInForeground() {
        return isAppInForeground;
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
            if(diferenciaTiempo > 4){
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
                    accidenteDetectado=true;
                    Log.i("Votacion", "HAY ACCIDENTE");
                    setResultadoText("HAY ACCIDENTE");
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fcv_main_container, simulacionFragment);
                    transaction.addToBackStack(null);
                    transaction.commitAllowingStateLoss();

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
                showUserEmptyNotification();
                Log.e("MainActivity", "El archivo JSON no existe.");
            }
        } catch (Exception e) {
            showUserEmptyNotification();
            Log.e("MainActivity", "Error al leer el archivo JSON: " + e.getMessage());
        }
        if (alias.isEmpty()){
            alias = "aliasGenerico";
        }
        return alias.trim(); // Devuelve el alias generado
    }


    // Método para obtener el numero del archivo JSON
    public String getMiNumero() {
        String miNumero = "";
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
                String miNumero1 = userData.get("miNumero").getAsString();

                String ultimos6Digitos="";
                // Verifica que el número tiene al menos 6 dígitos
                if (miNumero1.length() >= 6) {
                    // Obtén los últimos 6 dígitos
                    ultimos6Digitos = miNumero1.substring(miNumero1.length() - 6);
                    System.out.println("Últimos 6 dígitos: " + ultimos6Digitos);
                } else {
                    System.out.println("El número es demasiado corto para extraer los últimos 6 dígitos.");
                }

                miNumero = ultimos6Digitos;

                fileReader.close();
            } else {
                showUserEmptyNotification();
                Log.e("MainActivity", "El archivo JSON no existe.");
            }
        } catch (Exception e) {
            showUserEmptyNotification();
            Log.e("MainActivity", "Error al leer el archivo JSON: " + e.getMessage());
        }
        if (miNumero.isEmpty()){
            miNumero = "000000";
        }
        return miNumero.trim(); // Devuelve el alias generado
    }

    public boolean compararNumeros(String suNumero) {
        try {
            // Ruta del archivo JSON
            File file = new File(getFilesDir(), "user_data.json");

            if (file.exists()) {
                // Lee el archivo JSON
                FileReader fileReader = new FileReader(file);

                // Usa Gson para parsear el archivo JSON
                Gson gson = new Gson();
                JsonObject userData = gson.fromJson(fileReader, JsonObject.class);
                fileReader.close();

                // Obtén el campo de contactos como un arreglo
                JsonArray contactosArray = userData.getAsJsonArray("contactos");

                // Verifica los últimos 6 dígitos de suNumero
                String ultimos6SuNumero = suNumero.length() >= 6 ? suNumero.substring(suNumero.length() - 6) : suNumero;

                Log.d("NumeroQueViene", "ultimos6SuNumero: " + ultimos6SuNumero);

                // Recorre los contactos para verificar si alguno coincide con los últimos 6 dígitos
                for (int i = 0; i < contactosArray.size(); i++) {
                    String contacto = contactosArray.get(i).getAsString();
                    contacto = contacto.replaceAll("[^\\d]", "");
                    Log.d("Numero", "contactoArray: " + contacto);

                    // Extrae el número de cada contacto usando una expresión regular
                    Pattern pattern = Pattern.compile("\\d+"); // Encuentra secuencias de dígitos
                    Matcher matcher = pattern.matcher(contacto);

                    Log.d("Numero", "contactoPatter: " + contacto);

                    String numeroContacto = "";
                    while (matcher.find()) {
                        numeroContacto = matcher.group(); // Último grupo de números
                    }
                    Log.d("Numero", "numeroContacto: " + numeroContacto);

                    // Verifica los últimos 6 dígitos
                    if (numeroContacto.length() >= 6) {
                        String ultimos6Contacto = numeroContacto.substring(numeroContacto.length() - 6);
                        if (ultimos6SuNumero.equals(ultimos6Contacto)) {
                            Log.d("EncontroNumero", "Lo encontro al numero: " + ultimos6SuNumero);

                            FileUtils fileUtils = new FileUtils(this);

                            // Verifica si el número ya está guardado en "lista-contactos"
                            Set<String> listaContactos = fileUtils.leerContactosEmergencia();
                            if (!listaContactos.contains(numeroContacto)) {
                                fileUtils.agregarContactoEmergencia(numeroContacto);
                                final String finalNumeroContacto = numeroContacto; // Hacerlo final
                                // Mostrar el Toast en el hilo principal
                                runOnUiThread(() -> {
                                    // Create a notification channel (if needed)
                                    showNotification("Contacto de Emergencia", "El número " + finalNumeroContacto + " está en sus contactos de emergencia", 2);
                                });
                            }
                            return true;
                        }
                    }
                }
            } else {
                Log.e("PrincipalFragment", "El archivo JSON no existe.");
            }
        } catch (Exception e) {
            Log.e("PrincipalFragment", "Error al leer el archivo JSON: " + e.getMessage());
        }

        // Si no se encontró coincidencia
        Log.d("NoEncontroNumero", "No lo encontro");
        return false;
    }

    private void showUserEmptyNotification() {
        // Solo mostrar la notificación si aún no se ha mostrado en esta sesión
        if (!isUserNotificationShown) {
            showNotification("Datos de usuario incompletos", "Por favor, complete su información personal.", 1);
            isUserNotificationShown = true; // Marcar la notificación como mostrada
        }
    }

    private void showNotification(String title, String content, int notificationId) {
        // Asegura que el canal esté creado
        createNotificationChannelIfNeeded();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "USER_NOTIFICATION")
                .setSmallIcon(R.drawable.ic_notification) // Icono de la notificación
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build());
    }

    // Centraliza la creación de canales de notificación
    private void createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "USER_NOTIFICATION";
            CharSequence name = "User Notifications";
            String description = "Notificaciones relacionadas con información del usuario";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }


// ACA TERMINA ALIAS

    public void resetAccidenteDetectado() {
        accidenteDetectado = false;
        Log.d("MenuInicioActivity", "accidenteDetectado vuelve a: "+accidenteDetectado);
    }

    private void startAccidentDetectionService() {
        if (!isServiceRunning(AccidentDetectionService.class)) {
            Intent serviceIntent = new Intent(this, AccidentDetectionService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);  // Actualiza el Intent de la actividad

        // Verifica si la llamada es desde el servicio
        boolean fromService = intent.getBooleanExtra("from_service", false);
        if (fromService) {
            isCalledFromService = true;  // Marca que la llamada es desde el servicio
            Log.d("MenuInicioActivity", "Llamada desde el servicio detectada");
        }

        // Procesa el Intent
        handleIntent(intent);
    }


    private void handleIntent(Intent intent) {
        boolean fromService = intent.getBooleanExtra("from_service", false);
        Log.d("MenuInicioActivity", "fromService: " + fromService);

        FragmentManager fragmentManager = getSupportFragmentManager();
        SimulacionFragment simulacionFragment = SimulacionFragment.newInstance(fromService);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fcv_main_container, simulacionFragment);

        if (fromService ) {
            transaction.addToBackStack(null); // Agrega a la pila para permitir retroceso
        }

        transaction.commitAllowingStateLoss();
    }


}

