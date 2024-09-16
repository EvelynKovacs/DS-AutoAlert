package com.example.autoalert.view.activities;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class BroadcastSender {
    private static final int BROADCAST_PORT = 8888;
    private static final String BROADCAST_MESSAGE = "DISCOVER_IP_REQUEST";

    public void sendBroadcast() {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                socket.setBroadcast(true);

                byte[] sendData = BROADCAST_MESSAGE.getBytes();

                // Envía el mensaje de broadcast a la dirección de broadcast
                InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, BROADCAST_PORT);
                socket.send(sendPacket);

                socket.close();
                Log.d("BroadcastSender", "Mensaje de peticion broadcast enviada.");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

