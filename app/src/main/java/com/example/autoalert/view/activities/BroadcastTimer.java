package com.example.autoalert.view.activities;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class BroadcastTimer {

    private MainActivity mainActivity;

    public BroadcastTimer(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
    public void startBroadcastTimer() {
        new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(30000);
                    mainActivity.sendBroadcast();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("BroadcastTimer", "Error al enviar el mensaje de broadcast: " + e.getMessage());

                }
            }
        }).start();
    }

}


