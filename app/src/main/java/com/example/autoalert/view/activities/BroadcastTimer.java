package com.example.autoalert.view.activities;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Collections;

public class BroadcastTimer {

    private BroadcastSender broadcastSender = new BroadcastSender();
    public void startBroadcastTimer() {
        new Thread(() -> {
            while(true) {
                try {
                    // Pausa el hilo durante 30 segundos (30000 milisegundos)
                    Thread.sleep(30000);
                    broadcastSender.sendBroadcast();
                    Log.d("BroadcastTimer", "Timer! Mensaje broadcast enviado!.");

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("BroadcastTimer", "Error al enviar el mensaje de broadcast: " + e.getMessage());

                }
            }
        }).start();
    }

}


