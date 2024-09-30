package com.example.autoalert.view.activities;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.autoalert.R;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;



public class CreacionRedActivity extends AppCompatActivity implements WifiHotspot.HotspotListener{

    private WifiHotspot hotspotManager;
    private TextView statusTextView;
    private Button toggleHotspotButton;
    private boolean isHotspotActive = false;
    private TextView ssidTextView;
    private TextView passwordTextView;
    private EditText ssidEditText;
    private EditText passwordEditText;
    private TextView myIpTextView;
    private TextView estadoRedTextView;

    private boolean redCreada;


    private Context context;

    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_creacion_red);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        redCreada = false;

        ssidTextView = findViewById(R.id.ssidTextView);
        passwordTextView = findViewById(R.id.passwordTextView);
        ssidEditText = findViewById(R.id.ssidEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        statusTextView = findViewById(R.id.statusTextView);
        toggleHotspotButton = findViewById(R.id.toggleHotspotButton);
        estadoRedTextView = findViewById(R.id.redStatusTextView);

        hotspotManager = new WifiHotspot(this, this);
        // Inicializamos wifiManager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Recuperar los datos desde SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("MiPref", Context.MODE_PRIVATE);
        redCreada = sharedPref.getBoolean("redCreada", false);

        if(redCreada){
            Log.i("Verificacion de Red", "Ya existe una red. Obteniendo datos de SharedPreferences.");
            String nombreRed = sharedPref.getString("nombreRed", "Nombre no encontrado");
            String contrasenia = sharedPref.getString("contraseña", "Contraseña no encontrada"); // El segundo valor es el valor por defecto
            Log.i("Verificacion de red", "La red existente es " + nombreRed + " con password " + contrasenia);
            ssidTextView.setText(nombreRed);
            ssidEditText.setText(nombreRed);
            passwordEditText.setText(contrasenia);
            passwordTextView.setText(contrasenia);
            toggleHotspotButton.setText("Desactivar Hotspot");
            isHotspotActive = true;
        }

        toggleHotspotButton.setOnClickListener(view -> {
            crearRed();
        });
    }



    public void crearRed() {
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


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onHotspotStarted(String ssid, String password) {
        Log.i("Creacion de Hotspot", "Red creada.");

        // Mostrar SSID y contraseña en los TextViews
        ssidTextView.setText("SSID: " + ssid);
        passwordTextView.setText("Contraseña: " + password);

        // Guardar los datos en SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("MiPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // Guardar nombre y edad
        editor.putString("nombreRed", ssid);
        editor.putString("contraseña", password);
        editor.putBoolean("redCreada", true);
        editor.apply(); // Guardar los cambios

        MainActivity mainActivity = (MainActivity) context;

        // Llamar a un método en MainActivity o acceder a variables
        mainActivity.setMyIpTextView("Mi IP:" + getDeviceIpAddress());


        // Obtener y mostrar la IP del dispositivo al crear el hotspot
        String deviceIpAddress = getDeviceIpAddress();
        //ipTextView.setText("Mi IP: " + deviceIpAddress);

        //myIpTextView.setText("Mi IP: " + deviceIpAddress);
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
        //hotspotManager.stopHotspot();
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
            // Guardar los datos en SharedPreferences
            SharedPreferences sharedPref = getSharedPreferences("MiPref", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("redCreada", false);
            editor.apply(); // Guardar los cambios
        }

        isHotspotActive = !isHotspotActive; // Cambiar el estado
        String statusMessage = isHotspotActive ? "Hotspot activado" : "Hotspot desactivado";

        // Mostrar un mensaje de éxito o error
        //Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show();

        // Actualizar la interfaz
        statusTextView.setText(statusMessage);
        toggleHotspotButton.setText(isHotspotActive ? "Desactivar Hotspot" : "Activar Hotspot");

    }

}