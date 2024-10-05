package com.example.autoalert.view.activities;


import android.content.Context;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class WifiHotspot {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private HotspotListener hotspotListener;
    private boolean isHotspotEnabled = false;

    public WifiHotspot(Context context, HotspotListener listener) {
        manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(context, Looper.getMainLooper(), null);
        this.hotspotListener = listener;


    }

    // Método para iniciar el Hotspot usando Wi-Fi Direct (Android Q o superior)
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void startWifiDirectHotspot(String ssid, String password) {
        if (manager == null || channel == null) {
            Log.e("WifiDirectError", "Wi-Fi Direct no está disponible.");
            return;
        }

        WifiP2pConfig config = new WifiP2pConfig.Builder()
                .setNetworkName("DIRECT-" + ssid)
                .setPassphrase(password)
                .enablePersistentMode(false)
                .setGroupOperatingBand(WifiP2pConfig.GROUP_OWNER_BAND_AUTO)
                .build();

        manager.createGroup(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("Hotspot", "Hotspot iniciado. SSID: " + ssid + " Contraseña: " + password);
                isHotspotEnabled = true;

                // Notificar al listener (redActivity)
                if (hotspotListener != null) {
                    hotspotListener.onHotspotStarted(ssid, password);
                }
            }

            @Override
            public void onFailure(int reason) {
                Log.e("HotspotError", "Error al iniciar el hotspot. Razón: " + reason);
                Context context = null;
                //Toast.makeText(context, "Error al iniciar el hotspot", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para detener el Hotspot
    public void stopHotspot() {
        if (isHotspotEnabled && manager != null && channel != null) {
            manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d("Hotspot", "Hotspot detenido");
                    isHotspotEnabled = false;
                }

                @Override
                public void onFailure(int reason) {
                    Log.e("HotspotError", "Error al detener el hotspot. Razón: " + reason);
                }
            });
        } else {
            Log.d("Hotspot", "No hay un hotspot en ejecución");
        }
    }

    // Interface para comunicar el SSID y la contraseña
    public interface HotspotListener {
        void onHotspotStarted(String ssid, String password);
    }
}