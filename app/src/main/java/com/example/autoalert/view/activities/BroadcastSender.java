package com.example.autoalert.view.activities;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Collections;

public class BroadcastSender {
    private static final int BROADCAST_PORT = 8888;
    private static final String BROADCAST_MESSAGE = "DISCOVER_IP_REQUEST";

    public void sendBroadcast() {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                socket.setBroadcast(true);
                InetAddress broadcastAddress = getBroadcastAddress();

                byte[] sendData = BROADCAST_MESSAGE.getBytes();


                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, BROADCAST_PORT);
                socket.send(sendPacket);

                socket.close();
                Log.d("BroadcastSender", "Mensaje de peticion broadcast enviada.");

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("BroadcastSender", "Error al enviar el mensaje de broadcast: " + e.getMessage());

            }
        }).start();
    }

    // Obtener la dirección de broadcast de la red a la cual estás conectado
    private InetAddress getBroadcastAddress() throws Exception {
        // Recorre las interfaces de red disponibles y obtén la dirección de broadcast
        for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast != null) {
                        return broadcast;
                    }
                }
            }
        }
        throw new Exception("No se pudo obtener la dirección de broadcast");
    }
}

