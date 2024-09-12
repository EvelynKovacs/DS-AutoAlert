package com.example.autoalert.view.activities;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.LocalOnlyHotspotCallback;
import android.net.wifi.WifiManager.LocalOnlyHotspotReservation;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.RequiresApi;
import java.lang.reflect.Method;

public class WifiHotspot {

    private WifiManager wifiManager;
    private LocalOnlyHotspotReservation hotspotReservation;
    private HotspotListener hotspotListener;

    public WifiHotspot(Context context, HotspotListener listener) {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.hotspotListener = listener;
    }

    public boolean setWifiApEnabled(String ssid, String password, boolean enabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8 o superior
            if (enabled) {
                startHotspot(ssid, password);
            } else {
                stopHotspot();
            }
            return true;
        }

        else {
            // Android 7 o inferior
            return setWifiApEnabledLegacy(ssid, password, enabled);
        }
    }

    public void setCustomWifiApEnabled(String ssid, String password, boolean enabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8 o superior
            if (enabled) {
                startHotspot(ssid, password);
            } else {
                stopHotspot();
            }
        }
    }

    // Método para versiones anteriores a Android 8 (Android 7 o inferior)
    @SuppressWarnings("deprecation")
    private boolean setWifiApEnabledLegacy(String ssid, String password, boolean enabled) {
        try {
            // Si habilitamos el Hotspot, desactivamos el Wi-Fi normal
            if (enabled && wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false); // Apaga el Wi-Fi
            }

            // Configuración del hotspot sin contraseña (deprecated, pero funciona en Android 7)
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = "MyHotspot";  // Nombre de la red
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE); // Sin seguridad

            // Usar reflexión para invocar el método setWifiApEnabled (deprecado en versiones superiores)
            Method method = wifiManager.getClass().getDeclaredMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            return (Boolean) method.invoke(wifiManager, wifiConfig, enabled);
        } catch (Exception e) {
            Log.e("HotspotError", "Error al activar/desactivar el hotspot (legacy)", e);
            return false;
        }
    }

    // Método para iniciar el Hotspot en Android 8 y superior con SSID y contraseña personalizados
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startHotspot(String ssid, String password) {
        wifiManager.startLocalOnlyHotspot(new LocalOnlyHotspotCallback() {
            @Override
            public void onStarted(LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                hotspotReservation = reservation;

                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = ssid;  // Nombre personalizado del Hotspot
                wifiConfig.preSharedKey = password;  // Contraseña personalizada

                // Notificar al listener (MainActivity) del SSID y la contraseña
                if (hotspotListener != null) {
                    hotspotListener.onHotspotStarted(wifiConfig.SSID, wifiConfig.preSharedKey);
                }

                Log.d("Hotspot", "Hotspot iniciado. SSID: " + ssid + " Contraseña: " + password);
            }

            @Override
            public void onStopped() {
                super.onStopped();
                Log.d("Hotspot", "Hotspot detenido");
            }

            @Override
            public void onFailed(int reason) {
                Log.e("HotspotError", "Error al iniciar el hotspot. Razón: " + reason);
            }
        }, new Handler(Looper.getMainLooper()));
    }

    // Método para detener el Hotspot en Android 8 y superior
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void stopHotspot() {
        if (hotspotReservation != null) {
            hotspotReservation.close();
            Log.d("Hotspot", "Hotspot detenido");
            hotspotReservation = null;
        } else {
            Log.d("Hotspot", "No hay un hotspot en ejecución");
        }
    }

    // Interface para comunicar el SSID y la contraseña
    public interface HotspotListener {
        void onHotspotStarted(String ssid, String password);
    }
}
