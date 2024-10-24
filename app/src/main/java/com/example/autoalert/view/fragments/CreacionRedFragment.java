package com.example.autoalert.view.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.autoalert.R;
import com.example.autoalert.view.activities.MainActivity;
import com.example.autoalert.view.activities.WifiHotspot;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;



public class CreacionRedFragment extends Fragment implements WifiHotspot.HotspotListener {

    private WifiHotspot hotspotManager;
    private TextView statusTextView;
    private Button toggleHotspotButton;
    private boolean isHotspotActive = false;
    private TextView ssidTextView;
    private TextView passwordTextView;
    private EditText ssidEditText;
    private EditText passwordEditText;
    private TextView estadoRedTextView;
    private HashMap<String, String> configuracion = new HashMap<String, String>();
    private boolean redCreada;
    private Context context;
    private WifiManager wifiManager;
    private MainActivity mainActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_creacion_red, container, false);
        mainActivity = (MainActivity) requireActivity();
        redCreada = false;
        ssidTextView = view.findViewById(R.id.ssidTextView);
        passwordTextView = view.findViewById(R.id.passwordTextView);
        ssidEditText = view.findViewById(R.id.ssidEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        statusTextView = view.findViewById(R.id.statusTextView);
        toggleHotspotButton = view.findViewById(R.id.toggleHotspotButton);
        estadoRedTextView = view.findViewById(R.id.redStatusTextView);
        hotspotManager = new WifiHotspot(mainActivity, this);
        wifiManager = (WifiManager) mainActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        configuracion = mainActivity.readMapFromFile("conf-red");

        if (configuracion.get("creada").equals("true")) {
            String nombreRed = configuracion.get("nombreRed");
            String contrasenia = configuracion.get("contrasenia");
            Log.i("Verificacion de red", "La red existente es " + nombreRed + " con password " + contrasenia);
            ssidTextView.setText(nombreRed);
            ssidEditText.setText(nombreRed);
            passwordEditText.setText(contrasenia);
            passwordTextView.setText(contrasenia);
            toggleHotspotButton.setText("Desactivar Hotspot");
            isHotspotActive = true;

        }

        toggleHotspotButton.setOnClickListener(view1 -> {
            crearRed();
        });

        return view;
    }


    public void crearRed() {
        String ssid = ssidEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        if (ssid.isEmpty() || password.isEmpty()) {
            Log.i("Gestion de red", "No se pudo crear red. Debe ingresar un Nombre y/o una contraseña.");
            estadoRedTextView.setText("Estado: No se pudo crear red. Ingresar nombre y/o contraseña.");
            return;
        }

        if (password.length() < 8 || password.length() > 63) {
            Log.i("Gestion de red", "No se pudo crear red. La contraseña debe tener al menos 8 carácteres.");
            estadoRedTextView.setText("Estado: No se puedo crear. La contraseña debe tener al menos 8 carácteres.");
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
        } else {
            Log.i("HotspotStatus", "El Hotspot está desactivado.");
        }

        if (!ssid.isEmpty() && !password.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                toggleHotspot(ssid, password);

            } else {
                Log.i("Gestion de red", "No se pudo crear red. Wifi Direct Hotspot requiere Android 10 o superior");
                estadoRedTextView.setText("Estado: No se pudo crear la red. Wifi Direct Hotspot requiere Android 10 o superior.");
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onHotspotStarted(String ssid, String password) {
        Log.i("Creacion de Hotspot", "Red creada.");

        ssidTextView.setText("SSID: " + ssid);
        passwordTextView.setText("Contraseña: " + password);
        configuracion.put("nombreRed", ssid);
        configuracion.put("contrasenia", password);
        configuracion.put("creada", "true");
        mainActivity.saveMapInFile("conf-red", configuracion);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private boolean checkHotspotStatus() {
        WifiManager wifiManager = (WifiManager) mainActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isHotspotActiveForAndroid10() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && !wifiManager.isWifiEnabled();
            }
        }
        return false;
    }

    private void toggleHotspot(String ssid, String password) {
        Log.i("Red Hotspot", "Cambiando estado de Hotspot.");
        if (!isHotspotActive) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                hotspotManager.startWifiDirectHotspot(ssid, password);
            }
        } else {
            hotspotManager.stopHotspot();
            stopWifiDirectHotspot();
            configuracion = mainActivity.readMapFromFile("conf-red");
            configuracion.put("creada", "false");
            mainActivity.saveMapInFile("conf-red", configuracion);
        }

        isHotspotActive = !isHotspotActive; // Cambiar el estado
        String statusMessage = isHotspotActive ? "Hotspot activado" : "Hotspot desactivado";
        statusTextView.setText(statusMessage);
        toggleHotspotButton.setText(isHotspotActive ? "Desactivar Hotspot" : "Activar Hotspot");

    }

    public void stopWifiDirectHotspot() {
        WifiP2pManager wifiP2pManager = (WifiP2pManager) mainActivity.getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel channel = wifiP2pManager.initialize(mainActivity, mainActivity.getMainLooper(), null);
        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i("WiFiDirect", "El grupo Wi-Fi Direct se ha detenido.");
                isHotspotActive = false;
                statusTextView.setText("Hotspot desactivado");
                toggleHotspotButton.setText("Activar Hotspot");
            }
            @Override
            public void onFailure(int reason) {
                Log.e("WiFiDirect", "No se pudo detener el grupo Wi-Fi Direct. Razón: " + reason);
            }
        });
    }

}