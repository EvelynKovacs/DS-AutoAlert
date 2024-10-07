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
import android.net.wifi.WifiManager;
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
import androidx.lifecycle.ViewModelProvider;

import com.example.autoalert.R;
import com.example.autoalert.repository.AccelerometerQueueRepository;
import com.example.autoalert.utils.AccidentDetector;
import com.example.autoalert.utils.NotificadorAccidente;
import com.example.autoalert.utils.SmsUtils;
import com.example.autoalert.view.adapters.SensorAdapter;
import com.example.autoalert.viewmodel.AccidentViewModel;
import com.example.autoalert.viewmodel.SensorViewModel;
import com.example.autoalert.viewmodel.SpeedViewModel;

import java.lang.reflect.Method;
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
    private BroadcastReceiver broadcastReceiver;
    private Handler handler = new Handler(Looper.getMainLooper());
    private MessageSender messageSender;
    public List<String> ipList = new ArrayList<>(); // Lista de IPs obtenidas por broadcast
    private HashMap<String, String> ipMessageMap;
    private TextView ipMessageTextView;
    //private Button btnYes;
    //private Button btnNo;
    private TextView statusBtnTextView;
    //private TextView responseTextView;
    private TextView estadoRedTextView;
    private Button btnCreacionRed;
    private BroadcastTimer broadcastTimer;
    private int cont = 0;

    private WifiManager wifiManager;

    private NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();

    private AccidentViewModel accidentViewModel;
    private boolean huboAccidente = false; // Esta variable se actualizará desde el ViewModel



    private SensorViewModel sensorViewModel;
    private SensorAdapter sensorAdapter;
    private SpeedViewModel speedViewModel;
    private TextView  tvAddress;  // Nuevo TextView para la dirección
    private boolean isMessageSent = false;  // Bandera para controlar el envío del mensaje
    private AccidentDetector accidentDetector; // Instancia de AccidentDetector
    private AccelerometerQueueRepository accelerometerQueueRepository;




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
        //Button btnSendMessages = findViewById(R.id.btnSendMessages);
        ipTextView = findViewById(R.id.ipTextView);
        Button btnSendBroadcast = findViewById(R.id.btnSendBroadcast);
        statusTextView = findViewById(R.id.statusTextView);
        toggleHotspotButton = findViewById(R.id.toggleHotspotButton);
        ipMessageTextView = findViewById(R.id.ipMessageTextView);
        myIpTextView = findViewById(R.id.myIpTextView);
        ///btnYes = findViewById(R.id.btnYes);
        //btnNo = findViewById(R.id.btnNo);
       // statusBtnTextView = findViewById(R.id.statusBtnTextView);
        //responseTextView = findViewById(R.id.responseTextView);
        estadoRedTextView = findViewById(R.id.redStatusTextView);
        btnCreacionRed = findViewById(R.id.creacionRedbutton);

        accidentDetector = new AccidentDetector(getApplicationContext());
        accelerometerQueueRepository = new AccelerometerQueueRepository(getApplicationContext());

        tvAddress = findViewById(R.id.tvAddress);  // TextView para la dirección

        // Inicializar el ViewModel
        speedViewModel = new ViewModelProvider(this).get(SpeedViewModel.class);



        // Observar los cambios de dirección
        speedViewModel.getAddress().observe(this, address -> {
            tvAddress.setText("Dirección: " + address);  // Actualizar el TextView de la dirección
            System.out.println("DIRECCION: " + address);
            String emergencyMessage = "Emergencia. Dirección: " + address;
            System.out.println(emergencyMessage);
            String sanitizedAddress = " Mensaje de Emergencia. La siguiente direccion podria no ser exacta. " + address.replaceAll("[^a-zA-Z0-9\\s,.]", "");


//
            //SmsUtils.checkAndSendSms(this, new String[]{"2804559405", "2804611882", "2804382723"}, sanitizedAddress);

            if (!isMessageSent) {
                //String emergencyMessage = "Mensaje de emergencia. Dirección: " + address;

                //SmsUtils.checkAndSendSms(this, new String[]{"2804992455", "2804611882", "2804405851"}, sanitizedAddress);

                isMessageSent = true;  // Marcar como enviado
            }
        });

        // Observar el estado de los permisos
        speedViewModel.getLocationPermissionState().observe(this, permissionGranted -> {
            if (!permissionGranted) {
                speedViewModel.requestLocationPermissions(this);
            }
        });

        // Iniciar la configuración de ubicación
        speedViewModel.checkLocationSettings(this);



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
        //responseListener = new ResponseListener(this);

        // Inicializamos el HashMap
        ipMessageMap = new HashMap<>();

        Log.i("MainActivity", "Inicio de Gestionador de Red.");
        // Inicializar el administrador del hotspot
        hotspotManager = new WifiHotspot(this, this);

        // Obtener y mostrar la IP del dispositivo
        Log.i("MainActivity", "Mostrando IP....");
        String deviceIpAddress = getDeviceIpAddress();
        ipTextView.setText("Mi IP: " + deviceIpAddress);

        String myDeviceIpAddress = getDeviceIpAddress();
        myIpTextView.setText("Mi IP: " + myDeviceIpAddress);

        // Iniciar la recepción de broadcasts y respuestas
        Log.i("MainActivity", "Escuchando mensajes de broadcast.");
        broadcastReceiver.startListening();
        //responseListener.listenForResponses();

        broadcastTimer.startBroadcastTimer();
        // Enviar mensaje de broadcast cuando se haga clic en el botón
        btnSendBroadcast.setOnClickListener(view -> {
            broadcastSender.sendBroadcast();
        });

//        btnYes.setOnClickListener(view -> {
//            setStatusTextViewOnYes();
//        });
//
//        btnNo.setOnClickListener(view -> {
//            setStatusTextViewOnNo();
//        });

        btnCreacionRed.setOnClickListener(view -> {
            irACrecionRed(view);
        });

        // Verificar y solicitar permisos necesarios
        Log.i("MainActivity", "Verificando permisos....");
        checkPermissions();

        MessageReceiver messageReceiver = new MessageReceiver(this);
        messageReceiver.startListening();

        // Configuramos el botón para activar el hotspot
        toggleHotspotButton.setOnClickListener(view -> {
            crearRed();
        });






















        accidentViewModel = new ViewModelProvider(this).get(AccidentViewModel.class);

        // Pasar el ViewModel al NotificadorAccidente
        NotificadorAccidente notificador = NotificadorAccidente.getInstancia();
        notificador.setAccidentViewModel(accidentViewModel);

        // Observar los cambios en el estado del accidente
        accidentViewModel.getAccidenteDetectado().observe(this, accidentDetected -> {
            huboAccidente = true;
            if (isConnectedToWifi()){
                enviarMensaje();
            } else if (accidentDetected){
                Log.i("Accidente", "Se ha detectado un accidente.");
                // Aquí puedes iniciar acciones como iniciarConteo()
                iniciarConteo();

            }

        });


//        btnSendMessages.setOnClickListener(view -> {
//            if (isConnectedToWifi()){
//                enviarMensaje();
//            } else if (responseTextView.getText().equals("SI")){
//                iniciarConteo();
//            }
//        });


    }

    public void irACrecionRed(View view){
        Intent i = new Intent(this, CreacionRedActivity.class);
        startActivity(i);
    }

    public void enviarMensaje(){
        String message = "accidente";
        int port = 12345; // Puedes definir el puerto a utilizar
        Log.i("Enviar Mensaje", "Enviando mensaje.");

        // Supongamos que quieres enviar el mensaje a la primera IP de la lista
        if (!ipList.isEmpty()) {
            //String targetIp = ipList.get(0); // Usar la IP que quieras de la lista
            for(String targetIp : ipList) {
                messageSender.sendMessage(targetIp, port, message);
                Log.i("Envio de mensaje", "Mensaje enviado a: " + targetIp + " con " + message);
                Toast.makeText(MainActivity.this, "Mensaje enviado a: " + targetIp, Toast.LENGTH_SHORT).show();

            }

            if(huboAccidente){
                enviarEstado();
            }
        } else {
            Log.e("Envio de mensaje", "No hay IPs disponibles para enviar el mensaje.");
            Toast.makeText(this, "No hay IPs disponibles para enviar el mensaje", Toast.LENGTH_SHORT).show();
        }
    }

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

    public void setMyIpTextView(String newIp) {
        myIpTextView.setText(newIp);
    }

    public void limpiarListasIp(){
        StringBuilder displayText = new StringBuilder("Mensajes recibidos:\n");
        // Actualizar el TextView en el hilo de la UI
        runOnUiThread(() -> ipMessageTextView.setText(displayText.toString()));
        ipList.clear();
        ipMessageMap.clear();
    }
    public void crearRed(){
        String ssid = ssidEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        // Validar que el usuario haya ingresado el SSID y la contraseña
        if (ssid.isEmpty() || password.isEmpty()) {
            Log.i("Gestion de red", "No se pudo crear red. Debe ingresar un Nombre y/o una contraseña.");
            estadoRedTextView.setText("Estado: No se pudo crear red. Ingresar nombre y/o contraseña.");
            //Toast.makeText(MainActivity.this, "Ingrese un SSID y una contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar la longitud de la contraseña
        if (password.length() < 8 || password.length() > 63) {
            Log.i("Gestion de red", "No se pudo crear red. La contraseña debe tener al menos 8 carácteres.");
            estadoRedTextView.setText("Estado: No se puedo crear. La contraseña debe tener al menos 8 carácteres.");
            //Toast.makeText(MainActivity.this, "La contraseña debe tener entre 8 y 63 caracteres", Toast.LENGTH_LONG).show();
            return;
        }

        boolean isHotspotActive;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isHotspotActive = isHotspotActiveForAndroid10();
        } else {
            isHotspotActive = checkHotspotStatus();
        }


        if (isHotspotActive) {
            Log.i("HotspotStatus", "El Hotspot está activado.");
            // Actualiza la UI o haz lo que sea necesario
        } else {
            Log.i("HotspotStatus", "El Hotspot está desactivado.");
        }

        if (!ssid.isEmpty() && !password.isEmpty()) {
            // Verificar si el sistema operativo es Android 10 o superior
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Activar el Hotspot (Wi-Fi Direct) con el SSID y la contraseña ingresados por el usuario
                toggleHotspot(ssid, password);

            } else {
                Log.i("Gestion de red", "No se pudo crear red. Wifi Direct Hotspot requiere Android 10 o superior");
                estadoRedTextView.setText("Estado: No se pudo crear la red. Wifi Direct Hotspot requiere Android 10 o superior.");
                // Mostrar mensaje de error si la versión es inferior a Android 10
                //Toast.makeText(MainActivity.this, "Wi-Fi Direct Hotspot requiere Android 10 o superior", Toast.LENGTH_SHORT).show();
            }
        }

    }


    // Método para verificar el estado del hotspot en versiones anteriores a Android 10
    private boolean checkHotspotStatus() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            // Usamos reflexión para acceder al método privado isWifiApEnabled en versiones anteriores a Android 10
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Método adicional para manejar Android 10 y superiores
    private boolean isHotspotActiveForAndroid10() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);

            // Verificamos si el hotspot está activo, generalmente cuando el Wi-Fi está desactivado
            if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && !wifiManager.isWifiEnabled();
            }
        }
        return false;
    }

    // Método para alternar el estado del Hotspot
    private void toggleHotspot(String ssid, String password) {
        Log.i("Red Hotspot", "Cambiando estado de Hotspot.");
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
        //Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show();

        // Actualizar la interfaz
        statusTextView.setText(statusMessage);
        toggleHotspotButton.setText(isHotspotActive ? "Desactivar Hotspot" : "Activar Hotspot");

    }


//    public void setStatusTextViewOnYes() {
//        responseTextView.setText("SI");
//    }
//
//
//    public void setStatusTextViewOnNo() {
//        responseTextView.setText("NO");
//    }

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
        if(huboAccidente){
            Log.i("Conteo de votos", "Se añade un VOTO:SI");
            contPositivo++;
        } else {
            Log.i("Conteo de votos", "Se añade un VOTO:NO");
            contNegativo++;
        }

        // Mostrar resultado basado en la cantidad de votos
        if (contPositivo >= contNegativo) {
            Log.i("Conteo de votos", "Hay Accidente");
            displayText.append("ACCIDENTE").append("\n");
            ipMessageTextView.setText("ACCIDENTE");
        } else {
            Log.i("Conteo de votos", "No hubo accidente");
            ipMessageTextView.setText("NO HUBO ACCIDENTE");
        }
    }

    public void enviarEstado(){
        String message;
        Log.i("Envio de Estado", "Enviando estado");
        if(huboAccidente){
            message = "VOTO:SI";
            Log.i("Envio de Estado", "Enviando mensaje: VOTO:SI");

        } else {
            message = "VOTO:NO";
            Log.i("Envio de Estado", "Enviando mensaje: VOTO:NO");

        }

        int port = 12345; // Puedes definir el puerto a utilizar

        // Supongamos que quieres enviar el mensaje a la primera IP de la lista
        if (!ipList.isEmpty()) {
            //String targetIp = ipList.get(0); // Usar la IP que quieras de la lista
            for(String targetIp : ipList) {
                messageSender.sendMessage(targetIp, port, message);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onHotspotStarted(String ssid, String password) {
        Log.i("Creacion de Hotspot", "Red creada.");

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
    }

}
