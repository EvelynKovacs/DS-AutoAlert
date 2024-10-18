package com.example.autoalert.view.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.autoalert.R;
import com.example.autoalert.utils.WifiHotspot;

import java.lang.reflect.Method;

public class CreacionRedFragment extends Fragment implements WifiHotspot.HotspotListener {

    private WifiHotspot hotspotManager;
    private TextView statusTextView;
    private Button toggleHotspotButton;
    private boolean isHotspotActive = false;
    private TextView ssidTextView;
    private TextView passwordTextView;
    private EditText ssidEditText;
    private EditText passwordEditText;
    private WifiManager wifiManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_creacion_red, container, false);

        // Initialize UI elements
        ssidTextView = view.findViewById(R.id.ssidTextView);
        passwordTextView = view.findViewById(R.id.passwordTextView);
        ssidEditText = view.findViewById(R.id.ssidEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        statusTextView = view.findViewById(R.id.statusTextView);
        toggleHotspotButton = view.findViewById(R.id.toggleHotspotButton);

        hotspotManager = new WifiHotspot(getContext(), this);
        wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        toggleHotspotButton.setOnClickListener(v -> crearRed());

        return view;
    }

    public void crearRed() {
        String ssid = ssidEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (ssid.isEmpty() || password.isEmpty()) {
            Log.i("Gestion de red", "Debe ingresar un Nombre y/o una contraseña.");
            statusTextView.setText("No se pudo crear red. Ingresar nombre y/o contraseña.");
            return;
        }

        if (password.length() < 8 || password.length() > 63) {
            Log.i("Gestion de red", "Contraseña inválida.");
            statusTextView.setText("Contraseña inválida.");
            return;
        }

        boolean isHotspotActive = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? isHotspotActiveForAndroid10() : checkHotspotStatus();

        if (!isHotspotActive) {
            Log.i("HotspotStatus", "El Hotspot está desactivado.");
            toggleHotspot(ssid, password);
        } else {
            Log.i("HotspotStatus", "El Hotspot ya está activado.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onHotspotStarted(String ssid, String password) {
        Log.i("Hotspot", "Red creada.");

        ssidTextView.setText("SSID: " + ssid);
        passwordTextView.setText("Contraseña: " + password);
    }



    private boolean checkHotspotStatus() {

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
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

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

    private void toggleHotspot(String ssid, String password) {
        Log.i("Red Hotspot", "Cambiando estado de Hotspot.");
        if (!isHotspotActive) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                hotspotManager.startWifiDirectHotspot(ssid, password);
            }
        } else {
            hotspotManager.stopHotspot();
        }
        isHotspotActive = !isHotspotActive;
        String statusMessage = isHotspotActive ? "Hotspot activado" : "Hotspot desactivado";

        // Mostrar un mensaje de éxito o error
        //Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show();

        // Actualizar la interfaz
        statusTextView.setText(statusMessage);
        toggleHotspotButton.setText(isHotspotActive ? "Desactivar Hotspot" : "Activar Hotspot");

    }
}
