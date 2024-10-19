package com.example.autoalert.view.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class NetworkChangeReceiver extends BroadcastReceiver {

    Context context;
    private BroadcastSender broadcastSender;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (isConnectedToNetwork(context)) {
                Log.i("NetworkChangeReceiver", "Dispositivo conectado a una red.");
                // Cast seguro
                RedActivity redActivity = (RedActivity) context;

                // Llamar a un método en redActivity o acceder a variables
                redActivity.setMyIpTextView("Mi IP:" + getDeviceIpAddress());

                // Actualiza ipTextView
                redActivity.updateIpList("Mi IP:" + getDeviceIpAddress());



                redActivity.limpiarListasIp();
                broadcastSender = new BroadcastSender();
                broadcastSender.sendBroadcast();

            } else {
                Log.i("NetworkChangeReceiver", "Dispositivo desconectado de la red.");
            }
        }
    }

    private boolean isConnectedToNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
                return nc != null && (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            } else {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnected();
            }
        }
        return false;
    }

    // Método para obtener la IP del dispositivo
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
}