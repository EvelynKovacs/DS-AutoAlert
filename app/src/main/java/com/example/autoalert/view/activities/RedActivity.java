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
import java.util.Set;

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

    private BroadcastSender broadcastSender = new BroadcastSender(); // Ensure this matches your constructor

    private MessageSender messageSender  = new MessageSender();

    private Handler handler = new Handler(Looper.getMainLooper());
    private TextView ipMessageTextView;
    private Button btnYes;
    private Button btnNo;
    private TextView statusBtnTextView;
    private TextView responseTextView;
    private TextView estadoRedTextView;
    private Button btnCreacionRed;
    private int cont = 0;

    private TextView resultadoTextView;
    private boolean isSendingMessage = false;

    private EditText aliasEditText;

    private WifiManager wifiManager;

    private NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();

    private BroadcastReceiver networkReceiver;

    // HashMap para almacenar la asociación de IPs y alias
    public HashMap<String, String> ipAliasMap = new HashMap<>();

    private MainActivity mainActivity = new MainActivity();

//    private SistemaVotación sistemaVotación = new SistemaVotación(mainActivity);

    NetworkUtils networkUtils = new NetworkUtils();

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
        resultadoTextView = findViewById(R.id.resultadoTextView);
        Log.i("RedActivity", "Componentes inicializados.");

        // Inicializamos wifiManager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);


        // Recuperar los datos desde SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("MiPref", Context.MODE_PRIVATE);

        Set <String> ipListNueva = sharedPref.getStringSet("ipList", new HashSet<>());

        for (String ip : ipListNueva){
            Log.d("ForIp","ip: "+ip);
        }


        // Inicializar el administrador del hotspot
        hotspotManager = new WifiHotspot(this, this);

        // Obtener y mostrar la IP del dispositivo
        Log.i("RedActivity", "Mostrando IP....");
        String deviceIpAddress = networkUtils.getDeviceIpAddress();
        ipTextView.setText("Mi IP: " + deviceIpAddress);

        String myDeviceIpAddress = networkUtils.getDeviceIpAddress();
        myIpTextView.setText("Mi IP: " + myDeviceIpAddress);


        // Registro del BroadcastReceiver dinámicamente
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);

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


        btnSendMessages.setOnClickListener(view -> {
            if (isConnectedToWifi()){
                enviarMensaje();
            } else if (responseTextView.getText().equals("SI")){
                mainActivity.iniciarConteo();
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

        Log.i("Enviar Mensaje", "Enviando mensaje.");

        // Recuperar los datos desde SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("MiPref", Context.MODE_PRIVATE);

        Set <String> ipListNueva = sharedPref.getStringSet("ipList", new HashSet<>());

        for (String ip : ipListNueva){
            Log.d("ForIp EnviarMensaje","ip EnviarMensaje: "+ip);
        }

        // Supongamos que quieres enviar el mensaje a la primera IP de la lista
        if (!ipListNueva.isEmpty()) {
            //String targetIp = ipList.get(0); // Usar la IP que quieras de la lista
            for(String targetIp : ipListNueva) {
                Log.i("MensajeTexto", "El mensaje es:"+ message);
                Log.d("Redactivity","La ip:"+targetIp);
                messageSender.sendMessage(targetIp, message);
                Log.i("Envio de mensaje", "Mensaje enviado a: " + targetIp + " con " + message);
                Toast.makeText(RedActivity.this, "Mensaje enviado a: " + targetIp, Toast.LENGTH_SHORT).show();

            }

            if(responseTextView.getText().equals("SI")){
                mainActivity.enviarEstado();
            }
        } else {
            Log.e("Envio de mensaje", "No hay IPs disponibles para enviar el mensaje.");
            Toast.makeText(this, "No hay IPs disponibles para enviar el mensaje", Toast.LENGTH_SHORT).show();
        }
    }

    public void setMyIpTextView(String newIp) {
        myIpTextView.setText(newIp);
    }


    public void setStatusTextViewOnYes() {

        SharedPreferences sharedPref = getSharedPreferences("MiPref", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("estadoAccidente","SI");
        editor.apply();

        responseTextView.setText("SI");
    }


    public void setStatusTextViewOnNo() {

        SharedPreferences sharedPref = getSharedPreferences("MiPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("estadoAccidente","NO");
        editor.apply();

        responseTextView.setText("NO");
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
        String deviceIpAddress = networkUtils.getDeviceIpAddress();
        //ipTextView.setText("Mi IP: " + deviceIpAddress);

        myIpTextView.setText("Mi IP: " + deviceIpAddress);
    }



    public boolean isConnectedToWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return networkInfo != null && networkInfo.isConnected();
    }

    public void setResultadoText(String message){
        StringBuilder displayText = new StringBuilder("Mensajes recibidos:\n");
        displayText.append(message).append("\n");
        resultadoTextView.setText(message);
    }

    public String getResponseText(){
        return responseTextView.getText().toString();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Detener el hotspot al cerrar la aplicación
        hotspotManager.stopHotspot();
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
