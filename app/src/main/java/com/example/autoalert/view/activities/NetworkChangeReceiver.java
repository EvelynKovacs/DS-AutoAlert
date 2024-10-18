package com.example.autoalert.view.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;


import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class NetworkChangeReceiver extends BroadcastReceiver {

    Context context;
    private BroadcastSender broadcastSender;
    private NetworkUtils networkUtils = new NetworkUtils();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (isConnectedToNetwork(context)) {
                Log.i("NetworkChangeReceiver", "Dispositivo conectado a una red.");

                if (context instanceof RedActivity) {
                    RedActivity mainActivity = (RedActivity) context;
                    // Lógica específica de MainActivity
                    mainActivity.setMyIpTextView("Mi IP: " + networkUtils.getDeviceIpAddress());
                } else {
                    Log.w("NetworkChangeReceiver", "El contexto no es MainActivity.");
                }

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

}
