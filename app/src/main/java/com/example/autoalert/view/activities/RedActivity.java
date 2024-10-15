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

import com.example.autoalert.R;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class RedActivity extends AppCompatActivity implements WifiHotspot.HotspotListener{

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private WifiHotspot hotspotManager;
    //private TextView statusTextView;
    //private Button toggleHotspotButton;
    private boolean isHotspotActive = false;
    //private TextView ssidTextView;
    //private TextView passwordTextView;
    //private EditText ssidEditText;
    //private EditText passwordEditText;
    private TextView ipTextView;
    private TextView myIpTextView;
    private BroadcastSender broadcastSender;
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
    private TextView estadoRedTextView;
    private Button btnCreacionRed;
    private BroadcastTimer broadcastTimer;
    private int cont = 0;

    private EditText aliasEditText;

    private WifiManager wifiManager;

    private NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();

    // HashMap para almacenar la asociación de IPs y alias
    public HashMap<String, String> ipAliasMap = new HashMap<>();


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.red);

        Log.d("RedActivity", "Inicio de componentes visuales.");

    //    ssidTextView = findViewById(R.id.ssidTextView);
    //    passwordTextView = findViewById(R.id.passwordTextView);
    //    ssidEditText = findViewById(R.id.ssidEditText);
    //    passwordEditText = findViewById(R.id.passwordEditText);
        Button btnSendMessages = findViewById(R.id.btnSendMessages);
        ipTextView = findViewById(R.id.ipTextView);
        Button btnSendBroadcast = findViewById(R.id.btnSendBroadcast);
    //    statusTextView = findViewById(R.id.statusTextView);
    //    toggleHotspotButton = findViewById(R.id.toggleHotspotButton);
        ipMessageTextView = findViewById(R.id.ipMessageTextView);
        myIpTextView = findViewById(R.id.myIpTextView);
        btnYes = findViewById(R.id.btnYes);
        btnNo = findViewById(R.id.btnNo);
        statusBtnTextView = findViewById(R.id.statusBtnTextView);
        responseTextView = findViewById(R.id.responseTextView);
    //    estadoRedTextView = findViewById(R.id.redStatusTextView);
        btnCreacionRed = findViewById(R.id.creacionRedbutton);
        Log.i("RedActivity", "Componentes inicializados.");

        // Inicializamos wifiManager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Registro del BroadcastReceiver dinámicamente
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);


        // Inicializar los componentes
        Log.i("RedActivity", "Inicio de hilos");
        broadcastSender = new BroadcastSender();
        broadcastSender.setRedActivity(this);
        broadcastReceiver = new BroadcastReceiver(this);
        messageSender = new MessageSender();
        broadcastTimer = new BroadcastTimer();
        //responseListener = new ResponseListener(this);

        // Inicializamos el HashMap
        ipMessageMap = new HashMap<>();

        Log.i("RedActivity", "Inicio de Gestionador de Red.");
        // Inicializar el administrador del hotspot
        hotspotManager = new WifiHotspot(this, this);

        // Obtener y mostrar la IP del dispositivo
        Log.i("RedActivity", "Mostrando IP....");
        String deviceIpAddress = getDeviceIpAddress();
        ipTextView.setText("Mi IP: " + deviceIpAddress);

        String myDeviceIpAddress = getDeviceIpAddress();
        myIpTextView.setText("Mi IP: " + myDeviceIpAddress);

        // Iniciar la recepción de broadcasts y respuestas
        Log.i("RedActivity", "Escuchando mensajes de broadcast.");
        broadcastReceiver.startListening();
        //responseListener.listenForResponses();

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
        Log.i("RedActivity", "Verificando permisos....");
        checkPermissions();

        MessageReceiver messageReceiver = new MessageReceiver(this);
        messageReceiver.startListening();

        // Configuramos el botón para activar el hotspot
        /*
        toggleHotspotButton.setOnClickListener(view -> {
            crearRed();
        });*/


        btnSendMessages.setOnClickListener(view -> {
            if (isConnectedToWifi()){
                enviarMensaje();
            } else if (responseTextView.getText().equals("SI")){
                iniciarConteo();
            }
        });


    }

    public void irACrecionRed(View view){
        Intent intent = new Intent(RedActivity.this, CreacionRedActivity.class);
        intent.putExtra("ipTextView", myIpTextView.getText().toString()); // Si necesitas pasar datos
        startActivity(intent);
    }

    public void enviarMensaje(){
        String message = responseTextView.getText().toString();
        int port = 12345; // Puedes definir el puerto a utilizar
        Log.i("Enviar Mensaje", "Enviando mensaje.");

        // Supongamos que quieres enviar el mensaje a la primera IP de la lista
        if (!ipList.isEmpty()) {
            //String targetIp = ipList.get(0); // Usar la IP que quieras de la lista
            for(String targetIp : ipList) {
                messageSender.sendMessage(targetIp, port, message);
                Log.i("Envio de mensaje", "Mensaje enviado a: " + targetIp + " con " + message);
                Toast.makeText(RedActivity.this, "Mensaje enviado a: " + targetIp, Toast.LENGTH_SHORT).show();

            }

            if(responseTextView.getText().equals("SI")){
                enviarEstado();
            }
        } else {
            Log.e("Envio de mensaje", "No hay IPs disponibles para enviar el mensaje.");
            Toast.makeText(this, "No hay IPs disponibles para enviar el mensaje", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para actualizar la lista de IPs en el TextView
    public void updateIpList(String ip) {
        Log.i("Actualizacion de lista", "Intentando agregar IP: " + ip);

        // Revisamos si la IP es válida y no está vacía
        if (ip != null && !ip.isEmpty()) {
            handler.post(() -> {
                // Solo agregamos la IP si no está en la lista
                if (!ipList.contains(ip)) {
                    Log.i("Actualizacion de lista", "IP agregada: " + ip);
                    ipList.add(ip);
                } else {
                    Log.i("Actualizacion de lista", "La IP ya estaba en la lista: " + ip);
                }

                // Ahora recorremos toda la lista de IPs para mostrarlas sin repetición
                StringBuilder ips = new StringBuilder("IPs recibidas:\n");
                for (String storedIp : new HashSet<>(ipList)) { // Usamos un HashSet para evitar IPs duplicadas
                    ips.append(storedIp).append("\n");
                }

                // Actualizamos el TextView en el hilo principal
                runOnUiThread(() -> {
                    ipTextView.setText(ips.toString());
                    Log.i("Actualizacion de lista", "Lista de IPs actualizada en el TextView: " + ips.toString());
                });
            });
        } else {
            Log.e("Actualizacion de lista", "IP inválida recibida: " + ip);
        }
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
    /*
    public void crearRed(){
        String ssid = ssidEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        // Validar que el usuario haya ingresado el SSID y la contraseña
        if (ssid.isEmpty() || password.isEmpty()) {
            Log.i("Gestion de red", "No se pudo crear red. Debe ingresar un Nombre y/o una contraseña.");
            estadoRedTextView.setText("Estado: No se pudo crear red. Ingresar nombre y/o contraseña.");
            //Toast.makeText(RedActivity.this, "Ingrese un SSID y una contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar la longitud de la contraseña
        if (password.length() < 8 || password.length() > 63) {
            Log.i("Gestion de red", "No se pudo crear red. La contraseña debe tener al menos 8 carácteres.");
            estadoRedTextView.setText("Estado: No se puedo crear. La contraseña debe tener al menos 8 carácteres.");
            //Toast.makeText(RedActivity.this, "La contraseña debe tener entre 8 y 63 caracteres", Toast.LENGTH_LONG).show();
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
                //Toast.makeText(RedActivity.this, "Wi-Fi Direct Hotspot requiere Android 10 o superior", Toast.LENGTH_SHORT).show();
            }
        }

    }*/


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
    //    statusTextView.setText(statusMessage);
    //    toggleHotspotButton.setText(isHotspotActive ? "Desactivar Hotspot" : "Activar Hotspot");

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
            // Obtener el alias para la dirección IP actual
            String alias = ipAliasMap.get(ip); // Esto obtiene el alias correspondiente a la IP

            displayText.append("Alias: ").append(alias).append(" - Mensaje: ").append(ipMessageMap.get(ip)).append("\n");
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
        if(responseTextView.getText().equals("SI")){
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
    //    ssidTextView.setText("SSID: " + ssid);
    //    passwordTextView.setText("Contraseña: " + password);

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
    protected void onDestroy() {
        super.onDestroy();

        // Detener el hotspot al cerrar la aplicación
        hotspotManager.stopHotspot();
    }

}
